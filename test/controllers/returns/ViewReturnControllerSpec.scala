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
import connectors.AlcoholDutyReturnsConnector
import controllers.returns
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.HeadCell
import viewmodels.returns.ViewReturnViewModel
import viewmodels.{TableTotalViewModel, TableViewModel}
import views.html.returns.ViewReturnView

import java.time.Instant
import scala.concurrent.Future

class ViewReturnControllerSpec extends SpecBase {
  "ViewReturnController" - {
    "should return a view if able to fetch the return" in new SetUp {
      when(mockReturnsConnector.getReturn(eqTo(appaId), eqTo(periodKey))(any))
        .thenReturn(Future.successful(returnDetails))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(mockReturnsConnector))
        .overrides(bind[ViewReturnViewModel].toInstance(mockViewModel))
        .build()
      running(application) {
        implicit val messages = getMessages(application)

        when(mockViewModel.createAlcoholDeclaredViewModel(returnDetails)).thenReturn(tableModel)
        when(mockViewModel.createAdjustmentsViewModel(returnDetails)).thenReturn(tableModel)
        when(mockViewModel.createTotalDueViewModel(returnDetails)).thenReturn(totalTableModel)

        val request = FakeRequest(GET, returns.routes.ViewReturnController.onPageLoad(periodKey).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[ViewReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          returnPeriodStr,
          submittedAtDateStr,
          submittedAtTimeStr,
          tableModel,
          tableModel,
          totalTableModel
        )(
          request,
          messages
        ).toString
      }
    }

    "should redirect to the journey recovery page if unable to fetch the return" in new SetUp {
      when(mockReturnsConnector.getReturn(eqTo(appaId), eqTo(periodKey))(any))
        .thenReturn(Future.failed(new IllegalArgumentException("error")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(mockReturnsConnector))
        .build()
      running(application) {
        val request = FakeRequest(GET, returns.routes.ViewReturnController.onPageLoad(periodKey).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "should redirect to the journey recovery page if unable to parse the returned period key" in new SetUp {
      when(mockReturnsConnector.getReturn(eqTo(appaId), eqTo(periodKey))(any))
        .thenReturn(Future.successful(returnDetailsWithBadPeriodKey))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(mockReturnsConnector))
        .build()
      running(application) {
        val request = FakeRequest(GET, returns.routes.ViewReturnController.onPageLoad(periodKey).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  class SetUp {
    val periodKey                     = periodKeyMar
    val returnDetails                 = exampleReturnDetails(periodKey, Instant.now(clock))
    val returnDetailsWithBadPeriodKey = exampleReturnDetails(badPeriodKey, Instant.now(clock))
    val returnPeriodStr               = dateTimeHelper.formatMonthYear(returnPeriodMar.period)
    val submittedAtDateStr            = dateTimeHelper.formatDateMonthYear(
      dateTimeHelper.instantToLocalDate(returnDetails.identification.submittedTime)
    )
    val submittedAtTimeStr            = dateTimeHelper.formatHourMinuteMerediem(
      dateTimeHelper.instantToLocalTime(returnDetails.identification.submittedTime)
    )

    val tableModel           = TableViewModel.empty()
    val totalTableModel      = TableTotalViewModel(HeadCell(), HeadCell())
    val mockReturnsConnector = mock[AlcoholDutyReturnsConnector]
    val mockViewModel        = mock[ViewReturnViewModel]
  }
}
