package viewmodels.checkAnswers.adjustment

import base.SpecBase
import models.AlcoholRegime.Beer
import models.adjustment.AdjustmentEntry
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key, SummaryListRow, Value}

class AlcoholicProductTypeSummarySpec extends SpecBase {
  "AlcoholicProductTypeSummary" - {
    "must return a row if the spoilt regime can be fetched" in new SetUp(true) {
      alcoholicProductTypeSummary.row(adjustmentEntry) mustBe Some(SummaryListRow(Key(Text("Description")), Value(HtmlContent("Beer")), "", Some(Actions(items = List(ActionItem("/manage-alcohol-duty/complete-return/adjustments/adjustment/change/change/spoilt-product/alcohol-type", Text("Change"), Some("Alcoholic product type")))))))
    }

    "must return no row if no spoilt regime can be fetched" in new SetUp(false) {
      alcoholicProductTypeSummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(hasSpoiltRegime: Boolean) {
    val application = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val maybeSpoiltRegime = if (hasSpoiltRegime) {
      Some(Beer)
    } else {
      None
    }

    val adjustmentEntry = AdjustmentEntry(spoiltRegime = maybeSpoiltRegime)

    val alcoholicProductTypeSummary = new AlcoholicProductTypeSummary()
  }
}
