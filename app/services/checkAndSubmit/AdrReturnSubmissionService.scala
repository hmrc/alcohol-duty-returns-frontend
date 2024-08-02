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

import cats.data.EitherT
import com.google.inject.ImplementedBy
import connectors.AlcoholDutyCalculatorConnector
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import models.adjustment.AdjustmentType.{Drawback, Overdeclaration, RepackagedDraughtProducts, Spoilt, Underdeclaration}
import models.{AlcoholRegime, ReturnPeriod, UserAnswers}
import models.returns.{AdrAdjustmentItem, AdrAdjustments, AdrAlcoholQuantity, AdrDuty, AdrDutyDeclared, AdrDutyDeclaredItem, AdrDutySuspended, AdrDutySuspendedAlcoholRegime, AdrDutySuspendedProduct, AdrRepackagedDraughtAdjustmentItem, AdrReturnSubmission, AdrSpirits, AdrTotals, AlcoholDuty}
import pages.QuestionPage
import pages.adjustment.{AdjustmentEntryListPage, DeclareAdjustmentQuestionPage}
import pages.dutySuspended.{DeclareDutySuspendedDeliveriesQuestionPage, DutySuspendedBeerPage, DutySuspendedCiderPage, DutySuspendedOtherFermentedPage, DutySuspendedSpiritsPage, DutySuspendedWinePage}
import pages.returns.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage}
import play.api.libs.json.Reads
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdrReturnSubmissionServiceImpl @Inject() (
  calculatorConnector: AlcoholDutyCalculatorConnector
)(implicit
  ec: ExecutionContext
) extends AdrReturnSubmissionService {

  override def getAdrReturnSubmission(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): EitherT[Future, String, AdrReturnSubmission] =
    for {
      dutyDeclared  <- getDutyDeclared(userAnswers)
      adjustments   <- getAdjustments(userAnswers)
      dutySuspended <- getDutySuspended(userAnswers)
      spirits       <- getSpirits(userAnswers)
      totals        <- getTotals(userAnswers)
    } yield AdrReturnSubmission(
      dutyDeclared = dutyDeclared,
      adjustments = adjustments,
      dutySuspended = dutySuspended,
      spirits = spirits,
      totals = totals
    )

  private def getDutyDeclared(userAnswers: UserAnswers): EitherT[Future, String, AdrDutyDeclared] =
    for {
      declared          <- getValue(userAnswers, DeclareAlcoholDutyQuestionPage)
      dutyDeclaredItems <-
        if (declared) getDutyDeclaredItems(userAnswers) else EitherT.rightT[Future, String](Seq.empty)
    } yield AdrDutyDeclared(
      declared = declared,
      dutyDeclaredItems = dutyDeclaredItems
    )

  private def getDutyDeclaredItems(userAnswers: UserAnswers): EitherT[Future, String, Seq[AdrDutyDeclaredItem]] =
    getValue(userAnswers, AlcoholDutyPage).map { alcoholDuties: Map[AlcoholRegime, AlcoholDuty] =>
      alcoholDuties
        .flatMap(_._2.dutiesByTaxType)
        .toSeq
        .map(duty =>
          AdrDutyDeclaredItem(
            quantityDeclared = AdrAlcoholQuantity(
              litres = duty.totalLitres,
              lpa = duty.pureAlcohol
            ),
            dutyDue = AdrDuty(
              taxCode = duty.taxType,
              dutyRate = duty.dutyRate,
              dutyDue = duty.dutyDue
            )
          )
        )
    }

  private def getAdjustments(userAnswers: UserAnswers): EitherT[Future, String, AdrAdjustments] =
    getValue(userAnswers, DeclareAdjustmentQuestionPage).flatMap { isAnyAdjustmentDeclared =>
      if (isAnyAdjustmentDeclared) {
        getValue(userAnswers, AdjustmentEntryListPage).flatMap(adjustmentEntryList =>
          EitherT.fromEither {
            val adjustmentEntryByType = adjustmentEntryList.groupBy(_.adjustmentType)
            adjustmentEntryByType.get(None) match {
              case Some(_) => Left("Adjustment with no type found")
              case None    =>
                for {
                  overDeclarationProducts   <- mapAdjustmentItems(adjustmentEntryByType, Overdeclaration)
                  underDeclarationProducts  <- mapAdjustmentItems(adjustmentEntryByType, Underdeclaration)
                  spoiltProducts            <- mapAdjustmentItems(adjustmentEntryByType, Spoilt)
                  drawbackProducts          <- mapAdjustmentItems(adjustmentEntryByType, Drawback)
                  repackagedDraughtDeclared <- mapRepackagedAdjustments(adjustmentEntryByType)
                } yield AdrAdjustments(
                  overDeclarationDeclared = overDeclarationProducts.nonEmpty,
                  reasonForOverDeclaration = None,
                  overDeclarationProducts = overDeclarationProducts,
                  underDeclarationDeclared = underDeclarationProducts.nonEmpty,
                  reasonForUnderDeclaration = None,
                  underDeclarationProducts = underDeclarationProducts,
                  spoiltProductDeclared = spoiltProducts.nonEmpty,
                  spoiltProducts = spoiltProducts,
                  drawbackDeclared = drawbackProducts.nonEmpty,
                  drawbackProducts = drawbackProducts,
                  repackagedDraughtDeclared = repackagedDraughtDeclared.nonEmpty,
                  repackagedDraughtProducts = repackagedDraughtDeclared
                )
            }
          }
        )
      } else {
        EitherT.rightT(
          AdrAdjustments(
            overDeclarationDeclared = false,
            reasonForOverDeclaration = None,
            overDeclarationProducts = Seq.empty,
            underDeclarationDeclared = false,
            reasonForUnderDeclaration = None,
            underDeclarationProducts = Seq.empty,
            spoiltProductDeclared = false,
            spoiltProducts = Seq.empty,
            drawbackDeclared = false,
            drawbackProducts = Seq.empty,
            repackagedDraughtDeclared = false,
            repackagedDraughtProducts = Seq.empty
          )
        )
      }
    }

  private def mapAdjustmentItems(
    adjustmentEntries: Map[Option[AdjustmentType], Seq[AdjustmentEntry]],
    adjustmentType: AdjustmentType
  ): Either[String, Seq[AdrAdjustmentItem]] =
    adjustmentEntries.get(Some(adjustmentType)) match {
      case Some(entries) =>
        val mappedEntries = entries.map(mapAdjustmentItem)
        mappedEntries
          .collectFirst { case Left(error) =>
            Left(error)
          }
          .getOrElse(
            Right(
              mappedEntries.collect { case Right(entry) => entry }
            )
          )
      case None          => Right(Nil)
    }

  private def mapAdjustmentItem(adjustmentEntry: AdjustmentEntry): Either[String, AdrAdjustmentItem] = {
    for {
      period   <- adjustmentEntry.period
      litres   <- adjustmentEntry.totalLitresVolume
      lpa      <- adjustmentEntry.pureAlcoholVolume
      rateBand <- adjustmentEntry.rateBand
      dutyRate <- if (rateBand.rate.isDefined) rateBand.rate else adjustmentEntry.sprDutyRate
      dutyDue  <- adjustmentEntry.duty
    } yield AdrAdjustmentItem(
      returnPeriod = ReturnPeriod(period).toPeriodKey,
      adjustmentQuantity = AdrAlcoholQuantity(
        litres = litres,
        lpa = lpa
      ),
      dutyDue = AdrDuty(
        taxCode = rateBand.taxTypeCode,
        dutyRate = dutyRate,
        dutyDue = dutyDue
      )
    )
  }.toRight(s"Impossible to create Adjustment Item for values: $adjustmentEntry")

  private def mapRepackagedAdjustments(
    adjustmentEntries: Map[Option[AdjustmentType], Seq[AdjustmentEntry]]
  ): Either[String, Seq[AdrRepackagedDraughtAdjustmentItem]] =
    adjustmentEntries.get(Some(RepackagedDraughtProducts)) match {
      case Some(entries) =>
        val mappedEntries = entries.map(mapRepackagedAdjustment)
        mappedEntries
          .collectFirst { case Left(error) =>
            Left(error)
          }
          .getOrElse {
            Right(mappedEntries.collect { case Right(entry) =>
              entry
            })
          }
      case None          => Right(Nil)
    }

  private def mapRepackagedAdjustment(
    adjustmentEntry: AdjustmentEntry
  ): Either[String, AdrRepackagedDraughtAdjustmentItem] = {
    for {
      period           <- adjustmentEntry.period
      rateBand         <- adjustmentEntry.rateBand
      originalDutyRate <- adjustmentEntry.rate
      newRateBand      <- adjustmentEntry.repackagedRateBand
      newDutyRate      <- adjustmentEntry.repackagedRate
      litres           <- adjustmentEntry.totalLitresVolume
      lpa              <- adjustmentEntry.pureAlcoholVolume
      dutyAdjustment   <- adjustmentEntry.newDuty
    } yield AdrRepackagedDraughtAdjustmentItem(
      returnPeriod = ReturnPeriod(period).toPeriodKey,
      originalTaxCode = rateBand.taxTypeCode,
      originalDutyRate = originalDutyRate,
      newTaxCode = newRateBand.taxTypeCode,
      newDutyRate = newDutyRate,
      repackagedQuantity = AdrAlcoholQuantity(
        litres = litres,
        lpa = lpa
      ),
      dutyAdjustment = dutyAdjustment
    )
  }.toRight(s"Impossible to create a Repackaged Adjustment item with values: $adjustmentEntry")

  private def getDutySuspended(userAnswers: UserAnswers): EitherT[Future, String, AdrDutySuspended] =
    getValue(userAnswers, DeclareDutySuspendedDeliveriesQuestionPage).flatMap { hasDeclaredDutySuspended =>
      if (hasDeclaredDutySuspended) {
        getDutySuspendedProducts(userAnswers).map { dutySuspendedProducts =>
          AdrDutySuspended(
            declared = true,
            dutySuspendedProducts
          )
        }
      } else {
        EitherT.rightT[Future, String](AdrDutySuspended(declared = false, Seq.empty))
      }
    }

  private def getDutySuspendedProducts(
    userAnswers: UserAnswers
  ): EitherT[Future, String, Seq[AdrDutySuspendedProduct]] =
    for {
      beerDutySuspended           <- getValue(userAnswers, DutySuspendedBeerPage)
                                       .map(dutySuspendedBeer =>
                                         AdrDutySuspendedProduct(
                                           regime = AdrDutySuspendedAlcoholRegime.Beer,
                                           suspendedQuantity = AdrAlcoholQuantity(
                                             dutySuspendedBeer.totalBeer,
                                             dutySuspendedBeer.pureAlcoholInBeer
                                           )
                                         )
                                       )
      ciderDutySuspended          <- getValue(userAnswers, DutySuspendedCiderPage)
                                       .map(dutySuspendedCider =>
                                         AdrDutySuspendedProduct(
                                           regime = AdrDutySuspendedAlcoholRegime.Cider,
                                           suspendedQuantity = AdrAlcoholQuantity(
                                             dutySuspendedCider.totalCider,
                                             dutySuspendedCider.pureAlcoholInCider
                                           )
                                         )
                                       )
      spiritsDutySuspended        <- getValue(userAnswers, DutySuspendedSpiritsPage)
                                       .map(dutySuspendedSpirits =>
                                         AdrDutySuspendedProduct(
                                           regime = AdrDutySuspendedAlcoholRegime.Spirits,
                                           suspendedQuantity = AdrAlcoholQuantity(
                                             dutySuspendedSpirits.totalSpirits,
                                             dutySuspendedSpirits.pureAlcoholInSpirits
                                           )
                                         )
                                       )
      wineDutySuspended           <- getValue(userAnswers, DutySuspendedWinePage)
                                       .map(dutySuspendedWine =>
                                         AdrDutySuspendedProduct(
                                           regime = AdrDutySuspendedAlcoholRegime.Wine,
                                           suspendedQuantity = AdrAlcoholQuantity(
                                             dutySuspendedWine.totalWine,
                                             dutySuspendedWine.pureAlcoholInWine
                                           )
                                         )
                                       )
      otherFermentedDutySuspended <- getValue(userAnswers, DutySuspendedOtherFermentedPage)
                                       .map(dutySuspendedOtherFermentedProducts =>
                                         AdrDutySuspendedProduct(
                                           regime = AdrDutySuspendedAlcoholRegime.OtherFermentedProduct,
                                           suspendedQuantity = AdrAlcoholQuantity(
                                             dutySuspendedOtherFermentedProducts.totalOtherFermented,
                                             dutySuspendedOtherFermentedProducts.pureAlcoholInOtherFermented
                                           )
                                         )
                                       )
    } yield Seq(
      beerDutySuspended,
      ciderDutySuspended,
      spiritsDutySuspended,
      wineDutySuspended,
      otherFermentedDutySuspended
    )

  private def getSpirits(userAnswers: UserAnswers): EitherT[Future, String, Option[AdrSpirits]] =
    // TODO: map spirits
    EitherT.rightT(None)

  private def getAdjustmentsByType(
    userAnswers: UserAnswers
  ): EitherT[Future, String, Map[AdjustmentType, Seq[BigDecimal]]] =
    getValue(userAnswers, AdjustmentEntryListPage).map { adjustmentEntries =>
      adjustmentEntries.groupBy(_.adjustmentType).collect {
        case (Some(RepackagedDraughtProducts), entries) => RepackagedDraughtProducts -> entries.map(_.newDuty.get)
        case (Some(adjustmentType), entries)            => adjustmentType            -> entries.map(_.duty.get)
      }
    }

  private def getTotals(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): EitherT[Future, String, AdrTotals] =
    for {
      hasAlcoholDuty              <- getValue(userAnswers, DeclareAlcoholDutyQuestionPage)
      declaredAlcoholDutyByRegime <- if (hasAlcoholDuty) getValue(userAnswers, AlcoholDutyPage)
                                     else EitherT.rightT[Future, String](Map.empty[AlcoholRegime, AlcoholDuty])
      hasAdjustments              <- getValue(userAnswers, DeclareAdjustmentQuestionPage)
      declaredAlcoholDuty         <- calculateTotalDuties(declaredAlcoholDutyByRegime.map(_._2.totalDuty).toSeq)
      adjustmentDutiesByType      <- if (hasAdjustments) getAdjustmentsByType(userAnswers)
                                     else EitherT.rightT[Future, String](Map.empty[AdjustmentType, Seq[BigDecimal]])
      overDeclaration             <-
        conditionalTotalDuty(hasAdjustments, adjustmentDutiesByType.getOrElse(Overdeclaration, Seq.empty))
      underDeclaration            <-
        conditionalTotalDuty(hasAdjustments, adjustmentDutiesByType.getOrElse(Underdeclaration, Seq.empty))
      spoiltProduct               <- conditionalTotalDuty(hasAdjustments, adjustmentDutiesByType.getOrElse(Spoilt, Seq.empty))
      drawback                    <- conditionalTotalDuty(hasAdjustments, adjustmentDutiesByType.getOrElse(Drawback, Seq.empty))
      repackagedDraught           <-
        conditionalTotalDuty(hasAdjustments, adjustmentDutiesByType.getOrElse(RepackagedDraughtProducts, Seq.empty))
      totalDutyDue                <-
        calculateTotalDuties(
          Seq(declaredAlcoholDuty, overDeclaration, underDeclaration, spoiltProduct, drawback, repackagedDraught)
        )
    } yield AdrTotals(
      declaredDutyDue = declaredAlcoholDuty,
      overDeclaration = overDeclaration,
      underDeclaration = underDeclaration,
      spoiltProduct = spoiltProduct,
      drawback = drawback,
      repackagedDraught = repackagedDraught,
      totalDutyDue = totalDutyDue
    )

  private def conditionalTotalDuty(condition: Boolean, duties: Seq[BigDecimal])(implicit
    hc: HeaderCarrier
  ): EitherT[Future, String, BigDecimal] =
    if (condition) {
      calculateTotalDuties(duties)
    } else {
      EitherT.rightT[Future, String](BigDecimal(0))
    }

  private def getValue[T](userAnswers: UserAnswers, page: QuestionPage[T])(implicit
    reads: Reads[T]
  ): EitherT[Future, String, T] =
    userAnswers.get(page) match {
      case Some(value) => EitherT.rightT(value)
      case None        => EitherT.leftT(s"Value not found for page: $page")
    }

  private def calculateTotalDuties(
    duties: Seq[BigDecimal]
  )(implicit hc: HeaderCarrier): EitherT[Future, String, BigDecimal] =
    if (duties.isEmpty) {
      EitherT.rightT(BigDecimal(0))
    } else {
      EitherT {
        calculatorConnector
          .calculateTotalAdjustment(duties)
          .map { total =>
            Right(total.duty): Either[String, BigDecimal]
          }
          .recover(
            { case e => Left(s"Failed to calculate total duty: ${e.getMessage}") }
          )
      }
    }
}

@ImplementedBy(classOf[AdrReturnSubmissionServiceImpl])
trait AdrReturnSubmissionService {
  def getAdrReturnSubmission(userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, String, AdrReturnSubmission]
}
