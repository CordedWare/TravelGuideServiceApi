ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

lazy val root = (project in file("."))
    .settings(
        name := "TravelGuideApi"
    )

libraryDependencies ++= Seq(
    "org.typelevel"     %% "cats-effect"      % "3.3.14",
    "co.fs2"            %% "fs2-core"         % "3.2.14",
    "org.scalatest"     %% "scalatest"        % "3.2.13"   % Test,
    "org.scalatestplus" %% "scalacheck-1-15"  % "3.2.11.0" % Test,
    // imperative libraries:
    "com.typesafe.akka"  % "akka-actor_2.13"  % "2.6.19",
    "org.apache.jena"    % "apache-jena-libs" % "4.6.1",
    "org.apache.jena"    % "jena-fuseki-main" % "4.6.1",
    "org.slf4j"          % "slf4j-nop"        % "2.0.0",
)
initialCommands := s"""
      import fs2._, cats.effect._, cats.implicits._, cats.effect.unsafe.implicits.global
      import scala.concurrent.duration._, java.util.concurrent._
      import scala.jdk.javaapi.CollectionConverters.asScala
      import org.apache.jena.query._, org.apache.jena.rdfconnection._
    """
