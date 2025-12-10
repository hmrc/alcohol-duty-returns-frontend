import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.4.0"
  private val hmrcMongoVersion = "2.11.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"     % "12.22.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30"  % "3.4.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"     % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"             % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                      % "2.13.0",
    "com.beachape"      %% "enumeratum"                     % "1.9.0",
    "com.beachape"      %% "enumeratum-play"                % "1.9.0",
    "com.beachape"      %% "enumeratum-cats"                % "1.9.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatest"           %% "scalatest"               % "3.2.19",
    "org.scalatestplus"       %% "scalacheck-1-17"         % "3.2.18.0",
    "com.beachape"            %% "enumeratum-scalacheck"   % "1.9.0",
    "org.scalacheck"          %% "scalacheck"              % "1.19.0",
    "org.jsoup"               %  "jsoup"                   % "1.21.2",
    "com.vladsch.flexmark"    %  "flexmark-all"            % "0.64.8"
  ).map(_ % Test)

  val itDependencies = Seq.empty
  def apply(): Seq[ModuleID] = compile ++ test
}
