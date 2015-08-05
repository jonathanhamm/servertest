
name := "httpserver"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.3.11"
libraryDependencies += "io.spray" % "spray-client_2.11" % "1.3.3"
libraryDependencies += "org.scalikejdbc" %% "scalikejdbc" % "2.2.7"
libraryDependencies += "org.scalikejdbc" %% "scalikejdbc-test" % "2.2.7" % "test"
libraryDependencies += "com.h2database" % "h2" % "1.4.187"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"
