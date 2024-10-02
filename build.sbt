import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.14"

lazy val appName: String = "alcohol-duty-returns-frontend"

lazy val root = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings): _*)
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    RoutesKeys.routesImport ++= Seq(
      "models._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl",
    ),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.config._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._",
      "viewmodels.govuk.all._"
    ),
    PlayKeys.playDefaultPort := 16000,
    ScoverageKeys.coverageExcludedFiles := scoverageExcludedList.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 97,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= Seq(
      "-feature",
      "-rootdir",
      baseDirectory.value.getCanonicalPath,
      "-Wconf:cat=deprecation:ws,cat=feature:ws,cat=optimizer:ws,src=target/.*:s",
      "-deprecation",
      "-Ypatmat-exhaust-depth",
      "40"
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    resolvers ++= Seq(Resolver.jcenterRepo),
    // concatenate js
    Concat.groups := Seq(
      "javascripts/application.js" ->
        group(Seq(
          "javascripts/app.js"
        ))
    ),
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    uglifyOps := UglifyOps.singleFile,
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    Assets / pipelineStages := Seq(concat,uglify),
    // only compress files generated by concat
    uglify / includeFilter := GlobFilter("application.js"),
    scalafmtOnCompile := true,
  )

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    libraryDependencies ++= AppDependencies.itDependencies,
    Test / unmanagedResourceDirectories += baseDirectory.value / "it" / "test" / "resources",
    Test / parallelExecution := false,
    Test / fork := true
  )

lazy val scoverageExcludedList:Seq[String] = Seq(
  "<empty>",
  "Reverse.*",
  ".*handlers.*",
  ".*components.*",
  ".*pages.*",
  ".*Routes.*",
  ".*viewmodels.govuk.*",
  ".*views.*",
  "testOnly.*",
  ".*testOnly.*",
  ".*TestOnlyCacheConnector.*",
  ".*TestOnlyController.*",
  "testOnlyDoNotUseInAppConf.*"
)


addCommandAlias("runAllChecks", ";clean;compile;scalafmtAll;coverage;test;it/test;scalastyle;coverageReport")
