package controllers

import config.Constants.adrReturnCreatedDetails
import config.FrontendAppConfig
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.checkAndSubmit.AdrReturnCreatedDetails
import models.payments.PaymentStart
import models.requests.DataRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{MessagesControllerComponents, Request, Result, Session}
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

      //TODO: add selfHost url and check routes
      val returnUrl = controllers.checkAndSubmit.routes.ReturnSubmittedController.onPageLoad().url

      getReturnDetails(request.session) match {
        case Some(returnDetails) => {

        val paymentStart = PaymentStart.createPaymentStart(
            returnDetails,
            returnUrl,
            backUrl(backPage)
          )
          invokeApi(paymentStart)
        }
    case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }
}

  private def invokeApi(paymentStart: PaymentStart): Future[Result] = {
    ???
  }

  private def backUrl(backPage: String)(implicit request: Request[_]): String = {
    val url = backPage match {
      case "charges-owed" => controllers.checkAndSubmit.routes.ReturnSubmittedController.onPageLoad().url
      case _ => controllers.returns.routes.ViewPastReturnsController.onPageLoad.url
    }
    appConfig.signOutUrl + url
  }

  def getReturnDetails(session : Session): Option[AdrReturnCreatedDetails] = {
    session.get(adrReturnCreatedDetails).flatMap(returnDetailsString => Json.parse(returnDetailsString).asOpt[AdrReturnCreatedDetails])
  }
}

