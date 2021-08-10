# product-service #

The following endpoints are implemented:

1. `Get` /

2. `Post` /v1/product

Create a new product. It expects a json object with the following fields:

- `productName`: required, the name of the product.
- `productPriceUsd`: required, the product price in USD.
- `productDescription`: optional, the description of the product.

3. `Get` /v1/product/...productId...

Get a single product information. Support the following params:

- `currency`: optional, one of `USD`, `CAD`, `GBP`, and `EUR`.

4. `Delete` /v1/product/...productId...

Delete the product.

6. `Get` /v1/mostViewed

Get a list of products that got the most viewed. Support the following params:

- `limit`: default to 5. The maximum amount of products being returned.
- `currency`: optional, one of `USD`, `CAD`, `GBP`, and `EUR`.

## Requirement ##

Get an api key from https://currencylayer.com.

## Build & Run ##

```sh
$ sbt flywayMigrate
$ CURRENCY_LAYER_API_ACCESS_KEY="123_api_key" sbt ~jetty:start 
```

Or use [Just](https://github.com/casey/just)

```sh 
$ just CURRENCY_LAYER_API_ACCESS_KEY="123_api_key" jetty-start
```

If `browse` doesn't launch your browser, manually
open [http://localhost:8080/](http://localhost:8080/) in your browser.

## Test ##

```sh
$ sbt flywayMigrate
$ CURRENCY_LAYER_API_ACCESS_KEY="123_api_key" sbt ~jetty:quicktest 
```

Or use [Just](https://github.com/casey/just)

```sh 
$ just CURRENCY_LAYER_API_ACCESS_KEY="123_api_key" test-refresh
```

Tested on AdoptOpenJDK Java 11.0.11.
