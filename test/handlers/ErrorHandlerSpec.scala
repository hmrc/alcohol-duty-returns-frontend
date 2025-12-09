/*
 * Copyright 2025 HM Revenue & Customs
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

package handlers

import base.SpecBase
import controllers.routes
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.{ErrorTemplate, NotFound}

import scala.concurrent.Future

class ErrorHandlerSpec extends SpecBase {

  import ErrorHandlerSpec._

  "ErrorHandler" - {
    "must return the correct error page for a not found" in {

      when(
        notFoundTemplate.apply(
          eqTo("site.error.pageNotFound404.title"),
          eqTo("site.error.pageNotFound404.heading"),
          eqTo("site.error.pageNotFound404.message.1"),
          eqTo("site.error.pageNotFound404.message.2")
        )(any(), any())
      ).thenReturn(Html("<h1>not found template</h1>"))

      val errorHandler = new ErrorHandler(messagesApi, errorTemplate, notFoundTemplate)

      val result: Future[Html] =
        errorHandler.notFoundTemplate(fakeRequest)

      result.futureValue mustEqual Html("<h1>not found template</h1>")
    }
  }

}

object ErrorHandlerSpec {
  val messagesApi: MessagesApi                         = mock[MessagesApi]
  val notFoundTemplate: NotFound                       = mock[NotFound]
  val errorTemplate: ErrorTemplate                     = mock[ErrorTemplate]
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, routes.TaskListController.onPageLoad.url)
  implicit val fakeMessages: Messages                  = messagesApi.preferred(fakeRequest)
}
