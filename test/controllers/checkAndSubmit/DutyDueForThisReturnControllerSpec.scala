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

package controllers.checkAndSubmit

import base.SpecBase
import cats.data.EitherT
import connectors.AlcoholDutyReturnsConnector
import models.audit.AuditReturnSubmitted
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.inject.bind
import play.api.test.Helpers._
import services.AuditService
import services.checkAndSubmit.AdrReturnSubmissionService
import viewmodels.TableViewModel
import viewmodels.checkAnswers.checkAndSubmit.{DutyDueForThisReturnHelper, DutyDueForThisReturnViewModel}
import viewmodels.tasklist.TaskListViewModel
import views.html.checkAndSubmit.DutyDueForThisReturnView

import java.time.{Clock, Instant}

class DutyDueForThisReturnControllerSpec extends SpecBase {
  "onPageLoad" - {
    "must return OK and the correct view if Yes is selected and there is alcohol to declare" in new SetUp {
      when(taskListViewModelMock.checkAllDeclarationSectionsCompleted(any(), any())(any())).thenReturn(true)
      when(dutyDueForThisReturnHelper.getDutyDueViewModel(any(), any())(any(), any())).thenReturn(
        EitherT.rightT(viewModel)
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[DutyDueForThisReturnHelper].toInstance(dutyDueForThisReturnHelper))
        .overrides(bind[TaskListViewModel].toInstance(taskListViewModelMock))
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutyDueForThisReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, getMessages(application)).toString
      }
    }

    "must redirect in the Journey Recovery screen if any task is not complete" in new SetUp {
      when(taskListViewModelMock.checkAllDeclarationSectionsCompleted(any(), any())(any())).thenReturn(false)
      when(dutyDueForThisReturnHelper.getDutyDueViewModel(any(), any())(any(), any())).thenReturn(
        EitherT.rightT(viewModel)
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[DutyDueForThisReturnHelper].toInstance(dutyDueForThisReturnHelper))
        .overrides(bind[TaskListViewModel].toInstance(taskListViewModelMock))
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect in the Journey Recovery screen if the dutyDueForThisReturnHelper return an error" in new SetUp {
      when(taskListViewModelMock.checkAllDeclarationSectionsCompleted(any(), any())(any())).thenReturn(true)
      when(dutyDueForThisReturnHelper.getDutyDueViewModel(any(), any())(any(), any())).thenReturn(
        EitherT.leftT("Error message")
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[DutyDueForThisReturnHelper].toInstance(dutyDueForThisReturnHelper))
        .overrides(bind[TaskListViewModel].toInstance(taskListViewModelMock))
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "onSubmit" - {
    "must redirect to the Return Submitted page if the submission is successful" in new SetUp {
      when(taskListViewModelMock.checkAllDeclarationSectionsCompleted(any(), any())(any())).thenReturn(true)

      when(
        alcoholDutyReturnsConnector.submitReturn(any(), any(), any())(any())
      ).thenReturn(
        EitherT.rightT(adrReturnCreatedDetails)
      )

      when(adrReturnSubmissionService.getAdrReturnSubmission(any(), any())(any())).thenReturn(
        EitherT.rightT(fullReturn)
      )

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers))
        .overrides(bind[DutyDueForThisReturnHelper].toInstance(dutyDueForThisReturnHelper))
        .overrides(bind[AdrReturnSubmissionService].toInstance(adrReturnSubmissionService))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(alcoholDutyReturnsConnector))
        .overrides(bind[TaskListViewModel].toInstance(taskListViewModelMock))
        .overrides(bind[AuditService].toInstance(auditService))
        .overrides(bind[Clock].toInstance(clock))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.checkAndSubmit.routes.ReturnSubmittedController
          .onPageLoad()
          .url

        verify(auditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
      }
    }

    "must redirect in the Journey Recovery screen if any task is not complete" in new SetUp {
      when(taskListViewModelMock.checkAllDeclarationSectionsCompleted(any(), any())(any())).thenReturn(false)

      when(
        alcoholDutyReturnsConnector.submitReturn(any(), any(), any())(any())
      ).thenReturn(
        EitherT.rightT(adrReturnCreatedDetails)
      )

      when(adrReturnSubmissionService.getAdrReturnSubmission(any(), any())(any())).thenReturn(
        EitherT.rightT(fullReturn)
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[DutyDueForThisReturnHelper].toInstance(dutyDueForThisReturnHelper))
        .overrides(bind[AdrReturnSubmissionService].toInstance(adrReturnSubmissionService))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(alcoholDutyReturnsConnector))
        .overrides(bind[TaskListViewModel].toInstance(taskListViewModelMock))
        .overrides(bind[AuditService].toInstance(auditService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        verify(auditService, times(0)).audit(any())(any(), any())
      }
    }

    "must redirect to the Journey Recovery page if mapping from UserAnswers to AdrReturnSubmission return an error" in new SetUp {
      when(taskListViewModelMock.checkAllDeclarationSectionsCompleted(any(), any())(any())).thenReturn(true)

      when(adrReturnSubmissionService.getAdrReturnSubmission(any(), any())(any())).thenReturn(
        EitherT.leftT("Error message")
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[DutyDueForThisReturnHelper].toInstance(dutyDueForThisReturnHelper))
        .overrides(bind[AdrReturnSubmissionService].toInstance(adrReturnSubmissionService))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(alcoholDutyReturnsConnector))
        .overrides(bind[TaskListViewModel].toInstance(taskListViewModelMock))
        .overrides(bind[AuditService].toInstance(auditService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        verify(auditService, times(0)).audit(any())(any(), any())
      }
    }

    "must redirect to the Journey Recovery page if the submission is not successful" in new SetUp {
      when(taskListViewModelMock.checkAllDeclarationSectionsCompleted(any(), any())(any())).thenReturn(true)

      when(adrReturnSubmissionService.getAdrReturnSubmission(any(), any())(any())).thenReturn(
        EitherT.rightT(adrReturnSubmission)
      )

      when(alcoholDutyReturnsConnector.submitReturn(any(), any(), any())(any())).thenReturn(
        EitherT.leftT("Error message")
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[DutyDueForThisReturnHelper].toInstance(dutyDueForThisReturnHelper))
        .overrides(bind[AdrReturnSubmissionService].toInstance(adrReturnSubmissionService))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(alcoholDutyReturnsConnector))
        .overrides(bind[TaskListViewModel].toInstance(taskListViewModelMock))
        .overrides(bind[AuditService].toInstance(auditService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        verify(auditService, times(0)).audit(any())(any(), any())
      }
    }
  }

  class SetUp {
    val dutyDueForThisReturnHelper  = mock[DutyDueForThisReturnHelper]
    val taskListViewModelMock       = mock[TaskListViewModel]
    val adrReturnSubmissionService  = mock[AdrReturnSubmissionService]
    val alcoholDutyReturnsConnector = mock[AlcoholDutyReturnsConnector]
    val auditService                = mock[AuditService]
    val submissionTime              = Instant.now(clock)

    val emptyDutiesBreakdownTable: TableViewModel = TableViewModel(
      head = Seq.empty,
      rows = Seq.empty
    )

    val emptyYouveAlsoDeclaredTable: TableViewModel = TableViewModel(
      head = Seq.empty,
      rows = Seq.empty
    )

    val viewModel = DutyDueForThisReturnViewModel(
      dutiesBreakdownTable = emptyDutiesBreakdownTable,
      youveAlsoDeclaredTable = emptyYouveAlsoDeclaredTable,
      totalDue = BigDecimal(1)
    )

    val expectedAuditEvent = AuditReturnSubmitted(fullUserAnswers, fullReturn, submissionTime)

    val adrReturnSubmission = fullReturn
  }
}
