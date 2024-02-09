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

package controllers.productEntry

import base.SpecBase
import forms.productEntry.ProductListFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeProductEntryNavigator, ProductEntryNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.productEntry.ProductListPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import connectors.CacheConnector
import uk.gov.hmrc.http.HttpResponse
import viewmodels.TableViewModel
import views.html.productEntry.ProductListView

import scala.concurrent.Future

class ProductListControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ProductListFormProvider()
  val form         = formProvider()

  lazy val productListRoute = controllers.productEntry.routes.ProductListController.onPageLoad().url

  "ProductList Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, productListRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ProductListView]

        val productTable: TableViewModel =
          TableViewModel(
            head = Seq.empty,
            rows = Seq.empty,
            total = BigDecimal(0)
          )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, productTable)(request, messages(application)).toString
      }
    }

//    "must populate the view correctly on a GET when the question has previously been answered" in {
//
//      val userAnswers = UserAnswers(userAnswersId).set(ProductListPage, true).success.value
//
//      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, productListRoute)
//
//        val view = application.injector.instanceOf[ProductListView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(form.fill(true))(request, messages(application)).toString
//      }
//    }
//
//    "must redirect to the next page when valid data is submitted" in {
//
//      val mockCacheConnector = mock[CacheConnector]
//
//      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
//
//      val application =
//        applicationBuilder(userAnswers = Some(emptyUserAnswers))
//          .overrides(
//            bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute)),
//            bind[CacheConnector].toInstance(mockCacheConnector)
//          )
//          .build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, productListRoute)
//            .withFormUrlEncodedBody(("productList-yesNoValue", "true"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual onwardRoute.url
//      }
//    }
//
//    "must return a Bad Request and errors when invalid data is submitted" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, productListRoute)
//            .withFormUrlEncodedBody(("value", ""))
//
//        val boundForm = form.bind(Map("value" -> ""))
//
//        val view = application.injector.instanceOf[ProductListView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual BAD_REQUEST
//        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
//      }
//    }
//
//    "must redirect to Journey Recovery for a GET if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request = FakeRequest(GET, productListRoute)
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
//      }
//    }
//
//    "must redirect to Journey Recovery for a POST if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, productListRoute)
//            .withFormUrlEncodedBody(("value", "true"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
//      }
//    }
  }
}
