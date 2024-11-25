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
import common.TestData
import config.FrontendAppConfig
import connectors.AlcoholDutyCalculatorConnector
import models.{AlcoholRegime, AlcoholRegimes}
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.adjustment.AdjustmentDuty
import models.checkAndSubmit.{AdrAdjustments, AdrSpirits, AdrSpiritsGrainsQuantities, AdrSpiritsProduced, AdrTotals}
import models.spiritsQuestions.{EthyleneGasOrMolassesUsed, GrainsUsed}
import org.mockito.ArgumentMatchers.any
import pages.adjustment.{AdjustmentEntryListPage, DeclareAdjustmentQuestionPage}
import pages.dutySuspended._
import pages.declareDuty.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage}
import pages.spiritsQuestions.{DeclareQuarterlySpiritsPage, EthyleneGasOrMolassesUsedPage, GrainsUsedPage, OtherIngredientsUsedPage, OtherMaltedGrainsPage}
import play.api.Application
import queries.Settable
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class AdrReturnSubmissionServiceSpec extends SpecBase with TestData {

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

        whenReady(
          adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, notQuarterlySpiritsReturnPeriod).value
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

        whenReady(adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value) {
          result =>
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

        whenReady(
          adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
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

        whenReady(
          adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
        ) { result =>
          result mustBe Right(expectedReturn)
        }
      }

      "must return the valid return submission if the users didn't selected any 'other malted grains'" in new SetUp {
        val grainsUsed  = GrainsUsed(
          maltedBarleyQuantity = BigDecimal(1111),
          wheatQuantity = BigDecimal(2222),
          maizeQuantity = BigDecimal(3333),
          ryeQuantity = BigDecimal(4444),
          unmaltedGrainQuantity = BigDecimal(5555),
          usedMaltedGrainNotBarley = false
        )
        val userAnswers = fullUserAnswers
          .set(GrainsUsedPage, grainsUsed)
          .success
          .value
          .remove(OtherMaltedGrainsPage)
          .success
          .value

        val adrSpiritsGrainsQuantities: AdrSpiritsGrainsQuantities =
          fullReturn.spirits.get.spiritsProduced.get.grainsQuantities.copy(
            otherMaltedGrain = None
          )

        val adrSpiritsProduced: AdrSpiritsProduced = fullReturn.spirits.get.spiritsProduced.get.copy(
          hasOtherMaltedGrain = false,
          otherMaltedGrainType = None,
          grainsQuantities = adrSpiritsGrainsQuantities
        )

        val spirits: AdrSpirits = fullReturn.spirits.get.copy(spiritsProduced = Some(adrSpiritsProduced))
        val expectedReturn      = fullReturn.copy(spirits = Some(spirits))

        whenReady(
          adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
        ) { result =>
          result mustBe Right(expectedReturn)
        }

      }

      "must return the valid return submission if the users didn't selected any 'other ingredients' in the spirits journey" in new SetUp {
        val ethyleneGasOrMolassesUsed = EthyleneGasOrMolassesUsed(
          ethyleneGas = BigDecimal(6666),
          molasses = BigDecimal(7777),
          otherIngredients = false
        )
        val userAnswers               = fullUserAnswers
          .set(EthyleneGasOrMolassesUsedPage, ethyleneGasOrMolassesUsed)
          .success
          .value
          .remove(OtherIngredientsUsedPage)
          .success
          .value

        val adrSpiritsProduced: AdrSpiritsProduced = fullReturn.spirits.get.spiritsProduced.get.copy(
          otherIngredient = None
        )

        val spirits: AdrSpirits = fullReturn.spirits.get.copy(spiritsProduced = Some(adrSpiritsProduced))
        val expectedReturn      = fullReturn.copy(spirits = Some(spirits))

        whenReady(
          adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
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
          s"must return Left if $page is not present" in new SetUp {
            val userAnswers = fullUserAnswers.remove(page).success.value
            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
            ) { result =>
              result mustBe Left(s"Value not found for page: $page")
            }
          }
        }
      }

      "Adjustments section" - {
        Seq(
          DeclareAdjustmentQuestionPage,
          AdjustmentEntryListPage
        ).foreach { page: Settable[_] =>
          s"must return Left if $page is not present" in new SetUp {
            val userAnswers = fullUserAnswers.remove(page).success.value
            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
            ) { result =>
              result mustBe Left(s"Value not found for page: $page")
            }
          }
        }

        "must return Left if one of the AdjustmentType is None" in new SetUp {
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
          s"must return Left if $propertyName is None in one of the Repackaged Adjustment Entry" in new SetUp {
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
          s"must return Left if $propertyName is None in one of the Adjustment Entry" in new SetUp {
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

        "must return Left if OtherMaltedGrains is not populated but the producer answered 'yes' into the GrainsUsed page for otherMaltedGrain" in new SetUp {
          val userAnswers = fullUserAnswers.remove(OtherMaltedGrainsPage).success.value

          whenReady(
            adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
          ) { result =>
            result.isLeft mustBe true
            result.fold(
              errorMessage => errorMessage must include("Other malted grain value not found"),
              _ => fail()
            )
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
            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
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
          s"must return Right if the user doesn't have $regime as a regime and $page is not present " in new SetUp {
            val filteredRegimes = AlcoholRegime.values.filter(_ != regime).toSet
            val userAnswers     = fullUserAnswers
              .copy(regimes = AlcoholRegimes(filteredRegimes))
              .remove(page)
              .success
              .value

            whenReady(
              adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
            ) { result =>
              result.isRight mustBe true
              result.map { res =>
                res.dutySuspended.declared mustBe true
                res.dutySuspended.dutySuspendedProducts.size mustBe 4
              }
            }
          }
        }
      }

      "Spirits section" - {
        "must return no spirits if the user doesn't have the spirits regime" in new SetUp {
          whenReady(
            adrReturnSubmissionService
              .getAdrReturnSubmission(
                fullUserAnswers.copy(regimes = AlcoholRegimes(Set(Beer, Cider, Wine, OtherFermentedProduct))),
                quarterlySpiritsReturnPeriod
              )
              .value
          ) { result =>
            result.toOption.get.spirits mustBe None
          }
        }

        "must return no spirits if not a spirits quarter" in new SetUp {
          whenReady(
            adrReturnSubmissionService.getAdrReturnSubmission(fullUserAnswers, notQuarterlySpiritsReturnPeriod).value
          ) { result =>
            result.toOption.get.spirits mustBe None
          }
        }

        "must return no spirits if the feature toggle is off" in new SetUp(false) {
          whenReady(
            adrReturnSubmissionService.getAdrReturnSubmission(fullUserAnswers, quarterlySpiritsReturnPeriod).value
          ) { result =>
            result.toOption.get.spirits mustBe None
          }
        }

        "must return an error if DeclareQuarterlySpiritsPage is not present" in new SetUp {
          val userAnswers = fullUserAnswers.remove(DeclareQuarterlySpiritsPage).success.value
          whenReady(
            adrReturnSubmissionService.getAdrReturnSubmission(userAnswers, quarterlySpiritsReturnPeriod).value
          ) { result =>
            result mustBe Left(s"Value not found for page: $DeclareQuarterlySpiritsPage")
          }
        }

        "must return spirits otherwise" in new SetUp {
          whenReady(
            adrReturnSubmissionService.getAdrReturnSubmission(fullUserAnswers, quarterlySpiritsReturnPeriod).value
          ) { result =>
            result.toOption.get.spirits.nonEmpty mustBe true
            result.toOption.get.spirits.get.spiritsDeclared mustBe true
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

        val service = new AdrReturnSubmissionServiceImpl(failingCalculator, appConfig)

        whenReady(
          service.getAdrReturnSubmission(fullUserAnswers, quarterlySpiritsReturnPeriod).value
        ) { result =>
          result mustBe Left(s"Failed to calculate total duty: $errorMessage")
        }

      }
    }

    class SetUp(spiritsAndIngredientsEnabledFeatureToggle: Boolean = true) {
      val additionalConfig             = Map("features.spirits-and-ingredients" -> spiritsAndIngredientsEnabledFeatureToggle)
      val application: Application     = applicationBuilder().configure(additionalConfig).build()
      val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

      case object CalculatorMock extends AlcoholDutyCalculatorConnector(appConfig, mock[HttpClientV2]) {
        override def calculateTotalAdjustment(duties: Seq[BigDecimal])(implicit
          hc: HeaderCarrier
        ): Future[AdjustmentDuty] =
          Future.successful(AdjustmentDuty(duty = duties.sum))
      }

      val adrReturnSubmissionService = new AdrReturnSubmissionServiceImpl(CalculatorMock, appConfig)

      val notQuarterlySpiritsReturnPeriod = nonQuarterReturnPeriodGen.sample.value
      val quarterlySpiritsReturnPeriod    = quarterReturnPeriodGen.sample.value
    }

  }

}
