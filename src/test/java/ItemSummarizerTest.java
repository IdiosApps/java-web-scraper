import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemSummarizerTest {

    @Test
    public void vatTest() {
        List<Item> items = new ArrayList<>();
        Item firstItem = new Item(null, null, new BigDecimal("4.00"), null);
        Item secondItem = new Item(null, null, new BigDecimal("1.00"), null);
        items.add(firstItem);
        items.add(secondItem);

        BigDecimal gross = ItemsSummarizer.calculateGrossForItems(items);
        String vat = ItemsSummarizer.calculateVatFromGross(gross);

        assertEquals("0.83", vat);
    }
}
