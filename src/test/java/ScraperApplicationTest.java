import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScraperApplicationTest {

    @Test
    public void defaultSearchTest() throws Exception {
        ScraperApplication scraperApplication = new ScraperApplication();

        String json = scraperApplication.getJsonForPage(ScraperApplication.DEFAULT_SEARCH_URL);

        JsonObject jsonArray = new Gson().fromJson(json, JsonObject.class);

        assertEquals("39.50", jsonArray.get("gross").getAsString());
    }

    @Test
    public void alternativeSearchTest() throws Exception {
        String url = "https://jsainsburyplc.github.io/serverside-test/site/www.sainsburys.co.uk/shop/gb/groceries/fruit-veg/bananas-grapes.html";

        ScraperApplication scraperApplication = new ScraperApplication();
        // json missing all kcal and descriptions, as links direct to actual Sainsbury's site with different html format
        String json = scraperApplication.getJsonForPage(url);

        JsonObject jsonArray = new Gson().fromJson(json, JsonObject.class);

        assertEquals("31.46", jsonArray.get("gross").getAsString());
    }
}
