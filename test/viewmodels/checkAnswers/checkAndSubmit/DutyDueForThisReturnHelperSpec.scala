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
import models.AlcoholRegime._
import models.adjustment.AdjustmentDuty
import models.checkAndSubmit.{AdrDutySuspended, AdrDutySuspendedProduct, AdrSpirits}
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import play.api.i18n.Messages
import services.checkAndSubmit.AdrReturnSubmissionService
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}

import scala.concurrent.Future

class DutyDueForThisReturnHelperSpec extends SpecBase {
  "DutyDueForThisReturnHelper" - {
    "must return a duties breakdown summary list with the correct content and duty total" - {
      "when both declarations and adjustments are present" in new SetUp {
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
        when(mockAdrReturnSubmissionService.getSpirits(any(), any())).thenReturn(emptySpiritsModel)

        val viewModel = dutyDueForThisReturnHelper
          .getDutyDueViewModel(userAnswers, returnPeriod)
          .value
          .futureValue
          .toOption
          .get

        val summaryList = viewModel.dutiesBreakdownSummaryList

        val expectedKeys = Seq(
          "Beer declared",
          "Cider declared",
          "Wine declared",
          "Spirits declared",
          "Other fermented products declared",
          "Adjustments to previous returns"
        )

        val expectedValues = List("£1,225.50", "£278.10", "£39,900.00", "£12,650.00", "£8,670.00", "£2,400.00")

        viewModel.totalDue                                    mustBe totalDuty.duty
        summaryList.rows.map(_.key.content.asHtml.toString)   mustBe expectedKeys
        summaryList.rows.map(_.value.content.asHtml.toString) mustBe expectedValues
        summaryList.rows.map(_.actions.get.items.head)        mustBe declarationActionItems :+ adjustmentsDeclaredActionItem
      }

      "when no adjustments are present" in new SetUp {
        val userAnswers =
          declareAdjustmentQuestionPage(
            declareAlcoholDutyQuestionPage(specifyAllAlcoholDutiesUnsorted(emptyUserAnswers), true),
            false
          )

        when(mockCalculatorConnector.calculateTotalAdjustment(eqTo(totalDutiesNoAdjustments))(any))
          .thenReturn(Future.successful(totalDutyWithoutAdjustments))

        when(mockAdrReturnSubmissionService.getDutySuspended(any())).thenReturn(emptyDutySuspendedSuccessModel)
        when(mockAdrReturnSubmissionService.getSpirits(any(), any())).thenReturn(emptySpiritsModel)

        val viewModel = dutyDueForThisReturnHelper
          .getDutyDueViewModel(userAnswers, returnPeriod)
          .value
          .futureValue
          .toOption
          .get

        val summaryList = viewModel.dutiesBreakdownSummaryList

        val expectedKeys = Seq(
          "Beer declared",
          "Cider declared",
          "Wine declared",
          "Spirits declared",
          "Other fermented products declared",
          "Adjustments to previous returns"
        )

        val expectedValues = List("£1,225.50", "£278.10", "£39,900.00", "£12,650.00", "£8,670.00", "Nil")

        viewModel.totalDue                                    mustBe totalDutyWithoutAdjustments.duty
        summaryList.rows.map(_.key.content.asHtml.toString)   mustBe expectedKeys
        summaryList.rows.map(_.value.content.asHtml.toString) mustBe expectedValues
        summaryList.rows.map(_.actions.get.items.head)        mustBe declarationActionItems :+ adjustmentsNotDeclaredActionItem
      }

      "when no declarations are present" in new SetUp {
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
        when(mockAdrReturnSubmissionService.getSpirits(any(), any())).thenReturn(emptySpiritsModel)

        val viewModel = dutyDueForThisReturnHelper
          .getDutyDueViewModel(userAnswers, returnPeriod)
          .value
          .futureValue
          .toOption
          .get

        val summaryList = viewModel.dutiesBreakdownSummaryList

        val expectedKeys   = Seq("Declared duty", "Adjustments to previous returns")
        val expectedValues = Seq("Nil", "£2,400.00")

        viewModel.totalDue                                    mustBe totalAdjustments.duty
        summaryList.rows.map(_.key.content.asHtml.toString)   mustBe expectedKeys
        summaryList.rows.map(_.value.content.asHtml.toString) mustBe expectedValues
        summaryList.rows.map(_.actions.get.items.head)        mustBe
          Seq(nilDeclarationActionItem, adjustmentsDeclaredActionItem)
      }
    }

    "must return a You've Also Answered summary list with the correct content" - {
      "when no spirits" - {
        "and DSD is declared" in new SetUp {
          val userAnswers: UserAnswers = declareAdjustmentTotalPage(
            declareAdjustmentQuestionPage(
              declareAlcoholDutyQuestionPage(emptyUserAnswers, false),
              true
            ),
            adjustmentTotal
          )

          when(mockCalculatorConnector.calculateTotalAdjustment(eqTo(adjustmentsNoDuties))(any))
            .thenReturn(Future.successful(totalAdjustments))

          when(mockAdrReturnSubmissionService.getDutySuspended(any())).thenReturn(dutySuspendedDeclaredModel)
          when(mockAdrReturnSubmissionService.getSpirits(any(), any())).thenReturn(emptySpiritsModel)

          val result = dutyDueForThisReturnHelper
            .getDutyDueViewModel(userAnswers, returnPeriod)
            .value
            .futureValue
            .toOption
            .get
            .youveAlsoDeclaredSummaryList

          result.rows.map(_.key.content.asHtml.toString)   mustBe Seq("Duty suspended alcohol")
          result.rows.map(_.value.content.asHtml.toString) mustBe Seq("Declared")
          result.rows.map(_.actions.get.items.head)        mustBe Seq(dutySuspendedDeclaredActionItem)
        }

        "and DSD is not declared" in new SetUp {
          val userAnswers: UserAnswers = declareAdjustmentTotalPage(
            declareAdjustmentQuestionPage(
              declareAlcoholDutyQuestionPage(emptyUserAnswers, false),
              true
            ),
            adjustmentTotal
          )

          when(mockCalculatorConnector.calculateTotalAdjustment(eqTo(adjustmentsNoDuties))(any))
            .thenReturn(Future.successful(totalAdjustments))

          when(mockAdrReturnSubmissionService.getDutySuspended(any())).thenReturn(dutySuspendedNotDeclaredModel)
          when(mockAdrReturnSubmissionService.getSpirits(any(), any())).thenReturn(emptySpiritsModel)

          val result = dutyDueForThisReturnHelper
            .getDutyDueViewModel(userAnswers, returnPeriod)
            .value
            .futureValue
            .toOption
            .get
            .youveAlsoDeclaredSummaryList

          result.rows.map(_.key.content.asHtml.toString)   mustBe Seq("Duty suspended alcohol")
          result.rows.map(_.value.content.asHtml.toString) mustBe Seq("Nothing to declare")
          result.rows.map(_.actions.get.items.head)        mustBe Seq(dutySuspendedNotDeclaredActionItem)
        }
      }

      "when spirits declared" - {
        "and DSD is declared" in new SetUp {
          val userAnswers: UserAnswers = declareAdjustmentTotalPage(
            declareAdjustmentQuestionPage(
              declareAlcoholDutyQuestionPage(emptyUserAnswers, false),
              true
            ),
            adjustmentTotal
          )

          when(mockCalculatorConnector.calculateTotalAdjustment(eqTo(adjustmentsNoDuties))(any))
            .thenReturn(Future.successful(totalAdjustments))

          when(mockAdrReturnSubmissionService.getDutySuspended(any())).thenReturn(dutySuspendedDeclaredModel)
          when(mockAdrReturnSubmissionService.getSpirits(any(), any())).thenReturn(spiritsDeclaredModel)

          val result = dutyDueForThisReturnHelper
            .getDutyDueViewModel(userAnswers, returnPeriod)
            .value
            .futureValue
            .toOption
            .get
            .youveAlsoDeclaredSummaryList

          result.rows.map(_.key.content.asHtml.toString)   mustBe Seq("Duty suspended alcohol", "Spirits production")
          result.rows.map(_.value.content.asHtml.toString) mustBe Seq("Declared", "Declared")
          result.rows.map(_.actions.get.items.head)        mustBe
            Seq(dutySuspendedDeclaredActionItem, spiritsDeclaredActionItem)
        }

        "and DSD is not declared" in new SetUp {
          val userAnswers: UserAnswers = declareAdjustmentTotalPage(
            declareAdjustmentQuestionPage(
              declareAlcoholDutyQuestionPage(emptyUserAnswers, false),
              true
            ),
            adjustmentTotal
          )

          when(mockCalculatorConnector.calculateTotalAdjustment(eqTo(adjustmentsNoDuties))(any))
            .thenReturn(Future.successful(totalAdjustments))

          when(mockAdrReturnSubmissionService.getDutySuspended(any())).thenReturn(dutySuspendedNotDeclaredModel)
          when(mockAdrReturnSubmissionService.getSpirits(any(), any())).thenReturn(spiritsDeclaredModel)

          val result = dutyDueForThisReturnHelper
            .getDutyDueViewModel(userAnswers, returnPeriod)
            .value
            .futureValue
            .toOption
            .get
            .youveAlsoDeclaredSummaryList

          result.rows.map(_.key.content.asHtml.toString)   mustBe Seq("Duty suspended alcohol", "Spirits production")
          result.rows.map(_.value.content.asHtml.toString) mustBe Seq("Nothing to declare", "Declared")
          result.rows.map(_.actions.get.items.head)        mustBe
            Seq(dutySuspendedNotDeclaredActionItem, spiritsDeclaredActionItem)
        }
      }

      "when spirits not declared" - {
        "and DSD is declared" in new SetUp {
          val userAnswers: UserAnswers = declareAdjustmentTotalPage(
            declareAdjustmentQuestionPage(
              declareAlcoholDutyQuestionPage(emptyUserAnswers, false),
              true
            ),
            adjustmentTotal
          )

          when(mockCalculatorConnector.calculateTotalAdjustment(eqTo(adjustmentsNoDuties))(any))
            .thenReturn(Future.successful(totalAdjustments))

          when(mockAdrReturnSubmissionService.getDutySuspended(any())).thenReturn(dutySuspendedDeclaredModel)
          when(mockAdrReturnSubmissionService.getSpirits(any(), any())).thenReturn(spiritsNotDeclaredModel)

          val result = dutyDueForThisReturnHelper
            .getDutyDueViewModel(userAnswers, returnPeriod)
            .value
            .futureValue
            .toOption
            .get
            .youveAlsoDeclaredSummaryList

          result.rows.map(_.key.content.asHtml.toString)   mustBe Seq("Duty suspended alcohol", "Spirits production")
          result.rows.map(_.value.content.asHtml.toString) mustBe Seq("Declared", "Nothing to declare")
          result.rows.map(_.actions.get.items.head)        mustBe
            Seq(dutySuspendedDeclaredActionItem, spiritsNotDeclaredActionItem)
        }

        "and DSD not declared" in new SetUp {
          val userAnswers: UserAnswers = declareAdjustmentTotalPage(
            declareAdjustmentQuestionPage(
              declareAlcoholDutyQuestionPage(emptyUserAnswers, false),
              true
            ),
            adjustmentTotal
          )

          when(mockCalculatorConnector.calculateTotalAdjustment(eqTo(adjustmentsNoDuties))(any))
            .thenReturn(Future.successful(totalAdjustments))

          when(mockAdrReturnSubmissionService.getDutySuspended(any())).thenReturn(dutySuspendedNotDeclaredModel)
          when(mockAdrReturnSubmissionService.getSpirits(any(), any())).thenReturn(spiritsNotDeclaredModel)

          val result = dutyDueForThisReturnHelper
            .getDutyDueViewModel(userAnswers, returnPeriod)
            .value
            .futureValue
            .toOption
            .get
            .youveAlsoDeclaredSummaryList

          result.rows.map(_.key.content.asHtml.toString)   mustBe Seq("Duty suspended alcohol", "Spirits production")
          result.rows.map(_.value.content.asHtml.toString) mustBe Seq("Nothing to declare", "Nothing to declare")
          result.rows.map(_.actions.get.items.head)        mustBe
            Seq(dutySuspendedNotDeclaredActionItem, spiritsNotDeclaredActionItem)
        }
      }
    }

    "must error if unable to get duties when required" in new SetUp {
      val userAnswers = declareAdjustmentTotalPage(
        declareAdjustmentQuestionPage(
          declareAlcoholDutyQuestionPage(emptyUserAnswers, true),
          true
        ),
        adjustmentTotal
      )

      whenReady(dutyDueForThisReturnHelper.getDutyDueViewModel(userAnswers, returnPeriod).value) { result =>
        result.swap.toOption.get mustBe "Unable to get duties due when calculating duty due"
      }
    }

    "must error if unable to get adjustments when required" in new SetUp {
      val userAnswers =
        declareAdjustmentQuestionPage(
          declareAlcoholDutyQuestionPage(specifyAllAlcoholDutiesUnsorted(emptyUserAnswers), true),
          true
        )

      whenReady(dutyDueForThisReturnHelper.getDutyDueViewModel(userAnswers, returnPeriod).value) { result =>
        result.swap.toOption.get mustBe "Unable to get adjustment totals when calculating duty due"
      }
    }

    "must return an error message when calculator call for adjustment totals fails" in new SetUp {
      val errorMessage = "Error Message"
      when(mockCalculatorConnector.calculateTotalAdjustment(any())(any())).thenReturn(
        Future.failed(new Exception(errorMessage))
      )
      val result       =
        dutyDueForThisReturnHelper
          .getDutyDueViewModel(fullUserAnswers, returnPeriod)(hc, getMessages(app))
          .value
          .futureValue
      result mustBe Left(s"Failed to calculate total duty due: $errorMessage")
    }
  }

  class SetUp {
    implicit val messages: Messages = getMessages(app)

    val adjustmentTotal = BigDecimal(2400)

    val totalDuties               = Seq(beerDuty, ciderDuty, wineDuty, spiritsDuty, otherFermentedProductDuty).map(_.totalDuty)
    val totalDutiesNoAdjustments  = totalDuties :+ BigDecimal(0)
    val adjustmentsNoDuties       = Seq(adjustmentTotal)
    val totalDutiesAndAdjustments = totalDuties :+ adjustmentTotal

    val emptyDutySuspendedSuccessModel: EitherT[Future, String, AdrDutySuspended] = EitherT.rightT[Future, String](
      AdrDutySuspended(declared = false, dutySuspendedProducts = Seq.empty[AdrDutySuspendedProduct])
    )
    val dutySuspendedDeclaredModel: EitherT[Future, String, AdrDutySuspended]     = EitherT.rightT[Future, String](
      AdrDutySuspended(declared = true, dutySuspendedProducts = Seq.empty[AdrDutySuspendedProduct])
    )
    val dutySuspendedNotDeclaredModel: EitherT[Future, String, AdrDutySuspended]  = EitherT.rightT[Future, String](
      AdrDutySuspended(declared = false, dutySuspendedProducts = Seq.empty[AdrDutySuspendedProduct])
    )

    val emptySpiritsModel: EitherT[Future, String, Option[AdrSpirits]]       = EitherT.rightT[Future, String](None)
    val spiritsDeclaredModel: EitherT[Future, String, Option[AdrSpirits]]    = EitherT.rightT[Future, String](
      Some(AdrSpirits(spiritsDeclared = true, spiritsProduced = None))
    )
    val spiritsNotDeclaredModel: EitherT[Future, String, Option[AdrSpirits]] = EitherT.rightT[Future, String](
      Some(AdrSpirits(spiritsDeclared = false, spiritsProduced = None))
    )

    val totalDutyWithoutAdjustments = AdjustmentDuty(totalDuties.sum)
    val totalAdjustments            = AdjustmentDuty(adjustmentsNoDuties.sum)
    val totalDuty                   = AdjustmentDuty(totalDutiesAndAdjustments.sum)

    val mockCalculatorConnector        = mock[AlcoholDutyCalculatorConnector]
    val mockAdrReturnSubmissionService = mock[AdrReturnSubmissionService]
    val dutyDueForThisReturnHelper     =
      new DutyDueForThisReturnHelper(mockCalculatorConnector, mockAdrReturnSubmissionService)

    val declarationActionItems = Seq(
      ActionItem(
        content = Text("Change"),
        href = controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(Beer).url,
        visuallyHiddenText = Some("Beer declared")
      ),
      ActionItem(
        content = Text("Change"),
        href = controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(Cider).url,
        visuallyHiddenText = Some("Cider declared")
      ),
      ActionItem(
        content = Text("Change"),
        href = controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(Wine).url,
        visuallyHiddenText = Some("Wine declared")
      ),
      ActionItem(
        content = Text("Change"),
        href = controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(Spirits).url,
        visuallyHiddenText = Some("Spirits declared")
      ),
      ActionItem(
        content = Text("Change"),
        href = controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(OtherFermentedProduct).url,
        visuallyHiddenText = Some("Other fermented products declared")
      )
    )

    val nilDeclarationActionItem = ActionItem(
      content = Text("Change"),
      href = controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(NormalMode).url,
      visuallyHiddenText = Some("Declared duty")
    )

    val adjustmentsDeclaredActionItem = ActionItem(
      content = Text("Change"),
      href = controllers.adjustment.routes.AdjustmentListController.onPageLoad(1).url,
      visuallyHiddenText = Some("Adjustments to previous returns")
    )

    val adjustmentsNotDeclaredActionItem = ActionItem(
      content = Text("Change"),
      href = controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(NormalMode).url,
      visuallyHiddenText = Some("Adjustments to previous returns")
    )

    val dutySuspendedDeclaredActionItem = ActionItem(
      content = Text("Change"),
      href = controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad().url,
      visuallyHiddenText = Some("Duty suspended alcohol")
    )

    val dutySuspendedNotDeclaredActionItem = ActionItem(
      content = Text("Change"),
      href =
        controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(NormalMode).url,
      visuallyHiddenText = Some("Duty suspended alcohol")
    )

    val spiritsDeclaredActionItem = ActionItem(
      content = Text("Change"),
      href = controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url,
      visuallyHiddenText = Some("Spirits production")
    )

    val spiritsNotDeclaredActionItem = ActionItem(
      content = Text("Change"),
      href = controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(NormalMode).url,
      visuallyHiddenText = Some("Spirits production")
    )
  }
}
