
test-refresh:
	sbt flywayMigrate
	sbt ~jetty:quicktest
