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
import models.AlcoholRegime.{Beer, Cider}
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
      "must go from a page that doesn't exist in the route map to the Task List page" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers) mustBe routes.TaskListController.onPageLoad
      }

      "must go from the Adjustment Question Page to the Adjustment Type page if the answer is Yes" in {
        navigator.nextPage(
          pages.adjustment.DeclareAdjustmentQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.adjustment.DeclareAdjustmentQuestionPage, true).success.value
        ) mustBe controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode)
      }

      "must go from the Adjustment to declare to the Adjustment Type page if the answer is Yes" in {
        navigator.nextPage(
          DeclareAdjustmentQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.adjustment.DeclareAdjustmentQuestionPage, true).success.value
        ) mustBe controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode)
      }

      "must go from the Alcohol to declare to the task list page if the answer is No" in {
        navigator.nextPage(
          DeclareAdjustmentQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.adjustment.DeclareAdjustmentQuestionPage, false).success.value
        ) mustBe routes.TaskListController.onPageLoad
      }

      "must go from the Alcohol to declare to the Journey recovery page if the answer is not found" in {
        navigator.nextPage(
          DeclareAdjustmentQuestionPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from the Adjustment Type Page to the Alcoholic Product Type page if user has more than 1 approval" in {
        navigator.nextPage(
          pages.adjustment.AdjustmentTypePage,
          NormalMode,
          emptyUserAnswers
            .copy(regimes = AlcoholRegimes(Set(Beer, Cider)))
            .set(pages.adjustment.CurrentAdjustmentEntryPage, AdjustmentEntry(adjustmentType = Some(Spoilt)))
            .success
            .value
        ) mustBe
          controllers.adjustment.routes.AlcoholicProductTypeController.onPageLoad(NormalMode)
      }

      "must go from the Adjustment Type Page to the Spoilt Volume With Duty page if user has only 1 approval" in {
        navigator.nextPage(
          pages.adjustment.AdjustmentTypePage,
          NormalMode,
          emptyUserAnswers
            .copy(regimes = AlcoholRegimes(Set(Beer)))
            .set(pages.adjustment.CurrentAdjustmentEntryPage, AdjustmentEntry(adjustmentType = Some(Spoilt)))
            .success
            .value
        ) mustBe
          controllers.adjustment.routes.SpoiltVolumeWithDutyController.onPageLoad(NormalMode)
      }

      "must go from the Adjustment Type Page to the When Did You Pay Duty page" in {
        navigator.nextPage(
          pages.adjustment.AdjustmentTypePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe
          controllers.adjustment.routes.WhenDidYouPayDutyController.onPageLoad(NormalMode)
      }

      "must go from the Alcoholic Product Type Page to the Spoilt Volume page" in {
        navigator.nextPage(
          pages.adjustment.AlcoholicProductTypePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe
          controllers.adjustment.routes.SpoiltVolumeWithDutyController.onPageLoad(NormalMode)
      }

      "must go from the When Did You Pay Duty Page to Adjustment Tax Type page" in {
        navigator.nextPage(
          pages.adjustment.WhenDidYouPayDutyPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe
          controllers.adjustment.routes.AdjustmentTaxTypeController.onPageLoad(NormalMode)
      }

      "must go from the Adjustment Tax Type page to the Adjustment Volume Page if RateType is Core" in {
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

      "must go from the Adjustment Tax Type page to the Adjustment Volume Page if RateType is DraughtRelief" in {
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

      "must go from the Adjustment Tax Type page to the Adjustment Volume with Small Producer Relief Duty Rate Page if RateType is SmallProducerRelief" in {
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

      "must go from the Adjustment Tax Type page to the Adjustment Volume With SPR Page if RateType is DraughtAndSmallProducerRelief" in {
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

      "must go from the Adjustment Volume With SPR Page if adjustmentType is RepackagedDraughtProducts to the Adjustment Repackaged Tax Type page" in {
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

      "must go from the Repackaged Tax Type page to the Adjustment Small Producer Relief Duty Rate page if RateType is DraughtAndSmallProducerRelief" in {
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

      "must go from the Repackaged Tax Type page to the Adjustment Small Producer Relief Duty Rate page if RateType is SmallProducerRelief" in {
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

      "must go from the Repackaged Tax Type page to the Adjustment Small Producer Relief Duty Rate page if RateType is DraughtRelief" in {
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

      "must go from the Small Producer Relief Duty Rate page to the Duty Due page" in {
        navigator.nextPage(
          pages.adjustment.AdjustmentSmallProducerReliefDutyRatePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad()
      }

      "must go from the Adjustment Volume page to the Adjustment Duty Due page" in {
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

      "must go from the Spoilt Volume page to the CYA page" in {
        navigator.nextPage(
          pages.adjustment.SpoiltVolumeWithDutyPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe
          controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from the AdjustmentListPage to the task list page if the answer is No" in {
        navigator.nextPage(
          AdjustmentListPage,
          NormalMode,
          emptyUserAnswers.set(pages.adjustment.AdjustmentListPage, false).success.value
        ) mustBe routes.TaskListController.onPageLoad
      }

      "must go from the AdjustmentListPage to the adjustment type page if the answer is Yes" in {
        navigator.nextPage(
          AdjustmentListPage,
          NormalMode,
          emptyUserAnswers.set(pages.adjustment.AdjustmentListPage, true).success.value
        ) mustBe controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode)
      }

      "must go from the AdjustmentListPage to the Journey Recovery page page if the answer is missing" in {
        navigator.nextPage(
          AdjustmentListPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    }
  }

  "in Check mode" - {
    "must go from the Adjustment to declare to the Adjustment Type page if the answer has changed" in {
      navigator.nextPage(
        DeclareAdjustmentQuestionPage,
        CheckMode,
        emptyUserAnswers.set(pages.adjustment.DeclareAdjustmentQuestionPage, true).success.value,
        true
      ) mustBe controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode)
    }

    "must go from the Alcohol to declare to the CYA page if the answer is the same" in {
      navigator.nextPage(
        DeclareAdjustmentQuestionPage,
        CheckMode,
        emptyUserAnswers.set(pages.adjustment.DeclareAdjustmentQuestionPage, true).success.value,
        false
      ) mustBe controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    }

    "must go from a page that doesn't exist in the edit route map to the CYA page" in {
      case object UnknownPage extends Page
      navigator.nextPage(
        UnknownPage,
        CheckMode,
        emptyUserAnswers
      ) mustBe controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    }

    "must go from the Adjustment Type Page to the When Did You Pay Duty page if answer has changed" in {
      navigator.nextPage(
        pages.adjustment.AdjustmentTypePage,
        CheckMode,
        emptyUserAnswers,
        true
      ) mustBe
        controllers.adjustment.routes.WhenDidYouPayDutyController.onPageLoad(CheckMode)
    }

    "must go from the Adjustment Type Page to the CYA page if answer is the same" in {
      navigator.nextPage(
        pages.adjustment.AdjustmentTypePage,
        CheckMode,
        emptyUserAnswers,
        false
      ) mustBe
        controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    }

    "must go from the Alcoholic Product Type Page to the Spoilt Volume page if answer has changed" in {
      navigator.nextPage(
        pages.adjustment.AlcoholicProductTypePage,
        CheckMode,
        emptyUserAnswers,
        true
      ) mustBe
        controllers.adjustment.routes.SpoiltVolumeWithDutyController.onPageLoad(NormalMode)
    }

    "must go from the Alcoholic Product Type Page to the CYA page if answer is the same" in {
      navigator.nextPage(
        pages.adjustment.AlcoholicProductTypePage,
        CheckMode,
        emptyUserAnswers,
        false
      ) mustBe
        controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    }

    "must go from the When Did You Pay Duty page to the Adjustment Tax Type page if answer has changed" in {
      navigator.nextPage(
        pages.adjustment.WhenDidYouPayDutyPage,
        CheckMode,
        emptyUserAnswers,
        true
      ) mustBe
        controllers.adjustment.routes.AdjustmentTaxTypeController.onPageLoad(CheckMode)
    }

    "must go from the When Did You Pay Duty Page to the CYA page if answer is the same" in {
      navigator.nextPage(
        pages.adjustment.WhenDidYouPayDutyPage,
        CheckMode,
        emptyUserAnswers,
        false
      ) mustBe
        controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    }

    "must go from the Adjustment Tax Type page to Adjustment Volume page if RateType is Core if the answer has changed" in {
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
      ) mustBe controllers.adjustment.routes.AdjustmentVolumeController.onPageLoad(CheckMode)
    }

    "must go from the Adjustment Tax Type page to the CYA page if RateType is Core if the answer is the same" in {
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

    "must go from the Adjustment Tax Type page to the Adjustment Volume page if RateType is SmallProducerRelief if the answer has changed" in {
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
      ) mustBe controllers.adjustment.routes.AdjustmentVolumeWithSPRController.onPageLoad(CheckMode)
    }

    "must go from the Spoilt Volume page to the CYA page" in {
      navigator.nextPage(
        pages.adjustment.SpoiltVolumeWithDutyPage,
        CheckMode,
        emptyUserAnswers
      ) mustBe
        controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    }
    "must go from the Adjustment Tax Type page to CYA page if RateType is SmallProducerRelief if the answer is the same" in {
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

    "must go from the When Did You Pay Duty page to the Adjustment Tax Type page if the answer has changed" in {
      navigator.nextPage(
        pages.adjustment.WhenDidYouPayDutyPage,
        CheckMode,
        emptyUserAnswers,
        true
      ) mustBe
        controllers.adjustment.routes.AdjustmentTaxTypeController.onPageLoad(CheckMode)
    }

    "must go from the When Did You Pay Duty Page to the CYA page if the answer is the same" in {
      navigator.nextPage(
        pages.adjustment.WhenDidYouPayDutyPage,
        CheckMode,
        emptyUserAnswers,
        false
      ) mustBe
        controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    }

    "must go from the Adjustment Volume page to the Adjustment Duty Due page if the answer has changed" in {
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

    "must go from the Adjustment Volume page to the CYA page if the answer is the same" in {
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

    "must go from the Adjustment Volume With SPR page if adjustmentType is Core to the Adjustment Duty Due page if answer has changed" in {
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
      ) mustBe controllers.adjustment.routes.AdjustmentRepackagedTaxTypeController.onPageLoad(CheckMode)
    }

    "must go from the Adjustment Volume With SPR Page if adjustmentType is Core to the CYA page if the answer is the same" in {
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

    "must go from the Repackaged Tax Type page to the Adjustment Small Producer Relief Duty Rate page if RateType is DraughtAndSmallProducerRelief if answer has changed" in {
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
      ) mustBe controllers.adjustment.routes.AdjustmentSmallProducerReliefDutyRateController.onPageLoad(CheckMode)
    }

    "must go from the Repackaged Tax Type page to the Adjustment Small Producer Relief Duty Rate page if RateType is DraughtAndSmallProducerRelief if answer is the same" in {
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

    "must go from the Repackaged Tax Type page to the Adjustment Small Producer Relief Duty Rate page if RateType is DraughtRelief if answer has changed" in {
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

    "must go from the Repackaged Tax Type page to the Adjustment Small Producer Relief Duty Rate page if RateType is DraughtRelief if answer is the same" in {
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

    "must go from the Small Producer Relief Duty Rate Page to the Duty Due page if answer has changed" in {
      navigator.nextPage(
        pages.adjustment.AdjustmentSmallProducerReliefDutyRatePage,
        CheckMode,
        emptyUserAnswers,
        true
      ) mustBe controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad()
    }

    "must go from the Small Producer Relief Duty Rate Page to the CYA page if answer is the same" in {
      navigator.nextPage(
        pages.adjustment.AdjustmentSmallProducerReliefDutyRatePage,
        CheckMode,
        emptyUserAnswers,
        false
      ) mustBe controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    }
  }
}
