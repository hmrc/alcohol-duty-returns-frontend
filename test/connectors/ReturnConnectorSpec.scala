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

package connectors

import models.ReturnError.{ReturnLocked, ReturnNotFound, ReturnUpstreamError}
import models.{Return, ReturnPeriod}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, LOCKED, NOT_FOUND, OK}
import play.api.libs.json.Json

class ReturnConnectorSpec extends ConnectorBase {
  protected val endpointName = "alcohol-duty-returns"

  val periodKey = "24AE"
  val appaId = "XMADP0000000200"
  val internalId = "12345678"
  val returnPeriod = ReturnPeriod(periodKey, 2024, 5)
  val userReturn = Return(returnPeriod, appaId)
  val expectedQueryParams = Map(
    "appaId" -> appaId,
    "internalId" -> internalId,
    "periodKey" -> periodKey
  )

  "return connector should" - {
    "successfully get a return" in new ConnectorFixture {
      val url = config.adrReturnGetUrl()
      stubGetWithParameters(url, expectedQueryParams, OK, Json.toJson(Return(returnPeriod, appaId)).toString)
      whenReady(connector.getReturn(returnPeriod, appaId, internalId).value) {
        result =>
          result mustBe Right(userReturn)
          verifyGetWithParameters(url, expectedQueryParams)
      }
    }

    "return ReturnLocked if LOCKED is returned" in new ConnectorFixture {
      val url = config.adrReturnGetUrl()
      stubGetWithParameters(url, expectedQueryParams, LOCKED, "")
      whenReady(connector.getReturn(returnPeriod, appaId, internalId).value) {
        result =>
          result mustBe Left(ReturnLocked)
          verifyGetWithParameters(url, expectedQueryParams)
      }
    }

    "return ReturnNotFound if NOT_FOUND is returned" in new ConnectorFixture {
      val url = config.adrReturnGetUrl()
      stubGetWithParameters(url, expectedQueryParams, NOT_FOUND, "")
      whenReady(connector.getReturn(returnPeriod, appaId, internalId).value) {
        result =>
          result mustBe Left(ReturnNotFound)
          verifyGetWithParameters(url, expectedQueryParams)
      }
    }

    "return error details if anything else is returned" in new ConnectorFixture {
      val url = config.adrReturnGetUrl()
      stubGetWithParameters(url, expectedQueryParams, INTERNAL_SERVER_ERROR, "")
      whenReady(connector.getReturn(returnPeriod, appaId, internalId).value) {
        result =>
          result mustBe Left(ReturnUpstreamError)
          verifyGetWithParameters(url, expectedQueryParams)
      }
    }
  }
}