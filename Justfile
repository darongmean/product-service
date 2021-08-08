
test-refresh:
	sbt flywayMigrate
	sbt ~jetty:quicktest

jetty-start:
	sbt flywayMigrate
	sbt ~jetty:start
