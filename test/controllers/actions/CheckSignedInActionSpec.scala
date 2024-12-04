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

package controllers.actions

import base.SpecBase
import models.requests.SignedInRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.mvc.{BodyParsers, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval

import scala.concurrent.Future

class CheckSignedInActionSpec extends SpecBase {
  val testContent                            = "Test"
  val defaultBodyParser: BodyParsers.Default = app.injector.instanceOf[BodyParsers.Default]
  val mockAuthConnector: AuthConnector       = mock[AuthConnector]

  val checkSignedInAction = new CheckSignedInActionImpl(mockAuthConnector, defaultBodyParser)

  def testAction(expectedSignedIn: Boolean): SignedInRequest[_] => Future[Result] = { request =>
    request.signedIn mustBe expectedSignedIn

    Future(Ok(testContent))
  }

  "invokeBlock" - {

    "execute the block and return signed in if signed in to Government Gateway" in {
      when(
        mockAuthConnector.authorise(
          eqTo(
            AuthProviders(GovernmentGateway)
          ),
          eqTo(EmptyRetrieval)
        )(any(), any())
      )
        .thenReturn(Future.unit)

      val result: Future[Result] = checkSignedInAction.invokeBlock(FakeRequest(), testAction(true))

      status(result) mustBe OK
      contentAsString(result) mustBe testContent
    }

    "execute the block and return not signed in if no longer authorised or never logged in" in {
      List(
        BearerTokenExpired(),
        MissingBearerToken(),
        InvalidBearerToken(),
        SessionRecordNotFound()
      ).foreach { exception =>
        when(
          mockAuthConnector.authorise[Unit](
            eqTo(
              AuthProviders(GovernmentGateway)
            ),
            eqTo(EmptyRetrieval)
          )(any(), any())
        ).thenReturn(Future.failed(exception))

        val result: Future[Result] = checkSignedInAction.invokeBlock(FakeRequest(), testAction(false))

        status(result) mustBe OK
        contentAsString(result) mustBe testContent
      }
    }
  }
}
