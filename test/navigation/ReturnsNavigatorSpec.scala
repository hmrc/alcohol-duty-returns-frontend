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
import models.AlcoholRegime.Beer
import models.{AlcoholRegimes, CheckMode, NormalMode}
import models.RateType.{Core, DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}
import pages.Page
import pages.returns.DeclareAlcoholDutyQuestionPage

class ReturnsNavigatorSpec extends SpecBase {

  val navigator = new ReturnsNavigator
  val regime    = regimeGen.sample.value
  val rateBands = genListOfRateBandForRegime(regime).sample.value

  "ReturnsNavigator" - {

    "in Normal mode" - {

      "nextPage" - {
        "must go from a page that doesn't exist in the route map to the Task List page" in {
          case object UnknownPage extends Page
          navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the Alcohol to declare to task list page if the answer is Yes" in {

          navigator.nextPage(
            DeclareAlcoholDutyQuestionPage,
            NormalMode,
            emptyUserAnswers.set(pages.returns.DeclareAlcoholDutyQuestionPage, true).success.value
          ) mustBe controllers.returns.routes.AlcoholTypeController.onPageLoad(NormalMode)
        }

        "must go from the Alcohol to declare to task list page if the user has only 1 approval" in {

          navigator.nextPage(
            pages.returns.DeclareAlcoholDutyQuestionPage,
            NormalMode,
            emptyUserAnswers
              .copy(regimes = AlcoholRegimes(Set(Beer)))
              .set(pages.returns.DeclareAlcoholDutyQuestionPage, true)
              .success
              .value
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the Alcohol to declare to task list page if the answer is No" in {

          navigator.nextPage(
            pages.returns.DeclareAlcoholDutyQuestionPage,
            NormalMode,
            emptyUserAnswers.set(pages.returns.DeclareAlcoholDutyQuestionPage, false).success.value
          ) mustBe routes.TaskListController.onPageLoad
        }
      }

      "nextPageWithRegime" - {
        "must go from a page that doesn't exist in the route map to the Task List page" in {
          case object UnknownPage extends Page
          navigator.nextPageWithRegime(
            UnknownPage,
            NormalMode,
            emptyUserAnswers,
            regime
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

        "must go from WhatDoYouNeedToDeclare page to the Task List page if the user hasn't selected any entries" in {
          navigator.nextPageWithRegime(
            pages.returns.WhatDoYouNeedToDeclarePage,
            NormalMode,
            emptyUserAnswers,
            regime
          ) mustBe routes.TaskListController.onPageLoad
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

        "must go from HowMuchDoYouNeedToDeclare page to the Task List page if the user hasn't selected any entries" in {
          navigator.nextPageWithRegime(
            pages.returns.HowMuchDoYouNeedToDeclarePage,
            NormalMode,
            emptyUserAnswers,
            regime
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from DoYouHaveMultipleSPRDutyRates page to TellUsAboutMultipleSPRRate page if the user selected 'Yes' and the Multiple SPR list is empty" in {
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

        "must go from DoYouHaveMultipleSPRDutyRates page to TellUsAboutSingleSPRRate page if the user selected 'No'" in {
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

        "must go from DoYouHaveMultipleSPRDutyRates page to MultipleSPRList page if the user selected 'Yes' and the Multiple SPR list is not empty" in {

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

        "must go from DoYouHaveMultipleSPRDutyRates page to the Task List page if the user hasn't selected any entries" in {
          navigator.nextPageWithRegime(
            pages.returns.DoYouHaveMultipleSPRDutyRatesPage,
            NormalMode,
            emptyUserAnswers,
            regime
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from TellUsAboutMultipleSPRRate page to CheckYourAnswersSPR page if hasAnswerChanged variable is true and an index is provided" in {

          navigator.nextPageWithRegime(
            pages.returns.TellUsAboutMultipleSPRRatePage,
            NormalMode,
            emptyUserAnswers,
            regime,
            hasAnswerChanged = true,
            index = Some(0)
          ) mustBe controllers.returns.routes.CheckYourAnswersSPRController.onPageLoad(regime, index = Some(0))
        }

        "must go from TellUsAboutMultipleSPRRate page to CheckYourAnswersSPR page if no index is selected" in {
          navigator.nextPageWithRegime(
            pages.returns.TellUsAboutMultipleSPRRatePage,
            NormalMode,
            emptyUserAnswers,
            regime,
            index = None
          ) mustBe controllers.returns.routes.CheckYourAnswersSPRController.onPageLoad(regime)
        }

        "must go from TellUsAboutMultipleSPRRate page to MultipleSPRList page if no index is provided" in {
          navigator.nextPageWithRegime(
            pages.returns.TellUsAboutMultipleSPRRatePage,
            NormalMode,
            emptyUserAnswers,
            regime,
            index = Some(0)
          ) mustBe controllers.returns.routes.MultipleSPRListController.onPageLoad(regime)
        }

        "must go from TellUsAboutSingleSPRRate page to CheckYourAnswers page if no index is provided" in {
          navigator.nextPageWithRegime(
            pages.returns.TellUsAboutSingleSPRRatePage,
            NormalMode,
            emptyUserAnswers,
            regime,
            index = Some(0)
          ) mustBe controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from DoYouWantToAddMultipleSPRToList page to TellUsAboutMultipleSPRRate page if the user has selected 'true' has answer" in {
          navigator.nextPageWithRegime(
            pages.returns.DoYouWantToAddMultipleSPRToListPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.returns.DoYouWantToAddMultipleSPRToListPage, regime, true)
              .success
              .value,
            regime
          ) mustBe controllers.returns.routes.TellUsAboutMultipleSPRRateController.onPageLoad(CheckMode, regime)
        }

        "must go from DoYouWantToAddMultipleSPRToList page to CheckYourAnswers page if the user has selected 'false' has answer" in {
          navigator.nextPageWithRegime(
            pages.returns.DoYouWantToAddMultipleSPRToListPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.returns.DoYouWantToAddMultipleSPRToListPage, regime, false)
              .success
              .value,
            regime
          ) mustBe controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from DoYouWantToAddMultipleSPRToList page to TaskList page if the user hasn't selected any entries" in {
          navigator.nextPageWithRegime(
            pages.returns.DoYouWantToAddMultipleSPRToListPage,
            NormalMode,
            emptyUserAnswers,
            regime
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from DeleteMultipleSPREntry page to MultipleSPRList page if the list is not empty" in {

          val volumeAndRateByTaxType = genVolumeAndRateByTaxTypeRateBand(rateBands.head).arbitrary.sample.value

          navigator.nextPageWithRegime(
            pages.returns.DeleteMultipleSPREntryPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.returns.MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType))
              .success
              .value,
            regime
          ) mustBe controllers.returns.routes.MultipleSPRListController.onPageLoad(regime)
        }

        "must go from DeleteMultipleSPREntry page to DoYouHaveMultipleSPRDutyRates page if the list is empty" in {

          navigator.nextPageWithRegime(
            pages.returns.DeleteMultipleSPREntryPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.returns.MultipleSPRListPage, regime, Seq.empty)
              .success
              .value,
            regime
          ) mustBe controllers.returns.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
        }
      }
    }

    "in Check mode" - {

      "nextPage" - {
        "must go from a page that doesn't exist in the route map to the Task List page" in {
          case object UnknownPage extends Page
          navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswers) mustBe routes.TaskListController.onPageLoad
        }

        "must go from DeclareAlcoholDutyQuestion page to the Task List page" in {
          navigator.nextPage(
            DeclareAlcoholDutyQuestionPage,
            CheckMode,
            emptyUserAnswers.set(pages.returns.DeclareAlcoholDutyQuestionPage, true).success.value
          ) mustBe controllers.returns.routes.AlcoholTypeController.onPageLoad(CheckMode)
        }

        "must go from DeclareAlcoholDutyQuestion page to the Task List page when answer is No" in {
          navigator.nextPage(
            DeclareAlcoholDutyQuestionPage,
            CheckMode,
            emptyUserAnswers.set(pages.returns.DeclareAlcoholDutyQuestionPage, false).success.value
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the Alcohol to declare to task list page if the user has only 1 approval in CheckMode" in {

          navigator.nextPage(
            pages.returns.DeclareAlcoholDutyQuestionPage,
            CheckMode,
            emptyUserAnswers
              .copy(regimes = AlcoholRegimes(Set(Beer)))
              .set(pages.returns.DeclareAlcoholDutyQuestionPage, true)
              .success
              .value
          ) mustBe routes.TaskListController.onPageLoad
        }
      }

      "nextPageWithRegime" - {

        "must go from a page that doesn't exist in the route map to the TaskList page" in {
          case object UnknownPage extends Page
          navigator.nextPageWithRegime(
            UnknownPage,
            CheckMode,
            emptyUserAnswers,
            regime
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from WhatDoYouNeedToDeclare page to HowMuchDoYouNeedToDeclare page if the user has changed answers and selected any 'Core' or 'DraughtRelief' entries" in {
          val rateBandCore          = rateBands.head.copy(rateType = Core)
          val rateBandDraughtRelief = rateBands.last.copy(rateType = DraughtRelief)

          navigator.nextPageWithRegime(
            pages.returns.WhatDoYouNeedToDeclarePage,
            CheckMode,
            emptyUserAnswers
              .setByKey(pages.returns.WhatDoYouNeedToDeclarePage, regime, Set(rateBandCore, rateBandDraughtRelief))
              .success
              .value,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.returns.routes.HowMuchDoYouNeedToDeclareController.onPageLoad(NormalMode, regime)
        }

        "must go from WhatDoYouNeedToDeclare page to DoYouHaveMultipleSPRDutyRates page if the user has changed answers selected any 'SmallProducerRelief' or 'DraughtAndSmallProducerRelief' entries" in {
          val rateBandSPR           = rateBands.head.copy(rateType = SmallProducerRelief)
          val rateBandDraughtAndSPR = rateBands.last.copy(rateType = DraughtAndSmallProducerRelief)

          navigator.nextPageWithRegime(
            pages.returns.WhatDoYouNeedToDeclarePage,
            CheckMode,
            emptyUserAnswers
              .setByKey(pages.returns.WhatDoYouNeedToDeclarePage, regime, Set(rateBandSPR, rateBandDraughtAndSPR))
              .success
              .value,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.returns.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
        }

        "must go from WhatDoYouNeedToDeclare page to Check Your Answers page if the user did not change answers" in {
          navigator.nextPageWithRegime(
            pages.returns.WhatDoYouNeedToDeclarePage,
            CheckMode,
            emptyUserAnswers,
            regime
          ) mustBe controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from HowMuchDoYouNeedToDeclare page to Check Your Answers if the user has changed answers" in {
          navigator.nextPageWithRegime(
            pages.returns.HowMuchDoYouNeedToDeclarePage,
            CheckMode,
            emptyUserAnswers,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from HowMuchDoYouNeedToDeclare page to Check Your Answers if the user did not change answers" in {
          navigator.nextPageWithRegime(
            pages.returns.HowMuchDoYouNeedToDeclarePage,
            CheckMode,
            emptyUserAnswers,
            regime
          ) mustBe controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from TellUsAboutSingleSPRRate page to Check Your Answers if the user has changed answers" in {
          navigator.nextPageWithRegime(
            pages.returns.TellUsAboutSingleSPRRatePage,
            CheckMode,
            emptyUserAnswers,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from TellUsAboutSingleSPRRate page to Check Your Answers if the user did not change answers" in {
          navigator.nextPageWithRegime(
            pages.returns.TellUsAboutSingleSPRRatePage,
            CheckMode,
            emptyUserAnswers,
            regime
          ) mustBe controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from DoYouHaveMultipleSPRDutyRates page to TellUsAboutMultipleSPRRate page if the user has changed the answer and selected 'Yes' and the Multiple SPR list is empty" in {
          navigator.nextPageWithRegime(
            pages.returns.DoYouHaveMultipleSPRDutyRatesPage,
            CheckMode,
            emptyUserAnswers
              .setByKey(pages.returns.DoYouHaveMultipleSPRDutyRatesPage, regime, true)
              .success
              .value,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.returns.routes.TellUsAboutMultipleSPRRateController.onPageLoad(NormalMode, regime)
        }

        "must go from DoYouHaveMultipleSPRDutyRates page to TellUsAboutSingleSPRRate page if the user has changed the answer and selected 'No'" in {
          navigator.nextPageWithRegime(
            pages.returns.DoYouHaveMultipleSPRDutyRatesPage,
            CheckMode,
            emptyUserAnswers
              .setByKey(pages.returns.DoYouHaveMultipleSPRDutyRatesPage, regime, false)
              .success
              .value,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.returns.routes.TellUsAboutSingleSPRRateController.onPageLoad(NormalMode, regime)
        }

        "must go from DoYouHaveMultipleSPRDutyRates page to MultipleSPRList page if the user has change answer and selected 'Yes' and the Multiple SPR list is not empty" in {

          val volumeAndRateByTaxType = genVolumeAndRateByTaxTypeRateBand(rateBands.head).arbitrary.sample.value

          navigator.nextPageWithRegime(
            pages.returns.DoYouHaveMultipleSPRDutyRatesPage,
            CheckMode,
            emptyUserAnswers
              .setByKey(pages.returns.DoYouHaveMultipleSPRDutyRatesPage, regime, true)
              .success
              .value
              .setByKey(pages.returns.MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType))
              .success
              .value,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.returns.routes.MultipleSPRListController.onPageLoad(regime)
        }

        "must go from DoYouHaveMultipleSPRDutyRates page to the Check Your Answers page if the user not change the answer" in {
          navigator.nextPageWithRegime(
            pages.returns.DoYouHaveMultipleSPRDutyRatesPage,
            CheckMode,
            emptyUserAnswers,
            regime
          ) mustBe controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from TellUsAboutMultipleSPRRate page to CheckYourAnswersSPR page if hasAnswerChanged variable is true and an index is provided" in {

          navigator.nextPageWithRegime(
            pages.returns.TellUsAboutMultipleSPRRatePage,
            CheckMode,
            emptyUserAnswers,
            regime,
            hasAnswerChanged = true,
            index = Some(0)
          ) mustBe controllers.returns.routes.CheckYourAnswersSPRController.onPageLoad(regime, index = Some(0))
        }

        "must go from TellUsAboutMultipleSPRRate page to CheckYourAnswers page if no index is selected" in {
          navigator.nextPageWithRegime(
            pages.returns.TellUsAboutMultipleSPRRatePage,
            CheckMode,
            emptyUserAnswers,
            regime,
            index = None
          ) mustBe controllers.returns.routes.MultipleSPRListController.onPageLoad(regime)
        }

        "must go from TellUsAboutMultipleSPRRate page to MultipleSPRList page if no index is provided" in {
          navigator.nextPageWithRegime(
            pages.returns.TellUsAboutMultipleSPRRatePage,
            CheckMode,
            emptyUserAnswers,
            regime,
            index = Some(0)
          ) mustBe controllers.returns.routes.MultipleSPRListController.onPageLoad(regime)
        }
      }
    }
  }
}
