package viewmodels.checkAnswers.adjustment

import base.SpecBase
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.{RepackagedDraughtProducts, Spoilt}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class AdjustmentTaxTypeSummarySpec extends SpecBase {
  "AdjustmentTaxTypeSummary" - {
    "should create the summary list row view model from the rateBands" in new SetUp {
      val adjustmentEntry = AdjustmentEntry(adjustmentType = Some(Spoilt), rateBand = Some(coreRateBand))
      val result = AdjustmentTaxTypeSummary.row(adjustmentEntry).get

      result.key.content mustBe Text("Tax type")
      result.value.content mustBe Text("Non-draught beer between 1% and 2% ABV (123)")
    }

    "should create the summary list row view model from the rateBands where adjustmentType is RepackagedDraughtProducts" in new SetUp {
      val adjustmentEntry = AdjustmentEntry(adjustmentType = Some(RepackagedDraughtProducts), rateBand = Some(coreRateBand))
      val result = AdjustmentTaxTypeSummary.row(adjustmentEntry).get

      result.key.content mustBe Text("Original tax type")
      result.value.content mustBe Text("Non-draught beer between 1% and 2% ABV (123)")
    }

    "should throw an exception if unable to get the adjustment type" in new SetUp {
      val adjustmentEntry = AdjustmentEntry(adjustmentType = None, rateBand = Some(coreRateBand))
      a[RuntimeException] mustBe thrownBy(AdjustmentTaxTypeSummary.row(adjustmentEntry).get)
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)
  }
}
