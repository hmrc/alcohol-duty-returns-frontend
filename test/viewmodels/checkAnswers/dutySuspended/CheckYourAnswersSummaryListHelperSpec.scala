/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package viewmodels.checkAnswers.dutySuspended

import base.SpecBase
import models.AlcoholRegime.Wine
import pages.dutySuspended.{DutySuspendedAlcoholTypePage, DutySuspendedFinalVolumesPage}

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
