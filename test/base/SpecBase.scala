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

package base

import config.Constants.periodKeySessionKey
import controllers.actions._
import generators.ModelGenerators
import models.{ReturnId, UserAnswers}
import org.mockito.MockitoSugar
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results

import scala.concurrent.ExecutionContext

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with Results
    with GuiceOneAppPerSuite
    with MockitoSugar
    with IntegrationPatience
    with ModelGenerators {

  val appaId: String        = appaIdGen.sample.get
  val periodKey: String     = periodKeyGen.sample.get
  val groupId: String       = "groupid"
  val userAnswersId: String = "id"
  val returnId: ReturnId    = ReturnId(appaId, periodKey)

  def emptyUserAnswers: UserAnswers = UserAnswers(returnId, groupId, userAnswersId)

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[AuthorisedAction].to[FakeAuthorisedAction],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )
  def FakeRequest()                                                                                  = play.api.test.FakeRequest().withSession((periodKeySessionKey, periodKey))
  def FakeRequest(verb: String, route: String)                                                       =
    play.api.test.FakeRequest(verb, route).withSession((periodKeySessionKey, periodKey))
  def FakeRequestWithoutSession()                                                                    = play.api.test.FakeRequest()
  def FakeRequestWithoutSession(verb: String, route: String)                                         = play.api.test.FakeRequest(verb, route)

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}
