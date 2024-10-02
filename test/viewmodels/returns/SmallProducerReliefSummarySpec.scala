package viewmodels.returns

import base.SpecBase
import models.AlcoholRegime.Beer
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class SmallProducerReliefSummarySpec extends SpecBase {
  "SmallProducerReliefSummary" - {
    "should return a multiple SPR list summary if has multiple SPR duty rates" in new SetUp {
      val answers = doYouHaveMultipleSPRDutyRatesPage(userAnswersWithBeer, Beer, true)

      SmallProducerReliefSummary.summaryList(Beer, answers).get.card.get.title.get.content mustBe Text("Beer eligible for Small Producer Relief (multiple duty rates)")
    }

    "should return a single SPR list summary if has multiple SPR duty rates" in new SetUp {
      val answers = doYouHaveMultipleSPRDutyRatesPage(userAnswersWithBeer, Beer, false)

      SmallProducerReliefSummary.summaryList(Beer, answers).get.card.get.title.get.content mustBe Text("Beer eligible for Small Producer Relief")
    }

    "should return None if the question wasn't answered" in new SetUp {
      SmallProducerReliefSummary.summaryList(Beer, userAnswersWithBeer) mustBe None
    }
  }
  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)
  }
}
