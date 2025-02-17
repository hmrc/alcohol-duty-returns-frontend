import sbt._

object AppDependencies {

  private val bootstrapVersion = "9.9.0"
  private val hmrcMongoVersion = "2.5.0"
  val mockitoScalaVersion = "1.17.37"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"     % "11.11.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30"  % "3.2.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"     % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"             % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                      % "2.12.0",
    "com.beachape"      %% "enumeratum"                     % "1.7.4",
    "com.beachape"      %% "enumeratum-play"                % "1.8.1",
    "com.beachape"      %% "enumeratum-cats"                % "1.7.4"

  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatest"           %% "scalatest"               % "3.2.19",
    "org.scalatestplus"       %% "scalacheck-1-15"         % "3.2.11.0",
    "org.mockito"             %% "mockito-scala-cats"      % mockitoScalaVersion,
    "com.beachape"            %% "enumeratum-scalacheck"   % "1.7.3",
    "org.scalatestplus"       %% "mockito-3-4"             % "3.2.10.0",
    "org.mockito"             %% "mockito-scala"           % mockitoScalaVersion,
    "org.scalacheck"          %% "scalacheck"              % "1.18.0",
    "org.pegdown"             %  "pegdown"                 % "1.6.0",
    "org.jsoup"               %  "jsoup"                   % "1.18.1",
    "com.vladsch.flexmark"    %  "flexmark-all"            % "0.64.8"
  ).map(_ % Test)

  val itDependencies = Seq.empty
  def apply(): Seq[ModuleID] = compile ++ test
}
