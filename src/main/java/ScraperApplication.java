import java.util.List;

public class ScraperApplication {
    public final static String DEFAULT_SEARCH_URL = "https://jsainsburyplc.github.io/serverside-test/site/" +
            "www.sainsburys.co.uk/webapp/wcs/stores/servlet/gb/groceries/berries-cherries-currants6039.html";

    public static void main (String[] args) throws Exception {
        String searchUrl = DEFAULT_SEARCH_URL; // todo take command line args

        ScraperApplication application = new ScraperApplication();
        String json = application.getJsonForPage(searchUrl);

        System.out.println(json);
    }

    public String getJsonForPage(String url) throws Exception {
        WebScraper scraper = new WebScraper();
        List<Item> itemSummaries = scraper.getItemsInPage(url);

        if (itemSummaries.isEmpty())
            throw new Exception("No items found");
        return ItemsSummarizer.getJsonSummary(itemSummaries);
    }
}
