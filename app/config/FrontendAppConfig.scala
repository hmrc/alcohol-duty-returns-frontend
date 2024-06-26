/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost                  = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "alcohol-duty-returns-frontend"

  private lazy val adrReturnsHost: String =
    servicesConfig.baseUrl("alcohol-duty-returns")

  private lazy val adrCalculatorHost: String =
    servicesConfig.baseUrl("alcohol-duty-calculator")

  private val adrCalculatorRootUrl: String      =
    configuration.get[String]("microservice.services.alcohol-duty-calculator.rootUrl")
  private val adrCalculatorRatesUrlPart: String =
    configuration.get[String]("microservice.services.alcohol-duty-calculator.ratesUrl")

  private val adrCalculatorCalculateDutyUrlPart: String          =
    configuration.get[String]("microservice.services.alcohol-duty-calculator.calculateDutyUrl")
  private val adrCalculatorRateBandUrlPart: String               =
    configuration.get[String]("microservice.services.alcohol-duty-calculator.rateBandUrl")
  private val adrCalculatorRateTypeUrlPart: String               =
    configuration.get[String]("microservice.services.alcohol-duty-calculator.rateTypeUrl")
  def feedbackUrl(implicit request: RequestHeader): java.net.URL =
    url"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  val loginUrl: String              = configuration.get[String]("urls.login")
  val loginContinueUrl: String      = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String            = configuration.get[String]("urls.signOut")
  val appaIdRegisterUrl: String     = configuration.get[String]("urls.appaIdRegister")
  val businessTaxAccountUrl: String = configuration.get[String]("urls.businessTaxAccount")
  val requestAccessUrl: String      = configuration.get[String]("urls.requestAccess")

  val fromBusinessAccountPath: String = configuration.get[String]("fromBusinessAccountPath")

  private val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/alcohol-duty-returns-frontend"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  val enrolmentServiceName   = configuration.get[String]("enrolment.serviceName")
  val enrolmentIdentifierKey = configuration.get[String]("enrolment.identifierKey")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  def adrCacheGetUrl(appaId: String, periodKey: String): String =
    s"$adrReturnsHost/alcohol-duty-returns/cache/get/$appaId/$periodKey"

  def adrCacheSetUrl(): String =
    s"$adrReturnsHost/alcohol-duty-returns/cache/set"

  def adrCacheCreateUserAnswersUrl(): String =
    s"$adrReturnsHost/alcohol-duty-returns/cache/user-answers"

  def adrCacheClearAllUrl(): String =
    s"$adrReturnsHost/alcohol-duty-returns/test-only/cache/clear-all"

  def adrGetObligationDetailsUrl(appaId: String): String =
    s"$adrReturnsHost/alcohol-duty-returns/obligationDetails/$appaId"

  def adrCalculatorRatesUrl(): String =
    adrCalculatorHost + adrCalculatorRootUrl + adrCalculatorRatesUrlPart

  def adrCalculatorCalculateDutyUrl(): String =
    adrCalculatorHost + adrCalculatorRootUrl + adrCalculatorCalculateDutyUrlPart

  def adrCalculatorRateBandUrl(): String =
    adrCalculatorHost + adrCalculatorRootUrl + adrCalculatorRateBandUrlPart
  def adrCalculatorRateTypeUrl(): String =
    adrCalculatorHost + adrCalculatorRootUrl + adrCalculatorRateTypeUrlPart
}
