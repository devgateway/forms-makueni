# Planning Items

Allowing Item elements in Planning section, to reflect Makueni's item procurement planning.

```
{
  "definitions": {
    "Planning": {
      "properties": {
        "items": {
          "title": "Items to be procured",
          "description": "The goods and services to be purchased, broken into line items wherever possible. Items should not be duplicated, but a quantity of 2 specified instead.",
          "type": "array",
          "items": {
            "$ref": "#/definitions/Item"
          },
          "uniqueItems": true
        }
      }
    }
  }
}
```