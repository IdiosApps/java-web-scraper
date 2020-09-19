import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebScraper {
    private final static String BASE_URL = "https://jsainsburyplc.github.io/serverside-test/site/www.sainsburys.co.uk/";
    private final static String GRID_ITEM_CLASS = "gridItem";
    private final static String PRICE_PER_UNIT_CLASS = "pricePerUnit";
    private final static String NAME_AND_PROMOTIONS_CLASS = "productNameAndPromotions";
    private final static String GROUP_CONTAINER_CLASS = "itemTypeGroupContainer";
    private final static String PRODUCT_DESCRIPTION_CLASS = "productText";
    private static final String KCAL_CLASS = "nutritionLevel1";

    private final WebClient webClient;

    public WebScraper () {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        this.webClient = client;
    }

    public List<Item> getItemsInPage(String url) {
        List<HtmlElement> items = scrapeSearchUrlForItems(url);
        return extractDataFromItems(items);
    }

    public List<HtmlElement> scrapeSearchUrlForItems(String url) {
        try {
            HtmlPage page = webClient.getPage(url);

            // By inspecting the page in Chrome, can see that <li class=gridItem> is what contains each item
            return new ArrayList<>(
                    page.getByXPath("//li[@class='" + GRID_ITEM_CLASS + "']")
            );
        } catch (Exception e) { // MalformedURLException / IOException both give useful trace, so just log them out
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


            String itemPage = getItemPage(titleAnchor);
            Map<String, String> itemDetails = extractDetailsFromItemPage(itemPage);
            Integer kcal = itemDetails.containsKey("kcal") ? Integer.parseInt(itemDetails.get("kcal")) : null;
            String description = itemDetails.get("description");

            HtmlElement priceElement = item.getFirstByXPath(".//p[@class='"+ PRICE_PER_UNIT_CLASS + "']");
            String pricePerUnit = priceElement.asText(); // £1.75/unit
            String priceString = pricePerUnit.substring(1, pricePerUnit.indexOf("/"));
            BigDecimal price = new BigDecimal(priceString);

            Item itemData = new Item(title, description, price, kcal);
            dataItems.add(itemData);
        }

        return dataItems;
    }

    private String getItemPage(HtmlAnchor titleAnchor) {
        // In order to get description and kcal per 100g, need to extract for specific page
        // To get the item's target page, make some assumptions about the url:
        // Search url https://jsainsburyplc.github.io/serverside-test/site/www.sainsburys.co.uk/webapp/...berries-cherries-currants6039.html
        // Relative item url ../../../../../../shop/gb/groceries/berries-cherries-currants/sainsburys-british-strawberries-400g.html
        // Item url https://jsainsburyplc.github.io/serverside-test/site/www.sainsburys.co.uk/shop/gb/groceries/berries-cherries-currants/sainsburys-british-strawberries-400g.html
        // So, append (trimmed) relative url to base https://jsainsburyplc.github.io/serverside-test/site/www.sainsburys.co.uk/
        String itemPageRelative = titleAnchor.getHrefAttribute();
        String itemPageSuffix = itemPageRelative.replaceAll("\\.\\./", "");
        return BASE_URL + itemPageSuffix;
    }

    private Map<String, String> extractDetailsFromItemPage(String itemPage) {
        HashMap<String, String> itemDetails = new HashMap<>();

        try {
            HtmlPage page = webClient.getPage(itemPage);

            String description = getDescriptionForItem(page);
            itemDetails.put("description", description);

            String kcal = getKcalForItem(page);
            if (kcal != null) itemDetails.put("kcal", kcal);
        } catch (Exception e) {
            System.out.println("Failed to get data for specific item page - author's assumptions on relative page links may be incorrect");
            e.printStackTrace();
        }

        return itemDetails;
    }

    // There's 3 ways the item's description can be stored in html
    private String getDescriptionForItem(HtmlPage page) {
        HtmlElement productTextElement = page.getFirstByXPath("//div[@class='" + PRODUCT_DESCRIPTION_CLASS + "']");
        if (productTextElement != null)
            return productTextElement.asText();

        HtmlElement groupContainerElement = page.getFirstByXPath("//div[@class='" + GROUP_CONTAINER_CLASS + " " + PRODUCT_DESCRIPTION_CLASS +  "']");
        HtmlElement descriptionElement = groupContainerElement.getFirstByXPath(".//div[@class='memo']");
        if (descriptionElement != null)
            return descriptionElement.asText();

        HtmlElement statementsElement = (HtmlElement) groupContainerElement.getByXPath(".//p").get(1);
        return statementsElement.asText();
    }

    private String getKcalForItem(HtmlPage page) {
        HtmlElement kcalElement = page.getFirstByXPath("//td[@class='" + KCAL_CLASS + "']");
        if (kcalElement != null) {
            String kcalWithUnit = kcalElement.asText();
            return kcalWithUnit.substring(0, kcalWithUnit.indexOf("k"));
        }

        return null;
    }
}
