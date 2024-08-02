package controllers

import config.Constants.adrReturnCreatedDetails
import config.FrontendAppConfig
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.payments.PaymentStart
import models.requests.DataRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartPaymentController @Inject()  (
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          appConfig: FrontendAppConfig,
                                          val controllerComponents: MessagesControllerComponents
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController
  with I18nSupport
  with Logging {

  def initiateAndRedirect(chargeReference: String, backPage: String) = (identify andThen getData).async {
    implicit request =>

      //val returnUrl = appConfig.selfHost + controllers.routes.ReturnsOverviewController.onPageLoad().url

      request.session.get(adrReturnCreatedDetails) match {
        case Some(chargeReference) => {

        val paymentStart = PaymentStart(
            amountInPence,
            chargeReference,
            paymentDueDate,
            returnUrl,
            backUrl(backPage)
          )
          invokeApi(paymentStart)
        }
    case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }
}

  private def invokeApi(paymentStart: PaymentStart)(implicit request: DataRequest[_]): Future[Result] = {
    ???
  }

  private def backUrl(backPage: String)(implicit request: Request[_]): String = {
    val url = backPage match {
      case "charges-owed" => controllers.checkAndSubmit.routes.ReturnSubmittedController.onPageLoad().url
      case _ => controllers.returns.routes.ViewPastReturnsController.onPageLoad.url
    }
    appConfig.selfHost + url
  }
}

