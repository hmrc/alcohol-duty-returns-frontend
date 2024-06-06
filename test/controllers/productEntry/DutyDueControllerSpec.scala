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
import models.AlcoholByVolume
import models.productEntry.ProductEntry
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import play.api.test.Helpers._
import services.productEntry.ProductEntryService
import uk.gov.hmrc.http.HttpResponse
import views.html.productEntry.DutyDueView

import scala.concurrent.Future

class DutyDueControllerSpec extends SpecBase with ModelGenerators {

  private lazy val dutyDueRoute = controllers.productEntry.routes.DutyDueController.onPageLoad().url

  "DutyDue Controller" - {

    val dutyDue           = BigDecimal(34.2)
    val rate              = BigDecimal(9.27)
    val pureAlcoholVolume = BigDecimal(3.69)
    val taxCode           = "311"
    val volume            = BigDecimal(1)
    val abv               = AlcoholByVolume(1)

    val productEntry = ProductEntry(
      name = Some("Name"),
      abv = Some(AlcoholByVolume(1)),
      volume = Some(BigDecimal(1)),
      draughtRelief = Some(false),
      smallProducerRelief = Some(false),
      taxRate = Some(rate),
      pureAlcoholVolume = Some(pureAlcoholVolume),
      duty = Some(dutyDue),
      taxCode = Some(taxCode)
    )

    val mockCacheConnector = mock[CacheConnector]
    when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

    "must return OK and the correct view for a GET" in {

      val productEntryService = mock[ProductEntryService]
      when(productEntryService.createProduct(any())(any(), any())) thenReturn Future.successful(productEntry)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[ProductEntryService].toInstance(productEntryService),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, dutyDueRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutyDueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(abv.value, volume, dutyDue, pureAlcoholVolume, taxCode, rate)(
          request,
          messages(application)
        ).toString
      }
    }
    "must redirect to Journey Recovery if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, dutyDueRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    val incompleteProductEntries = List(
      (productEntry.copy(abv = None), "abv"),
      (productEntry.copy(volume = None), "volume"),
      (productEntry.copy(pureAlcoholVolume = None), "pureAlcoholVolume"),
      (productEntry.copy(taxRate = None, sprDutyRate = None), "rate"),
      (productEntry.copy(duty = None), "duty")
    )

    incompleteProductEntries.foreach { case (productEntry, msg) =>
      s"must redirect to Journey Recovery for a GET if product entry does not contain $msg" in {

        val productEntryService = mock[ProductEntryService]
        when(productEntryService.createProduct(any())(any(), any())) thenReturn Future.successful(productEntry)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[ProductEntryService].toInstance(productEntryService),
              bind[CacheConnector].toInstance(mockCacheConnector)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, dutyDueRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
