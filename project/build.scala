import sbt._
import Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import net.virtualvoid.sbt.graph._

object SpaceboxBuild extends Build {
  // It has to be this version or the xsbt plugin
  // causes us version conflicts because it loads
  // this version
  val jettyVersion = "9.2.1.v20140609"

  val growl = TaskKey[Unit]("growl", "just tests things")

  val mySettings =
      Seq(
        growl := {
          Seq("growlnotify", "-a", "/Applications/IntelliJ IDEA 14 CE.app/", "-m", "Container Restarting") !
        }
      )

  lazy val project = Project(
    "spacebox-sim2",
    file("."),
    settings =
      mySettings ++
        net.virtualvoid.sbt.graph.Plugin.graphSettings ++

        Seq(
          organization := "com.github.curzonj",
          name := "Spacebox Sim2",
          version := "0.1.0-SNAPSHOT",
          scalaVersion := "2.11.6",
          resolvers += Classpaths.typesafeReleases,
          resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
          resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/",

          libraryDependencies ++= Seq(
            "javax.servlet" % "javax.servlet-api" % "3.1.0" % "runtime;compile;provided;test" artifacts Artifact("javax.servlet-api", "jar", "jar"),

            "org.codehaus.groovy" %	"groovy-all" % "2.4.3",
            "ch.qos.logback" % "logback-classic" % "1.1.2",
            "com.typesafe.akka" % "akka-actor_2.11" % "2.4-M2",
            "org.json4s" %% "json4s-jackson" % "3.3.0.RC2",
            "com.livestream" %% "scredis" % "2.0.6",
            "com.twitter" %% "chill" % "0.6.0",

            "org.eclipse.jetty" % "jetty-plus" % jettyVersion % "container;runtime;provided;compile",
            "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container;runtime;compile",
            "org.eclipse.jetty.websocket" % "websocket-api" % jettyVersion,
            "org.eclipse.jetty.websocket" % "websocket-server" % jettyVersion
          )
        )
  ).enablePlugins(JavaAppPackaging)
}
