import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScraperApplication {
    private final static String BASE_URL = "https://jsainsburyplc.github.io/serverside-test/site/www.sainsburys.co.uk/";
    private final static String GRID_ITEM_CLASS = "gridItem";
    private final static String PRICE_PER_UNIT_CLASS = "pricePerUnit";
    private final static String NAME_AND_PROMOTIONS_CLASS = "productNameAndPromotions";
    private final static String GROUP_CONTAINER_CLASS = "itemTypeGroupContainer";
    private final static String PRODUCT_DESCRIPTION_CLASS = "productText";
    private static final String KCAL_CLASS = "nutritionLevel1";
    private final static String DEFAULT_SEARCH_URL = "https://jsainsburyplc.github.io/serverside-test/site/" +
            "www.sainsburys.co.uk/webapp/wcs/stores/servlet/gb/groceries/berries-cherries-currants6039.html";
    private final static BigDecimal VAT_FROM_GROSS_MULTIPLIER = new BigDecimal("0.16666666666"); // 1 - (1/1.2)

    public static void main (String[] args) {

        // By inspecting the page in Chrome, can see that <li class=gridItem> is what contains each item
        ScraperApplication scraper = new ScraperApplication();

        String searchUrl = DEFAULT_SEARCH_URL; // todo take command line args
        List<HtmlElement> items = scraper.scrapeSearchUrlForItems(searchUrl);
        List<Item> itemSummaries = scraper.extractDataFromItems(items);

        BigDecimal totalPrice = itemSummaries.stream()
                .map(item -> item.getPrice())
                .reduce(new BigDecimal("0.00"), BigDecimal::add);

        BigDecimal vat = totalPrice.multiply(VAT_FROM_GROSS_MULTIPLIER);
        BigDecimal vatTwoDecimalPlaces = vat.setScale(2, RoundingMode.HALF_EVEN);

        Type listType = new TypeToken<List<Item>>() {}.getType();
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create() ;
        JsonElement jsonElement = gson.toJsonTree(itemSummaries, listType);

        // todo Extract out JSON formation
        JsonArray array = new JsonArray();
        JsonObject summaryJson = new JsonObject();
        summaryJson.addProperty("gross", totalPrice.toString());
        summaryJson.addProperty("vat", vatTwoDecimalPlaces.toString());

        array.add(jsonElement);
        array.add(summaryJson);

        System.out.println(gson.toJson(array));
        // TODO @SerializedName(value = "kcal_per_100g") etc. in Item
    }

    public List<HtmlElement> scrapeSearchUrlForItems(String url) {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        try {
            String searchUrl = url; // + URLEncoder.encode(searchQuery, "UTF-8")
            HtmlPage page = client.getPage(searchUrl);

            List<HtmlElement> items = new ArrayList<>(
                    page.getByXPath("//li[@class='" + GRID_ITEM_CLASS + "']")
            );

            return items;
        } catch (Exception e) { // TODO this exception is too broad/mishandled
            e.printStackTrace();
        }

        System.out.println("No items found for url " + url);
        return new ArrayList<>();
    }

    private List<Item> extractDataFromItems(List<HtmlElement> htmlItems) {
        List<Item> dataItems = new ArrayList<>();

        for (HtmlElement item : htmlItems){
            // n.b. .// div searches from this element; // searches from root (had a lot of strawberries in my json!)
            HtmlElement nameAndPromoElement = item.getFirstByXPath(".//div[@class='" + NAME_AND_PROMOTIONS_CLASS + "']");
            HtmlAnchor titleAnchor = nameAndPromoElement.getFirstByXPath(".//a");
            String title = titleAnchor.asText();

            // In order to get description and kcal per 100g, need to extract for specific page
            // To get the item's target page, make some assumptions about the url
            // Search url https://jsainsburyplc.github.io/serverside-test/site/www.sainsburys.co.uk/webapp/...berries-cherries-currants6039.html
            // Relative item url ../../../../../../shop/gb/groceries/berries-cherries-currants/sainsburys-british-strawberries-400g.html
            // Item url https://jsainsburyplc.github.io/serverside-test/site/www.sainsburys.co.uk/shop/gb/groceries/berries-cherries-currants/sainsburys-british-strawberries-400g.html
            // So, append relative url to base https://jsainsburyplc.github.io/serverside-test/site/www.sainsburys.co.uk/
            String itemPageRelative = titleAnchor.getHrefAttribute();
            String itemPageSuffix = itemPageRelative.replaceAll("\\.\\.\\/", "");
            String itemPage = BASE_URL + itemPageSuffix;

            Map<String, String> itemDetails = extractDetailsFromItemPage(itemPage);

            // TODO Can probably tell gson to ignore null kcal values
            Integer kcal = itemDetails.containsKey("kcal") ? Integer.parseInt(itemDetails.get("kcal")) : null;

            String description = itemDetails.get("description");


            HtmlElement priceElement = item.getFirstByXPath(".//p[@class='pricePerUnit']");
            String pricePerUnit = priceElement.asText(); // £1.75/unit
            String priceString = pricePerUnit.substring(1, pricePerUnit.indexOf("/"));
            BigDecimal price = new BigDecimal(priceString);

            Item itemData = new Item(title, description, price, kcal);
            dataItems.add(itemData);
        }

        return dataItems;
    }

    private Map<String, String> extractDetailsFromItemPage(String itemPage) {
        HashMap<String, String> itemDetails = new HashMap<>();

        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        try {
            HtmlPage page = client.getPage(itemPage);

            // TODO refactor this if there's time
            String description; // there's 3 ways the item's description can be stored in html
            HtmlElement productTextElement = page.getFirstByXPath("//div[@class='" + PRODUCT_DESCRIPTION_CLASS + "']");
            if (productTextElement != null) {
                description = productTextElement.asText();
            } else {
                productTextElement = page.getFirstByXPath("//div[@class='" + GROUP_CONTAINER_CLASS + " " + PRODUCT_DESCRIPTION_CLASS +  "']");
                    HtmlElement descriptionElement = productTextElement.getFirstByXPath(".//div[@class='memo']");
                    if (descriptionElement != null)
                        description = descriptionElement.asText();
                    else {
                        HtmlElement statementsElement = (HtmlElement) productTextElement.getByXPath(".//p").get(1);
                        description = statementsElement.asText();
                    }
            }
            itemDetails.put("description", description);


            HtmlElement kcalElement = page.getFirstByXPath("//td[@class='" + KCAL_CLASS + "']");
            if (kcalElement != null) {
                String kcalWithUnit = kcalElement.asText();
                String kcal = kcalWithUnit.substring(0, kcalWithUnit.indexOf("k"));
                itemDetails.put("kcal", kcal);
            }
        } catch (Exception e) { // TODO this exception is too broad/mishandled
            e.printStackTrace();
        }


        return itemDetails;
    }
}
