import sbt.*

object AppDependencies {

  val bootstrapVersion = "10.7.0"

  val compile: Seq[ModuleID] = Seq(
    // format: OFF
    "uk.gov.hmrc"    %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc"    %% "play-frontend-hmrc-play-30"            % "13.0.0",
    "org.typelevel"  %% "cats-core"                             % "2.13.0",
    "uk.gov.hmrc"    %% "play-conditional-form-mapping-play-30" % "3.5.0",
    "com.beachape"   %% "enumeratum-play"                       % "1.9.6",
    "io.lemonlabs"   %% "scala-uri"                             % "4.0.3"
  // format: ON
  )

  val test: Seq[ModuleID] = Seq(
    // format: OFF
    "uk.gov.hmrc"                  %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.wiremock"                  % "wiremock-standalone"    % "3.13.2",
    "org.jsoup"                     % "jsoup"                  % "1.22.1"
  // format: ON
  ).map(_ % Test)
}
