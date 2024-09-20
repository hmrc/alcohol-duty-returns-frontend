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
import forms.adjustment.DeleteAdjustmentFormProvider
import navigation.{AdjustmentNavigator, FakeAdjustmentNavigator}
import org.mockito.ArgumentMatchers.any
import pages.adjustment.{DeleteAdjustmentPage, OverDeclarationTotalPage, UnderDeclarationTotalPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.CacheConnector
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers.adjustment.AdjustmentOverUnderDeclarationCalculationHelper
import views.html.adjustment.DeleteAdjustmentView

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

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteAdjustmentRoute)
            .withFormUrlEncodedBody(("delete-adjustment-yes-no-value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.adjustment.routes.AdjustmentListController
          .onPageLoad(pageNumber)
          .url

        verify(mockCacheConnector, times(1)).set(any())(any())
      }
    }

    "must redirect to the next page when No is selected on remove radio button" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = false))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteAdjustmentRoute)
            .withFormUrlEncodedBody(("delete-adjustment-yes-no-value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.adjustment.routes.AdjustmentListController
          .onPageLoad(pageNumber)
          .url

      }
    }

    "must not clear if total UnderDeclaration is above threshold" in {
      val mockCacheConnector = mock[CacheConnector]
      val mockHelper         = mock[AdjustmentOverUnderDeclarationCalculationHelper]
      when(mockHelper.fetchOverUnderDeclarationTotals(any(), any())(any())) thenReturn Future.successful(
        emptyUserAnswers
          .set(UnderDeclarationTotalPage, BigDecimal(1500))
          .success
          .value
      )
      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      val application        = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector),
          bind[AdjustmentOverUnderDeclarationCalculationHelper].toInstance(mockHelper)
        )
        .build()
      running(application) {
        val request = FakeRequest(POST, deleteAdjustmentRoute)
          .withFormUrlEncodedBody(("delete-adjustment-yes-no-value", "true"))
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.adjustment.routes.AdjustmentListController
          .onPageLoad(pageNumber)
          .url

        verify(mockCacheConnector, times(1)).set(any())(any())
      }
    }

    "must clear if total UnderDeclaration is not defined or is below threshold" in {
      val mockCacheConnector = mock[CacheConnector]
      val mockHelper         = mock[AdjustmentOverUnderDeclarationCalculationHelper]
      when(mockHelper.fetchOverUnderDeclarationTotals(any(), any())(any())) thenReturn Future.successful(
        emptyUserAnswers
          .set(OverDeclarationTotalPage, BigDecimal(1500))
          .success
          .value
      )
      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      val application        = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector),
          bind[AdjustmentOverUnderDeclarationCalculationHelper].toInstance(mockHelper)
        )
        .build()
      running(application) {
        val request = FakeRequest(POST, deleteAdjustmentRoute)
          .withFormUrlEncodedBody(("delete-adjustment-yes-no-value", "true"))
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.adjustment.routes.AdjustmentListController
          .onPageLoad(pageNumber)
          .url

        verify(mockCacheConnector, times(1)).set(any())(any())
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
