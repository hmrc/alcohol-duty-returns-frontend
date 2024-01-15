package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.RyeUsedPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RyeUsedSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RyeUsedPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "ryeUsed.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.RyeUsedController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("ryeUsed.change.hidden"))
          )
        )
    }
}
