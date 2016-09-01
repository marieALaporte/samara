lazy val commonSettings = Seq(
  organization := "org.planteome",
  version := "0.1.7",
  scalaVersion := "2.11.8"
)

val taxonCacheModule = "org.eol" % "eol-globi-datasets" % "1.0-SNAPSHOT" artifacts Artifact("eol-globi-datasets", "zip", "zip").copy(classifier = Some("taxa"))

lazy val installTaxonCache = taskKey[Seq[java.io.File]]("install taxon cache in resources")

installTaxonCache := {
  val log: Logger = streams.value.log
  log.info("taxon cache installing...")
  val targetDir = (resourceManaged in Compile).value / "org" / "eol" / "globi" / "taxon"
  IO.createDirectory(targetDir)

  val taxonArchive = (update in Compile).value
    .filter { (module: ModuleID) => {
      module.toString() == taxonCacheModule.toString()
    }
    }.select(configurationFilter("compile")).headOption

  val extracted = taxonArchive match {
    case Some(archiveFilename) => {
      log.info(s"[$taxonCacheModule] unpacking to [$targetDir]...")
      IO.unzip(archiveFilename, targetDir)
    }
    case None => {
      toError(Some(s"no archive found for [$taxonCacheModule]."))
      Set()
    }
  }
  log.info("taxon cache installed.")
  extracted.toSeq.filter(_.getName.contains("taxonMap"))
}


lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "samara",
    resolvers ++= Seq(Resolver.sonatypeRepo("public"),
      "GloBI releases" at "https://s3.amazonaws.com/globi/release/",
      "GloBI snapshots" at "https://s3.amazonaws.com/globi/snapshot/"),
    libraryDependencies ++= Seq(
      "net.ruippeixotog" %% "scala-scraper" % "1.0.0",
      "org.scalatest" %% "scalatest" % "2.2.5" % "test",
      "com.github.scopt" %% "scopt" % "3.5.0",
      taxonCacheModule
    ),
    resourceGenerators in Compile += installTaxonCache.taskValue,
    test in assembly := {}
  )
