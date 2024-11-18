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
import forms.adjustment.DeleteAdjustmentFormProvider
import navigation.{AdjustmentNavigator, FakeAdjustmentNavigator}
import org.mockito.ArgumentMatchers.any
import pages.adjustment.{AdjustmentEntryListPage, DeleteAdjustmentPage, OverDeclarationTotalPage, UnderDeclarationTotalPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.UserAnswersConnector
import models.AlcoholRegime.Beer
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.{Overdeclaration, Spoilt}
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, NormalMode, RangeDetailsByRegime, RateBand, RateType}
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers.adjustment.AdjustmentOverUnderDeclarationCalculationHelper
import views.html.adjustment.DeleteAdjustmentView

import java.time.YearMonth
import scala.concurrent.Future

class DeleteAdjustmentControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider               = new DeleteAdjustmentFormProvider()
  val form                       = formProvider()
  val index                      = 0
  val pageNumber                 = 1
  lazy val deleteAdjustmentRoute = controllers.adjustment.routes.DeleteAdjustmentController.onPageLoad(index).url

  "DeleteAdjustment Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, deleteAdjustmentRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeleteAdjustmentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, index)(request, getMessages(application)).toString
      }
    }
    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(DeleteAdjustmentPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, deleteAdjustmentRoute)

        val view = application.injector.instanceOf[DeleteAdjustmentView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), index)(request, getMessages(application)).toString
      }
    }
    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true)),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteAdjustmentRoute)
            .withFormUrlEncodedBody(("delete-adjustment-yes-no-value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
      }
    }

    "must redirect to the next page when No is selected on remove radio button" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteAdjustmentRoute)
            .withFormUrlEncodedBody(("delete-adjustment-yes-no-value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

      }
    }

    "must not clear if total UnderDeclaration is above threshold" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockHelper               = mock[AdjustmentOverUnderDeclarationCalculationHelper]
      when(mockHelper.fetchOverUnderDeclarationTotals(any(), any())(any())) thenReturn Future.successful(
        emptyUserAnswers
          .set(UnderDeclarationTotalPage, BigDecimal(1500))
          .success
          .value
      )
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      val application              = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
          bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true)),
          bind[AdjustmentOverUnderDeclarationCalculationHelper].toInstance(mockHelper)
        )
        .build()
      running(application) {
        val request = FakeRequest(POST, deleteAdjustmentRoute)
          .withFormUrlEncodedBody(("delete-adjustment-yes-no-value", "true"))
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
      }
    }

    "must clear if total UnderDeclaration is not defined or is below threshold" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockHelper               = mock[AdjustmentOverUnderDeclarationCalculationHelper]
      when(mockHelper.fetchOverUnderDeclarationTotals(any(), any())(any())) thenReturn Future.successful(
        emptyUserAnswers
          .set(OverDeclarationTotalPage, BigDecimal(1500))
          .success
          .value
      )
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      val application              = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
          bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true)),
          bind[AdjustmentOverUnderDeclarationCalculationHelper].toInstance(mockHelper)
        )
        .build()
      running(application) {
        val request = FakeRequest(POST, deleteAdjustmentRoute)
          .withFormUrlEncodedBody(("delete-adjustment-yes-no-value", "true"))
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
      }
    }

    "must clear the declaration question if the user answered 'yes' previously and the list is empty" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockHelper               = mock[AdjustmentOverUnderDeclarationCalculationHelper]
      when(mockHelper.fetchOverUnderDeclarationTotals(any(), any())(any())) thenReturn Future.successful(
        emptyUserAnswers
          .set(OverDeclarationTotalPage, BigDecimal(1500))
          .success
          .value
      )
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      val application              = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
          bind[AdjustmentOverUnderDeclarationCalculationHelper].toInstance(mockHelper)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, deleteAdjustmentRoute)
          .withFormUrlEncodedBody(("delete-adjustment-yes-no-value", "true"))
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.adjustment.routes.DeclareAdjustmentQuestionController
          .onPageLoad(NormalMode)
          .url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
      }
    }

    "must not clear the declaration question if the user answered 'yes' previously and there are items in the list" in {
      val rateBand            = RateBand(
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
      val adjustmentEntry     = AdjustmentEntry(
        pureAlcoholVolume = Some(BigDecimal(12.12)),
        totalLitresVolume = Some(BigDecimal(12.12)),
        rateBand = Some(rateBand),
        spoiltRegime = Some(Beer),
        duty = Some(BigDecimal(12.12)),
        adjustmentType = Some(Spoilt),
        period = Some(YearMonth.of(24, 1))
      )
      val adjustmentEntryList = List(adjustmentEntry, adjustmentEntry.copy(adjustmentType = Some(Overdeclaration)))
      val userAnswers         = emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryList).success.value

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockHelper               = mock[AdjustmentOverUnderDeclarationCalculationHelper]
      when(mockHelper.fetchOverUnderDeclarationTotals(any(), any())(any())) thenReturn Future.successful(
        userAnswers
          .set(OverDeclarationTotalPage, BigDecimal(1500))
          .success
          .value
      )

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
          bind[AdjustmentOverUnderDeclarationCalculationHelper].toInstance(mockHelper)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, deleteAdjustmentRoute)
          .withFormUrlEncodedBody(("delete-adjustment-yes-no-value", "true"))
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.adjustment.routes.AdjustmentListController
          .onPageLoad(1)
          .url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteAdjustmentRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeleteAdjustmentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, index)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, deleteAdjustmentRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteAdjustmentRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
