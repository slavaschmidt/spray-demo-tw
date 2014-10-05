name := "talk"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "org.scream3r"        % "jssc"              % "2.8.0",
  "com.netflix.rxjava"  % "rxjava-scala"      % "0.17.1",
  "org.slf4j"           % "slf4j-api"         % "1.7.7",
  "ch.qos.logback"      % "logback-classic"   % "1.1.2",
  "com.typesafe.akka"   %% "akka-actor"       % "2.3.3",
  "org.scalatest"       %% "scalatest"        % "2.1.7"   % "test"
)

libraryDependencies ++= Seq(
  "io.spray"            %% "spray-can"     % "1.3.1",
  "io.spray"            %% "spray-routing" % "1.3.1",
  "io.spray"            %% "spray-caching" % "1.3.1",
  "io.spray"            %% "spray-http"    % "1.3.1",
  "io.spray"            %% "spray-httpx"   % "1.3.1",
  "io.spray"            %% "spray-util"    % "1.3.1",
  "io.spray"            %% "spray-can"     % "1.3.1",
  "io.spray"            %% "spray-client"  % "1.3.1",
  "io.spray"            %% "spray-json"    % "1.2.6"
)

