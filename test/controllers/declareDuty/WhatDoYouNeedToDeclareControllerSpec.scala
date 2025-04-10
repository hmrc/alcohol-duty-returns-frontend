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

package controllers.declareDuty

import base.SpecBase
import connectors.{AlcoholDutyCalculatorConnector, UserAnswersConnector}
import forms.declareDuty.WhatDoYouNeedToDeclareFormProvider
import models.{CheckMode, NormalMode}
import navigation.ReturnsNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.declareDuty.{AlcoholTypePage, WhatDoYouNeedToDeclarePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.declareDuty.TaxBandsViewModel
import views.html.declareDuty.WhatDoYouNeedToDeclareView

import scala.concurrent.Future

class WhatDoYouNeedToDeclareControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val regime                                 = regimeGen.sample.value
  lazy val whatDoYouNeedToDeclareRoute       = routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url
  lazy val whatDoYouNeedToDeclareChangeRoute = routes.WhatDoYouNeedToDeclareController.onPageLoad(CheckMode, regime).url

  val formProvider = new WhatDoYouNeedToDeclareFormProvider()
  val form         = formProvider(regime)

  val rateBandList = genListOfRateBandForRegime(regime).sample.value

  val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
  val mockUserAnswersConnector           = mock[UserAnswersConnector]
  val mockReturnsNavigator               = mock[ReturnsNavigator]

  val userAnswersWithAlcoholType = emptyUserAnswers.set(AlcoholTypePage, emptyUserAnswers.regimes.regimes).success.value

  override def beforeEach(): Unit = {
    reset(mockAlcoholDutyCalculatorConnector)
    reset(mockUserAnswersConnector)
    reset(mockReturnsNavigator)

    when(mockAlcoholDutyCalculatorConnector.rateBandByRegime(any(), any())(any())) thenReturn Future.successful(
      rateBandList
    )
    when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
    when(
      mockReturnsNavigator.nextPageWithRegime(eqTo(WhatDoYouNeedToDeclarePage), any(), any(), any(), any(), any())
    ) thenReturn onwardRoute
  }

  "WhatDoYouNeedToDeclare Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithAlcoholType))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, whatDoYouNeedToDeclareRoute)

        val result = route(application, request).value

        val taxBandsViewModel = TaxBandsViewModel(rateBandList, regime)(getMessages(application))

        val view = application.injector.instanceOf[WhatDoYouNeedToDeclareView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, regime, taxBandsViewModel, NormalMode)(
          request,
          getMessages(application)
        ).toString

        verify(mockAlcoholDutyCalculatorConnector, times(1))
          .rateBandByRegime(any(), eqTo(userAnswersWithAlcoholType.regimes.regimes.toSeq))(any())
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        userAnswersWithAlcoholType.setByKey(WhatDoYouNeedToDeclarePage, regime, rateBandList.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, whatDoYouNeedToDeclareRoute)

        val view = application.injector.instanceOf[WhatDoYouNeedToDeclareView]

        val taxBandsViewModel = TaxBandsViewModel(rateBandList, regime)(getMessages(application))

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(rateBandList.map(_.taxTypeCode).toSet),
          regime,
          taxBandsViewModel,
          NormalMode
        )(
          request,
          getMessages(application)
        ).toString

        verify(mockAlcoholDutyCalculatorConnector, times(1))
          .rateBandByRegime(any(), eqTo(userAnswers.regimes.regimes.toSeq))(any())
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithAlcoholType))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {

        val selectedRateBand = rateBandList.head

        val request =
          FakeRequest(POST, whatDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody(("rateBand[0]", selectedRateBand.taxTypeCode))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockAlcoholDutyCalculatorConnector, times(1))
          .rateBandByRegime(any(), eqTo(userAnswersWithAlcoholType.regimes.regimes.toSeq))(any())
        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockReturnsNavigator, times(1))
          .nextPageWithRegime(eqTo(WhatDoYouNeedToDeclarePage), eqTo(NormalMode), any(), any(), eqTo(false), eqTo(None))
      }
    }

    "must redirect to the next page when valid data is submitted, in Check mode and the value has changed" in {

      val userAnswers =
        userAnswersWithAlcoholType.setByKey(WhatDoYouNeedToDeclarePage, regime, rateBandList.toSet).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {

        val selectedRateBand = rateBandList.head

        val request =
          FakeRequest(POST, whatDoYouNeedToDeclareChangeRoute)
            .withFormUrlEncodedBody(("rateBand[0]", selectedRateBand.taxTypeCode))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockAlcoholDutyCalculatorConnector, times(1))
          .rateBandByRegime(any(), eqTo(userAnswers.regimes.regimes.toSeq))(any())
        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockReturnsNavigator, times(1))
          .nextPageWithRegime(eqTo(WhatDoYouNeedToDeclarePage), eqTo(CheckMode), any(), any(), eqTo(true), eqTo(None))
      }
    }

    "must redirect to the next page when valid data is submitted, in Check mode and the value has not changed" in {

      val selectedRateBand = rateBandList.head

      val userAnswers =
        userAnswersWithAlcoholType.setByKey(WhatDoYouNeedToDeclarePage, regime, Set(selectedRateBand)).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, whatDoYouNeedToDeclareChangeRoute)
            .withFormUrlEncodedBody(("rateBand[0]", selectedRateBand.taxTypeCode))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockAlcoholDutyCalculatorConnector, times(1))
          .rateBandByRegime(any(), eqTo(userAnswers.regimes.regimes.toSeq))(any())
        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockReturnsNavigator, times(1))
          .nextPageWithRegime(eqTo(WhatDoYouNeedToDeclarePage), eqTo(CheckMode), any(), any(), eqTo(false), eqTo(None))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithAlcoholType))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, whatDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody((s"value[0]", "invalid value"))

        val boundForm = form.bind(Map(s"value[0]" -> "invalid value"))

        val view = application.injector.instanceOf[WhatDoYouNeedToDeclareView]

        val taxBandsViewModel = TaxBandsViewModel(rateBandList, regime)(getMessages(application))

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, regime, taxBandsViewModel, NormalMode)(
          request,
          getMessages(application)
        ).toString

        verify(mockAlcoholDutyCalculatorConnector, times(1))
          .rateBandByRegime(any(), eqTo(userAnswersWithAlcoholType.regimes.regimes.toSeq))(any())
      }
    }

    "must redirect to JourneyRecoveryController if an invalid tax type is submitted" in {

      val invalidTaxType = "invalidValue"

      val application = applicationBuilder(userAnswers = Some(userAnswersWithAlcoholType))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, whatDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody((s"rateBand[0]", invalidTaxType))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockAlcoholDutyCalculatorConnector, times(1))
          .rateBandByRegime(any(), eqTo(userAnswersWithAlcoholType.regimes.regimes.toSeq))(any())
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, whatDoYouNeedToDeclareRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val selectedRateBand = rateBandList.head
        val request          =
          FakeRequest(POST, whatDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody((s"value[${selectedRateBand.taxTypeCode}]", selectedRateBand.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
