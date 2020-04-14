resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val zioDeps = libraryDependencies ++= Seq(
  "dev.zio" %% "zio"              % Version.zio,
  "dev.zio" %% "zio-streams"      % Version.zio,
  "dev.zio" %% "zio-interop-cats" % Version.zioInteropCats,
  "dev.zio" %% "zio-test"         % Version.zio % "test",
  "dev.zio" %% "zio-test-sbt"     % Version.zio % "test"
)

lazy val catsDeps = libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % Version.cats
)

lazy val commonDeps = libraryDependencies ++= Seq(
  "org.typelevel"         %% "simulacrum" % Version.simulacrum,
  "com.github.pureconfig" %% "pureconfig" % Version.pureconfig
)

lazy val root = (project in file("."))
  .settings(
    organization := "Neurodyne",
    name := "zio-cookbook",
    version := "0.0.1",
    scalaVersion := "2.13.1",
    maxErrors := 3,
    zioDeps,
    catsDeps,
    commonDeps,
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
// Refine scalac params from tpolecat
scalacOptions --= Seq(
  "-Xfatal-warnings"
)
// Scala 2.13
scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-Ywarn-unused"
)

addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.0" cross CrossVersion.full)
addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")

// Aliases
addCommandAlias("rel", "reload")
addCommandAlias("com", "all compile test:compile it:compile")
addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")

scalafixDependencies in ThisBuild += "com.nequissimus" %% "sort-imports" % "0.3.2"
