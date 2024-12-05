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

package controllers.actions

import base.SpecBase
import models.requests.IsOrganisationRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.http.Status.OK
import play.api.mvc.{BodyParsers, Result}
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval

import scala.collection.mutable
import scala.concurrent.Future

class CheckAffinityGroupIsOrganisationActionSpec extends SpecBase {

  val defaultBodyParser: BodyParsers.Default = app.injector.instanceOf[BodyParsers.Default]
  val mockAuthConnector: AuthConnector       = mock[AuthConnector]

  val testContent = "Test result body"

  val newCheckAffinityGroupIsOrganisationAction =
    new CheckAffinityGroupIsOrganisationActionImpl(mockAuthConnector, defaultBodyParser)

  val isOrganisationKey = "isOrganisation"

  def testCodeBlock(isOrganisationStore: mutable.Map[String, Boolean]): IsOrganisationRequest[_] => Future[Result] = {
    request =>

      isOrganisationStore.synchronized {
        isOrganisationStore.put(isOrganisationKey, request.isOrganisation)
      }

      Future(Ok(testContent))
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }

  "CheckAffinityGroupIsOrganisationAction .invokeBlock" - {
    "must execute the block with isOrganisation set to true, when the request is an organisation" in {
      when(mockAuthConnector.authorise(eqTo(Organisation), eqTo(EmptyRetrieval))(any(), any()))
        .thenReturn(Future.unit)

      val isOrganisationStore = mutable.Map[String, Boolean]()

      val result: Future[Result] =
        newCheckAffinityGroupIsOrganisationAction.invokeBlock(FakeRequest(), testCodeBlock(isOrganisationStore))

      status(result) mustBe OK
      contentAsString(result) mustBe testContent

      isOrganisationStore.synchronized {
        isOrganisationStore(isOrganisationKey) mustBe true
      }
    }
    "must execute the block with isOrganisation set to false, when the request is NOT an organisation" in {
      when(mockAuthConnector.authorise(eqTo(Organisation), eqTo(EmptyRetrieval))(any(), any()))
        .thenReturn(Future.failed(new Exception("Test Exception")))

      val isOrganisationStore = mutable.Map[String, Boolean]()

      val result: Future[Result] =
        newCheckAffinityGroupIsOrganisationAction.invokeBlock(FakeRequest(), testCodeBlock(isOrganisationStore))

      status(result) mustBe OK
      contentAsString(result) mustBe testContent

      isOrganisationStore.synchronized {
        isOrganisationStore(isOrganisationKey) mustBe false
      }
    }
  }
}
