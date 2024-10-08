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
import config.Constants.periodKeySessionKey
import config.FrontendAppConfig
import connectors.AlcoholDutyReturnsConnector
import models.{ReturnId, ReturnPeriod}
import models.returns.{AdrReturnCreatedDetails, AdrReturnSubmission}
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import play.api.test.Helpers._
import services.AuditService
import services.checkAndSubmit.AdrReturnSubmissionService
import viewmodels.TableViewModel
import viewmodels.checkAnswers.checkAndSubmit.{DutyDueForThisReturnHelper, DutyDueForThisReturnViewModel}
import views.html.checkAndSubmit.DutyDueForThisReturnView

import java.time.{Instant, LocalDate}

class DutyDueForThisReturnControllerSpec extends SpecBase {

  val viewModel = DutyDueForThisReturnViewModel(
    dutiesBreakdownTable = TableViewModel(
      head = Seq.empty,
      rows = Seq.empty
    ),
    totalDue = BigDecimal(1)
  )

  val dutyDueForThisReturnHelper = mock[DutyDueForThisReturnHelper]

  "DutyDueForThisReturn Controller" - {

    "must return OK and the correct view for a GET if Yes is selected and there is alcohol to declare" in {
      when(dutyDueForThisReturnHelper.getDutyDueViewModel(any())(any(), any())).thenReturn(
        EitherT.rightT(viewModel)
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[DutyDueForThisReturnHelper].toInstance(dutyDueForThisReturnHelper))
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

    "must redirect in the Journey Recovery screen if the dutyDueForThisReturnHelper return an error" in {
      when(dutyDueForThisReturnHelper.getDutyDueViewModel(any())(any(), any())).thenReturn(
        EitherT.leftT("Error message")
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[DutyDueForThisReturnHelper].toInstance(dutyDueForThisReturnHelper))
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Check Your Answers page if the submission is successful without spirits" in {
      val adrReturnCreatedDetails = AdrReturnCreatedDetails(
        processingDate = Instant.now(),
        amount = BigDecimal(1),
        chargeReference = Some("1234567890"),
        paymentDueDate = Some(LocalDate.now())
      )

      val adrReturnSubmissionService  = mock[AdrReturnSubmissionService]
      val alcoholDutyReturnsConnector = mock[AlcoholDutyReturnsConnector]
      val auditService                = mock[AuditService]

      when(
        adrReturnSubmissionService.getDutyDeclared(any())
      ).thenReturn(
        EitherT.rightT(fullReturn.dutyDeclared)
      )

      when(
        adrReturnSubmissionService.getAdjustments(any())
      ).thenReturn(
        EitherT.rightT(fullReturn.adjustments)
      )

      when(
        adrReturnSubmissionService.getDutySuspended(any())
      ).thenReturn(
        EitherT.rightT(fullReturn.dutySuspended)
      )

      when(
        adrReturnSubmissionService.getSpirits(any())
      ).thenReturn(
        EitherT.rightT(fullReturn.spirits)
      )

      when(
        adrReturnSubmissionService.getTotals(any())(any())
      ).thenReturn(
        EitherT.rightT(fullReturn.totals)
      )

      when(adrReturnSubmissionService.getAdrReturnSubmission(any(), any())(any())).thenReturn(
        EitherT.rightT(fullReturn)
      )

      when(
        alcoholDutyReturnsConnector.submitReturn(any(), any(), any())(any())
      ).thenReturn(
        EitherT.rightT(adrReturnCreatedDetails)
      )

      val returnPeriod = nonQuarterReturnPeriodGen.sample.get
      val userAnswers  =
        fullUserAnswers.copy(returnId = ReturnId(fullUserAnswers.returnId.appaId, returnPeriod.toPeriodKey))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[DutyDueForThisReturnHelper].toInstance(dutyDueForThisReturnHelper))
        .overrides(bind[AdrReturnSubmissionService].toInstance(adrReturnSubmissionService))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(alcoholDutyReturnsConnector))
        .overrides(bind[AuditService].toInstance(auditService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.checkAndSubmit.routes.ReturnSubmittedController
          .onPageLoad()
          .url
      }
    }

    "must redirect to the Check Your Answers page if the submission is successful with spirits" in {
      val adrReturnCreatedDetails = AdrReturnCreatedDetails(
        processingDate = Instant.now(),
        amount = BigDecimal(1),
        chargeReference = Some("1234567890"),
        paymentDueDate = Some(LocalDate.now())
      )

      val adrReturnSubmissionService  = mock[AdrReturnSubmissionService]
      val alcoholDutyReturnsConnector = mock[AlcoholDutyReturnsConnector]
      val auditService                = mock[AuditService]
      val config                      = mock[FrontendAppConfig]

      when(config.spiritsAndIngredientsEnabled).thenReturn(true)

      when(adrReturnSubmissionService.getDutyDeclared(any()))
        .thenReturn(EitherT.rightT(fullReturn.dutyDeclared))

      when(adrReturnSubmissionService.getAdjustments(any()))
        .thenReturn(EitherT.rightT(fullReturn.adjustments))

      when(adrReturnSubmissionService.getDutySuspended(any()))
        .thenReturn(EitherT.rightT(fullReturn.dutySuspended))

      when(adrReturnSubmissionService.getSpirits(any()))
        .thenReturn(EitherT.rightT(fullReturn.spirits))

      when(adrReturnSubmissionService.getTotals(any())(any()))
        .thenReturn(EitherT.rightT(fullReturn.totals))

      when(adrReturnSubmissionService.getAdrReturnSubmission(any(), any())(any())).thenReturn(
        EitherT.rightT(fullReturn)
      )

      when(alcoholDutyReturnsConnector.submitReturn(any(), any(), any())(any()))
        .thenReturn(EitherT.rightT(adrReturnCreatedDetails))

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers))
        .overrides(bind[DutyDueForThisReturnHelper].toInstance(dutyDueForThisReturnHelper))
        .overrides(bind[AdrReturnSubmissionService].toInstance(adrReturnSubmissionService))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(alcoholDutyReturnsConnector))
        .overrides(bind[AuditService].toInstance(auditService))
        .overrides(bind[FrontendAppConfig].toInstance(config))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onSubmit().url)
            .withSession((periodKeySessionKey, fullUserAnswers.returnId.periodKey))

        val result = route(application, request).value

        fullUserAnswers.regimes.hasSpirits() mustEqual true
        ReturnPeriod.fromPeriodKey(fullUserAnswers.returnId.periodKey).get.hasQuarterlySpirits mustEqual true
        config.spiritsAndIngredientsEnabled mustEqual true

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.checkAndSubmit.routes.ReturnSubmittedController
          .onPageLoad()
          .url

        verify(adrReturnSubmissionService, times(1)).getSpirits(any())
      }
    }

    "must redirect to the Journey Recover page if mapping from UserAnswer to AdrReturnSubmission return an error" in {

      val adrReturnSubmissionService  = mock[AdrReturnSubmissionService]
      val alcoholDutyReturnsConnector = mock[AlcoholDutyReturnsConnector]

      when(adrReturnSubmissionService.getAdrReturnSubmission(any(), any())(any())).thenReturn(
        EitherT.leftT("Error message")
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[DutyDueForThisReturnHelper].toInstance(dutyDueForThisReturnHelper))
        .overrides(bind[AdrReturnSubmissionService].toInstance(adrReturnSubmissionService))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(alcoholDutyReturnsConnector))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Journey Recover page if the submission is not successful" in {
      val adrReturnSubmission = mock[AdrReturnSubmission]

      val adrReturnSubmissionService  = mock[AdrReturnSubmissionService]
      val alcoholDutyReturnsConnector = mock[AlcoholDutyReturnsConnector]

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
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
