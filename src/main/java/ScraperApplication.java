import java.util.List;

public class ScraperApplication {
    private final static String DEFAULT_SEARCH_URL = "https://jsainsburyplc.github.io/serverside-test/site/" +
            "www.sainsburys.co.uk/webapp/wcs/stores/servlet/gb/groceries/berries-cherries-currants6039.html";

    public static void main (String[] args) {
        String searchUrl = DEFAULT_SEARCH_URL; // todo take command line args

        WebScraper scraper = new WebScraper();
        List<Item> itemSummaries = scraper.getItemsInPage(searchUrl);

        String jsonSummary = ItemsSummarizer.getJsonSummary(itemSummaries);
        System.out.println(jsonSummary);
    }
}
