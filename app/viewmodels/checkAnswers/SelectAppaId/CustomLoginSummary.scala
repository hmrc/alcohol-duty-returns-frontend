package viewmodels.checkAnswers.SelectAppaId

import controllers.SelectAppaId.routes
import models.{CheckMode, UserAnswers}
import pages.SelectAppaId.CustomLoginPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CustomLoginSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CustomLoginPage).map {
      answers =>

        val value = ValueViewModel(
          HtmlContent(
            answers.map {
              answer => HtmlFormat.escape(messages(s"customLogin.$answer")).toString
            }
            .mkString(",<br>")
          )
        )

        SummaryListRowViewModel(
          key     = "customLogin.checkYourAnswersLabel",
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", routes.CustomLoginController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("customLogin.change.hidden"))
          )
        )
    }
}
