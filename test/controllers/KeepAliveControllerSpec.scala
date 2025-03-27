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
import connectors.UserAnswersConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class KeepAliveControllerSpec extends SpecBase {

  "keepAlive" - {

    "when the user has answered some questions" - {

      "must keep the answers alive and return OK" in {

        val mockUserAnswersConnector = mock[UserAnswersConnector]
        when(mockUserAnswersConnector.keepAlive(eqTo(returnId))(any()))
          .thenReturn(Future.successful(mock[HttpResponse]))

        val application =
          applicationBuilder(Some(emptyUserAnswers))
            .overrides(bind[UserAnswersConnector].toInstance(mockUserAnswersConnector))
            .build()

        running(application) {

          val request = FakeRequest(GET, routes.KeepAliveController.keepAlive.url)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }

    "when the user has not answered any questions" - {

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      when(mockUserAnswersConnector.keepAlive(eqTo(returnId))(any())).thenReturn(Future.successful(mock[HttpResponse]))

      "must return OK" in {
        val application = applicationBuilder(None)
          .overrides(bind[UserAnswersConnector].toInstance(mockUserAnswersConnector))
          .build()

        running(application) {

          val request = FakeRequest(GET, routes.KeepAliveController.keepAlive.url)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }

    "when there is no period key in the session" - {

      "must return OK" in {
        val application = applicationBuilder(None)
          .build()

        running(application) {

          val request = FakeRequestNoPeriodKey(GET, routes.KeepAliveController.keepAlive.url)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }
  }
}
