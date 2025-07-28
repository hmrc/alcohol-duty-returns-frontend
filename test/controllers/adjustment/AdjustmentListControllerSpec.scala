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

package controllers.adjustment

import base.SpecBase
import cats.data.NonEmptySeq
import config.Constants.rowsPerPage
import connectors.{AlcoholDutyCalculatorConnector, UserAnswersConnector}
import forms.adjustment.AdjustmentListFormProvider
import models.AlcoholRegime.Beer
import models.adjustment.AdjustmentType.Spoilt
import models.adjustment.{AdjustmentDuty, AdjustmentEntry}
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, NormalMode, RangeDetailsByRegime, RateBand, RateType, UserAnswers}
import navigation.AdjustmentNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.adjustment.{AdjustmentEntryListPage, AdjustmentListPage, AdjustmentTotalPage, CurrentAdjustmentEntryPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.{PaginationViewModel, TableViewModel}
import viewmodels.checkAnswers.adjustment.AdjustmentListSummaryHelper
import views.html.adjustment.AdjustmentListView

import java.time.YearMonth
import scala.concurrent.Future

class AdjustmentListControllerSpec extends SpecBase {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider                              = new AdjustmentListFormProvider()
  val form: Form[Boolean]                       = formProvider()
  val pageNumber                                = 1
  lazy val adjustmentListRoute: String          =
    controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
  val dutyDue: BigDecimal                       = BigDecimal(34.2)
  val rate: BigDecimal                          = BigDecimal(9.27)
  val pureAlcoholVolume: BigDecimal             = BigDecimal(3.69)
  val taxCode                                   = "311"
  val volume: BigDecimal                        = BigDecimal(10)
  val repackagedRate: BigDecimal                = BigDecimal(10)
  val repackagedDuty: BigDecimal                = BigDecimal(33.2)
  val newDuty: BigDecimal                       = BigDecimal(1)
  val rateBand: RateBand                        = RateBand(
    "310",
    "some band",
    RateType.DraughtRelief,
    Some(BigDecimal(10.99)),
    Set(
      RangeDetailsByRegime(
        AlcoholRegime.Beer,
        NonEmptySeq.one(
          ABVRange(
            AlcoholType.Beer,
            AlcoholByVolume(0.1),
            AlcoholByVolume(5.8)
          )
        )
      )
    )
  )
  val adjustmentEntry: AdjustmentEntry          = AdjustmentEntry(
    pureAlcoholVolume = Some(pureAlcoholVolume),
    totalLitresVolume = Some(volume),
    rateBand = Some(rateBand),
    spoiltRegime = Some(Beer),
    duty = Some(dutyDue),
    adjustmentType = Some(Spoilt),
    period = Some(YearMonth.of(24, 1))
  )
  val adjustmentEntryList: Seq[AdjustmentEntry] = List(adjustmentEntry)

  val userAnswers: UserAnswers = emptyUserAnswers
    .set(AdjustmentEntryListPage, adjustmentEntryList)
    .success
    .value

  val userAnswersWithCurrent: UserAnswers = userAnswers
    .set(CurrentAdjustmentEntryPage, adjustmentEntry)
    .success
    .value

  val cleanedUserAnswers: UserAnswers = userAnswers
    .set(AdjustmentListPage, true)
    .success
    .value

  val total: BigDecimal = BigDecimal(0)

  "AdjustmentList Controller" - {

    "must return OK and the correct view for a GET" in {
      val mockUserAnswersConnector           = mock[UserAnswersConnector]
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockAlcoholDutyCalculatorConnector.calculateTotalAdjustment(any())(any())) thenReturn Future.successful(
        AdjustmentDuty(total)
      )
      val application                        = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, adjustmentListRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdjustmentListView]

        val adjustmentTable: TableViewModel =
          AdjustmentListSummaryHelper.adjustmentEntryTable(userAnswers, total, pageNumber)(getMessages(app))

        val totalPages = Math.ceil(userAnswers.get(AdjustmentEntryListPage).size.toDouble / rowsPerPage).toInt

        val paginationViewModel: PaginationViewModel = PaginationViewModel(
          pageNumber,
          totalPages,
          controllers.adjustment.routes.AdjustmentListController.onPageLoad(_).url
        )

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, adjustmentTable, paginationViewModel)(
          request,
          getMessages(app)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val mockUserAnswersConnector           = mock[UserAnswersConnector]
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      val updatedUserAnswers                 =
        userAnswers.set(AdjustmentListPage, true).success.value.set(AdjustmentTotalPage, total).success.value
      val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockAlcoholDutyCalculatorConnector.calculateTotalAdjustment(any())(any())) thenReturn Future.successful(
        AdjustmentDuty(total)
      )
      val application                        = applicationBuilder(userAnswers = Some(updatedUserAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, adjustmentListRoute)

        val view = application.injector.instanceOf[AdjustmentListView]

        val result = route(application, request).value

        val adjustmentTable: TableViewModel =
          AdjustmentListSummaryHelper.adjustmentEntryTable(updatedUserAnswers, total, pageNumber)(getMessages(app))

        val totalPages = Math.ceil(userAnswers.get(AdjustmentEntryListPage).size.toDouble / rowsPerPage).toInt

        val paginationViewModel: PaginationViewModel = PaginationViewModel(
          pageNumber,
          totalPages,
          controllers.adjustment.routes.AdjustmentListController.onPageLoad(_).url
        )

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), adjustmentTable, paginationViewModel)(
          request,
          getMessages(app)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockAdjustmentNavigator  = mock[AdjustmentNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockAdjustmentNavigator.nextPage(eqTo(AdjustmentListPage), any(), any(), any())) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentListRoute)
            .withFormUrlEncodedBody(("adjustment-list-yes-no-value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(AdjustmentListPage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must clear current adjustment entry when valid data is submitted" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockAdjustmentNavigator  = mock[AdjustmentNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockAdjustmentNavigator.nextPage(eqTo(AdjustmentListPage), any(), any(), any())) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCurrent))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentListRoute)
            .withFormUrlEncodedBody(("adjustment-list-yes-no-value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(cleanedUserAnswers))(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(AdjustmentListPage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentListRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AdjustmentListView]

        val result = route(application, request).value

        val adjustmentTable: TableViewModel =
          AdjustmentListSummaryHelper.adjustmentEntryTable(userAnswers, total, pageNumber)(getMessages(app))

        val totalPages = Math.ceil(userAnswers.get(AdjustmentEntryListPage).size.toDouble / rowsPerPage).toInt

        val paginationViewModel: PaginationViewModel = PaginationViewModel(
          pageNumber,
          totalPages,
          controllers.adjustment.routes.AdjustmentListController.onPageLoad(_).url
        )

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, adjustmentTable, paginationViewModel)(
          request,
          getMessages(app)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentListRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentListRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to first page of AdjustmentList for a GET if a pageNumber is out of bounds" in {
      val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockAlcoholDutyCalculatorConnector.calculateTotalAdjustment(any())(any())) thenReturn Future.successful(
        AdjustmentDuty(total)
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
        )
        .build()

      running(application) {
        val outOfBoundRoute = controllers.adjustment.routes.AdjustmentListController.onPageLoad(999).url

        val request = FakeRequest(GET, outOfBoundRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.adjustment.routes.AdjustmentListController
          .onPageLoad(1)
          .url

        verify(mockAlcoholDutyCalculatorConnector, times(1)).calculateTotalAdjustment(any())(any())
      }
    }

    "must redirect to Journey Recovery if calculator call fails" in {
      val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]

      when(mockAlcoholDutyCalculatorConnector.calculateTotalAdjustment(any())(any())) thenReturn Future.failed(
        new Exception("Calculation failed")
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, adjustmentListRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockAlcoholDutyCalculatorConnector, times(1)).calculateTotalAdjustment(any())(any())
      }
    }
  }
}
