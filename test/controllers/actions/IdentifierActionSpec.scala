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
import controllers.routes
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.mvc.{BodyParsers, Request, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.CredentialStrength.strong
import uk.gov.hmrc.auth.core.retrieve.{~ => combine}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{allEnrolments, groupIdentifier, internalId => retriveInternalId}
import uk.gov.hmrc.http.UnauthorizedException

import scala.concurrent.Future

class IdentifierActionSpec extends SpecBase {
  val loginUrl = "loginUrl"
  val loginContinueUrl = "continueUrl"
  val testContent = "Test"
  val enrolment = "HMRC-AD-ORG"
  val appaIdKey = "APPAID"
  val state = "Activated"
  val enrolments = Enrolments(Set(Enrolment(enrolment, Seq(EnrolmentIdentifier(appaIdKey, appaId)), state)))
  val emptyEnrolments = Enrolments(Set.empty)
  val enrolmentsWithoutAppaId = Enrolments(Set(Enrolment(enrolment, Seq.empty, state)))

  val appConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val defaultBodyParser: BodyParsers.Default = app.injector.instanceOf[BodyParsers.Default]
  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  val identifierAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, defaultBodyParser)

  val testAction: Request[_] => Future[Result] = { _ =>
    Future(Ok(testContent))
  }

  "invokeBlock" - {

    "execute the block and return OK if authorised" in {
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
            retriveInternalId and groupIdentifier and allEnrolments
          )
        )(any(), any())
      )
        .thenReturn(Future(combine(combine(Some(internalId), Some(groupId)), enrolments)))

      val result: Future[Result] = identifierAction.invokeBlock(FakeRequest(), testAction)

      status(result) mustBe OK
      contentAsString(result) mustBe testContent
    }

    "execute the block and throw UnauthorizedException if cannot get the internalId" in {
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
            retriveInternalId and groupIdentifier and allEnrolments
          )
        )(any(), any())
      )
        .thenReturn(Future(combine(combine(None, Some(groupId)), enrolments)))

      intercept[UnauthorizedException] {
        await(identifierAction.invokeBlock(FakeRequest(), testAction))
      }
    }

    "execute the block and throw UnauthorizedException if cannot get the groupId" in {
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
            retriveInternalId and groupIdentifier and allEnrolments
          )
        )(any(), any())
      )
        .thenReturn(Future(combine(combine(Some(internalId), None), enrolments)))

      intercept[UnauthorizedException] {
        await(identifierAction.invokeBlock(FakeRequest(), testAction))
      }
    }

    "execute the block and throw UnauthorizedException if cannot get the enrolment" in {
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
            retriveInternalId and groupIdentifier and allEnrolments
          )
        )(any(), any())
      )
        .thenReturn(Future(combine(combine(Some(internalId), Some(groupId)), emptyEnrolments)))

      intercept[UnauthorizedException] {
        await(identifierAction.invokeBlock(FakeRequest(), testAction))
      }
    }

    "execute the block and throw UnauthorizedException if cannot get the APPAID enrolment" in {
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
            retriveInternalId and groupIdentifier and allEnrolments
          )
        )(any(), any())
      )
        .thenReturn(Future(combine(combine(Some(internalId), Some(groupId)), enrolmentsWithoutAppaId)))

      intercept[UnauthorizedException] {
        await(identifierAction.invokeBlock(FakeRequest(), testAction))
      }
    }

    "redirect to the authorised page if not be authorised" in {
      List(
        InsufficientEnrolments(),
        InsufficientConfidenceLevel(),
        UnsupportedAuthProvider(),
        UnsupportedAffinityGroup(),
        UnsupportedCredentialRole(),
        IncorrectCredentialStrength()
      ).foreach { exception =>
        when(appConfig.enrolmentServiceName).thenReturn(enrolment)
        when(mockAuthConnector.authorise[Unit](any(), any())(any(), any())).thenReturn(Future.failed(exception))

        val result: Future[Result] = identifierAction.invokeBlock(FakeRequest(), testAction)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
      }
    }

    "redirect to the login page if no longer authorised or never logged in" in {
      List(
        BearerTokenExpired(),
        MissingBearerToken(),
        InvalidBearerToken(),
        SessionRecordNotFound()
      ).foreach { exception =>
        when(appConfig.enrolmentServiceName).thenReturn(enrolment)
        when(appConfig.loginUrl).thenReturn(loginUrl)
        when(appConfig.loginContinueUrl).thenReturn(loginContinueUrl)
        when(mockAuthConnector.authorise[Unit](any(), any())(any(), any())).thenReturn(Future.failed(exception))

        val result: Future[Result] = identifierAction.invokeBlock(FakeRequest(), testAction)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe s"$loginUrl?continue=$loginContinueUrl"
      }
    }

    "return the exception if there is any other exception" in {
      val msg = "Test Exception"

      when(appConfig.enrolmentServiceName).thenReturn(enrolment)
      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(Future.failed(new RuntimeException(msg)))

      val result = intercept[RuntimeException] {
        await(identifierAction.invokeBlock(FakeRequest(), testAction))
      }

      result.getMessage mustBe msg
    }
  }
}
