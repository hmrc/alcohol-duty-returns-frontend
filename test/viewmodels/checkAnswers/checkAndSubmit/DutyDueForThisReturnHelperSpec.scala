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

package viewmodels.checkAnswers.checkAndSubmit

import base.SpecBase
import cats.data.EitherT
import connectors.AlcoholDutyCalculatorConnector
import models.{NormalMode, UserAnswers}
import models.adjustment.AdjustmentDuty
import models.returns.{AdrDutySuspended, AdrDutySuspendedProduct}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.i18n.Messages
import play.api.mvc.Call
import services.checkAndSubmit.AdrReturnSubmissionService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

import scala.concurrent.Future

class DutyDueForThisReturnHelperSpec extends SpecBase {
  "DutyDueForThisReturnHelper" - {
    "should total both duty due and adjustments" in new SetUp {
      val userAnswers = declareAdjustmentTotalPage(
        declareAdjustmentQuestionPage(
          declareAlcoholDutyQuestionPage(specifyAllAlcoholDutiesUnsorted(emptyUserAnswers), true),
          true
        ),
        adjustmentTotal
      )

      when(mockCalculatorConnector.calculateTotalAdjustment(eqTo(totalDutiesAndAdjustments))(any))
        .thenReturn(Future.successful(totalDuty))

      when(mockAdrReturnSubmissionService.getDutySuspended(any())).thenReturn(emptyDutySuspendedSuccessModel)

      whenReady(dutyDueForThisReturnHelper.getDutyDueViewModel(userAnswers).value) { result =>
        result.toOption.get.totalDue mustBe totalDuty.duty
        result.toOption.get.dutiesBreakdownTable.rows.map(
          _.cells(1).content.toString.filter(c => c.isDigit || c == '.')
        ) mustBe totalDutiesAndAdjustments.map(total => f"$total%.2f")
      }
    }

    "should total duty due where no adjustments" in new SetUp {
      val userAnswers =
        declareAdjustmentQuestionPage(
          declareAlcoholDutyQuestionPage(specifyAllAlcoholDutiesUnsorted(emptyUserAnswers), true),
          false
        )

      when(mockCalculatorConnector.calculateTotalAdjustment(eqTo(totalDutiesNoAdjustments))(any))
        .thenReturn(Future.successful(totalDutyWithoutAdjustments))

      when(mockAdrReturnSubmissionService.getDutySuspended(any())).thenReturn(emptyDutySuspendedSuccessModel)

      whenReady(dutyDueForThisReturnHelper.getDutyDueViewModel(userAnswers).value) { result =>
        result.toOption.get.totalDue mustBe totalDutyWithoutAdjustments.duty
        result.toOption.get.dutiesBreakdownTable.rows.map(
          _.cells(1).content.toString.filter(c => c.isDigit || c == '.')
        ) mustBe totalDuties.map(total => f"$total%.2f") :+ ""
      }
    }

    "should return adjustments where no duty due" in new SetUp {
      val userAnswers = declareAdjustmentTotalPage(
        declareAdjustmentQuestionPage(
          declareAlcoholDutyQuestionPage(emptyUserAnswers, false),
          true
        ),
        adjustmentTotal
      )

      when(mockCalculatorConnector.calculateTotalAdjustment(eqTo(adjustmentsNoDuties))(any))
        .thenReturn(Future.successful(totalAdjustments))

      when(mockAdrReturnSubmissionService.getDutySuspended(any())).thenReturn(emptyDutySuspendedSuccessModel)

      whenReady(dutyDueForThisReturnHelper.getDutyDueViewModel(userAnswers).value) { result =>
        result.toOption.get.totalDue mustBe totalAdjustments.duty
        result.toOption.get.dutiesBreakdownTable.rows.map(
          _.cells(1).content.toString.filter(c => c.isDigit || c == '.')
        ) mustBe "" +: adjustmentsNoDuties.map(total => f"$total%.2f")
      }
    }

    "should return a You've Also Answered Table View Model" - {
      "with the correct label, declared content and redirect to the 'Duty suspended deliveries check answers' page" in new SetUp {
        val userAnswers: UserAnswers = declareAdjustmentTotalPage(
          declareAdjustmentQuestionPage(
            declareAlcoholDutyQuestionPage(emptyUserAnswers, false),
            true
          ),
          adjustmentTotal
        )

        val expectedRedirectUrl: Call =
          controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad()

        when(mockCalculatorConnector.calculateTotalAdjustment(eqTo(adjustmentsNoDuties))(any))
          .thenReturn(Future.successful(totalAdjustments))

        when(mockAdrReturnSubmissionService.getDutySuspended(any())).thenReturn(dutySuspendedDeclaredModel)

        val result = dutyDueForThisReturnHelper
          .getDutyDueViewModel(userAnswers)
          .value
          .futureValue
          .toOption
          .get
          .youveAlsoDeclaredTable

        result.rows.head.actions.head.href mustBe expectedRedirectUrl
        result.rows.head.cells(1).content mustBe Text("Declared")
      }

      "with the correct label, nothing to declare content, and redirect to the 'Do you have any duty suspended deliveries to declare?' page" in new SetUp {
        val userAnswers: UserAnswers = declareAdjustmentTotalPage(
          declareAdjustmentQuestionPage(
            declareAlcoholDutyQuestionPage(emptyUserAnswers, false),
            true
          ),
          adjustmentTotal
        )

        val expectedRedirectUrl: Call =
          controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(NormalMode)

        when(mockCalculatorConnector.calculateTotalAdjustment(eqTo(adjustmentsNoDuties))(any))
          .thenReturn(Future.successful(totalAdjustments))

        when(mockAdrReturnSubmissionService.getDutySuspended(any())).thenReturn(dutySuspendedNotDeclaredModel)

        val result = dutyDueForThisReturnHelper
          .getDutyDueViewModel(userAnswers)
          .value
          .futureValue
          .toOption
          .get
          .youveAlsoDeclaredTable

        result.rows.head.actions.head.href mustBe expectedRedirectUrl
        result.rows.head.cells(1).content mustBe Text("Nothing to declare")
      }
    }

    "should error if unable to get duties when required" in new SetUp {
      val userAnswers = declareAdjustmentTotalPage(
        declareAdjustmentQuestionPage(
          declareAlcoholDutyQuestionPage(emptyUserAnswers, true),
          true
        ),
        adjustmentTotal
      )

      whenReady(dutyDueForThisReturnHelper.getDutyDueViewModel(userAnswers).value) { result =>
        result.swap.toOption.get mustBe "Unable to get duties due when calculating duty due"
      }
    }

    "should error if unable to get adjustments when required" in new SetUp {
      val userAnswers =
        declareAdjustmentQuestionPage(
          declareAlcoholDutyQuestionPage(specifyAllAlcoholDutiesUnsorted(emptyUserAnswers), true),
          true
        )

      whenReady(dutyDueForThisReturnHelper.getDutyDueViewModel(userAnswers).value) { result =>
        result.swap.toOption.get mustBe "Unable to get adjustment totals when calculating duty due"
      }
    }

    "must return an error message when calculator call for adjustment totals fails" in new SetUp {
      val errorMessage = "Error Message"
      when(mockCalculatorConnector.calculateTotalAdjustment(any())(any())).thenReturn(
        Future.failed(new Exception(errorMessage))
      )
      val result       =
        dutyDueForThisReturnHelper.getDutyDueViewModel(fullUserAnswers)(hc, getMessages(app)).value.futureValue
      result mustBe Left(s"Failed to calculate total duty due: $errorMessage")
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val adjustmentTotal = BigDecimal(2400)

    val totalDuties               = Seq(spiritsDuty, wineDuty, ciderDuty, otherFermentedProductDuty, beerDuty).map(_.totalDuty)
    val totalDutiesNoAdjustments  = totalDuties :+ BigDecimal(0)
    val adjustmentsNoDuties       = Seq(adjustmentTotal)
    val totalDutiesAndAdjustments =
      totalDuties :+ adjustmentTotal

    val emptyDutySuspendedSuccessModel: EitherT[Future, String, AdrDutySuspended] = EitherT.rightT[Future, String](
      AdrDutySuspended(declared = false, dutySuspendedProducts = Seq.empty[AdrDutySuspendedProduct])
    )
    val dutySuspendedDeclaredModel: EitherT[Future, String, AdrDutySuspended]     = EitherT.rightT[Future, String](
      AdrDutySuspended(declared = true, dutySuspendedProducts = Seq.empty[AdrDutySuspendedProduct])
    )
    val dutySuspendedNotDeclaredModel: EitherT[Future, String, AdrDutySuspended]  = EitherT.rightT[Future, String](
      AdrDutySuspended(declared = false, dutySuspendedProducts = Seq.empty[AdrDutySuspendedProduct])
    )
    val totalDutyWithoutAdjustments                                               = AdjustmentDuty(totalDuties.sum)
    val totalAdjustments                                                          = AdjustmentDuty(adjustmentsNoDuties.sum)
    val totalDuty                                                                 = AdjustmentDuty(totalDutiesAndAdjustments.sum)

    val mockCalculatorConnector        = mock[AlcoholDutyCalculatorConnector]
    val mockAdrReturnSubmissionService = mock[AdrReturnSubmissionService]
    val dutyDueForThisReturnHelper     =
      new DutyDueForThisReturnHelper(mockCalculatorConnector, mockAdrReturnSubmissionService)
  }
}
