package services.checkAndSubmit

import com.google.inject.ImplementedBy
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import models.adjustment.AdjustmentType.{Drawback, Overdeclaration, RepackagedDraughtProducts, Spoilt, Underdeclaration, adjustmentTypeOptions}
import models.{AlcoholRegime, ReturnPeriod, UserAnswers}
import models.returns.{AdrAdjustmentItem, AdrAdjustments, AdrAlcoholQuantity, AdrDuty, AdrDutyDeclared, AdrDutyDeclaredItem, AdrDutySuspended, AdrDutySuspendedProduct, AdrRepackagedDraughtAdjustmentItem, AdrReturnSubmission, AdrSpirits, AdrTotals, AlcoholDuty}
import pages.QuestionPage
import pages.adjustment.{AdjustmentEntryListPage, DeclareAdjustmentQuestionPage}
import pages.returns.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage}
import play.api.libs.json.Reads

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdrReturnSubmissionServiceImpl @Inject()(
  implicit ec:ExecutionContext
) extends AdrReturnSubmissionService {
  override def getAdrReturnSubmission(userAnswers: UserAnswers): Either[String, AdrReturnSubmission] = {
    for {
      dutyDeclared <- getDutyDeclared(userAnswers)
      adjustments <- getAdjustments(userAnswers)
      dutySuspended <- getDutySuspended(userAnswers)
      spirits <- getSpirits(userAnswers)
      totals <- getTotals(userAnswers)
    } yield AdrReturnSubmission(
      dutyDeclared = dutyDeclared,
      adjustments = adjustments,
      dutySuspended = dutySuspended,
      spirits = spirits,
      totals = totals
    )
  }

  private def getDutyDeclared(userAnswers: UserAnswers):Either[String, AdrDutyDeclared] = {
    for {
      declared <- getValue(userAnswers, DeclareAlcoholDutyQuestionPage)
      dutyDeclaredItems <- if(declared) getDutyDeclaredItems(userAnswers) else Right(Seq.empty)
    } yield AdrDutyDeclared(
      declared = declared,
      dutyDeclaredItems = dutyDeclaredItems
    )
  }

  private def getDutyDeclaredItems(userAnswers: UserAnswers):Either[String, Seq[AdrDutyDeclaredItem]] = {
    getValue(userAnswers, AlcoholDutyPage).map {
      alcoholDuties: Map[AlcoholRegime, AlcoholDuty] => {
        alcoholDuties.flatMap(_._2.dutiesByTaxType).toSeq.map( duty =>
          AdrDutyDeclaredItem(
            quantityDeclared = AdrAlcoholQuantity(
              litres = duty.totalLitres,
              lpa = duty.pureAlcohol
            ), dutyDue = AdrDuty(
              taxCode = duty.taxType,
              dutyRate = duty.dutyRate,
              dutyDue = duty.dutyDue
            )
          )
        )
      }
    }
  }

  private def getAdjustments(userAnswers: UserAnswers): Either[String, AdrAdjustments] = {
    getValue(userAnswers, DeclareAdjustmentQuestionPage).map {
      isAnyAdjustmentDeclared =>
        if(isAnyAdjustmentDeclared) {
          getValue(userAnswers, AdjustmentEntryListPage).map(
            adjustmentEntryList => {
              val adjustmentEntryByType = adjustmentEntryList.groupBy(_.adjustmentType)
              adjustmentEntryByType.get(None) match {
                case Some(_) => Left("Adjustment with no type found")
                case None =>
                  for {
                    overDeclarationProducts <- mapAdjustmentItems(adjustmentEntryByType, Overdeclaration)
                    underDeclarationProducts <- mapAdjustmentItems(adjustmentEntryByType, Underdeclaration)
                    spoiltProducts <- mapAdjustmentItems(adjustmentEntryByType, Spoilt)
                    drawbackProducts <- mapAdjustmentItems(adjustmentEntryByType, Drawback)
                    repackagedDraughtDeclared <- mapRepackagedAdjustments(adjustmentEntryByType)
                  } yield {
                    AdrAdjustments(
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
          }
        } else {
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
    }}
  }

  private def mapAdjustmentItems(adjustmentEntries: Map[Option[AdjustmentType], Seq[AdjustmentEntry]], adjustmentType:AdjustmentType): Either[String, Seq[AdrAdjustmentItem]] = {
    adjustmentEntries.get(Some(adjustmentType)) match {
      case Some(entries) => {
        val mappedEntries = entries.map(mapAdjustmentItem)
        mappedEntries.collectFirst {
          case Left(error) => Left(error)
        }.getOrElse(
          Right(
            mappedEntries.collect { case Right(entry) => entry }
          )
        )
      }
      case None => Right(Nil)
    }
  }

  private def mapAdjustmentItem(adjustmentEntry: AdjustmentEntry):Either[String, AdrAdjustmentItem] = {
    for {
      period <- adjustmentEntry.period
      litres <- adjustmentEntry.totalLitresVolume
      lpa <- adjustmentEntry.pureAlcoholVolume
      rateBand <- adjustmentEntry.rateBand
      dutyRate <- if(rateBand.rate.isDefined) rateBand.rate else adjustmentEntry.sprDutyRate
      dutyDue <- adjustmentEntry.duty
    } yield
      AdrAdjustmentItem(
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

  private def mapRepackagedAdjustments(adjustmentEntries: Map[Option[AdjustmentType], Seq[AdjustmentEntry]]): Either[String, Seq[AdrRepackagedDraughtAdjustmentItem]] = {
    adjustmentEntries.get(Some(RepackagedDraughtProducts)) match {
      case Some(entries) => {
        val mappedEntries = entries.map(mapRepackagedAdjustment)
        mappedEntries.collectFirst {
          case Left(error) => Left(error)
        }.getOrElse{
          Right(mappedEntries.collect {
            case Right(entry) => entry
          })
        }
      }
      case None => Right(Nil)
    }
  }

  private def mapRepackagedAdjustment(adjustmentEntry: AdjustmentEntry): Either[String, AdrRepackagedDraughtAdjustmentItem] = {
    {
      for {
        period <- adjustmentEntry.period
        rateBand <- adjustmentEntry.rateBand
        originalDutyRate <- adjustmentEntry.rate
        newRateBand <- adjustmentEntry.repackagedRateBand
        newDutyRate <- adjustmentEntry.repackagedRate
        litres <- adjustmentEntry.totalLitresVolume
        lpa <- adjustmentEntry.pureAlcoholVolume
        dutyAdjustment <- adjustmentEntry.repackagedDuty
      } yield AdrRepackagedDraughtAdjustmentItem(
        returnPeriod = ReturnPeriod(period).toPeriodKey,
        originalTaxCode = rateBand.taxTypeCode,
        originalDutyRate = originalDutyRate,
        newTaxCode = newRateBand.taxTypeCode,
        newDutyRate = newDutyRate,
        repackagedQuantity = AdrAlcoholQuantity(
          litres = litres, lpa = lpa
        ),
        dutyAdjustment = dutyAdjustment
      )
    }.toRight(s"Impossible to create a Repackaged Adjustment item with values: $adjustmentEntry")

  }

  private def getDutySuspended(userAnswers: UserAnswers): Either[String, AdrDutySuspended] = {
//    userAnswers.get(DeclareDutySuspendedDeliveriesQuestionPage) match {
//      case Some(false) => Right(AdrDutySuspended(declared = false, dutySuspendedProducts = Nil))
//      case Some(true) => Right(AdrDutySuspended(
//        declared = true, dutySuspendedProducts = AdrDutySuspendedProduct(
//
//        )
//      ))
//      case None => Left("Impossible to find Declare duty Suspended Delivery ")
//    }
    ???
  }

  private def getSpirits(userAnswers: UserAnswers):Either[String, Option[AdrSpirits]] = {
    ???
  }

  private def getTotals(userAnswers: UserAnswers):Either[String, AdrTotals] = {
    ???
  }

  private def getValue[T](userAnswers: UserAnswers, page:QuestionPage[T])
                         (implicit reads: Reads[T]): Either[String, T] = {
    userAnswers.get(page) match {
      case Some(value) => Right(value)
      case None => Left(s"Value not found for page: $page")
    }
  }

}


@ImplementedBy(classOf[AdrReturnSubmissionServiceImpl])
trait AdrReturnSubmissionService {
  def getAdrReturnSubmission(userAnswers: UserAnswers): Either[String, AdrReturnSubmission]
}
