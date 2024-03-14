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

package controllers.productEntry

import base.SpecBase
import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import forms.productEntry.TaxTypeFormProvider
import models.productEntry.ProductEntry
import models.{AlcoholByVolume, AlcoholRegime, CheckMode, NormalMode, RateBand, RateType, UserAnswers}
import navigation.{FakeProductEntryNavigator, ProductEntryNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.productEntry.CurrentProductEntryPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.TaxTypePageViewModel
import views.html.productEntry.TaxTypeView

import scala.concurrent.Future

class TaxTypeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val taxTypeRoute       = routes.TaxTypeController.onPageLoad(NormalMode).url
  lazy val changeTaxTypeRoute = routes.TaxTypeController.onPageLoad(CheckMode).url

  val formProvider = new TaxTypeFormProvider()
  val form         = formProvider()

  val productEntry = ProductEntry(
    abv = Some(AlcoholByVolume(3.5)),
    draughtRelief = Some(true),
    smallProducerRelief = Some(false)
  )

  val fullUserAnswers = UserAnswers(userAnswersId)
    .set(CurrentProductEntryPage, productEntry)
    .success
    .value

  val taxCode       = "310"
  val alcoholRegime = AlcoholRegime.Beer
  val rate          = Some(BigDecimal(10.99))

  val rateBand     = RateBand(
    taxCode,
    "some band",
    RateType.DraughtRelief,
    Set(alcoholRegime),
    AlcoholByVolume(0.1),
    AlcoholByVolume(5.8),
    rate
  )
  val rateBandList = Seq(rateBand)

  "TaxType Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockAlcoholDutyCalculatorConnector.rates(any(), any(), any(), any())(any())) thenReturn Future.successful(
        rateBandList
      )

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers))
        .overrides(
          bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute, hasValueChanged = true)),
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
      val userAnswers                        = fullUserAnswers
        .set(
          CurrentProductEntryPage,
          ProductEntry(
            abv = Some(AlcoholByVolume(3.5)),
            draughtRelief = Some(true),
            smallProducerRelief = Some(false),
            taxCode = Some(taxCode),
            regime = Some(alcoholRegime),
            taxRate = rate
          )
        )
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute, hasValueChanged = false)),
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

        contentAsString(result) mustEqual view(form.fill(s"${taxCode}_$alcoholRegime"), NormalMode, viewModel)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockAlcoholDutyCalculatorConnector.rates(any(), any(), any(), any())(any())) thenReturn Future.successful(
        rateBandList
      )

      val mockCacheConnector = mock[CacheConnector]
      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(fullUserAnswers))
          .overrides(
            bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute, hasValueChanged = true)),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, taxTypeRoute)
            .withFormUrlEncodedBody(("value", s"${taxCode}_$alcoholRegime"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when the same data is submitted" in {
      val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockAlcoholDutyCalculatorConnector.rates(any(), any(), any(), any())(any())) thenReturn Future.successful(
        rateBandList
      )

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(
            CurrentProductEntryPage,
            ProductEntry(
              abv = Some(AlcoholByVolume(3.5)),
              draughtRelief = Some(true),
              smallProducerRelief = Some(false),
              taxCode = Some(taxCode),
              regime = Some(alcoholRegime),
              taxRate = rate
            )
          )
          .success
          .value

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute, hasValueChanged = false)),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, taxTypeRoute)
            .withFormUrlEncodedBody(("value", s"${taxCode}_$alcoholRegime"))

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
          bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute, hasValueChanged = true)),
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
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
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

          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
    "must throw an Exception" - {
      "for a GET if one of the necessary userAnswer data are missing" in {
        val errorMapping = Seq(
          (productEntry.copy(abv = None), "abv")
        )
        errorMapping.foreach { case (incompleteProductEntry, expectedMessageKey) =>
          val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
          when(mockAlcoholDutyCalculatorConnector.rates(any(), any(), any(), any())(any())) thenReturn Future
            .successful(
              rateBandList
            )

          val userAnswers = fullUserAnswers.set(CurrentProductEntryPage, incompleteProductEntry).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[ProductEntryNavigator]
                .toInstance(new FakeProductEntryNavigator(onwardRoute, hasValueChanged = true)),
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
          (fullUserAnswers.remove(CurrentProductEntryPage).success.value, "currentProductEntry"),
          (fullUserAnswers.set(CurrentProductEntryPage, productEntry.copy(abv = None)).success.value, "abv")
        )
        errorMapping.foreach { case (incompleteUserAnswers, expectedMessageKey) =>
          val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
          when(mockAlcoholDutyCalculatorConnector.rates(any(), any(), any(), any())(any())) thenReturn Future
            .successful(
              rateBandList
            )

          val application = applicationBuilder(userAnswers = Some(incompleteUserAnswers))
            .overrides(
              bind[ProductEntryNavigator]
                .toInstance(new FakeProductEntryNavigator(onwardRoute, hasValueChanged = true)),
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

      "for a POST if the tax type code cannot be parsed" in {
        val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
        when(mockAlcoholDutyCalculatorConnector.rates(any(), any(), any(), any())(any())) thenReturn Future.successful(
          rateBandList
        )

        val application = applicationBuilder(userAnswers = Some(fullUserAnswers))
          .overrides(
            bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute, hasValueChanged = true)),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, taxTypeRoute)
              .withFormUrlEncodedBody(("value", "some"))

          val result = route(application, request).value

          whenReady(result.failed) { exception =>
            exception mustBe a[RuntimeException]
            exception.getMessage mustEqual "Couldn't parse tax type code"
          }
        }
      }

      "for a POST if the tax type regime cannot be parsed" in {
        val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
        when(mockAlcoholDutyCalculatorConnector.rates(any(), any(), any(), any())(any())) thenReturn Future.successful(
          rateBandList
        )

        val application = applicationBuilder(userAnswers = Some(fullUserAnswers))
          .overrides(
            bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute, hasValueChanged = true)),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, taxTypeRoute)
              .withFormUrlEncodedBody(("value", "310_some"))

          val result = route(application, request).value

          whenReady(result.failed) { exception =>
            exception mustBe a[RuntimeException]
            exception.getMessage mustEqual "Couldn't parse alcohol regime"
          }
        }
      }

      "for a POST if the tax type regime is not approved" in {
        val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
        when(mockAlcoholDutyCalculatorConnector.rates(any(), any(), any(), any())(any())) thenReturn Future.successful(
          rateBandList
        )

        val application = applicationBuilder(userAnswers = Some(fullUserAnswers))
          .overrides(
            bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute, hasValueChanged = true)),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, taxTypeRoute)
              .withFormUrlEncodedBody(("value", "310_Whisky"))

          val result = route(application, request).value

          whenReady(result.failed) { exception =>
            exception mustBe a[RuntimeException]
            exception.getMessage mustEqual "Couldn't parse alcohol regime"
          }
        }
      }
    }
  }
}
