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
import models.AlcoholRegime.Beer
import models.dutySuspended._
import models.{AlcoholRegimes, CheckMode}
import pages.dutySuspended._
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem

class CheckYourAnswersSummaryListHelperSpec extends SpecBase {

  val helper = new CheckYourAnswersSummaryListHelper

  val completedUserAnswers = emptyUserAnswers
    .set(DutySuspendedBeerPage, DutySuspendedBeer(5, 1))
    .success
    .value
    .set(DutySuspendedCiderPage, DutySuspendedCider(12.22, 3.33))
    .success
    .value
    .set(DutySuspendedWinePage, DutySuspendedWine(-8.88, -1.2345))
    .success
    .value
    .set(DutySuspendedSpiritsPage, DutySuspendedSpirits(0.5, -0.001))
    .success
    .value
    .set(DutySuspendedOtherFermentedPage, DutySuspendedOtherFermented(0, 0))
    .success
    .value

  val expectedKeys = Seq(
    "Total net quantity of duty suspended beer",
    "Total net quantity of pure alcohol in this beer",
    "Total net quantity of duty suspended cider",
    "Total net quantity of pure alcohol in this cider",
    "Total net quantity of duty suspended wine",
    "Total net quantity of pure alcohol in this wine",
    "Total net quantity of duty suspended spirits",
    "Total net quantity of pure alcohol in these spirits",
    "Total net quantity of duty suspended other fermented products",
    "Total net quantity of pure alcohol in these other fermented products"
  )

  val expectedValues = Seq(
    "5.00 litres",
    "1.0000 litres",
    "12.22 litres",
    "3.3300 litres",
    "-8.88 litres",
    "-1.2345 litres",
    "0.50 litres",
    "-0.0010 litres",
    "0.00 litres",
    "0.0000 litres"
  )

  val urlBase = "/manage-alcohol-duty"

  val expectedActions = Seq(
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.dutySuspended.routes.DutySuspendedBeerController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("total net quantity of duty suspended beer")
    ),
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.dutySuspended.routes.DutySuspendedBeerController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("total net quantity of pure alcohol in this beer")
    ),
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.dutySuspended.routes.DutySuspendedCiderController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("total net quantity of duty suspended cider")
    ),
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.dutySuspended.routes.DutySuspendedCiderController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("total net quantity of pure alcohol in this cider")
    ),
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.dutySuspended.routes.DutySuspendedWineController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("total net quantity of duty suspended wine")
    ),
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.dutySuspended.routes.DutySuspendedWineController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("total net quantity of pure alcohol in this wine")
    ),
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.dutySuspended.routes.DutySuspendedSpiritsController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("total net quantity of duty suspended spirits")
    ),
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.dutySuspended.routes.DutySuspendedSpiritsController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("total net quantity of pure alcohol in these spirits")
    ),
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.dutySuspended.routes.DutySuspendedOtherFermentedController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("total net quantity of duty suspended other fermented products")
    ),
    ActionItem(
      content = Text("Change"),
      href = urlBase + controllers.dutySuspended.routes.DutySuspendedOtherFermentedController.onPageLoad(CheckMode).url,
      visuallyHiddenText = Some("total net quantity of pure alcohol in these other fermented products")
    )
  )

  "CheckYourAnswersSummaryListHelper" - {

    "must return a summary list with the correct rows" in {
      val summaryList = helper.dutySuspendedDeliveriesSummaryList(completedUserAnswers)(getMessages(app))

      summaryList.rows.map(_.key.content.asHtml.toString)   mustBe expectedKeys
      summaryList.rows.map(_.value.content.asHtml.toString) mustBe expectedValues
      summaryList.rows.map(_.actions.get.items.head)        mustBe expectedActions
    }

    "must return a summary list with only rows for approved regimes" in {
      val completedUserAnswersOnlyBeer = completedUserAnswers.copy(regimes = AlcoholRegimes(Set(Beer)))

      val summaryList = helper.dutySuspendedDeliveriesSummaryList(completedUserAnswersOnlyBeer)(getMessages(app))

      summaryList.rows.map(_.key.content.asHtml.toString)   mustBe expectedKeys.take(2)
      summaryList.rows.map(_.value.content.asHtml.toString) mustBe expectedValues.take(2)
      summaryList.rows.map(_.actions.get.items.head)        mustBe expectedActions.take(2)
    }
  }
}
