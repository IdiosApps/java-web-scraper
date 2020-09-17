import java.math.BigDecimal;

public class Item {
    private String title ;
    private String description;
    private BigDecimal price ;
    private Integer kcal; // assume int is precise enough // snake case to help json formatting?

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
