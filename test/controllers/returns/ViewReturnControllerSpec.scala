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

package controllers.returns

import base.SpecBase
import connectors.{AlcoholDutyCalculatorConnector, AlcoholDutyReturnsConnector}
import models.ReturnPeriod
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.TableViewModel
import viewmodels.returns.ViewReturnViewModel
import views.html.returns.ViewReturnView

import java.time.Instant
import scala.concurrent.Future

class ViewReturnControllerSpec extends SpecBase {
  "ViewReturnController" - {
    "must return a view if able to fetch the return and a spirits month" in new SetUp {
      override def periodKeyUnderTest: String = periodKeyForSpirits

      when(mockReturnsConnector.getReturn(eqTo(appaId), eqTo(periodKeyUnderTest))(any))
        .thenReturn(Future.successful(returnDetails))
      when(mockCalculatorConnector.rateBands(any())(any))
        .thenReturn(Future.successful(rateBands))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(mockReturnsConnector))
        .overrides(bind[AlcoholDutyCalculatorConnector].toInstance(mockCalculatorConnector))
        .overrides(bind[ViewReturnViewModel].toInstance(mockViewModel))
        .build()
      running(application) {
        implicit val messages = getMessages(application)

        when(mockViewModel.createTotalDueSummaryList(returnDetails)).thenReturn(totalDueSummaryList)
        when(mockViewModel.createAlcoholDeclaredViewModel(eqTo(returnDetails), any())(any()))
          .thenReturn(tableModel)
        when(mockViewModel.createAdjustmentsViewModel(eqTo(returnDetails), any())(any()))
          .thenReturn(tableModel)
        when(mockViewModel.createNetDutySuspensionViewModel(eqTo(returnDetails))(any())).thenReturn(tableModel)
        when(mockViewModel.createSpiritsViewModels(eqTo(returnDetails))(any())).thenReturn(Seq(tableModel))
        when(mockViewModel.createAlcoholDeclaredViewModel(eqTo(returnDetails), any())(any()))
          .thenReturn(tableModel)
        when(mockViewModel.createAdjustmentsViewModel(eqTo(returnDetails), any())(any()))
          .thenReturn(tableModel)

        val request =
          FakeRequest(GET, controllers.returns.routes.ViewReturnController.onPageLoad(periodKeyUnderTest).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[ViewReturnView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          returnPeriodStr,
          Some(chargeReference),
          submittedAtDateStr,
          submittedAtTimeStr,
          tableModel,
          tableModel,
          totalDueSummaryList,
          tableModel,
          Seq(tableModel)
        )(
          request,
          messages
        ).toString
      }

      verify(mockViewModel, times(1)).createSpiritsViewModels(any)(any)
      verify(mockCalculatorConnector, times(1)).rateBands(any)(any)
    }

    "must return a view if able to fetch the return and not a spirits month" in new SetUp {
      override def periodKeyUnderTest: String = periodKeyNotForSpirits

      when(mockReturnsConnector.getReturn(eqTo(appaId), eqTo(periodKeyUnderTest))(any))
        .thenReturn(Future.successful(returnDetails))
      when(mockCalculatorConnector.rateBands(any())(any))
        .thenReturn(Future.successful(rateBands))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(mockReturnsConnector))
        .overrides(bind[AlcoholDutyCalculatorConnector].toInstance(mockCalculatorConnector))
        .overrides(bind[ViewReturnViewModel].toInstance(mockViewModel))
        .build()

      running(application) {
        implicit val messages = getMessages(application)

        when(mockViewModel.createTotalDueSummaryList(returnDetails)).thenReturn(totalDueSummaryList)
        when(mockViewModel.createAlcoholDeclaredViewModel(eqTo(returnDetails), any())(any()))
          .thenReturn(tableModel)
        when(mockViewModel.createAdjustmentsViewModel(eqTo(returnDetails), any())(any()))
          .thenReturn(tableModel)
        when(mockViewModel.createNetDutySuspensionViewModel(returnDetails)).thenReturn(tableModel)
        when(mockViewModel.createSpiritsViewModels(returnDetails)).thenReturn(Seq(tableModel))
        when(mockViewModel.createAlcoholDeclaredViewModel(eqTo(returnDetails), any())(any()))
          .thenReturn(tableModel)
        when(mockViewModel.createAdjustmentsViewModel(eqTo(returnDetails), any())(any()))
          .thenReturn(tableModel)

        val request =
          FakeRequest(GET, controllers.returns.routes.ViewReturnController.onPageLoad(periodKeyUnderTest).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[ViewReturnView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          returnPeriodStr,
          Some(chargeReference),
          submittedAtDateStr,
          submittedAtTimeStr,
          tableModel,
          tableModel,
          totalDueSummaryList,
          tableModel,
          Seq.empty
        )(
          request,
          messages
        ).toString
      }

      verify(mockViewModel, never).createSpiritsViewModels(any)(any)
      verify(mockCalculatorConnector, times(1)).rateBands(any)(any)
    }

    "must not pass a charge reference to the view if a nil return" in new SetUp {
      override def periodKeyUnderTest: String = periodKeyForSpirits

      when(mockReturnsConnector.getReturn(eqTo(appaId), eqTo(periodKeyUnderTest))(any))
        .thenReturn(Future.successful(nilReturn))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(mockReturnsConnector))
        .overrides(bind[AlcoholDutyCalculatorConnector].toInstance(mockCalculatorConnector))
        .overrides(bind[ViewReturnViewModel].toInstance(mockViewModel))
        .build()
      running(application) {
        implicit val messages = getMessages(application)

        when(mockViewModel.createTotalDueSummaryList(nilReturn)).thenReturn(totalDueSummaryList)
        when(mockViewModel.createAlcoholDeclaredViewModel(eqTo(nilReturn), any())(any()))
          .thenReturn(tableModel)
        when(mockViewModel.createAdjustmentsViewModel(eqTo(nilReturn), any())(any()))
          .thenReturn(tableModel)
        when(mockViewModel.createNetDutySuspensionViewModel(eqTo(nilReturn))(any())).thenReturn(tableModel)
        when(mockViewModel.createSpiritsViewModels(eqTo(nilReturn))(any())).thenReturn(Seq(tableModel))
        when(mockViewModel.createAlcoholDeclaredViewModel(eqTo(nilReturn), any())(any()))
          .thenReturn(tableModel)
        when(mockViewModel.createAdjustmentsViewModel(eqTo(nilReturn), any())(any()))
          .thenReturn(tableModel)

        val request =
          FakeRequest(GET, controllers.returns.routes.ViewReturnController.onPageLoad(periodKeyUnderTest).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[ViewReturnView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          returnPeriodStr,
          None,
          submittedAtDateStr,
          submittedAtTimeStr,
          tableModel,
          tableModel,
          totalDueSummaryList,
          tableModel,
          Seq(tableModel)
        )(
          request,
          messages
        ).toString
      }

      verify(mockViewModel, times(1)).createSpiritsViewModels(any)(any)
    }

    "must not look up tax codes if it's a nil return and so there are none" in new SetUp {
      override def periodKeyUnderTest: String = periodKeyForSpirits

      when(mockReturnsConnector.getReturn(eqTo(appaId), eqTo(periodKeyUnderTest))(any))
        .thenReturn(Future.successful(nilReturn))
      when(mockCalculatorConnector.rateBands(any)(any))
        .thenReturn(Future.successful(rateBands))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(mockReturnsConnector))
        .overrides(bind[AlcoholDutyCalculatorConnector].toInstance(mockCalculatorConnector))
        .overrides(bind[ViewReturnViewModel].toInstance(mockViewModel))
        .build()
      running(application) {
        implicit val messages = getMessages(application)

        when(mockViewModel.createTotalDueSummaryList(nilReturn)).thenReturn(totalDueSummaryList)
        when(mockViewModel.createAlcoholDeclaredViewModel(eqTo(nilReturn), any())(any()))
          .thenReturn(tableModel)
        when(mockViewModel.createAdjustmentsViewModel(eqTo(nilReturn), any())(any()))
          .thenReturn(tableModel)
        when(mockViewModel.createNetDutySuspensionViewModel(eqTo(nilReturn))(any())).thenReturn(tableModel)
        when(mockViewModel.createSpiritsViewModels(eqTo(nilReturn))(any())).thenReturn(Seq(tableModel))
        when(mockViewModel.createAlcoholDeclaredViewModel(eqTo(nilReturn), any())(any()))
          .thenReturn(tableModel)
        when(mockViewModel.createAdjustmentsViewModel(eqTo(nilReturn), any())(any()))
          .thenReturn(tableModel)

        val request =
          FakeRequest(GET, controllers.returns.routes.ViewReturnController.onPageLoad(periodKeyUnderTest).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[ViewReturnView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          returnPeriodStr,
          None,
          submittedAtDateStr,
          submittedAtTimeStr,
          tableModel,
          tableModel,
          totalDueSummaryList,
          tableModel,
          Seq(tableModel)
        )(
          request,
          messages
        ).toString
      }

      verify(mockViewModel, times(1)).createSpiritsViewModels(any)(any)
      verify(mockCalculatorConnector, never).rateBands(any)(any)
    }

    "must redirect to the journey recovery page if unable to parse the period key" in new SetUp {
      override def periodKeyUnderTest: String = periodKeyForSpirits

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(mockReturnsConnector))
        .overrides(bind[AlcoholDutyCalculatorConnector].toInstance(mockCalculatorConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.returns.routes.ViewReturnController.onPageLoad(badPeriodKey).url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockReturnsConnector, never).getReturn(any, any)(any)
    }

    "must redirect to the journey recovery page if unable to fetch the return" in new SetUp {
      override def periodKeyUnderTest: String = periodKeyForSpirits

      when(mockReturnsConnector.getReturn(eqTo(appaId), eqTo(periodKeyUnderTest))(any))
        .thenReturn(Future.failed(new IllegalArgumentException("error")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(mockReturnsConnector))
        .build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.returns.routes.ViewReturnController.onPageLoad(periodKeyUnderTest).url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockReturnsConnector, times(1)).getReturn(any, any)(any)
      verify(mockCalculatorConnector, never).rateBands(any())(any)
    }

    "must redirect to the journey recovery page if the period key on the return doesn't match that of the request" in new SetUp {
      override def periodKeyUnderTest: String = periodKeyForSpirits

      when(mockReturnsConnector.getReturn(eqTo(appaId), eqTo(periodKeyUnderTest))(any))
        .thenReturn(
          Future.successful(
            returnDetails.copy(identification = returnDetails.identification.copy(periodKey = periodKeyNotForSpirits))
          )
        )
      when(mockCalculatorConnector.rateBands(any())(any))
        .thenReturn(Future.successful(rateBands))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(mockReturnsConnector))
        .build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.returns.routes.ViewReturnController.onPageLoad(periodKeyUnderTest).url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockReturnsConnector, times(1)).getReturn(any, any)(any)
      verify(mockCalculatorConnector, never).rateBands(any())(any)
    }
  }

  abstract class SetUp {
    implicit val messages: Messages = getMessages(app)
    val periodKeyForSpirits         = periodKeyDec23
    val periodKeyNotForSpirits      = periodKeyJan
    def periodKeyUnderTest: String

    val dateTimeHelper = createDateTimeHelper()

    val returnPeriodUnderTest = ReturnPeriod.fromPeriodKeyOrThrow(periodKeyUnderTest)
    val returnDetails         = exampleReturnDetails(periodKeyUnderTest, Instant.now(clock))
    val nilReturn             = nilReturnDetails(periodKeyUnderTest, Instant.now(clock))
    val returnPeriodStr       = dateTimeHelper.formatMonthYear(returnPeriodUnderTest.period)
    val submittedAtDateStr    = dateTimeHelper.formatDateMonthYear(
      dateTimeHelper.instantToLocalDate(returnDetails.identification.submittedTime)
    )
    val submittedAtTimeStr    = dateTimeHelper.formatHourMinuteMeridiem(
      dateTimeHelper.instantToLocalTime(returnDetails.identification.submittedTime)
    )
    val rateBands             = exampleRateBands(periodKeyUnderTest)

    val tableModel              = TableViewModel.empty()
    val totalDueSummaryList     = SummaryList(Seq(SummaryListRow()))
    val mockReturnsConnector    = mock[AlcoholDutyReturnsConnector]
    val mockCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
    val mockViewModel           = mock[ViewReturnViewModel]
  }
}
