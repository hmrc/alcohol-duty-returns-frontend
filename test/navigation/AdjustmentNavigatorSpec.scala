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
import models.RateType.{Core, DraughtAndSmallProducerRelief, SmallProducerRelief}
import pages._
import models._
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.Spoilt

class AdjustmentNavigatorSpec extends SpecBase {

  val navigator = new AdjustmentNavigator
  val rateBand  = RateBand(
    "310",
    "some band",
    RateType.DraughtRelief,
    Set(
      AlcoholRegime(
        AlcoholRegimeName.Beer,
        NonEmptySeq.one(
          ABVRange(
            ABVRangeName.Beer,
            AlcoholByVolume(0.1),
            AlcoholByVolume(5.8)
          )
        )
      )
    ),
    Some(BigDecimal(10.99))
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

      "must go from Adjustment Type Page to WhenDidYouPayDutyController" in {

        navigator.nextPage(
          pages.adjustment.AdjustmentTypePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe
          controllers.adjustment.routes.WhenDidYouPayDutyController.onPageLoad(NormalMode)
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

      "must go from the Adjustment Tax Type page to Adjustment Volume Rate Page if RateType is DraughtAndSmallProducerRelief" in {

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
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }

}
