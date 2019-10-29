val ZioVersion = "1.0.0-RC16"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val root = (project in file("."))
  .settings(
    organization := "Neurodyne",
    name := "zio-cookbook",
    version := "0.0.1",
    scalaVersion := "2.12.10",
    maxErrors := 3,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % ZioVersion,
      "dev.zio" %% "zio-test"     % ZioVersion % "test",
      "dev.zio" %% "zio-test-sbt" % ZioVersion % "test"
    )
  )

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

// Refine scalac params from tpolecat
scalacOptions --= Seq(
  "-Xfatal-warnings"
)
addCompilerPlugin(scalafixSemanticdb)
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

// Aliases
addCommandAlias("rel", "reload")
addCommandAlias("com", "all compile test:compile it:compile")
addCommandAlias("lint", "; compile:scalafix --check ; test:scalafix --check")
addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheckAll")
addCommandAlias("cov", "; clean; coverage; test; coverageReport")
