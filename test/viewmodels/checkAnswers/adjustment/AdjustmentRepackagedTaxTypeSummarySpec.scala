package viewmodels.checkAnswers.adjustment

import base.SpecBase
import models.adjustment.AdjustmentEntry
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key, SummaryListRow, Value}

class AdjustmentRepackagedTaxTypeSummarySpec extends SpecBase {
  "AdjustmentRepackagedTaxTypeSummary" - {
    "must return a row if the period and adjustment type can be fetched" in new SetUp(true) {
      adjustmentRepackagedTaxTypeSummary.row(adjustmentEntry) mustBe Some(SummaryListRow(Key(Text("New tax type")), Value(Text("Non-draught beer between 1% and 2% ABV (123)")), "", Some(Actions(items = List(ActionItem("/manage-alcohol-duty/complete-return/adjustments/change/repackaged/new-tax-type-code", Text("Change"), Some("new tax type")))))))
    }

    "must return no row if no repackaged rate band can be fetched" in new SetUp(false) {
      adjustmentRepackagedTaxTypeSummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(hasRepackagedRateBand: Boolean) {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val maybeRepackagedRateBand = if (hasRepackagedRateBand) { Some(coreRateBand) } else { None }
    val adjustmentEntry = AdjustmentEntry(repackagedRateBand = maybeRepackagedRateBand)

    val adjustmentRepackagedTaxTypeSummary = new AdjustmentRepackagedTaxTypeSummary()
  }
}
