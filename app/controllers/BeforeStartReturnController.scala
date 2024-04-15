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

import connectors.{CacheConnector, ReturnConnector}
import controllers.actions._
import models.{Return, ReturnPeriod, UserAnswers}
import pages.{ReturnPage, ReturnPeriodPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BeforeStartReturnView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BeforeStartReturnController @Inject() (
  cacheConnector: CacheConnector,
  returnConnector: ReturnConnector,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  view: BeforeStartReturnView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(periodKey: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))

    ReturnPeriod.fromPeriodKey(periodKey) match {
      case Left(error)         =>
        // TODO: Log the error here
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Right(returnPeriod) =>
        val appaID = "XMADP0000000200"
        returnConnector
          .getReturn(returnPeriod, appaID, request.userId)
          .foldF(
            _ => {
              println("fold!!")
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            },
            handleReturn(_, returnPeriod, userAnswers)
          )
    }
  }

  private def handleReturn(
    maybeUserReturn: Option[Return],
    returnPeriod: ReturnPeriod,
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] =
    maybeUserReturn match {
      case Some(userReturn) =>
        println("here")
        userAnswers
          .set(ReturnPage, userReturn)
          .fold(
            _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())),
            ua => cacheConnector.set(ua).map(_ => Redirect(controllers.routes.TaskListController.onPageLoad))
          )
      case None             =>
        println("here too")
        val fromDate = returnPeriod.firstDateViewString()
        val toDate   = returnPeriod.lastDateViewString()
        userAnswers
          .set(ReturnPeriodPage, returnPeriod)
          .fold(
            _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())),
            ua => cacheConnector.set(ua).map(_ => Ok(view(fromDate, toDate)))
          )
    }
}
