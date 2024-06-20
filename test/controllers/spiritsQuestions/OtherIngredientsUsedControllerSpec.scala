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

package controllers.spiritsQuestions

import base.SpecBase
import forms.spiritsQuestions.OtherIngredientsUsedFormProvider
import models.{AlcoholRegimeName, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages.spiritsQuestions.OtherIngredientsUsedPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.CacheConnector
import models.UnitsOfMeasure.Tonnes
import models.spiritsQuestions.OtherIngredientsUsed
import navigation.{FakeQuarterlySpiritsQuestionsNavigator, QuarterlySpiritsQuestionsNavigator}
import pages.AlcoholRegimePage
import uk.gov.hmrc.http.HttpResponse
import views.html.spiritsQuestions.OtherIngredientsUsedView

import scala.concurrent.Future

class OtherIngredientsUsedControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new OtherIngredientsUsedFormProvider()
  val form         = formProvider()

  lazy val otherIngredientsUsedRoute = routes.OtherIngredientsUsedController.onPageLoad(NormalMode).url

  val otherIngredientsTypes    = "Coco Pops"
  val otherIngredientsUnit     = Tonnes
  val otherIngredientsQuantity = BigDecimal(100000)

  val userAnswers = UserAnswers(
    returnId,
    groupId,
    internalId,
    Json.obj(
      OtherIngredientsUsedPage.toString -> Json.obj(
        "otherIngredientsUsedTypes"    -> otherIngredientsTypes,
        "otherIngredientsUsedUnit"     -> otherIngredientsUnit,
        "otherIngredientsUsedQuantity" -> otherIngredientsQuantity
      ),
      AlcoholRegimePage.toString        -> Json.toJson(AlcoholRegimeName.values)
    )
  )

  "OtherIngredientsUsed Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, otherIngredientsUsedRoute)

        val view = application.injector.instanceOf[OtherIngredientsUsedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, otherIngredientsUsedRoute)

        val view = application.injector.instanceOf[OtherIngredientsUsedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(OtherIngredientsUsed(otherIngredientsTypes, otherIngredientsUnit, otherIngredientsQuantity)),
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[QuarterlySpiritsQuestionsNavigator]
              .toInstance(new FakeQuarterlySpiritsQuestionsNavigator(onwardRoute, hasValueChanged = true)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, otherIngredientsUsedRoute)
            .withFormUrlEncodedBody(
              ("otherIngredientsUsedTypes", otherIngredientsTypes),
              ("otherIngredientsUsedUnit", otherIngredientsUnit.entryName),
              ("otherIngredientsUsedQuantity", otherIngredientsQuantity.toString())
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, otherIngredientsUsedRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[OtherIngredientsUsedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, otherIngredientsUsedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, otherIngredientsUsedRoute)
            .withFormUrlEncodedBody(
              ("otherIngredientsUsedTypes", otherIngredientsTypes),
              ("otherIngredientsUsedUnit", otherIngredientsUnit.entryName),
              ("otherIngredientsUsedQuantity", otherIngredientsQuantity.toString())
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
