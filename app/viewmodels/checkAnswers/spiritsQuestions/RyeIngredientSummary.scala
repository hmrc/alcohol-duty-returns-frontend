package viewmodels.checkAnswers.spiritsQuestions

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.spiritsQuestions.RyeIngredientPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RyeIngredientSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RyeIngredientPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "ryeIngredient.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.RyeIngredientController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("ryeIngredient.change.hidden"))
          )
        )
    }
}
