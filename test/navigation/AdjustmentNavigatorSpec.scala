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
import cats.data.NonEmptySeq
import controllers._
import models.RateType.{Core, DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}
import pages._
import models._
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.{RepackagedDraughtProducts, Spoilt}
import pages.adjustment.{AdjustmentListPage, DeclareAdjustmentQuestionPage}

class AdjustmentNavigatorSpec extends SpecBase {

  val navigator = new AdjustmentNavigator
  val rateBand  = RateBand(
    "310",
    "some band",
    RateType.DraughtRelief,
    Some(BigDecimal(10.99)),
    Set(
      RangeDetailsByRegime(
        AlcoholRegime.Beer,
        NonEmptySeq.one(
          ABVRange(
            AlcoholType.Beer,
            AlcoholByVolume(0.1),
            AlcoholByVolume(5.8)
          )
        )
      )
    )
  )

  "AdjustmentNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers) mustBe routes.IndexController.onPageLoad
      }

      "must go from the Adjustment Question Page to Adjustment Type page if the answer is Yes" in {

        navigator.nextPage(
          pages.adjustment.DeclareAdjustmentQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.adjustment.DeclareAdjustmentQuestionPage, true).success.value
        ) mustBe controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode)
      }

      "must go from the Adjustment to declare to Adjustment Type page if the answer is Yes" in {

        navigator.nextPage(
          DeclareAdjustmentQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.adjustment.DeclareAdjustmentQuestionPage, true).success.value
        ) mustBe controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode)
      }

      "must go from the Alcohol to declare to task list page if the answer is No" in {

        navigator.nextPage(
          DeclareAdjustmentQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.adjustment.DeclareAdjustmentQuestionPage, false).success.value
        ) mustBe routes.TaskListController.onPageLoad
      }

      "must go from Adjustment Type Page to WhenDidYouPayDutyController" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentTypePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe
          controllers.adjustment.routes.WhenDidYouPayDutyController.onPageLoad(NormalMode)
      }

      "must go from WhenDidYouPayDuty Page to AdjustmentTaxTypeController" in {

        navigator.nextPage(
          pages.adjustment.WhenDidYouPayDutyPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe
          controllers.adjustment.routes.AdjustmentTaxTypeController.onPageLoad(NormalMode)
      }

      "must go from the Adjustment Tax Type page to Adjustment Volume Page if RateType is Core" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentTaxTypePage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(rateBand = Some(rateBand.copy(rateType = Core)))
            )
            .success
            .value
        ) mustBe controllers.adjustment.routes.AdjustmentVolumeController.onPageLoad(NormalMode)
      }

      "must go from the Adjustment Tax Type page to Adjustment Volume Page if RateType is DraughtRelief" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentTaxTypePage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(rateBand = Some(rateBand))
            )
            .success
            .value
        ) mustBe controllers.adjustment.routes.AdjustmentVolumeController.onPageLoad(NormalMode)
      }

      "must go from the Adjustment Tax Type page to Adjustment Volume with Small Producer Relief Duty Rate Page if RateType is SmallProducerRelief" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentTaxTypePage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(rateBand = Some(rateBand.copy(rateType = SmallProducerRelief)))
            )
            .success
            .value
        ) mustBe controllers.adjustment.routes.AdjustmentVolumeWithSPRController.onPageLoad(NormalMode)
      }

      "must go from the Adjustment Tax Type page to Adjustment Volume With SPR Page if RateType is DraughtAndSmallProducerRelief" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentTaxTypePage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(rateBand = Some(rateBand.copy(rateType = DraughtAndSmallProducerRelief)))
            )
            .success
            .value
        ) mustBe controllers.adjustment.routes.AdjustmentVolumeWithSPRController.onPageLoad(NormalMode)
      }

      "must go from the Adjustment Volume With SPR Page if adjustmentType is RepackagedDraughtProducts to AdjustmentRepackagedTaxTypeController" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentVolumeWithSPRPage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(adjustmentType = Some(RepackagedDraughtProducts))
            )
            .success
            .value
        ) mustBe controllers.adjustment.routes.AdjustmentRepackagedTaxTypeController.onPageLoad(NormalMode)
      }

      "must go from the Repackaged Tax Type page to AdjustmentSmallProducerReliefDutyRateController if RateType is DraughtAndSmallProducerRelief" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentRepackagedTaxTypePage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(repackagedRateBand = Some(rateBand.copy(rateType = DraughtAndSmallProducerRelief)))
            )
            .success
            .value
        ) mustBe controllers.adjustment.routes.AdjustmentSmallProducerReliefDutyRateController.onPageLoad(NormalMode)
      }

      "must go from the Repackaged Tax Type page to AdjustmentSmallProducerReliefDutyRateController if RateType is SmallProducerRelief" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentRepackagedTaxTypePage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(repackagedRateBand = Some(rateBand.copy(rateType = SmallProducerRelief)))
            )
            .success
            .value
        ) mustBe controllers.adjustment.routes.AdjustmentSmallProducerReliefDutyRateController.onPageLoad(NormalMode)
      }

      "must go from the Repackaged Tax Type page to AdjustmentSmallProducerReliefDutyRateController if RateType is DraughtRelief" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentRepackagedTaxTypePage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(repackagedRateBand = Some(rateBand.copy(rateType = DraughtRelief)))
            )
            .success
            .value
        ) mustBe controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad()
      }

      "must go from the Small Producer Relief Duty Rate Page to Duty Due page" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentSmallProducerReliefDutyRatePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad()
      }

      "must go from the Adjustment Volume page to Adjustment Duty Due Page" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentVolumePage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(adjustmentType = Some(Spoilt))
            )
            .success
            .value
        ) mustBe controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad()
      }

      "must go from the AdjustmentListPage to task list page if the answer is No" in {

        navigator.nextPage(
          AdjustmentListPage,
          NormalMode,
          emptyUserAnswers.set(pages.adjustment.AdjustmentListPage, false).success.value
        ) mustBe routes.TaskListController.onPageLoad
      }

      "must go from the AdjustmentListPage to task list page if the answer is Yes" in {

        navigator.nextPage(
          AdjustmentListPage,
          NormalMode,
          emptyUserAnswers.set(pages.adjustment.AdjustmentListPage, true).success.value
        ) mustBe controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode)
      }
    }

    "in Check mode" - {

      "must go from the Adjustment to declare to Adjustment Type page if the answer is Yes" in {

        navigator.nextPage(
          DeclareAdjustmentQuestionPage,
          CheckMode,
          emptyUserAnswers.set(pages.adjustment.DeclareAdjustmentQuestionPage, true).success.value,
          true
        ) mustBe controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode)
      }

      "must go from the Alcohol to declare to task list page if the answer is No" in {

        navigator.nextPage(
          DeclareAdjustmentQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.adjustment.DeclareAdjustmentQuestionPage, false).success.value,
          true
        ) mustBe routes.TaskListController.onPageLoad
      }

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from Adjustment Type Page to WhenDidYouPayDutyController if answer has changed" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentTypePage,
          CheckMode,
          emptyUserAnswers,
          true
        ) mustBe
          controllers.adjustment.routes.WhenDidYouPayDutyController.onPageLoad(CheckMode)
      }

      "must go from Adjustment Type Page to CheckYourAnswersController if answer is the same" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentTypePage,
          CheckMode,
          emptyUserAnswers,
          false
        ) mustBe
          controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from WhenDidYouPayDuty Page to AdjustmentTaxTypeController if answer has changed" in {

        navigator.nextPage(
          pages.adjustment.WhenDidYouPayDutyPage,
          NormalMode,
          emptyUserAnswers,
          true
        ) mustBe
          controllers.adjustment.routes.AdjustmentTaxTypeController.onPageLoad(NormalMode)
      }

      "must go from WhenDidYouPayDuty Page to CheckYourAnswersController if answer is the same" in {

        navigator.nextPage(
          pages.adjustment.WhenDidYouPayDutyPage,
          CheckMode,
          emptyUserAnswers,
          false
        ) mustBe
          controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from the Adjustment Tax Type page to Adjustment Volume Page if RateType is Core and if answer has changed" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentTaxTypePage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(rateBand = Some(rateBand.copy(rateType = Core)))
            )
            .success
            .value,
          true
        ) mustBe controllers.adjustment.routes.AdjustmentVolumeController.onPageLoad(NormalMode)
      }

      "must go from the Adjustment Tax Type page to CheckYourAnswersController if RateType is Core and if answer is the same" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentTaxTypePage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(rateBand = Some(rateBand.copy(rateType = Core)))
            )
            .success
            .value,
          false
        ) mustBe controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
      }
      "must go from the Adjustment Tax Type page to Adjustment Volume Page if RateType is SmallProducerRelief and if answer has changed" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentTaxTypePage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(rateBand = Some(rateBand.copy(rateType = SmallProducerRelief)))
            )
            .success
            .value,
          true
        ) mustBe controllers.adjustment.routes.AdjustmentVolumeWithSPRController.onPageLoad(NormalMode)
      }

      "must go from the Adjustment Tax Type page to CheckYourAnswersController if RateType is SmallProducerRelief and if answer is the same" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentTaxTypePage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(rateBand = Some(rateBand.copy(rateType = SmallProducerRelief)))
            )
            .success
            .value,
          false
        ) mustBe controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from WhenDidYouPayDuty Page to AdjustmentTaxTypeController and if answer has changed" in {

        navigator.nextPage(
          pages.adjustment.WhenDidYouPayDutyPage,
          CheckMode,
          emptyUserAnswers,
          true
        ) mustBe
          controllers.adjustment.routes.AdjustmentTaxTypeController.onPageLoad(CheckMode)
      }

      "must go from WhenDidYouPayDuty Page to CheckYourAnswersController and if answer is the same" in {

        navigator.nextPage(
          pages.adjustment.WhenDidYouPayDutyPage,
          CheckMode,
          emptyUserAnswers,
          false
        ) mustBe
          controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from the Adjustment Volume page to Adjustment Duty Due Page and if answer has changed" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentVolumePage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(adjustmentType = Some(Spoilt))
            )
            .success
            .value,
          true
        ) mustBe controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad()
      }

      "must go from the Adjustment Volume page to CheckYourAnswersController and if answer is the same" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentVolumePage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(adjustmentType = Some(Spoilt))
            )
            .success
            .value,
          false
        ) mustBe controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from the Adjustment Volume With SPR Page if adjustmentType is Core to AdjustmentDutyDueController if answer has changed" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentVolumeWithSPRPage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(adjustmentType = Some(RepackagedDraughtProducts))
            )
            .success
            .value,
          true
        ) mustBe controllers.adjustment.routes.AdjustmentRepackagedTaxTypeController.onPageLoad(NormalMode)
      }

      "must go from the Adjustment Volume With SPR Page if adjustmentType is Core to CheckYourAnswersController and if answer is the same" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentVolumeWithSPRPage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(adjustmentType = Some(RepackagedDraughtProducts))
            )
            .success
            .value,
          false
        ) mustBe controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from the Repackaged Tax Type page to AdjustmentSmallProducerReliefDutyRateController if RateType is DraughtAndSmallProducerRelief if answer has changed" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentRepackagedTaxTypePage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(repackagedRateBand = Some(rateBand.copy(rateType = DraughtAndSmallProducerRelief)))
            )
            .success
            .value,
          true
        ) mustBe controllers.adjustment.routes.AdjustmentSmallProducerReliefDutyRateController.onPageLoad(NormalMode)
      }

      "must go from the Repackaged Tax Type page to AdjustmentSmallProducerReliefDutyRateController if RateType is DraughtAndSmallProducerRelief if answer is the same" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentRepackagedTaxTypePage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(repackagedRateBand = Some(rateBand.copy(rateType = DraughtAndSmallProducerRelief)))
            )
            .success
            .value,
          false
        ) mustBe controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from the Repackaged Tax Type page to AdjustmentSmallProducerReliefDutyRateController if RateType is DraughtRelief if answer has changed" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentRepackagedTaxTypePage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(repackagedRateBand = Some(rateBand.copy(rateType = DraughtRelief)))
            )
            .success
            .value
        ) mustBe controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad()
      }

      "must go from the Repackaged Tax Type page to AdjustmentSmallProducerReliefDutyRateController if RateType is DraughtRelief if answer is the same" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentRepackagedTaxTypePage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.adjustment.CurrentAdjustmentEntryPage,
              AdjustmentEntry(repackagedRateBand = Some(rateBand.copy(rateType = DraughtRelief)))
            )
            .success
            .value,
          false
        ) mustBe controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from the Small Producer Relief Duty Rate Page to Duty Due page if answer has changed" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentSmallProducerReliefDutyRatePage,
          CheckMode,
          emptyUserAnswers,
          true
        ) mustBe controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad()
      }

      "must go from the Small Producer Relief Duty Rate Page to CheckYourAnswersController page if answer is the same" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentSmallProducerReliefDutyRatePage,
          CheckMode,
          emptyUserAnswers,
          false
        ) mustBe controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
      }

    }
  }

}
