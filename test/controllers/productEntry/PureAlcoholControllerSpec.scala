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
import models.productEntry.ProductEntry
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.productEntry.ProductEntryService
import uk.gov.hmrc.http.HttpResponse
import views.html.productEntry.PureAlcoholView

import scala.concurrent.Future

class PureAlcoholControllerSpec extends SpecBase with MockitoSugar {

  "PureAlcohol Controller" - {

    "must return OK and the correct view for a GET" in {

      val abv               = BigDecimal(1)
      val volume            = BigDecimal(1)
      val pureAlcoholVolume = BigDecimal(1)

      val productEntry = ProductEntry(
        abv = abv,
        volume = volume,
        draughtRelief = false,
        smallProduceRelief = false,
        rate = BigDecimal(1),
        pureAlcoholVolume = pureAlcoholVolume,
        duty = BigDecimal(1),
        taxCode = "311"
      )

      val mockCacheConnector = mock[CacheConnector]
      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val productEntryService = mock[ProductEntryService]
      when(productEntryService.createProduct(any())(any(), any())) thenReturn Future.successful(
        productEntry
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[ProductEntryService].toInstance(productEntryService),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {

        val request = FakeRequest(GET, controllers.productEntry.routes.PureAlcoholController.onPageLoad().url)

        val view = application.injector.instanceOf[PureAlcoholView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(abv, volume, pureAlcoholVolume)(request, messages(application)).toString
      }
    }
  }
}
