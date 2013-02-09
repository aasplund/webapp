import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtStartScript
import com.typesafe.sbtscalariform.ScalariformPlugin
import com.typesafe.sbtscalariform.ScalariformPlugin.ScalariformKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseCreateSrc

object WebAppBuild extends Build {
  val Organization = "webapp-example"
  val Version      = "1.0-SNAPSHOT"
  val ScalaVersion = "2.10"

  lazy val webApp = Project(
    id = "webapp-example",
    base = file("."),
    settings = defaultSettings ++
      Seq(SbtStartScript.stage in Compile := Unit) ++
      Seq(libraryDependencies ++= Dependencies.webApp ++ Dependencies.unfiltered ++ Dependencies.cassandra)
  )

  lazy val defaultSettings = Defaults.defaultSettings ++ Seq(
    resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",

    // compile options
    scalacOptions ++= Seq("-encoding", "UTF-8", "-optimise", "-deprecation", "-unchecked"),
    

    javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),

    // disable parallel tests
    parallelExecution in Test := false,

    EclipseKeys.withSource := true,
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
  )

}

object Dependencies {
  import Dependency._
  val webApp = Seq(scalaTest, jUnit, jacksonMapper, jacksonCore, jacksonScala)
  val unfiltered = Seq(unfilteredFilter, unfilteredNetty, unfilteredNettyServer, unfilteredWebsockets)
  val cassandra = Seq(hectorCore, cassandraThrift)
}

object Dependency {
  object Version {
    val Akka      = "2.1.0"
    val Cassandra = "1.0.6"
    val Hector = "1.0-2"
    val Jackson = "2.1.3"
    val JUnit     = "4.5"
    val Scalatest = "1.9.1"
    val Unfiltered = "0.6.5"
  }

  // ---- Application dependencies ----

  val akkaActor   = "com.typesafe.akka" % "akka-actor" % Version.Akka

  val unfilteredFilter = "net.databinder" % "unfiltered-filter_2.10" % Version.Unfiltered
  val unfilteredNetty = "net.databinder" % "unfiltered-netty_2.10" % Version.Unfiltered
  val unfilteredNettyServer = "net.databinder" % "unfiltered-netty-server_2.10" % Version.Unfiltered
  val unfilteredWebsockets = "net.databinder" % "unfiltered-netty-websockets_2.10" % Version.Unfiltered

  val jacksonMapper = "com.fasterxml.jackson.core" % "jackson-databind" % Version.Jackson
  val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % Version.Jackson
  val jacksonScala = "com.fasterxml.jackson.module" % "jackson-module-scala_2.10" % Version.Jackson

  val cassandraAll = "org.apache.cassandra" % "cassandra-all" % Version.Cassandra
  val cassandraThrift = "org.apache.cassandra" % "cassandra-thrift" % Version.Cassandra
  val hector = "me.prettyprint" % "hector" % Version.Hector
  val hectorCore = "me.prettyprint" % "hector-core" % Version.Hector

  // ---- Test dependencies ----

  val scalaTest   = "org.scalatest"       % "scalatest_2.10"           % Version.Scalatest  % "test"
  val jUnit       = "junit"               % "junit"                    % Version.JUnit      % "test"
  val akkaTest    = "com.typesafe.akka"   % "akka-testkit"             % Version.Akka       % "test"
}
