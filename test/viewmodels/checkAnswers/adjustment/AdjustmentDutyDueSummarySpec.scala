package viewmodels.checkAnswers.adjustment

import base.SpecBase
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.{RepackagedDraughtProducts, Spoilt}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key, SummaryListRow, Value}

class AdjustmentDutyDueSummarySpec extends SpecBase {
  "AdjustmentDutyDueSummary" - {
    "must return a row without actions and prioritise new duty over duty if not Spoilt" in new SetUp(false, true, false) {
      adjustmentDutyDueSummary.row(adjustmentEntry) mustBe Some(SummaryListRow(Key(Text("Duty value")), Value(Text("£1.20")), "", Some(Actions(items = List()))))
    }

    "must return a row with an action and prioritise new duty over duty if Spoilt" in new SetUp(true, true, false) {
      adjustmentDutyDueSummary.row(adjustmentEntry) mustBe Some(SummaryListRow(Key(Text("Duty value")), Value(Text("£1.20")), "", Some(Actions(items = List(ActionItem("/manage-alcohol-duty/complete-return/adjustments/adjustment/change/spoilt-product/volume", Text("Change"), Some("duty value")))))))
    }

    "must return a row with an action and duty if no new duty, and Spoilt" in new SetUp(true, false, true) {
      adjustmentDutyDueSummary.row(adjustmentEntry) mustBe Some(SummaryListRow(Key(Text("Duty value")), Value(Text("£2.30")), "", Some(Actions(items = List(ActionItem("/manage-alcohol-duty/complete-return/adjustments/adjustment/change/spoilt-product/volume", Text("Change"), Some("duty value")))))))
    }

    "must return no row if neither duty can be fetched" in new SetUp(true, false, false) {
      adjustmentDutyDueSummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(isSpoilt: Boolean, hasNewDuty: Boolean, hasDuty: Boolean) {
    val application = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val maybeAdjustmentType = if (isSpoilt) {
      Some(Spoilt)
    } else {
      Some(RepackagedDraughtProducts)
    }

    val maybeNewDuty = if (hasNewDuty) {
      Some(BigDecimal("1.20"))
    } else {
      None
    }

    val maybeDuty = if (hasDuty || hasNewDuty) {
      Some(BigDecimal("2.30"))
    } else {
      None
    }


    val adjustmentEntry = AdjustmentEntry(adjustmentType = maybeAdjustmentType, duty = maybeDuty, newDuty = maybeNewDuty)

    val adjustmentDutyDueSummary = new AdjustmentDutyDueSummary
  }
}
