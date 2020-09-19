This Java console application project scrapes a sample [Sainsbury's website](https://jsainsburyplc.github.io/serverside-test/site/www.sainsburys.co.uk/webapp/wcs/stores/servlet/gb/groceries/berries-cherries-currants6039.html) and log out a JSON summary of products on the page.

If all information is available on an item's page, JSON for that item will look like:
 ```
{
    "title": "Sainsbury's Strawberries 400g",
    "description": "by Sainsbury's strawberries",
    "unit_price": 1.75,
    "kcal_per_100g": 33
}
```
, as described in the [brief's specification.](https://jsainsburyplc.github.io/serverside-test/). `kcal_per_100g` will be absent if the item's calories are unavailable. The JSON will include a summary of gross and vat, for example:
```
  {
    "gross": "39.50",
    "vat": "6.58"
  }
```

There's just one CLI arg at the moment:

- `-u` / `--url`, to specify the page to scrape (must be similar to the example berries-cherries page, e.g. [this bananas-grapes page](https://jsainsburyplc.github.io/serverside-test/site/www.sainsburys.co.uk/shop/gb/groceries/fruit-veg/bananas-grapes.html)  which also has items stored in `gridItem` classes)

Written using `Java 8`, `JUnit` tests (`ItemSummarizerTest`, `ScraperApplicationTest` - just press run in `IntelliJ`), `Gradle`, `commons-cli`, and `HtmlUnit` 
