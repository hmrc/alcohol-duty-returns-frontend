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
import forms.returns.WhatDoYouNeedToDeclareFormProvider
import models.{AlcoholByVolume, NormalMode, RateBand, RateType}
import models.returns.WhatDoYouNeedToDeclare
import navigation.{FakeReturnsNavigator, ReturnsNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.returns.WhatDoYouNeedToDeclarePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import models.AlcoholRegime.Beer
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers.returns.TaxBandsViewModel
import views.html.returns.WhatDoYouNeedToDeclareView

import scala.concurrent.Future

class WhatDoYouNeedToDeclareControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val whatDoYouNeedToDeclareRoute = routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, Beer).url

  val formProvider = new WhatDoYouNeedToDeclareFormProvider()
  val form         = formProvider()
  val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]

  private val rateBandList = Seq(
    RateBand(
      "311",
      "some band",
      RateType.Core,
      Set(Beer),
      AlcoholByVolume(1.2),
      AlcoholByVolume(3.4),
      Some(BigDecimal(10))
    ),
    RateBand(
      "321",
      "some band",
      RateType.Core,
      Set(Beer),
      AlcoholByVolume(3.5),
      AlcoholByVolume(8.4),
      Some(BigDecimal(10))
    ))

  when(mockAlcoholDutyCalculatorConnector.rateBandByRegime(any(), any())) thenReturn Future.successful(
    rateBandList
  )

  "WhatDoYouNeedToDeclare Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector))
        .build()


      running(application) {
        val request = FakeRequest(GET, whatDoYouNeedToDeclareRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatDoYouNeedToDeclareView]

        status(result) mustEqual OK
        val taxBandsViewModel = TaxBandsViewModel(rateBandList)(messages(application))
        contentAsString(result) mustEqual view(form, Beer, taxBandsViewModel, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        emptyUserAnswers.set(WhatDoYouNeedToDeclarePage, WhatDoYouNeedToDeclare.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whatDoYouNeedToDeclareRoute)

        val view = application.injector.instanceOf[WhatDoYouNeedToDeclareView]

        val result = route(application, request).value

        status(result) mustEqual OK
        val taxBandsViewModel = TaxBandsViewModel(rateBandList)(messages(application))
        contentAsString(result) mustEqual view(form.fill(WhatDoYouNeedToDeclare.values.toSet), Beer, taxBandsViewModel, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(new FakeReturnsNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whatDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody(("value[0]", WhatDoYouNeedToDeclare.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, whatDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[WhatDoYouNeedToDeclareView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        val taxBandsViewModel = TaxBandsViewModel(rateBandList)(messages(application))
        contentAsString(result) mustEqual view(boundForm, Beer, taxBandsViewModel, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, whatDoYouNeedToDeclareRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, whatDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody(("value[0]", WhatDoYouNeedToDeclare.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
