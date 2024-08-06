package service.checkAndSubmit

import base.SpecBase
import connectors.AlcoholDutyCalculatorConnector
import models.adjustment.AdjustmentDuty
import models.returns.{AdrAdjustments, AdrDutyDeclared, AdrDutySuspended, AdrReturnSubmission, AdrSpirits, AdrTotals}
import org.mockito.ArgumentMatchers.any
import pages.adjustment.DeclareAdjustmentQuestionPage
import pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage
import pages.returns.DeclareAlcoholDutyQuestionPage
import pages.spiritsQuestions.DeclareQuarterlySpiritsPage
import services.checkAndSubmit.AdrReturnSubmissionServiceImpl

import scala.concurrent.Future

class AdrReturnSubmissionServiceSpec extends SpecBase {

  "AdrReturnSubmissionService" - {

    val calculator = mock[AlcoholDutyCalculatorConnector]
    when(calculator.calculateTotalAdjustment(any())(any())).thenReturn(Future.successful(AdjustmentDuty(duty = BigDecimal(0))))

    val adrReturnSubmissionService = new AdrReturnSubmissionServiceImpl(calculator)

    val notQuarterlySpiritsReturnPeriod = nonQuarterReturnPeriodGen.sample.value
    val quarterlySpiritsReturnPeriod = quarterReturnPeriodGen.sample.value

    val nilReturn = AdrReturnSubmission(
      dutyDeclared = AdrDutyDeclared(false, Nil),
      adjustments = AdrAdjustments(
        overDeclarationDeclared = false,
        reasonForOverDeclaration = None,
        overDeclarationProducts = Nil,
        underDeclarationDeclared = false,
        reasonForUnderDeclaration = None,
        underDeclarationProducts = Nil,
        spoiltProductDeclared = false,
        spoiltProducts = Nil,
        drawbackDeclared = false,
        drawbackProducts = Nil,
        repackagedDraughtDeclared = false,
        repackagedDraughtProducts = Nil
      ),
      dutySuspended = AdrDutySuspended(false, Nil),
      spirits = None,
      totals = AdrTotals(
        declaredDutyDue = 0,
        overDeclaration = 0,
        underDeclaration = 0,
        spoiltProduct = 0,
        drawback = 0,
        repackagedDraught = 0,
        totalDutyDue = 0
      )
    )

    "must return a successful response when for a Nil return" in {
      val userAnswers = emptyUserAnswers
        .set(DeclareAlcoholDutyQuestionPage, false).success.value
        .set(DeclareAdjustmentQuestionPage, false).success.value
        .set(DeclareDutySuspendedDeliveriesQuestionPage, false).success.value

      whenReady(adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, notQuarterlySpiritsReturnPeriod).value) {
        result =>
          result mustBe Right(nilReturn)
      }
    }

    "must return a successful response when for a Nil return with Quarterly Spirits" in {
      val userAnswers = emptyUserAnswers
        .set(DeclareAlcoholDutyQuestionPage, false).success.value
        .set(DeclareAdjustmentQuestionPage, false).success.value
        .set(DeclareDutySuspendedDeliveriesQuestionPage, false).success.value
        .set(DeclareQuarterlySpiritsPage, false).success.value

      whenReady(adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value) {
        result =>
          result mustBe Right(nilReturn.copy(spirits = Some(AdrSpirits(false, None))))
      }
    }
  }
}
