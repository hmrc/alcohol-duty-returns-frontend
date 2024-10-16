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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import connectors.CacheConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class BusinessTaxAccountRedirectSpec extends SpecBase {

  "businessTaxAccount" - {

    "if the period key is not available must redirect to the business tax account page" in {

      val mockCacheConnector = mock[CacheConnector]

      val application =
        applicationBuilder(None)
          .overrides(bind[CacheConnector].toInstance(mockCacheConnector))
          .build()

      running(application) {

        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequestWithoutSession(GET, controllers.routes.BusinessTaxAccountRedirect.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual appConfig.businessTaxAccountUrl
        verify(mockCacheConnector, times(0)).releaseLock(eqTo(returnId))(any())
      }
    }

    "if the period key is retrieved, it must release the lock, and redirect to the business tax account page" in {

      val mockCacheConnector = mock[CacheConnector]
      when(mockCacheConnector.releaseLock(eqTo(returnId))(any())).thenReturn(Future.successful(HttpResponse(OK)))

      val application =
        applicationBuilder(None)
          .overrides(bind[CacheConnector].toInstance(mockCacheConnector))
          .build()

      running(application) {

        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, controllers.routes.BusinessTaxAccountRedirect.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual appConfig.businessTaxAccountUrl
        verify(mockCacheConnector, times(1)).releaseLock(eqTo(returnId))(any())
      }
    }
  }
}
