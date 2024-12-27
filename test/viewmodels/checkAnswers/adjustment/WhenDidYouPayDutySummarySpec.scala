package viewmodels.checkAnswers.adjustment

import base.SpecBase
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.Underdeclaration
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key, SummaryListRow, Value}

import java.time.YearMonth

class WhenDidYouPayDutySummarySpec extends SpecBase {
  "WhenDidYouPayDutySummary" - {
    "must return a row if the period and adjustment type can be fetched" in new SetUp(true, true) {
       whenDidYouPayDutySummary.row(adjustmentEntry) mustBe Some(SummaryListRow(Key(Text("Original return period")), Value(HtmlContent("June 2024")), "", Some(Actions(items = List(ActionItem("/manage-alcohol-duty/complete-return/adjustments/adjustment/change/return-period", Text("Change"), Some("original return period")))))))
    }

    "must return no row if no period type can be fetched" in new SetUp(false, true) {
      whenDidYouPayDutySummary.row(adjustmentEntry) mustBe None
    }

    "must return no row if no adjustment type can be fetched" in new SetUp(true, false) {
      whenDidYouPayDutySummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(hasPeriod: Boolean, hasAdjustmentType: Boolean) {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val maybePeriod = if (hasPeriod) { Some(YearMonth.now(clock)) } else { None }
    val maybeAdjustmentType = if (hasAdjustmentType) { Some(Underdeclaration) } else { None }
    val adjustmentEntry = AdjustmentEntry(adjustmentType = maybeAdjustmentType, period = maybePeriod)

    val whenDidYouPayDutySummary = new WhenDidYouPayDutySummary(createDateTimeHelper())
  }
}
