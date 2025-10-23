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
import models.spiritsQuestions.Whisky
import models.{CheckMode, SpiritType}
import pages.spiritsQuestions.{DeclareSpiritsTotalPage, OtherSpiritsProducedPage, SpiritTypePage, WhiskyPage}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem

class CheckYourAnswersSummaryListHelperSpec extends SpecBase {

  val completedUserAnswers = emptyUserAnswers
    .set(SpiritTypePage, Set[SpiritType](SpiritType.Other))
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

  val urlBase = "/manage-alcohol-duty"

  val expectedBaseActions = Seq(
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("total of all spirits")
    ),
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.spiritsQuestions.routes.WhiskyController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("Scotch Whisky")
    ),
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.spiritsQuestions.routes.WhiskyController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("Irish Whiskey")
    ),
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.spiritsQuestions.routes.SpiritTypeController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("type of spirits")
    )
  )

  val expectedFullActions = expectedBaseActions :+ ActionItem(
    content = Text("Change"),
    href = urlBase + controllers.spiritsQuestions.routes.OtherSpiritsProducedController.onPageLoad(CheckMode).url,
    visuallyHiddenText = Some("other spirits produced")
  )

  "CheckYourAnswersSummaryListHelper" - {

    "must return a summary list with the correct rows if pages are populated (Other spirits is selected)" in {
      val expectedSummaryListKeys =
        List("Total of all spirits", "Scotch Whisky", "Irish Whiskey", "Type of spirits", "Other spirits produced")

      val expectedSummaryListValues = List(
        "11.00 litres of pure alcohol",
        "1.00 litres of pure alcohol",
        "2.00 litres of pure alcohol",
        "<span aria-label='Type of spirits'><span class='break'>Other spirits</span>",
        "Coco Pops"
      )

      val summaryList = CheckYourAnswersSummaryListHelper
        .spiritsSummaryList(completedUserAnswers)(getMessages(app))
        .getOrElse(fail("Expected a Some containing a summary list"))

      summaryList.rows.map(_.key.content.asHtml.toString)   mustBe expectedSummaryListKeys
      summaryList.rows.map(_.value.content.asHtml.toString) mustBe expectedSummaryListValues
      summaryList.rows.map(_.actions.get.items.head)        mustBe expectedFullActions
      summaryList.card.get.title.get.content                mustBe Text("Spirits you’ve produced")
      summaryList.card.get.actions                          mustBe None
    }

    "must return a summary list with the correct rows if all required pages are populated (Other spirits is not selected)" in {
      val userAnswersWithoutOtherSpiritsSelected = completedUserAnswers
        .remove(List(OtherSpiritsProducedPage, SpiritTypePage))
        .success
        .value
        .set(SpiritTypePage, Set[SpiritType](SpiritType.Maltspirits, SpiritType.Grainspirits))
        .success
        .value

      val expectedSummaryListKeys = List("Total of all spirits", "Scotch Whisky", "Irish Whiskey", "Type of spirits")

      val expectedSummaryListValues = List(
        "11.00 litres of pure alcohol",
        "1.00 litres of pure alcohol",
        "2.00 litres of pure alcohol",
        "<span aria-label='Type of spirits'><span class='break'>Malt spirits,</span><span class='break'>Grain spirits</span>"
      )

      val summaryList = CheckYourAnswersSummaryListHelper
        .spiritsSummaryList(userAnswersWithoutOtherSpiritsSelected)(getMessages(app))
        .getOrElse(fail("Expected a Some containing a summary list"))

      summaryList.rows.map(_.key.content.asHtml.toString)   mustBe expectedSummaryListKeys
      summaryList.rows.map(_.value.content.asHtml.toString) mustBe expectedSummaryListValues
      summaryList.rows.map(_.actions.get.items.head)        mustBe expectedBaseActions
      summaryList.card.get.title.get.content                mustBe Text("Spirits you’ve produced")
      summaryList.card.get.actions                          mustBe None
    }

    "must return None if a required page is not populated" - {

      "spirit type page (always required) is not populated" in {
        val userAnswersWithoutSpiritTypePage = completedUserAnswers
          .remove(List(OtherSpiritsProducedPage, SpiritTypePage))
          .success
          .value

        val summaryListOption =
          CheckYourAnswersSummaryListHelper.spiritsSummaryList(userAnswersWithoutSpiritTypePage)(getMessages(app))

        summaryListOption mustBe None
      }

      "Other spirits is selected but other spirits produced page is not populated" in {
        val userAnswersWithoutOtherSpiritsProducedPage = completedUserAnswers
          .remove(List(OtherSpiritsProducedPage))
          .success
          .value

        val summaryListOption =
          CheckYourAnswersSummaryListHelper.spiritsSummaryList(userAnswersWithoutOtherSpiritsProducedPage)(
            getMessages(app)
          )

        summaryListOption mustBe None
      }
    }
  }
}
