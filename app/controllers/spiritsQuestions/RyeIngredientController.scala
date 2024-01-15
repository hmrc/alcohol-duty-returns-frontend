package controllers.spiritsQuestions

import connectors.CacheConnector
import controllers.actions._
import forms.spiritsQuestions.RyeIngredientFormProvider
import models.Mode
import pages.spiritsQuestions.RyeIngredientPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RyeIngredientController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cacheConnector: CacheConnector,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: RyeIngredientFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: RyeIngredientView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(RyeIngredientPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(RyeIngredientPage, value))
            _              <- cacheConnector.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(RyeIngredientPage, mode, updatedAnswers))
      )
  }
}
