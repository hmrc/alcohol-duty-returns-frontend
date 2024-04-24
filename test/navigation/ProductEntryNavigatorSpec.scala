/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers._
import models.RateType.{Core, DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}
import pages._
import models._
import models.productEntry.ProductEntry
import pages.productEntry.{AlcoholByVolumeQuestionPage, CurrentProductEntryPage, DeclareAlcoholDutyQuestionPage, DeclareSmallProducerReliefDutyRatePage, DraughtReliefQuestionPage, ProductNamePage, ProductVolumePage, SmallProducerReliefQuestionPage, TaxTypePage}

class ProductEntryNavigatorSpec extends SpecBase {

  val navigator = new ProductEntryNavigator

  "ProductEntryNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers) mustBe routes.IndexController.onPageLoad
      }

      "must go from the Alcohol to declare to Product Entry Guidance page if the answer is Yes" in {

        navigator.nextPage(
          pages.productEntry.DeclareAlcoholDutyQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.productEntry.DeclareAlcoholDutyQuestionPage, true).success.value
        ) mustBe controllers.productEntry.routes.ProductEntryGuidanceController.onPageLoad()
      }

      "must go from the Alcohol to declare to task list page if the answer is No" in {

        navigator.nextPage(
          pages.productEntry.DeclareAlcoholDutyQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.productEntry.DeclareAlcoholDutyQuestionPage, false).success.value
        ) mustBe routes.TaskListController.onPageLoad
      }

      "must go from the Alcohol to declare to Journey Recovery page if there is no answer" in {

        navigator.nextPage(
          pages.productEntry.DeclareAlcoholDutyQuestionPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from Product name page to Alcohol by volume page" in {

        navigator.nextPage(
          pages.productEntry.ProductNamePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.productEntry.routes.AlcoholByVolumeQuestionController.onPageLoad(NormalMode)
      }

      "must go from Alcohol by volume page to Draught relief question page" in {

        navigator.nextPage(
          pages.productEntry.AlcoholByVolumeQuestionPage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.productEntry.CurrentProductEntryPage,
              ProductEntry(rateType = Some(DraughtRelief))
            )
            .success
            .value
        ) mustBe controllers.productEntry.routes.DraughtReliefQuestionController.onPageLoad(NormalMode)
      }

      "must go from Alcohol by volume page to Tax Type page if RateType is Core" in {

        navigator.nextPage(
          pages.productEntry.AlcoholByVolumeQuestionPage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.productEntry.CurrentProductEntryPage,
              ProductEntry(rateType = Some(Core))
            )
            .success
            .value
        ) mustBe controllers.productEntry.routes.TaxTypeController.onPageLoad(NormalMode)
      }

      "must go from Alcohol by volume page to Draught Relief page if RateType is DraughtAndSmallProducerRelief" in {

        navigator.nextPage(
          pages.productEntry.AlcoholByVolumeQuestionPage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.productEntry.CurrentProductEntryPage,
              ProductEntry(rateType = Some(DraughtAndSmallProducerRelief))
            )
            .success
            .value
        ) mustBe controllers.productEntry.routes.DraughtReliefQuestionController.onPageLoad(NormalMode)
      }

      "must go from Alcohol by volume page to Small Producer Relief page if RateType is SmallProducerRelief" in {

        navigator.nextPage(
          pages.productEntry.AlcoholByVolumeQuestionPage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.productEntry.CurrentProductEntryPage,
              ProductEntry(rateType = Some(SmallProducerRelief))
            )
            .success
            .value
        ) mustBe controllers.productEntry.routes.SmallProducerReliefQuestionController.onPageLoad(NormalMode)
      }

      "must go from the Alcohol by volume page to Journey Recovery page if the rate type is missing" in {

        navigator.nextPage(
          pages.productEntry.AlcoholByVolumeQuestionPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from the Draught relief question page to Small producer relief question page" in {

        navigator.nextPage(
          pages.productEntry.DraughtReliefQuestionPage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.productEntry.CurrentProductEntryPage,
              ProductEntry(rateType = Some(DraughtAndSmallProducerRelief))
            )
            .success
            .value
        ) mustBe controllers.productEntry.routes.SmallProducerReliefQuestionController.onPageLoad(NormalMode)
      }

      "must go from the Draught relief question page to Tax type page if RateType is DraughtRelief" in {

        navigator.nextPage(
          pages.productEntry.DraughtReliefQuestionPage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.productEntry.CurrentProductEntryPage,
              ProductEntry(rateType = Some(DraughtRelief))
            )
            .success
            .value
        ) mustBe controllers.productEntry.routes.TaxTypeController.onPageLoad(NormalMode)
      }

      "must go from the Draught relief question page to Journey Recovery page if the rate type is incorrect" in {

        navigator.nextPage(
          pages.productEntry.DraughtReliefQuestionPage,
          NormalMode,
          emptyUserAnswers
            .set(
              pages.productEntry.CurrentProductEntryPage,
              ProductEntry(rateType = Some(SmallProducerRelief))
            )
            .success
            .value
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from the Small producer relief question page to Tax Type page" in {

        navigator.nextPage(
          pages.productEntry.SmallProducerReliefQuestionPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.productEntry.routes.TaxTypeController.onPageLoad(NormalMode)
      }

      "must go from the Tax type page to Product volume page if the trader is not eligible for Small Producer Relief" in {

        navigator.nextPage(
          pages.productEntry.TaxTypePage,
          NormalMode,
          emptyUserAnswers
            .set(pages.productEntry.CurrentProductEntryPage, ProductEntry(smallProducerRelief = Some(false)))
            .success
            .value
        ) mustBe controllers.productEntry.routes.ProductVolumeController.onPageLoad(NormalMode)
      }

      "must go from the Tax type page to SPR Duty Rate entry page if the trader is eligible for Small Producer Relief" in {

        navigator.nextPage(
          pages.productEntry.TaxTypePage,
          NormalMode,
          emptyUserAnswers
            .set(pages.productEntry.CurrentProductEntryPage, ProductEntry(smallProducerRelief = Some(true)))
            .success
            .value
        ) mustBe controllers.productEntry.routes.DeclareSmallProducerReliefDutyRateController.onPageLoad(NormalMode)
      }

      "must go from the Declare SPR Duty Rate entry page to the Product Volume page" in {

        navigator.nextPage(
          pages.productEntry.DeclareSmallProducerReliefDutyRatePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.productEntry.routes.ProductVolumeController.onPageLoad(NormalMode)
      }

      "must go from the Product Volume page to Duty Due page" in {

        navigator.nextPage(
          pages.productEntry.ProductVolumePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.productEntry.routes.DutyDueController.onPageLoad()
      }

      "must go from the Product List Page page to Product Name page if the Answer is Yes" in {
        val userAnswers = emptyUserAnswers.set(pages.productEntry.ProductListPage, true).success.value
        navigator.nextPage(
          pages.productEntry.ProductListPage,
          NormalMode,
          userAnswers
        ) mustBe controllers.productEntry.routes.ProductNameController.onPageLoad(NormalMode)
      }

      "must go from the Product List Page page to task list page if the Answer is No" in {
        val userAnswers = emptyUserAnswers.set(pages.productEntry.ProductListPage, false).success.value
        navigator.nextPage(
          pages.productEntry.ProductListPage,
          NormalMode,
          userAnswers
        ) mustBe routes.IndexController.onPageLoad
      }

      "must go from the Journey Recovery if there is no Answer for the Product List page" in {
        navigator.nextPage(
          pages.productEntry.ProductListPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from Declare Alcohol Duty page to TaskList in Check Mode if the answer has not changed" in {
        navigator.nextPage(
          DeclareAlcoholDutyQuestionPage,
          CheckMode,
          emptyUserAnswers,
          false
        ) mustBe controllers.routes.TaskListController.onPageLoad
      }

      "must go from Declare Alcohol Duty page to TaskList in Check Mode if the answer has changed from true to false" in {
        navigator.nextPage(
          DeclareAlcoholDutyQuestionPage,
          CheckMode,
          emptyUserAnswers
            .set(
              DeclareAlcoholDutyQuestionPage,
              false
            )
            .success
            .value,
          true
        ) mustBe controllers.routes.TaskListController.onPageLoad
      }

      "must go from Declare Alcohol Duty page to Guidance in Check Mode if the answer has changed from false to true" in {
        navigator.nextPage(
          DeclareAlcoholDutyQuestionPage,
          CheckMode,
          emptyUserAnswers
            .set(
              DeclareAlcoholDutyQuestionPage,
              true
            )
            .success
            .value,
          true
        ) mustBe controllers.productEntry.routes.ProductEntryGuidanceController.onPageLoad
      }

      "must go from Product Name page to Check Your Answers Controller irrespective of answer" in {
        navigator.nextPage(
          ProductNamePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from Alcohol by volume page to Draught relief question page if answer has changed" in {
        navigator.nextPage(
          AlcoholByVolumeQuestionPage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.productEntry.CurrentProductEntryPage,
              ProductEntry(rateType = Some(DraughtRelief))
            )
            .success
            .value,
          true
        ) mustBe controllers.productEntry.routes.DraughtReliefQuestionController.onPageLoad(NormalMode)
      }

      "must go from Alcohol by volume page to Tax Type page if answer has changed and if RateType is Core" in {
        navigator.nextPage(
          AlcoholByVolumeQuestionPage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.productEntry.CurrentProductEntryPage,
              ProductEntry(rateType = Some(Core))
            )
            .success
            .value,
          true
        ) mustBe controllers.productEntry.routes.TaxTypeController.onPageLoad(NormalMode)
      }

      "must go from Alcohol by volume page to CheckYourAnswersController if answer is the same" in {
        navigator.nextPage(
          AlcoholByVolumeQuestionPage,
          CheckMode,
          emptyUserAnswers,
          false
        ) mustBe controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from Draught relief question page to Small producer relief if answer has changed" in {
        navigator.nextPage(
          DraughtReliefQuestionPage,
          CheckMode,
          emptyUserAnswers
            .set(
              pages.productEntry.CurrentProductEntryPage,
              ProductEntry(rateType = Some(DraughtAndSmallProducerRelief))
            )
            .success
            .value,
          true
        ) mustBe controllers.productEntry.routes.SmallProducerReliefQuestionController.onPageLoad(NormalMode)
      }

      "must go from Draught relief question page to CheckYourAnswersController if answer is the same" in {
        navigator.nextPage(
          DraughtReliefQuestionPage,
          CheckMode,
          emptyUserAnswers,
          false
        ) mustBe controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from Small producer relief to Tax Type page if answer has changed" in {
        navigator.nextPage(
          SmallProducerReliefQuestionPage,
          CheckMode,
          emptyUserAnswers,
          true
        ) mustBe controllers.productEntry.routes.TaxTypeController.onPageLoad(NormalMode)
      }

      "must go from Small relief question page to CheckYourAnswersController if answer is the same" in {
        navigator.nextPage(
          SmallProducerReliefQuestionPage,
          CheckMode,
          emptyUserAnswers,
          false
        ) mustBe controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from Tax Type page to Product Volume page if answer has changed and SPR relief is not applicable" in {
        navigator.nextPage(
          TaxTypePage,
          CheckMode,
          emptyUserAnswers
            .set(
              CurrentProductEntryPage,
              ProductEntry(
                draughtRelief = Some(true),
                smallProducerRelief = Some(false)
              )
            )
            .success
            .value,
          true
        ) mustBe controllers.productEntry.routes.ProductVolumeController.onPageLoad(NormalMode)
      }

      "must go from Tax Type page to SPR Duty rate page if answer has changed and SPR relief is applicable" in {
        navigator.nextPage(
          TaxTypePage,
          CheckMode,
          emptyUserAnswers
            .set(
              CurrentProductEntryPage,
              ProductEntry(
                draughtRelief = Some(true),
                smallProducerRelief = Some(true)
              )
            )
            .success
            .value,
          true
        ) mustBe controllers.productEntry.routes.DeclareSmallProducerReliefDutyRateController.onPageLoad(NormalMode)
      }

      "must go from Tax Type page to CheckYourAnswersController if answer is the same" in {
        navigator.nextPage(
          TaxTypePage,
          CheckMode,
          emptyUserAnswers,
          false
        ) mustBe controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from Small producer duty rate page to Product Volume page if answer has changed" in {
        navigator.nextPage(
          DeclareSmallProducerReliefDutyRatePage,
          CheckMode,
          emptyUserAnswers,
          true
        ) mustBe controllers.productEntry.routes.ProductVolumeController.onPageLoad(NormalMode)
      }

      "must go from Small producer duty rate page to CheckYourAnswersController if answer is the same" in {
        navigator.nextPage(
          DeclareSmallProducerReliefDutyRatePage,
          CheckMode,
          emptyUserAnswers,
          false
        ) mustBe controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from Product Volume page to Duty Due page if answer has changed" in {
        navigator.nextPage(
          ProductVolumePage,
          CheckMode,
          emptyUserAnswers,
          true
        ) mustBe controllers.productEntry.routes.DutyDueController.onPageLoad()
      }

      "must go from Product Volume page to CheckYourAnswersController if answer is the same" in {
        navigator.nextPage(
          ProductVolumePage,
          CheckMode,
          emptyUserAnswers,
          false
        ) mustBe controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
