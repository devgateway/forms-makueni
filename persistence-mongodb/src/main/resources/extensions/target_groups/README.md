# Target Groups

Procurement Plan, Tender and Organization Target Groups

```
{
  "definitions": {
    "TargetGroup": {
      "title": "Target Group",
      "description": "The name of the target group. Eg PWD, Women, Youth, etc.",
      "type": [
        "string",
        "null"
      ]
    },
    "Item": {
      "properties": {
        "targetGroup": {
          "title": "Target Group",
          "$ref": "#/definitions/TargetGroup"
        },
        "targetGroupValue": {
          "title": "Target Group Value",
          "description": "The monetary value of a single unit, allocated to the target group",
          "$ref": "#/definitions/Value"
        }
      }
    },
    "Tender": {
      "properties": {
        "targetGroup": {
          "title": "Target Group",
          "$ref": "#/definitions/TargetGroup"
        }
      }
    },
    "Organization": {
      "properties": {
        "targetGroup": {
          "title": "Target Group",
          "$ref": "#/definitions/TargetGroup"
        }
      }
    }
  }
}
```