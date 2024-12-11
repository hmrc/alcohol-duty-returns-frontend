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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import models.requests.RequestWithOptAppaId
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.mvc.{BodyParsers, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.CredentialStrength.strong
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.http.UnauthorizedException

import scala.collection.mutable
import scala.concurrent.Future

class SignOutActionSpec extends SpecBase {
  val loginUrl                = "loginUrl"
  val loginContinueUrl        = "continueUrl"
  val testContent             = "Test"
  val enrolment               = "HMRC-AD-ORG"
  val appaIdKey               = "APPAID"
  val state                   = "Activated"
  val enrolments              = Enrolments(Set(Enrolment(enrolment, Seq(EnrolmentIdentifier(appaIdKey, appaId)), state)))
  val emptyEnrolments         = Enrolments(Set.empty)
  val enrolmentsWithoutAppaId = Enrolments(Set(Enrolment(enrolment, Seq.empty, state)))
  val signOutUrl: String      = "http://localhost:9553/bas-gateway/sign-out-without-state"
  val appaIdStoreKey: String  = "AppaId"

  val appConfig: FrontendAppConfig           = mock[FrontendAppConfig]
  val defaultBodyParser: BodyParsers.Default = app.injector.instanceOf[BodyParsers.Default]
  val mockAuthConnector: AuthConnector       = mock[AuthConnector]

  val signOutAction = new SignOutActionImpl(mockAuthConnector, appConfig, defaultBodyParser)

  def testAction(appaIdStore: mutable.Map[String, Option[String]]): RequestWithOptAppaId[_] => Future[Result] = {
    request =>
      appaIdStore.synchronized {
        appaIdStore.put(appaIdStoreKey, request.appaId)
      }

      Future(Redirect(signOutUrl))
  }

  "invokeBlock" - {
    "execute the block and return SEE_OTHER if authorised" in {
      when(appConfig.enrolmentServiceName).thenReturn(enrolment)
      when(appConfig.enrolmentIdentifierKey).thenReturn(appaIdKey)
      when(
        mockAuthConnector.authorise(
          eqTo(
            AuthProviders(GovernmentGateway)
              and Enrolment(enrolment)
              and CredentialStrength(strong)
              and Organisation
              and ConfidenceLevel.L50
          ),
          eqTo(
            allEnrolments
          )
        )(any(), any())
      ).thenReturn(Future(enrolments))

      val appaIdStore = mutable.Map[String, Option[String]]()

      val result: Future[Result] = signOutAction.invokeBlock(FakeRequest(), testAction(appaIdStore))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(signOutUrl)

      appaIdStore.synchronized {
        appaIdStore(appaIdStoreKey) mustBe Some(appaId)
      }
    }

    "execute the block and throw IllegalStateException if cannot get the enrolment" in {
      when(appConfig.enrolmentServiceName).thenReturn(enrolment)
      when(appConfig.enrolmentIdentifierKey).thenReturn(appaIdKey)
      when(
        mockAuthConnector.authorise(
          eqTo(
            AuthProviders(GovernmentGateway)
              and Enrolment(enrolment)
              and CredentialStrength(strong)
              and Organisation
              and ConfidenceLevel.L50
          ),
          eqTo(
            allEnrolments
          )
        )(any(), any())
      ).thenReturn(Future(emptyEnrolments))

      val appaIdStore = mutable.Map[String, Option[String]]()

      intercept[IllegalStateException] {
        await(signOutAction.invokeBlock(FakeRequest(), testAction(appaIdStore)))
      }
    }

    "execute the block and throw IllegalStateException if cannot get the APPAID enrolment" in {
      when(appConfig.enrolmentServiceName).thenReturn(enrolment)
      when(appConfig.enrolmentIdentifierKey).thenReturn(appaIdKey)
      when(
        mockAuthConnector.authorise(
          eqTo(
            AuthProviders(GovernmentGateway)
              and Enrolment(enrolment)
              and CredentialStrength(strong)
              and Organisation
              and ConfidenceLevel.L50
          ),
          eqTo(
            allEnrolments
          )
        )(any(), any())
      ).thenReturn(Future(enrolmentsWithoutAppaId))

      val appaIdStore = mutable.Map[String, Option[String]]()

      intercept[IllegalStateException] {
        await(signOutAction.invokeBlock(FakeRequest(), testAction(appaIdStore)))
      }
    }

    "redirect to the sign out page and do not release locks if not authorised" in {
      List(
        InsufficientEnrolments(),
        InsufficientConfidenceLevel(),
        UnsupportedAuthProvider(),
        UnsupportedAffinityGroup(),
        UnsupportedCredentialRole(),
        IncorrectCredentialStrength(),
        new UnauthorizedException("")
      ).foreach { exception =>
        when(appConfig.enrolmentServiceName).thenReturn(enrolment)
        when(mockAuthConnector.authorise[Unit](any(), any())(any(), any())).thenReturn(Future.failed(exception))

        val appaIdStore = mutable.Map[String, Option[String]]()

        val result: Future[Result] = signOutAction.invokeBlock(FakeRequest(), testAction(appaIdStore))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(signOutUrl)

        appaIdStore.synchronized {
          appaIdStore(appaIdStoreKey) mustBe None
        }
      }
    }

    "return the exception if there is any other exception" in {
      val msg = "Test Exception"

      when(appConfig.enrolmentServiceName).thenReturn(enrolment)
      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(Future.failed(new RuntimeException(msg)))

      val appaIdStore = mutable.Map[String, Option[String]]()

      val result = intercept[RuntimeException] {
        await(signOutAction.invokeBlock(FakeRequest(), testAction(appaIdStore)))
      }

      result.getMessage mustBe msg
    }
  }
}
