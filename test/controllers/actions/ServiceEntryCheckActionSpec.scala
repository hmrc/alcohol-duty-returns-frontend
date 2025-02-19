/*
 * Copyright 2024 HM Revenue & Customs
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
import models.requests.IdentifierWithoutEnrolmentRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, EnrolmentIdentifier, Enrolments, InsufficientEnrolments}

import scala.concurrent.Future

class ServiceEntryCheckActionSpec extends SpecBase {

  val enrolment               = "HMRC-AD-ORG"
  val appaIdKey               = "APPAID"
  val state                   = "Activated"
  val testContent             = "Ok"
  val enrolments              = Enrolments(Set(Enrolment(enrolment, Seq(EnrolmentIdentifier(appaIdKey, appaId)), state)))
  val emptyEnrolments         = Enrolments(Set.empty)
  val enrolmentsWithoutAppaId = Enrolments(Set(Enrolment(enrolment, Seq.empty, state)))

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  val enrolmentAction = new ServiceEntryCheckActionImpl(mockAuthConnector, mockAppConfig)

  val request = IdentifierWithoutEnrolmentRequest(FakeRequest(), groupId, internalId)

  val testAction: Request[_] => Future[Result] = { _ =>
    Future(Ok(testContent))
  }

  "invokeBlock" - {
    "execute the block and return OK if authorised" in {
      when(mockAppConfig.enrolmentServiceName).thenReturn(enrolment)
      when(mockAppConfig.enrolmentIdentifierKey).thenReturn(appaIdKey)
      when(mockAuthConnector.authorise(any(), eqTo(allEnrolments))(any(), any())).thenReturn(Future(enrolments))

      val result: Future[Result] = enrolmentAction.invokeBlock(request, testAction)

      status(result) mustBe OK
      contentAsString(result) mustBe testContent
    }

    "must redirect to enrolment request page if not adr appaId is present" in {
      when(mockAppConfig.enrolmentServiceName).thenReturn(enrolment)
      when(mockAppConfig.enrolmentIdentifierKey).thenReturn(appaIdKey)
      when(mockAuthConnector.authorise(any(), eqTo(allEnrolments))(any(), any()))
        .thenReturn(Future(enrolmentsWithoutAppaId))

      val result: Future[Result] = enrolmentAction.invokeBlock(request, testAction)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.auth.routes.DoYouHaveAnAppaIdController.onPageLoad().url
    }

    "must redirect to enrolment request page if not there are no enrolments" in {
      when(mockAppConfig.enrolmentServiceName).thenReturn(enrolment)
      when(mockAppConfig.enrolmentIdentifierKey).thenReturn(appaIdKey)
      when(mockAuthConnector.authorise(any(), eqTo(allEnrolments))(any(), any())).thenReturn(Future(emptyEnrolments))

      val result: Future[Result] = enrolmentAction.invokeBlock(request, testAction)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.auth.routes.DoYouHaveAnAppaIdController.onPageLoad().url
    }

    "must redirect to enrolment request page if the AppaId is an empty string" in {
      when(mockAppConfig.enrolmentServiceName).thenReturn(enrolment)
      when(mockAppConfig.enrolmentIdentifierKey).thenReturn(appaIdKey)
      val enrolmentsWithEmptyAppaId =
        Enrolments(Set(Enrolment(enrolment, Seq(EnrolmentIdentifier(appaIdKey, "")), state)))
      when(mockAuthConnector.authorise(any(), eqTo(allEnrolments))(any(), any()))
        .thenReturn(Future(enrolmentsWithEmptyAppaId))

      val result: Future[Result] = enrolmentAction.invokeBlock(request, testAction)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.auth.routes.DoYouHaveAnAppaIdController.onPageLoad().url
    }

    "must redirect Unauthorised if the authorization method throw an exception" in {
      when(mockAppConfig.enrolmentServiceName).thenReturn(enrolment)
      when(mockAppConfig.enrolmentIdentifierKey).thenReturn(appaIdKey)
      when(mockAuthConnector.authorise(any(), eqTo(allEnrolments))(any(), any()))
        .thenReturn(Future.failed(new Exception()))

      val result: Future[Result] = enrolmentAction.invokeBlock(request, testAction)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
    }

    "must redirect request access url if the authorization method throw an InsufficientEnrolments exception" in {
      when(mockAppConfig.enrolmentServiceName).thenReturn(enrolment)
      when(mockAppConfig.enrolmentIdentifierKey).thenReturn(appaIdKey)
      when(mockAuthConnector.authorise(any(), eqTo(allEnrolments))(any(), any()))
        .thenReturn(Future.failed(InsufficientEnrolments()))

      val result: Future[Result] = enrolmentAction.invokeBlock(request, testAction)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.auth.routes.DoYouHaveAnAppaIdController.onPageLoad().url
    }
  }

}
