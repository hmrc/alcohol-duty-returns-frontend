package viewmodels.checkAnswers.dutySuspendedNew

import base.SpecBase
import models.AlcoholRegime.Wine
import pages.dutySuspendedNew.{DutySuspendedAlcoholTypePage, DutySuspendedFinalVolumesPage}

class CheckYourAnswersSummaryListHelperSpec extends SpecBase {
  val summaryListHelper = new CheckYourAnswersSummaryListHelper

  "alcoholTypeSummaryList" - {
    "must return a summary list with one row for the selected alcohol types" in {
      val expectedSummaryListKeys   = List("Type of alcohol")
      val expectedSummaryListValues = List("Beer<br>Cider")

      val summaryList = summaryListHelper
        .alcoholTypeSummaryList(userAnswersWithDutySuspendedData)(getMessages(app))
        .getOrElse(fail("Expected a Some containing a summary list"))

      summaryList.rows.map(_.key.content.asHtml.toString)   mustBe expectedSummaryListKeys
      summaryList.rows.map(_.value.content.asHtml.toString) mustBe expectedSummaryListValues
    }

    "must return None if DutySuspendedAlcoholTypePage is not populated" in {
      val userAnswersWithoutAlcoholTypePage = userAnswersWithDutySuspendedData
        .remove(DutySuspendedAlcoholTypePage)
        .success
        .value

      val summaryListOption =
        summaryListHelper.alcoholTypeSummaryList(userAnswersWithoutAlcoholTypePage)(getMessages(app))

      summaryListOption mustBe None
    }
  }

  "dutySuspendedAmountsSummaryList" - {
    "must return a summary list with the correct rows if all required pages are populated" in {
      val expectedSummaryListKeys   = List("Beer", "Cider", "Wine", "Spirits", "Other fermented products")
      val expectedSummaryListValues = List(
        "100.00 litres of total product<br>10.0000 litres of pure alcohol",
        "100.00 litres of total product<br>10.0000 litres of pure alcohol",
        "0.00 litres of total product<br>0.0000 litres of pure alcohol",
        "3.40 litres of total product<br>0.3400 litres of pure alcohol",
        "-5.50 litres of total product<br>-0.8200 litres of pure alcohol"
      )

      val summaryList = summaryListHelper
        .dutySuspendedAmountsSummaryList(userAnswersWithDutySuspendedDataAllRegimes)(getMessages(app))
        .getOrElse(fail("Expected a Some containing a summary list"))

      summaryList.rows.map(_.key.content.asHtml.toString)   mustBe expectedSummaryListKeys
      summaryList.rows.map(_.value.content.asHtml.toString) mustBe expectedSummaryListValues
    }

    "must return None if DutySuspendedAlcoholTypePage is not populated" in {
      val userAnswersWithoutAlcoholTypePage = userAnswersWithDutySuspendedDataAllRegimes
        .remove(DutySuspendedAlcoholTypePage)
        .success
        .value

      val summaryListOption =
        summaryListHelper.dutySuspendedAmountsSummaryList(userAnswersWithoutAlcoholTypePage)(getMessages(app))

      summaryListOption mustBe None
    }

    "must return None if calculated volumes are not present for a selected regime" in {
      val userAnswersMissingFinalVolumes = userAnswersWithDutySuspendedDataAllRegimes
        .removeByKey(DutySuspendedFinalVolumesPage, Wine)
        .success
        .value

      val summaryListOption =
        summaryListHelper.dutySuspendedAmountsSummaryList(userAnswersMissingFinalVolumes)(getMessages(app))

      summaryListOption mustBe None
    }
  }
}
