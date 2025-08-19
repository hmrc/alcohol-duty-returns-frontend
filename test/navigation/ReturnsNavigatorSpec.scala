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
import models.AlcoholRegime._
import models.RateType.{Core, DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}
import models.{AlcoholRegime, AlcoholRegimes, CheckMode, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.scalacheck.Gen
import pages.Page
import viewmodels.declareDuty.MissingSPRRateBandHelper

class ReturnsNavigatorSpec extends SpecBase {

  val mockMissingSPRRateBandHelper = mock[MissingSPRRateBandHelper]

  val navigator = new ReturnsNavigator(mockMissingSPRRateBandHelper)
  val regime    = regimeGen.sample.value
  val rateBands = genListOfRateBandForRegime(regime).sample.value

  val regimeNotWine = Gen.oneOf(Seq(Beer, Cider, Spirits, OtherFermentedProduct)).sample.value

  "ReturnsNavigator" - {

    "in Normal mode" - {

      "nextPage" - {
        "must go from a page that doesn't exist in the route map to the Task List page" in {
          case object UnknownPage extends Page
          navigator.nextPage(
            UnknownPage,
            NormalMode,
            emptyUserAnswers,
            Some(false)
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the DeclareAlcoholDutyQuestion page to the Alcohol Type page if the answer is Yes and the user has more than 1 approval" in {
          navigator.nextPage(
            pages.declareDuty.DeclareAlcoholDutyQuestionPage,
            NormalMode,
            emptyUserAnswers.set(pages.declareDuty.DeclareAlcoholDutyQuestionPage, true).success.value,
            Some(false)
          ) mustBe controllers.declareDuty.routes.AlcoholTypeController.onPageLoad(NormalMode)
        }

        "must go from the DeclareAlcoholDutyQuestion page to the first page of the regime's journey if the user has only 1 approval (not wine)" in {
          navigator.nextPage(
            pages.declareDuty.DeclareAlcoholDutyQuestionPage,
            NormalMode,
            emptyUserAnswers
              .copy(regimes = AlcoholRegimes(Set(regimeNotWine)))
              .set(pages.declareDuty.DeclareAlcoholDutyQuestionPage, true)
              .success
              .value,
            Some(false)
          ) mustBe controllers.declareDuty.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regimeNotWine)
        }

        "must go from the DeclareAlcoholDutyQuestion page to the first page of the regime's journey if the user has only 1 approval (wine)" in {
          navigator.nextPage(
            pages.declareDuty.DeclareAlcoholDutyQuestionPage,
            NormalMode,
            emptyUserAnswers
              .copy(regimes = AlcoholRegimes(Set(Wine)))
              .set(pages.declareDuty.DeclareAlcoholDutyQuestionPage, true)
              .success
              .value,
            Some(false)
          ) mustBe controllers.declareDuty.routes.DeclaringWineDutyGuidanceController.onPageLoad()
        }

        "must go from the DeclareAlcoholDutyQuestion page to the task list page if the answer is No" in {
          navigator.nextPage(
            pages.declareDuty.DeclareAlcoholDutyQuestionPage,
            NormalMode,
            emptyUserAnswers.set(pages.declareDuty.DeclareAlcoholDutyQuestionPage, false).success.value,
            Some(false)
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the DeclareAlcoholDutyQuestion page to the Journey recovery page if the answer is missing" in {
          navigator.nextPage(
            pages.declareDuty.DeclareAlcoholDutyQuestionPage,
            NormalMode,
            emptyUserAnswers,
            Some(false)
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }

        "must go from the Alcohol Type page to the task list page if multiple regimes are selected" in {
          navigator.nextPage(
            pages.declareDuty.AlcoholTypePage,
            NormalMode,
            emptyUserAnswers.set(pages.declareDuty.AlcoholTypePage, Set[AlcoholRegime](Beer, Cider)).success.value,
            Some(false)
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the Alcohol Type page to the first page of the regime's journey if only 1 regime is selected (not wine)" in {
          navigator.nextPage(
            pages.declareDuty.AlcoholTypePage,
            NormalMode,
            emptyUserAnswers.set(pages.declareDuty.AlcoholTypePage, Set[AlcoholRegime](regimeNotWine)).success.value,
            Some(false)
          ) mustBe controllers.declareDuty.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regimeNotWine)
        }

        "must go from the Alcohol Type page to the first page of the regime's journey if only 1 regime is selected (wine)" in {
          navigator.nextPage(
            pages.declareDuty.AlcoholTypePage,
            NormalMode,
            emptyUserAnswers.set(pages.declareDuty.AlcoholTypePage, Set[AlcoholRegime](Wine)).success.value,
            Some(false)
          ) mustBe controllers.declareDuty.routes.DeclaringWineDutyGuidanceController.onPageLoad()
        }

        "must go from the Alcohol Type page to the Journey recovery page if the answer is missing" in {
          navigator.nextPage(
            pages.declareDuty.AlcoholTypePage,
            NormalMode,
            emptyUserAnswers,
            Some(false)
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
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

        "must go from the WhatDoYouNeedToDeclare page to the HowMuchDoYouNeedToDeclare page if the user selected any 'Core' or 'DraughtRelief' entries" in {
          val rateBandCore          = rateBands.head.copy(rateType = Core)
          val rateBandDraughtRelief = rateBands.last.copy(rateType = DraughtRelief)

          navigator.nextPageWithRegime(
            pages.declareDuty.WhatDoYouNeedToDeclarePage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.WhatDoYouNeedToDeclarePage, regime, Set(rateBandCore, rateBandDraughtRelief))
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.HowMuchDoYouNeedToDeclareController.onPageLoad(NormalMode, regime)
        }

        "must go from the WhatDoYouNeedToDeclare page to the DoYouHaveMultipleSPRDutyRates page if the user selected any 'SmallProducerRelief' or 'DraughtAndSmallProducerRelief' entries" in {
          val rateBandSPR           = rateBands.head.copy(rateType = SmallProducerRelief)
          val rateBandDraughtAndSPR = rateBands.last.copy(rateType = DraughtAndSmallProducerRelief)

          navigator.nextPageWithRegime(
            pages.declareDuty.WhatDoYouNeedToDeclarePage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.WhatDoYouNeedToDeclarePage, regime, Set(rateBandSPR, rateBandDraughtAndSPR))
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
        }

        "must go from the WhatDoYouNeedToDeclare page to the Task List page if the user hasn't selected any entries" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.WhatDoYouNeedToDeclarePage,
            NormalMode,
            emptyUserAnswers,
            regime
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the HowMuchDoYouNeedToDeclare page to the DoYouHaveMultipleSPRDutyRates page if the user selected any 'SmallProducerRelief' or 'DraughtAndSmallProducerRelief' entries" in {
          val rateBandSPR           = rateBands.head.copy(rateType = SmallProducerRelief)
          val rateBandDraughtAndSPR = rateBands.last.copy(rateType = DraughtAndSmallProducerRelief)

          navigator.nextPageWithRegime(
            pages.declareDuty.HowMuchDoYouNeedToDeclarePage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.WhatDoYouNeedToDeclarePage, regime, Set(rateBandSPR, rateBandDraughtAndSPR))
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
        }

        "must go from the HowMuchDoYouNeedToDeclare page to the Check Your Answer page if the user did not select any 'SmallProducerRelief' or 'DraughtAndSmallProducerRelief' entries" in {
          val rateBandCore          = rateBands.head.copy(rateType = Core)
          val rateBandDraughtRelief = rateBands.last.copy(rateType = DraughtRelief)

          navigator.nextPageWithRegime(
            pages.declareDuty.HowMuchDoYouNeedToDeclarePage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.WhatDoYouNeedToDeclarePage, regime, Set(rateBandCore, rateBandDraughtRelief))
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from the HowMuchDoYouNeedToDeclare page to the Task List page if the user hasn't selected any entries" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.HowMuchDoYouNeedToDeclarePage,
            NormalMode,
            emptyUserAnswers,
            regime
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the DoYouHaveMultipleSPRDutyRates page to the TellUsAboutMultipleSPRRate page if the user selected 'Yes' and the Multiple SPR list is empty" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage, regime, true)
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController.onPageLoad(NormalMode, regime)
        }

        "must go from the DoYouHaveMultipleSPRDutyRates page to the TellUsAboutSingleSPRRate page if the user selected 'No'" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage, regime, false)
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.TellUsAboutSingleSPRRateController.onPageLoad(NormalMode, regime)
        }

        "must go from the DoYouHaveMultipleSPRDutyRates page to the MultipleSPRList page if the user selected 'Yes' and the Multiple SPR list is not empty" in {
          val volumeAndRateByTaxType = genVolumeAndRateByTaxTypeRateBand(rateBands.head).arbitrary.sample.value

          navigator.nextPageWithRegime(
            pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage, regime, true)
              .success
              .value
              .setByKey(pages.declareDuty.MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType))
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(regime)
        }

        "must go from the DoYouHaveMultipleSPRDutyRates page to the Task List page if the user hasn't selected any entries" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage,
            NormalMode,
            emptyUserAnswers,
            regime
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the TellUsAboutMultipleSPRRate page to the CheckYourAnswersSPR page if hasAnswerChanged variable is true and an index is provided" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.TellUsAboutMultipleSPRRatePage,
            NormalMode,
            emptyUserAnswers,
            regime,
            hasAnswerChanged = true,
            index = Some(0)
          ) mustBe controllers.declareDuty.routes.CheckYourAnswersSPRController.onPageLoad(regime, index = Some(0))
        }

        "must go from the TellUsAboutMultipleSPRRate page to the CheckYourAnswersSPR page if no index is selected" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.TellUsAboutMultipleSPRRatePage,
            NormalMode,
            emptyUserAnswers,
            regime,
            index = None
          ) mustBe controllers.declareDuty.routes.CheckYourAnswersSPRController.onPageLoad(regime)
        }

        "must go from the TellUsAboutMultipleSPRRate page to the MultipleSPRList page if no index is provided" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.TellUsAboutMultipleSPRRatePage,
            NormalMode,
            emptyUserAnswers,
            regime,
            index = Some(0)
          ) mustBe controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(regime)
        }

        "must go from the TellUsAboutSingleSPRRate page to the CheckYourAnswers page if no index is provided" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.TellUsAboutSingleSPRRatePage,
            NormalMode,
            emptyUserAnswers,
            regime,
            index = Some(0)
          ) mustBe controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from the DoYouWantToAddMultipleSPRToList page to the TellUsAboutMultipleSPRRate page if the user has selected 'true'" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.DoYouWantToAddMultipleSPRToListPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.DoYouWantToAddMultipleSPRToListPage, regime, true)
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController.onPageLoad(CheckMode, regime)
        }

        "if the user has selected 'false' on the DoYouWantToAddMultipleSPRToList page" - {
          val userAnswers = emptyUserAnswers
            .setByKey(pages.declareDuty.DoYouWantToAddMultipleSPRToListPage, regime, false)
            .success
            .value

          "must go to the MultipleSPRMissingDetails page if there are any missing rate bands" in {
            when(mockMissingSPRRateBandHelper.findMissingSPRRateBands(eqTo(regime), any())) thenReturn Some(
              Set(smallProducerReliefRateBand)
            )

            navigator.nextPageWithRegime(
              pages.declareDuty.DoYouWantToAddMultipleSPRToListPage,
              NormalMode,
              userAnswers,
              regime
            ) mustBe controllers.declareDuty.routes.MultipleSPRMissingDetailsController.onPageLoad(regime)
          }

          "must go to the CheckYourAnswers page if there are no missing rate bands" in {
            when(mockMissingSPRRateBandHelper.findMissingSPRRateBands(eqTo(regime), any())) thenReturn Some(Set.empty)

            navigator.nextPageWithRegime(
              pages.declareDuty.DoYouWantToAddMultipleSPRToListPage,
              NormalMode,
              userAnswers,
              regime
            ) mustBe controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
          }

          "must go to the Task List page if user answers are missing required data" in {
            when(mockMissingSPRRateBandHelper.findMissingSPRRateBands(eqTo(regime), any())) thenReturn None

            navigator.nextPageWithRegime(
              pages.declareDuty.DoYouWantToAddMultipleSPRToListPage,
              NormalMode,
              userAnswers,
              regime
            ) mustBe routes.TaskListController.onPageLoad
          }
        }

        "must go from the DoYouWantToAddMultipleSPRToList page to the Task List page if the user hasn't selected any entries" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.DoYouWantToAddMultipleSPRToListPage,
            NormalMode,
            emptyUserAnswers,
            regime
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the MultipleSPRMissingDetails page to the TellUsAboutMultipleSPRRate page if the user selected to add declaration details" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.MultipleSPRMissingDetailsPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.MultipleSPRMissingDetailsPage, regime, true)
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController.onPageLoad(CheckMode, regime)
        }

        "must go from the MultipleSPRMissingDetails page to the MultipleSPRMissingDetailsConfirmation page if the user selected to delete declarations" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.MultipleSPRMissingDetailsPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.MultipleSPRMissingDetailsPage, regime, false)
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.MultipleSPRMissingDetailsConfirmationController.onPageLoad(regime)
        }

        "must go from the MultipleSPRMissingDetails page to the Task List page if the user hasn't selected any entries" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.MultipleSPRMissingDetailsPage,
            NormalMode,
            emptyUserAnswers,
            regime
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the MultipleSPRMissingDetailsConfirmation page to the CheckYourAnswers page if the user selected 'Yes'" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.MultipleSPRMissingDetailsConfirmationPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.MultipleSPRMissingDetailsConfirmationPage, regime, true)
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from the MultipleSPRMissingDetailsConfirmation page to the TellUsAboutMultipleSPRRate page if the user selected 'No'" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.MultipleSPRMissingDetailsConfirmationPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.MultipleSPRMissingDetailsConfirmationPage, regime, false)
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController.onPageLoad(CheckMode, regime)
        }

        "must go from the MultipleSPRMissingDetailsConfirmation page to the Task List page if the user hasn't selected any entries" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.MultipleSPRMissingDetailsConfirmationPage,
            NormalMode,
            emptyUserAnswers,
            regime
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the DeleteMultipleSPREntry page to the MultipleSPRList page if the list is not empty" in {
          val volumeAndRateByTaxType = genVolumeAndRateByTaxTypeRateBand(rateBands.head).arbitrary.sample.value

          navigator.nextPageWithRegime(
            pages.declareDuty.DeleteMultipleSPREntryPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType))
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(regime)
        }

        "must go from the DeleteMultipleSPREntry page to the DoYouHaveMultipleSPRDutyRates page if the list is empty" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.DeleteMultipleSPREntryPage,
            NormalMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.MultipleSPRListPage, regime, Seq.empty)
              .success
              .value,
            regime
          ) mustBe controllers.declareDuty.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
        }
      }
    }

    "in Check mode" - {

      "nextPage" - {
        "must go from a page that doesn't exist in the route map to the Task List page" in {
          case object UnknownPage extends Page
          navigator.nextPage(
            UnknownPage,
            CheckMode,
            emptyUserAnswers,
            Some(false)
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the DeclareAlcoholDutyQuestion page to the Alcohol Type page if the answer is Yes and the user has more than 1 approval" in {
          navigator.nextPage(
            pages.declareDuty.DeclareAlcoholDutyQuestionPage,
            CheckMode,
            emptyUserAnswers.set(pages.declareDuty.DeclareAlcoholDutyQuestionPage, true).success.value,
            Some(false)
          ) mustBe controllers.declareDuty.routes.AlcoholTypeController.onPageLoad(CheckMode)
        }

        "must go from the DeclareAlcoholDutyQuestion page to the first page of the regime's journey if the user has only 1 approval (not wine)" in {
          navigator.nextPage(
            pages.declareDuty.DeclareAlcoholDutyQuestionPage,
            CheckMode,
            emptyUserAnswers
              .copy(regimes = AlcoholRegimes(Set(regimeNotWine)))
              .set(pages.declareDuty.DeclareAlcoholDutyQuestionPage, true)
              .success
              .value,
            Some(false)
          ) mustBe controllers.declareDuty.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regimeNotWine)
        }

        "must go from the DeclareAlcoholDutyQuestion page to the first page of the regime's journey if the user has only 1 approval (wine)" in {
          navigator.nextPage(
            pages.declareDuty.DeclareAlcoholDutyQuestionPage,
            CheckMode,
            emptyUserAnswers
              .copy(regimes = AlcoholRegimes(Set(Wine)))
              .set(pages.declareDuty.DeclareAlcoholDutyQuestionPage, true)
              .success
              .value,
            Some(false)
          ) mustBe controllers.declareDuty.routes.DeclaringWineDutyGuidanceController.onPageLoad()
        }

        "must go from the DeclareAlcoholDutyQuestion page to the Task List page when answer is No" in {
          navigator.nextPage(
            pages.declareDuty.DeclareAlcoholDutyQuestionPage,
            CheckMode,
            emptyUserAnswers.set(pages.declareDuty.DeclareAlcoholDutyQuestionPage, false).success.value,
            Some(false)
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the DeclareAlcoholDutyQuestion page to the Journey recovery page if the answer is missing" in {
          navigator.nextPage(
            pages.declareDuty.DeclareAlcoholDutyQuestionPage,
            CheckMode,
            emptyUserAnswers,
            Some(false)
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }

        "must go from the Alcohol Type page to the task list page if multiple regimes are selected" in {
          navigator.nextPage(
            pages.declareDuty.AlcoholTypePage,
            CheckMode,
            emptyUserAnswers.set(pages.declareDuty.AlcoholTypePage, Set[AlcoholRegime](Beer, Cider)).success.value,
            Some(false)
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the Alcohol Type page to the first page of the regime's journey if only 1 regime is selected (not wine)" in {
          navigator.nextPage(
            pages.declareDuty.AlcoholTypePage,
            CheckMode,
            emptyUserAnswers.set(pages.declareDuty.AlcoholTypePage, Set[AlcoholRegime](regimeNotWine)).success.value,
            Some(false)
          ) mustBe controllers.declareDuty.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regimeNotWine)
        }

        "must go from the Alcohol Type page to the first page of the regime's journey if only 1 regime is selected (wine)" in {
          navigator.nextPage(
            pages.declareDuty.AlcoholTypePage,
            CheckMode,
            emptyUserAnswers.set(pages.declareDuty.AlcoholTypePage, Set[AlcoholRegime](Wine)).success.value,
            Some(false)
          ) mustBe controllers.declareDuty.routes.DeclaringWineDutyGuidanceController.onPageLoad()
        }

        "must go from the Alcohol Type page to the Journey recovery page if the answer is missing" in {
          navigator.nextPage(
            pages.declareDuty.AlcoholTypePage,
            CheckMode,
            emptyUserAnswers,
            Some(false)
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
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

        "must go from the WhatDoYouNeedToDeclare page to the HowMuchDoYouNeedToDeclare page if the user has changed answers and selected any 'Core' or 'DraughtRelief' entries" in {
          val rateBandCore          = rateBands.head.copy(rateType = Core)
          val rateBandDraughtRelief = rateBands.last.copy(rateType = DraughtRelief)

          navigator.nextPageWithRegime(
            pages.declareDuty.WhatDoYouNeedToDeclarePage,
            CheckMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.WhatDoYouNeedToDeclarePage, regime, Set(rateBandCore, rateBandDraughtRelief))
              .success
              .value,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.declareDuty.routes.HowMuchDoYouNeedToDeclareController.onPageLoad(NormalMode, regime)
        }

        "must go from the WhatDoYouNeedToDeclare page to the DoYouHaveMultipleSPRDutyRates page if the user has changed answers selected any 'SmallProducerRelief' or 'DraughtAndSmallProducerRelief' entries" in {
          val rateBandSPR           = rateBands.head.copy(rateType = SmallProducerRelief)
          val rateBandDraughtAndSPR = rateBands.last.copy(rateType = DraughtAndSmallProducerRelief)

          navigator.nextPageWithRegime(
            pages.declareDuty.WhatDoYouNeedToDeclarePage,
            CheckMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.WhatDoYouNeedToDeclarePage, regime, Set(rateBandSPR, rateBandDraughtAndSPR))
              .success
              .value,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.declareDuty.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
        }

        "must go from the WhatDoYouNeedToDeclare page to the Check Your Answers page if the user did not change answers" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.WhatDoYouNeedToDeclarePage,
            CheckMode,
            emptyUserAnswers,
            regime
          ) mustBe controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from the HowMuchDoYouNeedToDeclare page to the Check Your Answers if the user has changed answers" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.HowMuchDoYouNeedToDeclarePage,
            CheckMode,
            emptyUserAnswers,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from the HowMuchDoYouNeedToDeclare page to the Check Your Answers if the user did not change answers" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.HowMuchDoYouNeedToDeclarePage,
            CheckMode,
            emptyUserAnswers,
            regime
          ) mustBe controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from the TellUsAboutSingleSPRRate page to the Check Your Answers if the user has changed answers" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.TellUsAboutSingleSPRRatePage,
            CheckMode,
            emptyUserAnswers,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from the TellUsAboutSingleSPRRate page to the Check Your Answers if the user did not change answers" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.TellUsAboutSingleSPRRatePage,
            CheckMode,
            emptyUserAnswers,
            regime
          ) mustBe controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from the DoYouHaveMultipleSPRDutyRates page to the TellUsAboutMultipleSPRRate page if the user has changed the answer and selected 'Yes' and the Multiple SPR list is empty" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage,
            CheckMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage, regime, true)
              .success
              .value,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController.onPageLoad(NormalMode, regime)
        }

        "must go from the DoYouHaveMultipleSPRDutyRates page to the TellUsAboutSingleSPRRate page if the user has changed the answer and selected 'No'" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage,
            CheckMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage, regime, false)
              .success
              .value,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.declareDuty.routes.TellUsAboutSingleSPRRateController.onPageLoad(NormalMode, regime)
        }

        "must go from the DoYouHaveMultipleSPRDutyRates page to the MultipleSPRList page if the user has change answer and selected 'Yes' and the Multiple SPR list is not empty" in {

          val volumeAndRateByTaxType = genVolumeAndRateByTaxTypeRateBand(rateBands.head).arbitrary.sample.value

          navigator.nextPageWithRegime(
            pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage,
            CheckMode,
            emptyUserAnswers
              .setByKey(pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage, regime, true)
              .success
              .value
              .setByKey(pages.declareDuty.MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType))
              .success
              .value,
            regime,
            hasAnswerChanged = true
          ) mustBe controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(regime)
        }

        "must go from the DoYouHaveMultipleSPRDutyRates page to the Check Your Answers page if the user not change the answer" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage,
            CheckMode,
            emptyUserAnswers,
            regime
          ) mustBe controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
        }

        "must go from the TellUsAboutMultipleSPRRate page to the CheckYourAnswersSPR page if hasAnswerChanged variable is true and an index is provided" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.TellUsAboutMultipleSPRRatePage,
            CheckMode,
            emptyUserAnswers,
            regime,
            hasAnswerChanged = true,
            index = Some(0)
          ) mustBe controllers.declareDuty.routes.CheckYourAnswersSPRController.onPageLoad(regime, index = Some(0))
        }

        "must go from the TellUsAboutMultipleSPRRate page to the CheckYourAnswers page if no index is selected" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.TellUsAboutMultipleSPRRatePage,
            CheckMode,
            emptyUserAnswers,
            regime,
            index = None
          ) mustBe controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(regime)
        }

        "must go from the TellUsAboutMultipleSPRRate page to the MultipleSPRList page if no index is provided" in {
          navigator.nextPageWithRegime(
            pages.declareDuty.TellUsAboutMultipleSPRRatePage,
            CheckMode,
            emptyUserAnswers,
            regime,
            index = Some(0)
          ) mustBe controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(regime)
        }
      }
    }
  }
}
