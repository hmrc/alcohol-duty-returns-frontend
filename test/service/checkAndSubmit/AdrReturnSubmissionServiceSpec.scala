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

package service.checkAndSubmit

import base.SpecBase
import common.TestData
import config.FrontendAppConfig
import connectors.AlcoholDutyCalculatorConnector
import models.adjustment.AdjustmentDuty
import models.returns.AdrSpirits
import org.mockito.ArgumentMatchers.any
import pages.adjustment.{AdjustmentEntryListPage, DeclareAdjustmentQuestionPage}
import pages.dutySuspended.{DeclareDutySuspendedDeliveriesQuestionPage, DutySuspendedBeerPage, DutySuspendedCiderPage, DutySuspendedOtherFermentedPage, DutySuspendedSpiritsPage, DutySuspendedWinePage}
import pages.returns.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage}
import pages.spiritsQuestions.DeclareQuarterlySpiritsPage
import queries.Settable
import services.checkAndSubmit.AdrReturnSubmissionServiceImpl
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.Future

class AdrReturnSubmissionServiceSpec extends SpecBase with TestData {

  "AdrReturnSubmissionService" - {

    case object CalculatorMock extends AlcoholDutyCalculatorConnector(mock[FrontendAppConfig], mock[HttpClient]) {
      override def calculateTotalAdjustment(duties: Seq[BigDecimal])(implicit
        hc: HeaderCarrier
      ): Future[AdjustmentDuty] =
        Future.successful(AdjustmentDuty(duty = duties.sum))
    }

    val adrReturnSubmissionService = new AdrReturnSubmissionServiceImpl(CalculatorMock)

    val notQuarterlySpiritsReturnPeriod = nonQuarterReturnPeriodGen.sample.value
    val quarterlySpiritsReturnPeriod    = quarterReturnPeriodGen.sample.value

    "Nil return" - {
      "must return the correct return submission when for a Nil return" in {
        val userAnswers = emptyUserAnswers
          .set(DeclareAlcoholDutyQuestionPage, false)
          .success
          .value
          .set(DeclareAdjustmentQuestionPage, false)
          .success
          .value
          .set(DeclareDutySuspendedDeliveriesQuestionPage, false)
          .success
          .value

        whenReady(
          adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, notQuarterlySpiritsReturnPeriod).value
        ) { result =>
          result mustBe Right(nilReturn)
        }
      }

      "must return the correct return submission when for a Nil return with Quarterly Spirits" in {
        val userAnswers = emptyUserAnswers
          .set(DeclareAlcoholDutyQuestionPage, false)
          .success
          .value
          .set(DeclareAdjustmentQuestionPage, false)
          .success
          .value
          .set(DeclareDutySuspendedDeliveriesQuestionPage, false)
          .success
          .value
          .set(DeclareQuarterlySpiritsPage, false)
          .success
          .value

        whenReady(adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value) {
          result =>
            result mustBe Right(nilReturn.copy(spirits = Some(AdrSpirits(false, None))))
        }
      }
    }

    "Full return" - {

      "must return the correct return submission when for a return is passed" in {
        whenReady(
          adrReturnSubmissionService.getAdrReturnSubmission(fullUserAnswers, quarterlySpiritsReturnPeriod).value
        ) { result =>
          result mustBe Right(fullReturn)
        }
      }

      "must return the correct return submission without Spirits if the return period is not a quarter return" in {
        val expectedReturn = fullReturn.copy(spirits = None)

        whenReady(
          adrReturnSubmissionService.getAdrReturnSubmission(fullUserAnswers, notQuarterlySpiritsReturnPeriod).value
        ) { result =>
          result mustBe Right(expectedReturn)
        }
      }
    }

    "Errors for missing data" - {
      "Return section" - {
        Seq(
          DeclareAlcoholDutyQuestionPage,
          AlcoholDutyPage
        ).foreach { page =>
          s"must return Left if ${page.toString} is not present" in {
            val userAnswers = fullUserAnswers.remove(page).success.value
            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
            ) { result =>
              result mustBe Left(s"Value not found for page: ${page.toString}")
            }
          }
        }
      }

      "Adjustments section" - {
        Seq(
          DeclareAdjustmentQuestionPage,
          AdjustmentEntryListPage
        ).foreach { page: Settable[_] =>
          s"must return Left if ${page.toString} is not present" in {
            val userAnswers = fullUserAnswers.remove(page).success.value
            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
            ) { result =>
              result mustBe Left(s"Value not found for page: ${page.toString}")
            }
          }
        }

        "must return Left if one of the AdjustmentType is None" in {
          val incompleteAdjustment = fullRepackageAdjustmentEntry.copy(
            adjustmentType = None
          )
          val userAnswers          = fullUserAnswers.addToSeq(AdjustmentEntryListPage, incompleteAdjustment).success.value
          whenReady(
            adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
          ) { result =>
            result mustBe Left(s"Adjustment with no type found")
          }
        }

        Seq(
          "period"                -> fullRepackageAdjustmentEntry.copy(period = None),
          "rateBand"              -> fullRepackageAdjustmentEntry.copy(rateBand = None),
          "totalLitresVolume"     -> fullRepackageAdjustmentEntry.copy(totalLitresVolume = None),
          "pureAlcoholVolume"     -> fullRepackageAdjustmentEntry.copy(pureAlcoholVolume = None),
          "repackagedRateBand"    -> fullRepackageAdjustmentEntry.copy(repackagedRateBand = None),
          "repackagedSprDutyRate" -> fullRepackageAdjustmentEntry.copy(repackagedSprDutyRate = None),
          "duty"                  -> fullRepackageAdjustmentEntry.copy(duty = None),
          "repackagedDuty"        -> fullRepackageAdjustmentEntry.copy(repackagedDuty = None),
          "newDuty"               -> fullRepackageAdjustmentEntry.copy(newDuty = None)
        ).foreach { case (propertyName, adjustmentEntry) =>
          s"must return Left if $propertyName is None in one of the Repackaged Adjustment Entry" in {
            val userAnswers = fullUserAnswers.addToSeq(AdjustmentEntryListPage, adjustmentEntry).success.value
            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
            ) { result =>
              result.isLeft mustBe true
              result.fold(
                errorMessage =>
                  errorMessage must include("Impossible to create a Repackaged Adjustment item for values"),
                _ => fail()
              )
            }
          }
        }

        Seq(
          "period"            -> fullAdjustmentEntry.copy(period = None),
          "rateBand"          -> fullAdjustmentEntry.copy(rateBand = None),
          "totalLitresVolume" -> fullAdjustmentEntry.copy(totalLitresVolume = None),
          "pureAlcoholVolume" -> fullAdjustmentEntry.copy(pureAlcoholVolume = None),
          "duty"              -> fullAdjustmentEntry.copy(duty = None)
        ).foreach { case (propertyName, adjustmentEntry) =>
          s"must return Left if $propertyName is None in one of the Adjustment Entry" in {
            val userAnswers = fullUserAnswers.addToSeq(AdjustmentEntryListPage, adjustmentEntry).success.value
            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
            ) { result =>
              result.isLeft mustBe true
              result.fold(
                errorMessage => errorMessage must include("Impossible to create Adjustment Item for values"),
                _ => fail()
              )
            }
          }
        }

      }

      "Duty Suspended section" - {
        Seq(
          DeclareDutySuspendedDeliveriesQuestionPage,
          DutySuspendedBeerPage,
          DutySuspendedCiderPage,
          DutySuspendedSpiritsPage,
          DutySuspendedWinePage,
          DutySuspendedOtherFermentedPage
        ).foreach { page: Settable[_] =>
          s"must return Left if ${page.toString} is not present" in {
            val userAnswers = fullUserAnswers.remove(page).success.value
            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
            ) { result =>
              result mustBe Left(s"Value not found for page: ${page.toString}")
            }
          }
        }
      }

      "Spirits section" - {
        Seq(
          DeclareQuarterlySpiritsPage
        ).foreach { page =>
          s"must return Left if ${page.toString} is not present" in {
            val userAnswers = fullUserAnswers.remove(page).success.value
            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
            ) { result =>
              result mustBe Left(s"Value not found for page: ${page.toString}")
            }
          }
        }
      }
    }

    "Calculations" - {
      "must return Left if the calculator return an error" - {
        val failingCalculator = mock[AlcoholDutyCalculatorConnector]

        val errorMessage = "Error Message"

        when(failingCalculator.calculateTotalAdjustment(any())(any())).thenReturn(
          Future.failed(new Exception(errorMessage))
        )

        val service = new AdrReturnSubmissionServiceImpl(failingCalculator)

        whenReady(
          service.getAdrReturnSubmission(fullUserAnswers, quarterlySpiritsReturnPeriod).value
        ) { result =>
          result mustBe Left(s"Failed to calculate total duty: $errorMessage")
        }

      }
    }

  }

}
