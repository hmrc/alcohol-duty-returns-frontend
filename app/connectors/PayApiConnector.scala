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
