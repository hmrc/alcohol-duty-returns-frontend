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

package services.checkAndSubmit

import base.SpecBase
import config.FrontendAppConfig
import connectors.AlcoholDutyCalculatorConnector
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.adjustment.AdjustmentDuty
import models.checkAndSubmit.*
import models.{AlcoholRegime, AlcoholRegimes}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.adjustment.{AdjustmentEntryListPage, DeclareAdjustmentQuestionPage}
import pages.declareDuty.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage}
import pages.dutySuspended.*
import pages.spiritsQuestions.DeclareQuarterlySpiritsPage
import play.api.Application
import queries.Settable
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import viewmodels.tasklist.TaskListViewModel

import scala.concurrent.Future

class AdrReturnSubmissionServiceSpec extends SpecBase {

  "AdrReturnSubmissionService" - {
    "Nil return" - {
      "must return the valid return submission when for a Nil return" in new SetUp {
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

        when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(false)

        whenReady(
          adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, returnPeriod).value
        ) { result =>
          result mustBe Right(nilReturn)
        }
      }

      "must return the valid return submission when for a Nil return with Quarterly Spirits" in new SetUp {
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

        when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(true)

        whenReady(adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, returnPeriod).value) { result =>
          result mustBe Right(nilReturn.copy(spirits = Some(AdrSpirits(false, None))))
        }
      }
    }

    "Full return" - {
      "must return the valid return submission if one adjustment type is not filled" in new SetUp {
        val drawbackAdjustmentIndex = 5
        val userAnswers             =
          fullUserAnswers.removeBySeqIndex(AdjustmentEntryListPage, drawbackAdjustmentIndex).success.value

        val adjustments: AdrAdjustments = fullReturn.adjustments.copy(
          drawbackDeclared = false,
          drawbackProducts = Nil
        )

        val totals: AdrTotals = AdrTotals(
          declaredDutyDue = 1748.2,
          overDeclaration = -1854,
          underDeclaration = 1019.7,
          spoiltProduct = -92.7,
          drawback = 0,
          repackagedDraught = 606,
          totalDutyDue = 1427.2
        )

        val expectedReturn = fullReturn.copy(adjustments = adjustments, totals = totals)

        when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(true)

        whenReady(
          adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, returnPeriod).value
        ) { result =>
          result mustBe Right(expectedReturn)
        }

      }

      "must return the valid return submission if there are no repackage adjustments" in new SetUp {
        val repackagedAdjustmentIndex = 3
        val userAnswers               =
          fullUserAnswers.removeBySeqIndex(AdjustmentEntryListPage, repackagedAdjustmentIndex).success.value

        val adjustments: AdrAdjustments = fullReturn.adjustments.copy(
          repackagedDraughtDeclared = false,
          repackagedDraughtProducts = Nil
        )

        val totals: AdrTotals = AdrTotals(
          declaredDutyDue = 1748.2,
          overDeclaration = -1854,
          underDeclaration = 1019.7,
          spoiltProduct = -92.7,
          drawback = -194.67,
          repackagedDraught = 0,
          totalDutyDue = 626.53
        )

        val expectedReturn = fullReturn.copy(adjustments = adjustments, totals = totals)

        when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(true)

        whenReady(
          adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, returnPeriod).value
        ) { result =>
          result mustBe Right(expectedReturn)
        }
      }
    }

    "Errors for missing data" - {
      "Return section" -
        Seq(
          DeclareAlcoholDutyQuestionPage,
          AlcoholDutyPage
        ).foreach { page =>
          s"must return Left if $page is not present" in new SetUp {
            val userAnswers = fullUserAnswers.remove(page).success.value

            when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(true)

            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, returnPeriod).value
            ) { result =>
              result mustBe Left(s"Value not found for page: $page")
            }
          }
        }

      "Adjustments section" - {
        Seq(
          DeclareAdjustmentQuestionPage,
          AdjustmentEntryListPage
        ).foreach { (page: Settable[_]) =>
          s"must return Left if $page is not present" in new SetUp {
            val userAnswers = fullUserAnswers.remove(page).success.value

            when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(true)

            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, returnPeriod).value
            ) { result =>
              result mustBe Left(s"Value not found for page: $page")
            }
          }
        }

        "must return Left if one of the AdjustmentType is None" in new SetUp {
          val incompleteAdjustment = fullRepackageAdjustmentEntry.copy(
            adjustmentType = None
          )

          val userAnswers = fullUserAnswers.addToSeq(AdjustmentEntryListPage, incompleteAdjustment).success.value

          when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(true)

          whenReady(
            adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, returnPeriod).value
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
          s"must return Left if $propertyName is None in one of the Repackaged Adjustment Entry" in new SetUp {
            val userAnswers = fullUserAnswers.addToSeq(AdjustmentEntryListPage, adjustmentEntry).success.value

            when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(true)

            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, returnPeriod).value
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
          s"must return Left if $propertyName is None in one of the Adjustment Entry" in new SetUp {
            val userAnswers = fullUserAnswers.addToSeq(AdjustmentEntryListPage, adjustmentEntry).success.value

            when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(true)

            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, returnPeriod).value
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
        ).foreach { (page: Settable[_]) =>
          s"must return Left if $page is not present" in new SetUp {
            val userAnswers = fullUserAnswers.remove(page).success.value

            when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(true)

            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, returnPeriod).value
            ) { result =>
              result mustBe Left(s"Value not found for page: $page")
            }
          }
        }

        val dutySuspendedPages: Seq[(AlcoholRegime, Settable[_])] = Seq(
          Beer                  -> DutySuspendedBeerPage,
          Cider                 -> DutySuspendedCiderPage,
          Spirits               -> DutySuspendedSpiritsPage,
          Wine                  -> DutySuspendedWinePage,
          OtherFermentedProduct -> DutySuspendedOtherFermentedPage
        )

        dutySuspendedPages.foreach { case (regime, page) =>
          s"must return Right if the user doesn't have $regime as a regime and $page is not present" in new SetUp {
            val filteredRegimes = AlcoholRegime.values.filter(_ != regime).toSet
            val userAnswers     = fullUserAnswers
              .copy(regimes = AlcoholRegimes(filteredRegimes))
              .remove(page)
              .success
              .value

            when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(true)

            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, returnPeriod).value
            ) { result =>
              result.isRight mustBe true
              result.map { res =>
                res.dutySuspended.declared                   mustBe true
                res.dutySuspended.dutySuspendedProducts.size mustBe 4
              }
            }
          }
        }
      }

      "Spirits section" - {
        "must return no spirits if the task is not expected" in new SetUp {

          when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(false)

          whenReady(
            adrReturnSubmissionService
              .getAdrReturnSubmission(
                fullUserAnswers.copy(regimes = AlcoholRegimes(Set(Beer, Cider, Wine, OtherFermentedProduct))),
                returnPeriod
              )
              .value
          ) { result =>
            result.toOption.get.spirits mustBe None
          }
        }

        "must return an error if DeclareQuarterlySpiritsPage is not present when expected" in new SetUp {
          val userAnswers = fullUserAnswers.remove(DeclareQuarterlySpiritsPage).success.value

          when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(true)

          whenReady(
            adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, returnPeriod).value
          ) { result =>
            result mustBe Left(s"Value not found for page: $DeclareQuarterlySpiritsPage")
          }
        }

        "must return spirits otherwise when expected" in new SetUp {
          when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(true)

          whenReady(
            adrReturnSubmissionService.getAdrReturnSubmission(fullUserAnswers, returnPeriod).value
          ) { result =>
            result.toOption.get.spirits.nonEmpty                     mustBe true
            result.toOption.get.spirits.get.spiritsDeclared          mustBe true
            result.toOption.get.spirits.get.spiritsProduced.nonEmpty mustBe true
          }
        }
      }
    }

    "Calculations" - {
      "must return Left if the calculator return an error" in new SetUp {
        val failingCalculator = mock[AlcoholDutyCalculatorConnector]

        val errorMessage = "Error Message"

        when(failingCalculator.calculateTotalAdjustment(any())(any())).thenReturn(
          Future.failed(new Exception(errorMessage))
        )

        val service = new AdrReturnSubmissionServiceImpl(failingCalculator, taskListViewModelMock)

        when(taskListViewModelMock.hasSpiritsTask(any(), any())).thenReturn(true)

        whenReady(
          service.getAdrReturnSubmission(fullUserAnswers, returnPeriod).value
        ) { result =>
          result mustBe Left(s"Failed to calculate total duty: $errorMessage")
        }

      }
    }

    class SetUp {
      val application: Application     = applicationBuilder().build()
      val taskListViewModelMock        = mock[TaskListViewModel]
      val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

      case object CalculatorMock extends AlcoholDutyCalculatorConnector(appConfig, mock[HttpClientV2]) {
        override def calculateTotalAdjustment(duties: Seq[BigDecimal])(implicit
          hc: HeaderCarrier
        ): Future[AdjustmentDuty] =
          Future.successful(AdjustmentDuty(duty = duties.sum))
      }

      val adrReturnSubmissionService =
        new AdrReturnSubmissionServiceImpl(CalculatorMock, taskListViewModelMock)
    }
  }
}
