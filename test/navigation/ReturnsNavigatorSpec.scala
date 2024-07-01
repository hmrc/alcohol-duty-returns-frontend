/*
 * Copyright 2024 HM Revenue & Customs
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

package navigation

import base.SpecBase
import controllers.routes
import models.NormalMode
import models.RateType.{Core, DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}
import pages.Page
import pages.returns.DeclareAlcoholDutyQuestionPage

class ReturnsNavigatorSpec extends SpecBase {

  val navigator = new ReturnsNavigator
  val regime    = regimeGen.sample.value
  val rateBands = genListOfRateBandForRegime(regime).sample.value

  "ReturnsNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers) mustBe routes.IndexController.onPageLoad
      }

      "must go from a page that doesn't exist in the route map to Index with regime" in {
        case object UnknownPage extends Page
        navigator.nextPageWithRegime(
          UnknownPage,
          NormalMode,
          emptyUserAnswers,
          regime
        ) mustBe routes.IndexController.onPageLoad
      }

      "must go from the Alcohol to declare to task list page if the answer is Yes" in {

        navigator.nextPage(
          DeclareAlcoholDutyQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.returns.DeclareAlcoholDutyQuestionPage, true).success.value
        ) mustBe routes.TaskListController.onPageLoad
      }

      "must go from the Alcohol to declare to task list page if the answer is No" in {

        navigator.nextPage(
          pages.returns.DeclareAlcoholDutyQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.returns.DeclareAlcoholDutyQuestionPage, false).success.value
        ) mustBe routes.TaskListController.onPageLoad
      }

      "must go from WhatDoYouNeedToDeclare page to HowMuchDoYouNeedToDeclare page if the user selected any 'Core' or 'DraughtRelief' entries" in {
        val rateBandCore          = rateBands.head.copy(rateType = Core)
        val rateBandDraughtRelief = rateBands.last.copy(rateType = DraughtRelief)

        navigator.nextPageWithRegime(
          pages.returns.WhatDoYouNeedToDeclarePage,
          NormalMode,
          emptyUserAnswers
            .setByKey(pages.returns.WhatDoYouNeedToDeclarePage, regime, Set(rateBandCore, rateBandDraughtRelief))
            .success
            .value,
          regime
        ) mustBe controllers.returns.routes.HowMuchDoYouNeedToDeclareController.onPageLoad(NormalMode, regime)
      }

      "must go from WhatDoYouNeedToDeclare page to DoYouHaveMultipleSPRDutyRates page if the user selected any 'SmallProducerRelief' or 'DraughtAndSmallProducerRelief' entries" in {
        val rateBandSPR           = rateBands.head.copy(rateType = SmallProducerRelief)
        val rateBandDraughtAndSPR = rateBands.last.copy(rateType = DraughtAndSmallProducerRelief)

        navigator.nextPageWithRegime(
          pages.returns.WhatDoYouNeedToDeclarePage,
          NormalMode,
          emptyUserAnswers
            .setByKey(pages.returns.WhatDoYouNeedToDeclarePage, regime, Set(rateBandSPR, rateBandDraughtAndSPR))
            .success
            .value,
          regime
        ) mustBe controllers.returns.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
      }

      "must go from WhatDoYouNeedToDeclare page to the Index page if the user hasn't selected any entries" in {
        navigator.nextPageWithRegime(
          pages.returns.WhatDoYouNeedToDeclarePage,
          NormalMode,
          emptyUserAnswers,
          regime
        ) mustBe routes.IndexController.onPageLoad
      }

      "must go from HowMuchDoYouNeedToDeclare page to DoYouHaveMultipleSPRDutyRates page if the user selected any 'SmallProducerRelief' or 'DraughtAndSmallProducerRelief' entries" in {
        val rateBandSPR           = rateBands.head.copy(rateType = SmallProducerRelief)
        val rateBandDraughtAndSPR = rateBands.last.copy(rateType = DraughtAndSmallProducerRelief)

        navigator.nextPageWithRegime(
          pages.returns.HowMuchDoYouNeedToDeclarePage,
          NormalMode,
          emptyUserAnswers
            .setByKey(pages.returns.WhatDoYouNeedToDeclarePage, regime, Set(rateBandSPR, rateBandDraughtAndSPR))
            .success
            .value,
          regime
        ) mustBe controllers.returns.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
      }

      "must go from HowMuchDoYouNeedToDeclare page to Check Your Answer page if the user did not select any 'SmallProducerRelief' or 'DraughtAndSmallProducerRelief' entries" in {
        val rateBandCore          = rateBands.head.copy(rateType = Core)
        val rateBandDraughtRelief = rateBands.last.copy(rateType = DraughtRelief)

        navigator.nextPageWithRegime(
          pages.returns.HowMuchDoYouNeedToDeclarePage,
          NormalMode,
          emptyUserAnswers
            .setByKey(pages.returns.WhatDoYouNeedToDeclarePage, regime, Set(rateBandCore, rateBandDraughtRelief))
            .success
            .value,
          regime
        ) mustBe controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime)
      }

      "must go from DoYouHaveMultipleSPRDutyRates page to TellUsAboutMultipleSPRRate page if the user did select 'Yes' and the Multiple SPR list is empty" in {
        navigator.nextPageWithRegime(
          pages.returns.DoYouHaveMultipleSPRDutyRatesPage,
          NormalMode,
          emptyUserAnswers
            .setByKey(pages.returns.DoYouHaveMultipleSPRDutyRatesPage, regime, true)
            .success
            .value,
          regime
        ) mustBe controllers.returns.routes.TellUsAboutMultipleSPRRateController.onPageLoad(NormalMode, regime)
      }

      "must go from DoYouHaveMultipleSPRDutyRates page to TellUsAboutSingleSPRRate page if the user did select 'No'" in {
        navigator.nextPageWithRegime(
          pages.returns.DoYouHaveMultipleSPRDutyRatesPage,
          NormalMode,
          emptyUserAnswers
            .setByKey(pages.returns.DoYouHaveMultipleSPRDutyRatesPage, regime, false)
            .success
            .value,
          regime
        ) mustBe controllers.returns.routes.TellUsAboutSingleSPRRateController.onPageLoad(NormalMode, regime)
      }

      "must go from DoYouHaveMultipleSPRDutyRates page to MultipleSPRList page if the user did select 'Yes' and the Multiple SPR list is not empty" in {

        val volumeAndRateByTaxType = genVolumeAndRateByTaxTypeRateBand(rateBands.head).arbitrary.sample.value

        navigator.nextPageWithRegime(
          pages.returns.DoYouHaveMultipleSPRDutyRatesPage,
          NormalMode,
          emptyUserAnswers
            .setByKey(pages.returns.DoYouHaveMultipleSPRDutyRatesPage, regime, true)
            .success
            .value
            .setByKey(pages.returns.MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType))
            .success
            .value,
          regime
        ) mustBe controllers.returns.routes.MultipleSPRListController.onPageLoad(regime)
      }
    }
  }
}
