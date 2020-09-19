import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ItemsSummarizer {
    private final static BigDecimal VAT_FROM_GROSS_MULTIPLIER = new BigDecimal("0.16666666666"); // 1 - (1/1.2)

    public static String getJsonSummary(List<Item> itemSummaries) {

        BigDecimal gross = calculateGrossForItems(itemSummaries);
        String vat = calculateVatFromGross(gross);

        JsonArray array = new JsonArray();

        Type listType = new TypeToken<List<Item>>() {}.getType();
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create() ;
        JsonElement itemsJson = gson.toJsonTree(itemSummaries, listType);
        array.add(itemsJson);

        JsonObject summaryJson = new JsonObject();
        summaryJson.addProperty("gross", gross.toString());
        summaryJson.addProperty("vat", vat);
        array.add(summaryJson);

        return gson.toJson(array);
    }

    public static BigDecimal calculateGrossForItems(List<Item> items) {
         return items.stream()
                .map(Item::getPrice)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    public static String calculateVatFromGross(BigDecimal gross) {
        BigDecimal vat = gross.multiply(VAT_FROM_GROSS_MULTIPLIER);
        BigDecimal vatTwoDecimalPlaces = vat.setScale(2, RoundingMode.HALF_EVEN);
        return vatTwoDecimalPlaces.toString();
    }
}
