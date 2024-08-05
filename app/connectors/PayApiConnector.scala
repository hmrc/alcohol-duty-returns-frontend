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

package connectors

import cats.data.EitherT
import config.FrontendAppConfig
import models.payments.PaymentStart
import play.api.Logging
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances, HttpResponse, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class PaymentJourneyDescription(journeyId: String,
                                     nextUrl: String)

object PaymentJourneyDescription {
  implicit val formats: OFormat[PaymentJourneyDescription] = Json.format[PaymentJourneyDescription]
}

class PayApiConnector @Inject() (
                                  config: FrontendAppConfig,
                                  implicit val httpClient: HttpClient
                                )(implicit ec: ExecutionContext)
  extends HttpReadsInstances
    with Logging {

  def startPayment(paymentStart: PaymentStart)(implicit hc: HeaderCarrier): EitherT[Future, String, PaymentJourneyDescription] = {
    EitherT {
      httpClient
        .POST[PaymentStart, [UpstreamErrorResponse, HttpResponse]](url = config.)
  }

}
