import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class Item {
    private String title ;
    private String description;
    @SerializedName(value = "unit_price")
    private BigDecimal price ;
    @SerializedName(value = "kcal_per_100g")
    private Integer kcal;

    public Item(String title, String description, BigDecimal price, Integer kcal) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.kcal = kcal;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
