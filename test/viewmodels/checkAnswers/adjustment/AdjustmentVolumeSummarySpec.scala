package viewmodels.checkAnswers.adjustment

import base.SpecBase
import models.adjustment.AdjustmentEntry
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key, SummaryListRow, Value}

class AdjustmentVolumeSummarySpec extends SpecBase {
  "AdjustmentVolumeSummary" - {
    "must return a row if the both the total litres volume and pure alcohol volume can be fetched" in new SetUp(true, true) {
      adjustmentVolumeSummary.row(adjustmentEntry) mustBe Some(SummaryListRow(Key(Text("Volume")), Value(HtmlContent("12.20 litres<br/>1.2000 litres of pure alcohol (LPA)")), "", Some(Actions(items = List(ActionItem("/manage-alcohol-duty/complete-return/adjustments/adjustment/change/volume", Text("Change"), Some("volume")))))))
    }

    "must return no row if the total litres volume type can't be fetched" in new SetUp(true, false) {
      adjustmentVolumeSummary.row(adjustmentEntry) mustBe None
    }

    "must return no row if the pure alcohol volume can't be fetched" in new SetUp(false, true) {
      adjustmentVolumeSummary.row(adjustmentEntry) mustBe None
    }

    "must return no row if neither volume can be fetched" in new SetUp(false, false) {
      adjustmentVolumeSummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(hasTotalLitresVolume: Boolean, hasPureAlcoholVolume: Boolean) {
    val application = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val maybeTotalLitresVolume = if (hasTotalLitresVolume) {
      Some(BigDecimal("12.2"))
    } else {
      None
    }

    val maybePureAlcoholVolume = if (hasPureAlcoholVolume) {
      Some(BigDecimal("1.2000"))
    } else {
      None
    }

    val adjustmentEntry = AdjustmentEntry(totalLitresVolume = maybeTotalLitresVolume, pureAlcoholVolume = maybePureAlcoholVolume)

    val adjustmentVolumeSummary = new AdjustmentVolumeSummary
  }
}
