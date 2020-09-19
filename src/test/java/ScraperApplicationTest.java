import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class ScraperApplicationTest {

    @Test
    public void defaultSearchTest() {
        ScraperApplication scraperApplication = new ScraperApplication();

        String json = scraperApplication.getJsonForPage(ScraperApplication.DEFAULT_SEARCH_URL);

        JsonObject jsonArray = new Gson().fromJson(json, JsonObject.class);

        assertEquals("39.50", jsonArray.get("gross").getAsString());
    }

    @Test
    public void userSpecifiedUrlSearchTest() throws Exception {
        // n.b. json will be missing all kcal and descriptions
        // item links are direct to actual Sainsbury's site with different html format to brief's example html format
        String url = "https://jsainsburyplc.github.io/serverside-test/site/www.sainsburys.co.uk/shop/gb/groceries/fruit-veg/bananas-grapes.html";
        String[] args = new String[] {"-u", url};

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        ScraperApplication.main(args);

        assertTrue(outContent.toString().contains("\"gross\": \"31.46\""));
        outContent.close();
    }

    @Test
    public void unknownArgTest() {
        String[] args = new String[] {"-z", "false"};

        Exception exception = assertThrows(UnrecognizedOptionException.class, () -> ScraperApplication.main(args));

        assertEquals("Unrecognized option: -z", exception.getMessage());
    }
}
