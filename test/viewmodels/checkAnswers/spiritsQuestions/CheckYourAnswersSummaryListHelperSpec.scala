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

package viewmodels.checkAnswers.spiritsQuestions

import base.SpecBase
import models.SpiritType
import models.spiritsQuestions.Whisky
import pages.spiritsQuestions.{DeclareSpiritsTotalPage, OtherSpiritsProducedPage, SpiritTypePage, WhiskyPage}

class CheckYourAnswersSummaryListHelperSpec extends SpecBase {

  val completedUserAnswers = emptyUserAnswers
    .set(
      SpiritTypePage,
      Set[SpiritType](SpiritType.Other)
    )
    .success
    .value
    .set(OtherSpiritsProducedPage, "Coco Pops")
    .success
    .value
    .set(DeclareSpiritsTotalPage, BigDecimal(11))
    .success
    .value
    .set(WhiskyPage, Whisky(BigDecimal(1), BigDecimal(2)))
    .success
    .value

  "CheckYourAnswersSummaryListHelper" - {

    "must return a summary list with the correct rows if pages are populated (Other spirits is selected)" in {
      val expectedSummaryListKeys =
        List("Total of all spirits", "Scotch Whisky", "Irish Whiskey", "Type of spirits", "Other spirits produced")

      val summaryListOption =
        CheckYourAnswersSummaryListHelper.spiritsSummaryList(completedUserAnswers)(getMessages(app))

      summaryListOption match {
        case None              => fail("Expected a Some containing a summary list")
        case Some(summaryList) => summaryList.rows.map(_.key.content.asHtml.toString) mustBe expectedSummaryListKeys
      }
    }

    "must return a summary list with the correct rows if all required pages are populated (Other spirits is not selected)" in {
      val userAnswers = completedUserAnswers
        .remove(List(OtherSpiritsProducedPage, SpiritTypePage))
        .success
        .value
        .set(
          SpiritTypePage,
          Set[SpiritType](SpiritType.Maltspirits, SpiritType.Grainspirits)
        )
        .success
        .value

      val expectedSummaryListKeys = List("Total of all spirits", "Scotch Whisky", "Irish Whiskey", "Type of spirits")

      val summaryListOption = CheckYourAnswersSummaryListHelper.spiritsSummaryList(userAnswers)(getMessages(app))

      summaryListOption match {
        case None              => fail("Expected a Some containing a summary list")
        case Some(summaryList) => summaryList.rows.map(_.key.content.asHtml.toString) mustBe expectedSummaryListKeys
      }
    }

    "must return None if a required page is not populated" - {

      "spirit type page (always required) is not populated" in {
        val userAnswers = completedUserAnswers
          .remove(List(OtherSpiritsProducedPage, SpiritTypePage))
          .success
          .value

        val summaryListOption = CheckYourAnswersSummaryListHelper.spiritsSummaryList(userAnswers)(getMessages(app))

        summaryListOption mustBe None
      }

      "Other spirits is selected but other spirits produced page is not populated" in {
        val userAnswers = completedUserAnswers
          .remove(List(OtherSpiritsProducedPage))
          .success
          .value

        val summaryListOption = CheckYourAnswersSummaryListHelper.spiritsSummaryList(userAnswers)(getMessages(app))

        summaryListOption mustBe None
      }
    }
  }
}
