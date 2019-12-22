val zioVersion         = "1.0.0-RC17"
val catsVersion        = "2.1.0-RC1"
val catsEffVersion     = "2.0.0"
val catsInteropVersion = "2.0.0.0-RC10"
val simulaVersion      = "1.0.0"

resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val zioDeps = libraryDependencies ++= Seq(
  "dev.zio" %% "zio"              % zioVersion,
  "dev.zio" %% "zio-interop-cats" % catsInteropVersion,
  "dev.zio" %% "zio-test"         % zioVersion % "test",
  "dev.zio" %% "zio-test-sbt"     % zioVersion % "test"
)

lazy val catsDeps = libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core"   % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffVersion
)

lazy val root = (project in file("."))
  .settings(
    organization := "Neurodyne",
    name := "zio-cookbook",
    version := "0.0.1",
    scalaVersion := "2.13.1",
    maxErrors := 3,
    // Refine scalac params from tpolecat
    scalacOptions --= Seq(
      "-Xfatal-warnings"
    ),
    // Scala 2.13
    scalacOptions ++= Seq(
      "-Ymacro-annotations"
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "simulacrum" % simulaVersion
    ),
    zioDeps,
    catsDeps,
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )

addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.0" cross CrossVersion.full)
addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
// addCompilerPlugin("org.scalamacros" % "paradise"            % "2.1.1" cross CrossVersion.full)

// Aliases
addCommandAlias("rel", "reload")
addCommandAlias("com", "all compile test:compile it:compile")
addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
