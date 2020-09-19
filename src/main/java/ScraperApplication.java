import org.apache.commons.cli.*;

import java.util.List;
import java.util.NoSuchElementException;

public class ScraperApplication {
    public final static String DEFAULT_SEARCH_URL = "https://jsainsburyplc.github.io/serverside-test/site/" +
            "www.sainsburys.co.uk/webapp/wcs/stores/servlet/gb/groceries/berries-cherries-currants6039.html";

    public static void main (String[] args) throws ParseException {
        Options options = new Options();

        Option inputUrl = new Option("u", "url", true, "url to Sainbury's page");
        inputUrl.setRequired(false);
        options.addOption(inputUrl);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            String url = cmd.getOptionValue("url", DEFAULT_SEARCH_URL);
            ScraperApplication application = new ScraperApplication();
            String json = application.getJsonForPage(url);
            System.out.println(json);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("ScraperApplication", options);
            throw e;
        }
    }

    public String getJsonForPage(String url) throws NoSuchElementException {
        WebScraper scraper = new WebScraper();
        List<Item> itemSummaries = scraper.getItemsInPage(url);

        if (itemSummaries.isEmpty())
            throw new NoSuchElementException("No items found");
        return ItemsSummarizer.getJsonSummary(itemSummaries);
    }
}
