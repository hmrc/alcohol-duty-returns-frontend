package controllers.SelectAppaId

import controllers.actions._
import forms.SelectAppaId.CustomLoginFormProvider
import javax.inject.Inject
import models.Mode
import pages.SelectAppaId.CustomLoginPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SelectAppaId.CustomLoginView

import scala.concurrent.{ExecutionContext, Future}

class CustomLoginController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cacheConnector: CacheConnector,
                                       identify: IdentifyWithEnrolmentAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: CustomLoginFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: CustomLoginView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(CustomLoginPage) match {
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
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CustomLoginPage, value))
              _              <- cacheConnector.set(updatedAnswers)
          } yield Redirect(???)
      )
  }
}