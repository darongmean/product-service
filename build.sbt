val ScalatraVersion = "2.8.0"
val WingtipsVersion = "0.23.1"

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / organization := "com.darongmean"

lazy val productService = (project in file("."))
  .settings(
    name := "product-service",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      // web
      "org.scalatra" %% "scalatra" % ScalatraVersion,
      "org.eclipse.jetty" % "jetty-webapp" % "9.4.35.v20201120" % "container",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
      // json
      "org.scalatra" %% "scalatra-json" % ScalatraVersion,
      "org.json4s" %% "json4s-jackson" % "4.0.3",
      // database
      "com.typesafe.slick" %% "slick" % "3.3.2",
      "com.h2database" % "h2" % "1.4.200",
      "com.mchange" % "c3p0" % "0.9.5.2",
      // http client
      "org.apache.httpcomponents" % "httpclient" % "4.5.6",
      // distributed system tracing
      "com.nike.wingtips" % "wingtips-core" % WingtipsVersion,
      "com.nike.wingtips" % "wingtips-servlet-api" % WingtipsVersion,
      "com.nike.wingtips" % "wingtips-apache-http-client" % WingtipsVersion,
      // log
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
      // testing
      "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
      // swagger
      "org.scalatra" %% "scalatra-swagger" % ScalatraVersion,
    ),
  )

flywayLocations := Seq("migration/h2database")
flywayUrl := "jdbc:h2:./target/productServiceH2"
flywayUser := "root"
flywayPassword := ""

enablePlugins(JettyPlugin)
enablePlugins(FlywayPlugin)
