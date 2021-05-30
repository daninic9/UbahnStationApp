[[_TOC_]]

__Requirements__
Android Min SDK: 24
Android Target SDK: 30

__Using:__
Apollo Android Library for GraphQl
https://bahnql.herokuapp.com/graphql GraphQl server



# WIKI
__Librarie List:__
Apollo Android (for GraphQl) 
https://www.apollographql.com/docs/android/

### GraphQl server
https://bahnql.herokuapp.com/graphql

### Schema 
__Ask for schema with__

```
{
  __schema {
    queryType {
      fields {
        name
      }
    }
  }
}
```
__ Get schema __
```
{
  "data": {
    "__schema": {
      "queryType": {
        "fields": [
          {
            "name": "routing"
          },
          {
            "name": "stationWithEvaId"
          },
          {
            "name": "stationWithStationNumber"
          },
          {
            "name": "stationWithRill100"
          },
          {
            "name": "search"
          },
          {
            "name": "nearby"
          },
          {
            "name": "parkingSpace"
          }
        ]
      }
    }
  }
}
```

