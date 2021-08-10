export CURRENCY_LAYER_API_ACCESS_KEY := `gpg -q -d secret/currency-layer-api-access-key-darongmean.txt.gpg`

test-refresh:
	sbt flywayMigrate
	sbt ~jetty:quicktest

jetty-start:
	sbt flywayMigrate
	sbt ~jetty:start

show-env:
	echo $CURRENCY_LAYER_API_ACCESS_KEY
