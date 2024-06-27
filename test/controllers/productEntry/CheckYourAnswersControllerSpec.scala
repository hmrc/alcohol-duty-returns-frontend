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
import connectors.CacheConnector
import generators.ModelGenerators
import models.RateType.Core
import models.productEntry.ProductEntry
import models.{AlcoholByVolume, AlcoholRegime, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages.productEntry._
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers.productEntry.CheckYourAnswersSummaryListHelper
import views.html.productEntry.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with ModelGenerators {

  val name              = "Name"
  val abv               = AlcoholByVolume(3.0)
  val rateType          = Core
  val volume            = BigDecimal(1.2)
  val rate              = BigDecimal(1.2)
  val pureAlcoholVolume = BigDecimal(1)
  val taxCode           = "310"
  val duty              = BigDecimal(100)

  val currentProductEntry = ProductEntry(
    name = Some(name),
    abv = Some(abv),
    rateType = Some(rateType),
    volume = Some(volume),
    draughtRelief = Some(false),
    smallProducerRelief = Some(true),
    regime = Some(AlcoholRegime.Beer),
    sprDutyRate = Some(rate),
    pureAlcoholVolume = Some(pureAlcoholVolume),
    taxCode = Some(taxCode),
    duty = Some(duty)
  )

  val savedProductEntry = arbitraryProductEntry.arbitrary.sample.value

  val completeProductEntryUserAnswers: UserAnswers = emptyUserAnswers
    .set(CurrentProductEntryPage, currentProductEntry)
    .success
    .value
    .set(ProductEntryListPage, Seq(savedProductEntry))
    .success
    .value

  "CheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET if all necessary questions are answered" in {
      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(completeProductEntryUserAnswers))
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.productEntry.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val list = CheckYourAnswersSummaryListHelper
          .currentProductEntrySummaryList(currentProductEntry)(getMessages(application))
          .get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, getMessages(application)).toString
      }
    }

    "must return OK and load the saved product entry from the cache if index is defined" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(completeProductEntryUserAnswers))
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.productEntry.routes.CheckYourAnswersController.onPageLoad(index = Some(0)).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val list = CheckYourAnswersSummaryListHelper
          .currentProductEntrySummaryList(savedProductEntry)(getMessages(application))
          .get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, getMessages(application)).toString
      }
    }

    "must return OK and load the saved product entry from the cache if index is defined inside the current product entry" in {
      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val userAnswers =
        completeProductEntryUserAnswers
          .set(CurrentProductEntryPage, savedProductEntry.copy(index = Some(0)))
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.productEntry.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val list = CheckYourAnswersSummaryListHelper
          .currentProductEntrySummaryList(savedProductEntry)(getMessages(application))
          .get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, getMessages(application)).toString
      }
    }

    "must return OK and the correct view for a GET if any optional questions are not answered" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      Seq(
        currentProductEntry.copy(name = None),
        currentProductEntry.copy(draughtRelief = None),
        currentProductEntry.copy(smallProducerRelief = None)
      ).foreach { incompleteProductEntry =>
        val userAnswers = completeProductEntryUserAnswers
          .set(CurrentProductEntryPage, incompleteProductEntry)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.productEntry.routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersView]

          val list = CheckYourAnswersSummaryListHelper
            .currentProductEntrySummaryList(incompleteProductEntry)(getMessages(application))
            .get

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list)(request, getMessages(application)).toString
        }
      }
    }

    "must return OK and the correct view for a GET if all necessary questions are answered, the TaxType contains a rate and SPR duty relief is absent" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val productEntry = currentProductEntry.copy(
        smallProducerRelief = Some(false),
        taxRate = Some(rate),
        sprDutyRate = None
      )

      val userAnswers = completeProductEntryUserAnswers
        .set(CurrentProductEntryPage, productEntry)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.productEntry.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val list =
          CheckYourAnswersSummaryListHelper.currentProductEntrySummaryList(productEntry)(getMessages(application)).get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET" - {

      "if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, controllers.productEntry.routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "if no existing data is found for a given index" in {

        val application = applicationBuilder(userAnswers = Some(completeProductEntryUserAnswers)).build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.productEntry.routes.CheckYourAnswersController.onPageLoad(index = Some(100)).url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "if all necessary questions are answered are not answered" in {
        val incompleteUserAnswers1 =
          completeProductEntryUserAnswers
            .set(CurrentProductEntryPage, currentProductEntry.copy(abv = None))
            .success
            .value
        val incompleteUserAnswers2 =
          completeProductEntryUserAnswers
            .set(CurrentProductEntryPage, currentProductEntry.copy(taxCode = None))
            .success
            .value
        val incompleteUserAnswers3 =
          completeProductEntryUserAnswers
            .set(CurrentProductEntryPage, currentProductEntry.copy(volume = None))
            .success
            .value
        val incompleteUserAnswers4 =
          completeProductEntryUserAnswers
            .set(CurrentProductEntryPage, currentProductEntry.copy(pureAlcoholVolume = None))
            .success
            .value
        val incompleteUserAnswers5 =
          completeProductEntryUserAnswers
            .set(CurrentProductEntryPage, currentProductEntry.copy(duty = None))
            .success
            .value
        val incompleteUserAnswers6 =
          completeProductEntryUserAnswers
            .set(CurrentProductEntryPage, currentProductEntry.copy(taxRate = None, sprDutyRate = None))
            .success
            .value

        val incompleteUserAnswersList = Seq(
          incompleteUserAnswers1,
          incompleteUserAnswers2,
          incompleteUserAnswers3,
          incompleteUserAnswers4,
          incompleteUserAnswers5,
          incompleteUserAnswers6
        )

        incompleteUserAnswersList.foreach { (incompleteUserAnswers: UserAnswers) =>
          val application = applicationBuilder(Some(incompleteUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, controllers.productEntry.routes.CheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(completeProductEntryUserAnswers))
          .overrides(
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.productEntry.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.productEntry.routes.ProductListController.onPageLoad().url

        verify(mockCacheConnector, times(1)).set(any())(any())
      }
    }

    "must redirect to the next page when valid data is submitted and the product entry has an index" in {

      val userAnswers =
        completeProductEntryUserAnswers
          .set(CurrentProductEntryPage, savedProductEntry.copy(index = Some(0)))
          .success
          .value

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.productEntry.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.productEntry.routes.ProductListController.onPageLoad().url

        verify(mockCacheConnector, times(1)).set(any())(any())
      }
    }

    "must redirect to the Journey Recovery page when uncompleted data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val incompleteProductEntryUserAnswers: UserAnswers = emptyUserAnswers
        .set(CurrentProductEntryPage, currentProductEntry.copy(abv = None))
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(incompleteProductEntryUserAnswers))
          .overrides(
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.productEntry.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockCacheConnector, times(0)).set(any())(any())
      }
    }

    "must redirect to the Journey Recovery page when product entry is absent" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.productEntry.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockCacheConnector, times(0)).set(any())(any())
      }
    }
  }
}
