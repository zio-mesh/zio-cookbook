val zioVersion         = "1.0.0-RC17"
val catsVersion        = "2.1.0-RC1"
val catsEffVersion     = "2.0.0"
val catsInteropVersion = "2.0.0.0-RC8"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val root = (project in file("."))
  .settings(
    organization := "Neurodyne",
    name := "zio-cookbook",
    version := "0.0.1",
    scalaVersion := "2.13.1",
    maxErrors := 3,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"        % catsVersion,
      "org.typelevel" %% "cats-effect"      % catsEffVersion,
      "dev.zio"       %% "zio"              % zioVersion,
      "dev.zio"       %% "zio-interop-cats" % catsInteropVersion,
      "dev.zio"       %% "zio-test"         % zioVersion % "test",
      "dev.zio"       %% "zio-test-sbt"     % zioVersion % "test"
    )
  )

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

// Refine scalac params from tpolecat
scalacOptions --= Seq(
  "-Xfatal-warnings"
)
addCompilerPlugin("org.typelevel"   %% "kind-projector"     % "0.11.0" cross CrossVersion.full)
addCompilerPlugin("com.olegpy"      %% "better-monadic-for" % "0.3.1")
// addCompilerPlugin("org.scalamacros" % "paradise"            % "2.1.1" cross CrossVersion.full)

// Aliases
addCommandAlias("rel", "reload")
addCommandAlias("com", "all compile test:compile it:compile")
addCommandAlias("lint", "; compile:scalafix --check ; test:scalafix --check")
addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheckAll")
addCommandAlias("cov", "; clean; coverage; test; coverageReport")
