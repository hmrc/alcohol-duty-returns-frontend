/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import base.SpecBase
import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import forms.TaxTypeFormProvider
import models.{AlcoholByVolume, AlcoholRegime, NormalMode, RateBand, RateType, UserAnswers}
import navigation.{FakeProductEntryNavigator, ProductEntryNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{AlcoholByVolumeQuestionPage, DraughtReliefQuestionPage, SmallProducerReliefQuestionPage, TaxTypePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.Settable
import uk.gov.hmrc.http.HttpResponse
import viewmodels.TaxTypePageViewModel
import views.html.TaxTypeView

import scala.concurrent.Future

class TaxTypeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val taxTypeRoute = routes.TaxTypeController.onPageLoad(NormalMode).url

  val formProvider = new TaxTypeFormProvider()
  val form         = formProvider()

  val fullUserAnswers = UserAnswers(userAnswersId)
    .set(AlcoholByVolumeQuestionPage, BigDecimal(3.5))
    .success
    .value
    .set(DraughtReliefQuestionPage, true)
    .success
    .value
    .set(SmallProducerReliefQuestionPage, false)
    .success
    .value

  val rateBandList =
    Seq(
      RateBand(
        "310",
        "some band",
        RateType.DraughtRelief,
        Set(AlcoholRegime.Beer),
        AlcoholByVolume(0.1),
        AlcoholByVolume(5.8),
        Some(BigDecimal(10.99))
      )
    )

  "TaxType Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockAlcoholDutyCalculatorConnector.rates(any(), any(), any(), any())(any())) thenReturn Future.successful(
        rateBandList
      )

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers))
        .overrides(
          bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute)),
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, taxTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaxTypeView]

        val viewModel = TaxTypePageViewModel(
          AlcoholByVolume(3.5),
          eligibleForDraughtRelief = true,
          eligibleForSmallProducerRelief = false,
          rateBandList
        )(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, viewModel)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockAlcoholDutyCalculatorConnector.rates(any(), any(), any(), any())(any())) thenReturn Future.successful(
        rateBandList
      )

      val userAnswers = fullUserAnswers.set(TaxTypePage, "some value").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute)),
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, taxTypeRoute)

        val view = application.injector.instanceOf[TaxTypeView]

        val result = route(application, request).value

        val viewModel = TaxTypePageViewModel(
          AlcoholByVolume(3.5),
          eligibleForDraughtRelief = true,
          eligibleForSmallProducerRelief = false,
          rateBandList
        )(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("some value"), NormalMode, viewModel)(
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
            bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, taxTypeRoute)
            .withFormUrlEncodedBody(("value", "some value"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockAlcoholDutyCalculatorConnector.rates(any(), any(), any(), any())(any())) thenReturn Future.successful(
        rateBandList
      )

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers))
        .overrides(
          bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute)),
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, taxTypeRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[TaxTypeView]

        val result = route(application, request).value

        val viewModel = TaxTypePageViewModel(
          AlcoholByVolume(3.5),
          eligibleForDraughtRelief = true,
          eligibleForSmallProducerRelief = false,
          rateBandList
        )(messages(application))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, viewModel)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET" - {
      "if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, taxTypeRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "redirect to Journey Recovery for a POST" - {
      "if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, taxTypeRoute)
              .withFormUrlEncodedBody(("value", "some value"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
    "must throw an Exception" - {
      "for a GET if one of the necessary userAnswer data are missing" in {
        val errorMapping = Seq(
          (AlcoholByVolumeQuestionPage, "abv"),
          (DraughtReliefQuestionPage, "eligibleForDraughtRelief"),
          (SmallProducerReliefQuestionPage, "eligibleForSmallProducerRelief")
        )
        errorMapping.foreach { case (missingKey, expectedMessageKey) =>
          val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
          when(mockAlcoholDutyCalculatorConnector.rates(any(), any(), any(), any())(any())) thenReturn Future
            .successful(
              rateBandList
            )

          val userAnswers = fullUserAnswers.remove(missingKey.asInstanceOf[Settable[_]]).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute)),
              bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
            )
            .build()

          running(application) {
            val request = FakeRequest(GET, taxTypeRoute)

            val result = route(application, request).value

            whenReady(result.failed) { exception =>
              exception mustBe a[RuntimeException]
              exception.getMessage mustEqual s"Couldn't fetch $expectedMessageKey value from cache"
            }
          }
        }
      }
      "for a POST if one of the necessary userAnswer data are missing" in {

        val errorMapping = Seq(
          (AlcoholByVolumeQuestionPage, "abv"),
          (DraughtReliefQuestionPage, "eligibleForDraughtRelief"),
          (SmallProducerReliefQuestionPage, "eligibleForSmallProducerRelief")
        )
        errorMapping.foreach { case (missingKey, expectedMessageKey) =>
          val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
          when(mockAlcoholDutyCalculatorConnector.rates(any(), any(), any(), any())(any())) thenReturn Future
            .successful(
              rateBandList
            )

          val userAnswers = fullUserAnswers.remove(missingKey.asInstanceOf[Settable[_]]).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute)),
              bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
            )
            .build()

          running(application) {
            val request =
              FakeRequest(POST, taxTypeRoute)
                .withFormUrlEncodedBody(("value", ""))

            val result = route(application, request).value

            whenReady(result.failed) { exception =>
              exception mustBe a[RuntimeException]
              exception.getMessage mustEqual s"Couldn't fetch $expectedMessageKey value from cache"
            }
          }
        }
      }
    }
  }
}
