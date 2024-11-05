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

package common

import cats.data.NonEmptySeq
import generators.ModelGenerators
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import models.checkAndSubmit.AdrUnitOfMeasure.Tonnes
import models.declareDuty._
import models._
import org.scalacheck.Gen
import pages.adjustment._
import pages.dutySuspended._
import pages.spiritsQuestions._
import play.api.libs.json.Json
import org.scalacheck.Gen.{listOfN, numChar}
import pages.declareDuty.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage}
import models.TransactionType.{LPI, RPI, Return}
import models.{AlcoholRegimes, ObligationData, ObligationStatus, OpenPayments, OutstandingPayment, ReturnId, ReturnPeriod, UnallocatedPayment, UserAnswers}
import models.checkAndSubmit.{AdrAdjustmentItem, AdrAdjustments, AdrAlcoholQuantity, AdrDuty, AdrDutyDeclared, AdrDutyDeclaredItem, AdrDutySuspended, AdrDutySuspendedAlcoholRegime, AdrDutySuspendedProduct, AdrOtherIngredient, AdrRepackagedDraughtAdjustmentItem, AdrReturnSubmission, AdrSpirits, AdrSpiritsGrainsQuantities, AdrSpiritsIngredientsVolumes, AdrSpiritsProduced, AdrSpiritsVolumes, AdrTotals, AdrTypeOfSpirit}
import models.returns._
import models.returns.{ReturnAdjustments, ReturnAlcoholDeclared, ReturnAlcoholDeclaredRow, ReturnDetails, ReturnDetailsIdentification, ReturnTotalDutyDue}
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails

import java.time.{Clock, Instant, LocalDate, Month, YearMonth, ZoneId}

trait TestData extends ModelGenerators {
  val clock              = Clock.fixed(Instant.ofEpochMilli(1718118467838L), ZoneId.of("UTC"))
  val appaId: String     = appaIdGen.sample.get
  val periodKey: String  = periodKeyGen.sample.get
  val groupId: String    = "groupid"
  val internalId: String = "id"
  val returnId: ReturnId = ReturnId(appaId, periodKey)

  val returnAndUserDetails = ReturnAndUserDetails(returnId, groupId, internalId)

  val returnPeriod = ReturnPeriod.fromPeriodKey(periodKey).get

  val badPeriodKey = "24A"

  val periodKeyJan = "24AA"
  val periodKeyFeb = "24AB"
  val periodKeyMar = "24AC"
  val periodKeyApr = "24AD"
  val periodKeyMay = "24AE"
  val periodKeyJun = "24AF"
  val periodKeyJul = "24AG"
  val periodKeyAug = "24AH"
  val periodKeySep = "24AI"
  val periodKeyOct = "24AJ"
  val periodKeyNov = "24AK"
  val periodKeyDec = "24AL"

  private val adrPeriodStartDay = 1

  private def periodFrom(monthsInThePast: Int, date: LocalDate): LocalDate = {
    val newDate = date.minusMonths(monthsInThePast)
    newDate.withDayOfMonth(adrPeriodStartDay)
  }

  val quarterReturnPeriods    = Set(
    ReturnPeriod(YearMonth.of(2024, Month.MARCH)),
    ReturnPeriod(YearMonth.of(2024, Month.JUNE)),
    ReturnPeriod(YearMonth.of(2024, Month.SEPTEMBER)),
    ReturnPeriod(YearMonth.of(2024, Month.DECEMBER))
  )
  val nonQuarterReturnPeriods =
    Set(
      ReturnPeriod(YearMonth.of(2024, Month.JANUARY)),
      ReturnPeriod(YearMonth.of(2024, Month.FEBRUARY)),
      ReturnPeriod(YearMonth.of(2024, Month.APRIL)),
      ReturnPeriod(YearMonth.of(2024, Month.MAY)),
      ReturnPeriod(YearMonth.of(2024, Month.JULY)),
      ReturnPeriod(YearMonth.of(2024, Month.AUGUST)),
      ReturnPeriod(YearMonth.of(2024, Month.OCTOBER)),
      ReturnPeriod(YearMonth.of(2024, Month.NOVEMBER))
    )

  val nonQuarterReturnPeriodGen = Gen.oneOf(nonQuarterReturnPeriods.toSeq)
  val quarterReturnPeriodGen    = Gen.oneOf(quarterReturnPeriods.toSeq)

  val returnPeriodMar = ReturnPeriod.fromPeriodKey(periodKeyMar).get

  val emptyUserAnswers: UserAnswers = UserAnswers(
    returnId,
    groupId,
    internalId,
    regimes = AlcoholRegimes(Set(Beer, Cider, Wine, Spirits, OtherFermentedProduct))
  )

  val userAnswersWithBeer: UserAnswers                     = emptyUserAnswers.copy(
    regimes = AlcoholRegimes(Set(Beer))
  )
  val userAnswersWithoutBeer: UserAnswers                  = emptyUserAnswers.copy(
    regimes = AlcoholRegimes(Set(Cider, Wine, Spirits, OtherFermentedProduct))
  )
  val userAnswersWithCider: UserAnswers                    = emptyUserAnswers.copy(
    regimes = AlcoholRegimes(Set(Cider))
  )
  val userAnswersWithoutCider: UserAnswers                 = emptyUserAnswers.copy(
    regimes = AlcoholRegimes(Set(Beer, Wine, Spirits, OtherFermentedProduct))
  )
  val userAnswersWithWine: UserAnswers                     = emptyUserAnswers.copy(
    regimes = AlcoholRegimes(Set(Wine))
  )
  val userAnswersWithoutWine: UserAnswers                  = emptyUserAnswers.copy(
    regimes = AlcoholRegimes(Set(Beer, Cider, Spirits, OtherFermentedProduct))
  )
  val userAnswersWithSpirits: UserAnswers                  = emptyUserAnswers.copy(
    regimes = AlcoholRegimes(Set(Spirits))
  )
  val userAnswersWithoutSpirits: UserAnswers               = emptyUserAnswers.copy(
    regimes = AlcoholRegimes(Set(Beer, Cider, Wine, OtherFermentedProduct))
  )
  val userAnswersWithOtherFermentedProduct: UserAnswers    = emptyUserAnswers.copy(
    regimes = AlcoholRegimes(Set(OtherFermentedProduct))
  )
  val userAnswersWithoutOtherFermentedProduct: UserAnswers = emptyUserAnswers.copy(
    regimes = AlcoholRegimes(Set(Beer, Spirits))
  )

  val userAnswersWithAllRegimes: UserAnswers = emptyUserAnswers.copy(
    regimes = AlcoholRegimes(Set(Beer, Cider, Wine, Spirits, OtherFermentedProduct))
  )

  def exampleRateBands(periodKey: String): Map[(YearMonth, String), RateBand] = {
    val periodDate = ReturnPeriod.fromPeriodKeyOrThrow(periodKey).period
    Map(
      (periodDate, "341") -> coreRateBand,
      (periodDate, "331") -> coreRateBand2,
      (periodDate, "321") -> coreRateBand3
    )
  }

  val emptyRateBands: Map[(YearMonth, String), RateBand] =
    Map.empty[(YearMonth, String), RateBand]

  def exampleReturnDetails(periodKey: String, now: Instant): ReturnDetails = {
    val periodDate = ReturnPeriod.fromPeriodKeyOrThrow(periodKey).periodFromDate()

    ReturnDetails(
      identification = ReturnDetailsIdentification(periodKey = periodKey, submittedTime = now),
      alcoholDeclared = ReturnAlcoholDeclared(
        alcoholDeclaredDetails = Some(
          Seq(
            ReturnAlcoholDeclaredRow(
              taxType = "311",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("9.27"),
              dutyValue = BigDecimal("4171.50")
            ),
            ReturnAlcoholDeclaredRow(
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("9454.50")
            ),
            ReturnAlcoholDeclaredRow(
              taxType = "331",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("28.50"),
              dutyValue = BigDecimal("12825.00")
            ),
            ReturnAlcoholDeclaredRow(
              taxType = "341",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("31.64"),
              dutyValue = BigDecimal("14238.00")
            ),
            ReturnAlcoholDeclaredRow(
              taxType = "351",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("8.42"),
              dutyValue = BigDecimal("3789.00")
            ),
            ReturnAlcoholDeclaredRow(
              taxType = "356",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("19.08"),
              dutyValue = BigDecimal("8586.00")
            ),
            ReturnAlcoholDeclaredRow(
              taxType = "361",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("8.40"),
              dutyValue = BigDecimal("3780.00")
            ),
            ReturnAlcoholDeclaredRow(
              taxType = "366",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("16.47"),
              dutyValue = BigDecimal("7411.50")
            ),
            ReturnAlcoholDeclaredRow(
              taxType = "371",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("8.20"),
              dutyValue = BigDecimal("3960.00")
            ),
            ReturnAlcoholDeclaredRow(
              taxType = "376",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("15.63"),
              dutyValue = BigDecimal("7033.50")
            )
          )
        ),
        total = BigDecimal("75249.00")
      ),
      adjustments = ReturnAdjustments(
        adjustmentDetails = Some(
          Seq(
            ReturnAdjustmentsRow(
              adjustmentTypeKey = ReturnAdjustments.underDeclaredKey,
              returnPeriodAffected = ReturnPeriod.fromDateInPeriod(periodFrom(1, periodDate)).toPeriodKey,
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(150),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("3151.50")
            ),
            ReturnAdjustmentsRow(
              adjustmentTypeKey = ReturnAdjustments.overDeclaredKey,
              returnPeriodAffected = ReturnPeriod.fromDateInPeriod(periodFrom(2, periodDate)).toPeriodKey,
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(1150),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("-24161.50")
            ),
            ReturnAdjustmentsRow(
              adjustmentTypeKey = ReturnAdjustments.drawbackKey,
              returnPeriodAffected = ReturnPeriod.fromDateInPeriod(periodFrom(3, periodDate)).toPeriodKey,
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(75),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("-1575.50")
            ),
            ReturnAdjustmentsRow(
              adjustmentTypeKey = ReturnAdjustments.repackagedDraughtKey,
              returnPeriodAffected = ReturnPeriod.fromDateInPeriod(periodFrom(4, periodDate)).toPeriodKey,
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(150),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("3151.50")
            )
          )
        ),
        total = BigDecimal("-19434")
      ),
      totalDutyDue = ReturnTotalDutyDue(totalDue = BigDecimal("55815")),
      netDutySuspension = Some(
        ReturnNetDutySuspension(
          totalLtsBeer = Some(BigDecimal("0.15")),
          totalLtsCider = Some(BigDecimal("0.38")),
          totalLtsSpirit = Some(BigDecimal("0.02")),
          totalLtsWine = Some(BigDecimal("0.44")),
          totalLtsOtherFermented = Some(BigDecimal("0.02")),
          totalLtsPureAlcoholBeer = Some(BigDecimal("0.4248")),
          totalLtsPureAlcoholCider = Some(BigDecimal("0.0379")),
          totalLtsPureAlcoholSpirit = Some(BigDecimal("0.2492")),
          totalLtsPureAlcoholWine = Some(BigDecimal("0.5965")),
          totalLtsPureAlcoholOtherFermented = Some(BigDecimal("0.1894"))
        )
      )
    )
  }

  def nilReturnDetails(periodKey: String, now: Instant): ReturnDetails =
    ReturnDetails(
      identification = ReturnDetailsIdentification(periodKey = periodKey, submittedTime = now),
      alcoholDeclared = ReturnAlcoholDeclared(
        alcoholDeclaredDetails = None,
        total = BigDecimal(0)
      ),
      adjustments = ReturnAdjustments(
        adjustmentDetails = None,
        total = BigDecimal("0")
      ),
      totalDutyDue = ReturnTotalDutyDue(totalDue = BigDecimal("0")),
      netDutySuspension = None
    )

  def nilReturnDetailsWithEmptySections(periodKey: String, now: Instant): ReturnDetails =
    ReturnDetails(
      identification = ReturnDetailsIdentification(periodKey = periodKey, submittedTime = now),
      alcoholDeclared = ReturnAlcoholDeclared(
        alcoholDeclaredDetails = Some(Seq.empty),
        total = BigDecimal(0)
      ),
      adjustments = ReturnAdjustments(
        adjustmentDetails = Some(Seq.empty),
        total = BigDecimal("0")
      ),
      totalDutyDue = ReturnTotalDutyDue(totalDue = BigDecimal("0")),
      netDutySuspension = None
    )

  def returnWithSpoiltAdjustment(periodKey: String, now: Instant): ReturnDetails = {
    val periodDate = ReturnPeriod.fromPeriodKeyOrThrow(periodKey).periodFromDate()
    ReturnDetails(
      identification = ReturnDetailsIdentification(periodKey = periodKey, submittedTime = now),
      alcoholDeclared = ReturnAlcoholDeclared(
        alcoholDeclaredDetails = Some(Seq.empty),
        total = BigDecimal(0)
      ),
      adjustments = ReturnAdjustments(
        adjustmentDetails = Some(
          Seq(
            ReturnAdjustmentsRow(
              adjustmentTypeKey = ReturnAdjustments.spoiltKey,
              returnPeriodAffected = ReturnPeriod.fromDateInPeriod(periodFrom(3, periodDate)).toPeriodKey,
              taxType = "333",
              litresOfPureAlcohol = BigDecimal(150),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("-3151.50")
            ),
            ReturnAdjustmentsRow(
              adjustmentTypeKey = ReturnAdjustments.spoiltKey,
              returnPeriodAffected = ReturnPeriod.fromDateInPeriod(periodFrom(1, periodDate)).toPeriodKey,
              taxType = "123",
              litresOfPureAlcohol = BigDecimal(150),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("-3151.50")
            )
          )
        ),
        total = BigDecimal("-3151.50")
      ),
      totalDutyDue = ReturnTotalDutyDue(totalDue = BigDecimal("-6303.00")),
      netDutySuspension = None
    )
  }

  val obligationDataSingleOpen = ObligationData(
    ObligationStatus.Open,
    LocalDate.of(2024, 8, 1),
    LocalDate.of(2024, 8, 31),
    LocalDate.of(2024, 9, 25),
    periodKeyAug
  )

  val obligationDataSingleFulfilled = ObligationData(
    ObligationStatus.Fulfilled,
    LocalDate.of(2024, 7, 1),
    LocalDate.of(2024, 7, 31),
    LocalDate.of(2024, 7, 25),
    periodKeyJul
  )

  val multipleOpenObligations = Seq(
    obligationDataSingleOpen.copy(dueDate = LocalDate.of(2024, 11, 30), periodKey = "24AK"),
    obligationDataSingleOpen.copy(dueDate = LocalDate.of(2024, 12, 30), periodKey = "24AL"),
    obligationDataSingleOpen.copy(dueDate = LocalDate.of(2024, 9, 30), periodKey = "24AI"),
    obligationDataSingleOpen,
    obligationDataSingleOpen.copy(dueDate = LocalDate.of(2024, 10, 28), periodKey = "24AJ")
  )

  val multipleFulfilledObligations                                       = Seq(
    obligationDataSingleFulfilled,
    obligationDataSingleFulfilled.copy(dueDate = LocalDate.of(2023, 6, 30), periodKey = "24AE"),
    obligationDataSingleFulfilled.copy(dueDate = LocalDate.of(2023, 7, 30), periodKey = "24AF"),
    obligationDataSingleFulfilled.copy(dueDate = LocalDate.of(2023, 5, 30), periodKey = "24AD")
  )
  def obligationDataSingleOpenDueToday(today: LocalDate): ObligationData =
    ObligationData(
      ObligationStatus.Open,
      today.withDayOfMonth(1),
      today.withDayOfMonth(28),
      today,
      ReturnPeriod(YearMonth.of(today.getYear, today.getMonth)).toPeriodKey
    )

  def obligationDataSingleOpenOverdue(today: LocalDate): ObligationData =
    ObligationData(
      ObligationStatus.Open,
      today.withDayOfMonth(1),
      today.withDayOfMonth(28),
      today.minusDays(1),
      ReturnPeriod(YearMonth.of(today.getYear, today.getMonth)).toPeriodKey
    )

  val chargeReference = "XA" + listOfN(10, numChar).sample.get.toString()

  val outstandingPartialPayment = OutstandingPayment(
    Return,
    LocalDate.of(9998, 7, 25),
    Some(chargeReference),
    BigDecimal(3234.12)
  )

  val outstandingDuePayment = OutstandingPayment(
    Return,
    LocalDate.of(9999, 6, 25),
    Some(chargeReference),
    BigDecimal(4773.34)
  )

  val outstandingOverduePartialPayment = OutstandingPayment(
    Return,
    LocalDate.of(2022, 9, 25),
    Some(chargeReference),
    BigDecimal(4773.34)
  )

  val outstandingCreditPayment = OutstandingPayment(
    Return,
    LocalDate.of(2024, 10, 25),
    Some(chargeReference),
    BigDecimal(-4773.34)
  )

  val outstandingLPIPayment = OutstandingPayment(
    LPI,
    LocalDate.of(9997, 8, 25),
    Some(chargeReference),
    BigDecimal(3234.18)
  )

  val RPIPayment = OutstandingPayment(
    RPI,
    LocalDate.of(2024, 7, 25),
    Some(chargeReference),
    BigDecimal(-2011)
  )

  val openPaymentsData = OpenPayments(
    outstandingPayments = Seq(
      outstandingCreditPayment,
      outstandingPartialPayment,
      outstandingLPIPayment,
      RPIPayment,
      outstandingOverduePartialPayment,
      outstandingDuePayment
    ),
    unallocatedPayments = Seq(
      UnallocatedPayment(LocalDate.of(2024, 9, 25), BigDecimal(-123)),
      UnallocatedPayment(LocalDate.of(2024, 8, 25), BigDecimal(-1273)),
      UnallocatedPayment(LocalDate.of(2024, 7, 25), BigDecimal(-1273))
    ),
    totalOpenPaymentsAmount = BigDecimal(12134.67),
    totalUnallocatedPayments = BigDecimal(134.67),
    totalOutstandingPayments = BigDecimal(1234.67)
  )

  val openPaymentsWithoutUnallocatedData = OpenPayments(
    outstandingPayments = Seq(
      outstandingCreditPayment,
      outstandingPartialPayment
    ),
    unallocatedPayments = Seq.empty,
    totalOpenPaymentsAmount = BigDecimal(12134.67),
    totalUnallocatedPayments = BigDecimal(0),
    totalOutstandingPayments = BigDecimal(1234.67)
  )

  val outstandingPaymentMissingChargeReference = OutstandingPayment(
    Return,
    LocalDate.of(9999, 6, 25),
    None,
    BigDecimal(4773.34)
  )

  val emptyOutstandingPaymentData = OpenPayments(
    outstandingPayments = Seq.empty,
    unallocatedPayments = Seq.empty,
    totalOpenPaymentsAmount = BigDecimal(0),
    totalUnallocatedPayments = BigDecimal(0),
    totalOutstandingPayments = BigDecimal(0)
  )

  val historicReturnPayment =
    HistoricPayment(ReturnPeriod(YearMonth.of(2024, Month.DECEMBER)), Return, Some(chargeReference), BigDecimal(123.45))
  val historicLPIPayment    =
    HistoricPayment(ReturnPeriod(YearMonth.of(2024, Month.NOVEMBER)), LPI, Some(chargeReference), BigDecimal(12.45))
  val historicRPIPayment    =
    HistoricPayment(ReturnPeriod(YearMonth.of(2024, Month.OCTOBER)), RPI, Some(chargeReference), BigDecimal(-123.45))
  val historicRefundPayment = HistoricPayment(
    ReturnPeriod(YearMonth.of(2024, Month.SEPTEMBER)),
    Return,
    Some(chargeReference),
    BigDecimal(-1236.45)
  )

  val historicPayments =
    HistoricPayments(2024, Seq(historicReturnPayment, historicLPIPayment, historicRPIPayment, historicRefundPayment))

  val emptyHistoricPayment = HistoricPayments(2024, Seq.empty)

  val currentDate    = LocalDate.now()
  val paymentDueDate = LocalDate.of(currentDate.getYear, currentDate.getMonth, 25)

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

  val fullUserAnswers: UserAnswers = UserAnswers(
    ReturnId(appaId, quarterReturnPeriodGen.sample.get.toPeriodKey),
    groupId,
    internalId,
    regimes = AlcoholRegimes(Set(Beer, Cider, Wine, Spirits, OtherFermentedProduct)),
    data = Json.obj(
      DeclareAlcoholDutyQuestionPage.toString             -> true,
      AlcoholDutyPage.toString                            -> Json.obj(
        "Beer"                  -> Json.obj(
          "dutiesByTaxType" -> Json.arr(
            Json.obj(
              "taxType"     -> "311",
              "totalLitres" -> 100,
              "pureAlcohol" -> 10,
              "dutyRate"    -> 9.27,
              "dutyDue"     -> 92.7
            ),
            Json.obj(
              "taxType"     -> "361",
              "totalLitres" -> 100,
              "pureAlcohol" -> 10,
              "dutyRate"    -> 10.01,
              "dutyDue"     -> 100.1
            )
          ),
          "totalDuty"       -> 192.8
        ),
        "Cider"                 -> Json.obj(
          "dutiesByTaxType" -> Json.arr(
            Json.obj(
              "taxType"     -> "312",
              "totalLitres" -> 200,
              "pureAlcohol" -> 20,
              "dutyRate"    -> 9.27,
              "dutyDue"     -> 185.4
            ),
            Json.obj(
              "taxType"     -> "372",
              "totalLitres" -> 300,
              "pureAlcohol" -> 30,
              "dutyRate"    -> 3.03,
              "dutyDue"     -> 90.9
            )
          ),
          "totalDuty"       -> 276.3
        ),
        "Wine"                  -> Json.obj(
          "dutiesByTaxType" -> Json.arr(
            Json.obj(
              "taxType"     -> "313",
              "totalLitres" -> 110,
              "pureAlcohol" -> 10,
              "dutyRate"    -> 9.27,
              "dutyDue"     -> 92.7
            ),
            Json.obj(
              "taxType"     -> "373",
              "totalLitres" -> 1000,
              "pureAlcohol" -> 100,
              "dutyRate"    -> 10.01,
              "dutyDue"     -> 1001
            )
          ),
          "totalDuty"       -> 1093.7
        ),
        "Spirits"               -> Json.obj(
          "dutiesByTaxType" -> Json.arr(
            Json.obj(
              "taxType"     -> "315",
              "totalLitres" -> 100,
              "pureAlcohol" -> 10,
              "dutyRate"    -> 9.27,
              "dutyDue"     -> 92.7
            )
          ),
          "totalDuty"       -> 92.7
        ),
        "OtherFermentedProduct" -> Json.obj(
          "dutiesByTaxType" -> Json.arr(
            Json.obj(
              "taxType"     -> "314",
              "totalLitres" -> 100,
              "pureAlcohol" -> 10,
              "dutyRate"    -> 9.27,
              "dutyDue"     -> 92.7
            )
          ),
          "totalDuty"       -> 92.7
        )
      ),
      DeclareAdjustmentQuestionPage.toString              -> true,
      AdjustmentEntryListPage.toString                    -> Json.arr(
        Json.obj(
          "adjustmentType"     -> "under-declaration",
          "period"             -> "2023-12",
          "rateBand"           -> Json.obj(
            "taxTypeCode"  -> "311",
            "description"  -> "Beer from 1.3% to 3.4%",
            "rateType"     -> "Core",
            "rate"         -> 9.27,
            "rangeDetails" -> Json.arr(
              Json.obj(
                "alcoholRegime" -> "Beer",
                "abvRanges"     -> Json.arr(
                  Json.obj(
                    "alcoholType" -> "Beer",
                    "minABV"      -> 1.3,
                    "maxABV"      -> 3.4
                  )
                )
              )
            )
          ),
          "totalLitresVolume"  -> 1000,
          "pureAlcoholVolume"  -> 100,
          "duty"               -> 927
        ),
        Json.obj(
          "adjustmentType"     -> "under-declaration",
          "period"             -> "2023-12",
          "rateBand"           -> Json.obj(
            "taxTypeCode"  -> "371",
            "description"  -> "Beer to 3.4%, eligible for small producer relief and draught relief",
            "rateType"     -> "DraughtAndSmallProducerRelief",
            "rangeDetails" -> Json.arr(
              Json.obj(
                "alcoholRegime" -> "Beer",
                "abvRanges"     -> Json.arr(
                  Json.obj(
                    "alcoholType" -> "Beer",
                    "minABV"      -> 1.3,
                    "maxABV"      -> 3.4
                  )
                )
              )
            )
          ),
          "totalLitresVolume"  -> 100,
          "pureAlcoholVolume"  -> 10,
          "sprDutyRate"        -> 9.27,
          "duty"               -> 92.7
        ),
        Json.obj(
          "adjustmentType"     -> "over-declaration",
          "period"             -> "2024-01",
          "rateBand"           -> Json.obj(
            "taxTypeCode"  -> "312",
            "description"  -> "Cider (but not sparkling cider) from 1.3% to 3.4%",
            "rateType"     -> "Core",
            "rate"         -> 9.27,
            "rangeDetails" -> Json.arr(
              Json.obj(
                "alcoholRegime" -> "Cider",
                "abvRanges"     -> Json.arr(
                  Json.obj(
                    "alcoholType" -> "Cider",
                    "minABV"      -> 1.3,
                    "maxABV"      -> 3.4
                  )
                )
              )
            )
          ),
          "totalLitresVolume"  -> 2000,
          "pureAlcoholVolume"  -> 200,
          "duty"               -> -1854
        ),
        Json.obj(
          "adjustmentType"     -> "repackaged-draught-products",
          "period"             -> "2024-02",
          "rateBand"           -> Json.obj(
            "taxTypeCode"  -> "371",
            "description"  -> "Beer to 3.4%, eligible for small producer relief and draught relief",
            "rateType"     -> "DraughtAndSmallProducerRelief",
            "rangeDetails" -> Json.arr(
              Json.obj(
                "alcoholRegime" -> "Beer",
                "abvRanges"     -> Json.arr(
                  Json.obj(
                    "alcoholType" -> "Beer",
                    "minABV"      -> 1.3,
                    "maxABV"      -> 3.4
                  )
                )
              )
            )
          ),
          "totalLitresVolume"  -> 1000,
          "pureAlcoholVolume"  -> 100,
          "sprDutyRate"        -> 3.21,
          "repackagedRateBand" -> Json.obj(
            "taxTypeCode"  -> "311",
            "description"  -> "Beer from 1.3% to 3.4%",
            "rateType"     -> "Core",
            "rate"         -> 9.27,
            "rangeDetails" -> Json.arr(
              Json.obj(
                "alcoholRegime" -> "Beer",
                "abvRanges"     -> Json.arr(
                  Json.obj(
                    "alcoholType" -> "Beer",
                    "minABV"      -> 1.3,
                    "maxABV"      -> 3.4
                  )
                )
              )
            )
          ),
          "duty"               -> 321,
          "repackagedDuty"     -> 927,
          "newDuty"            -> 606
        ),
        Json.obj(
          "adjustmentType"     -> "spoilt",
          "period"             -> "2024-03",
          "rateBand"           -> Json.obj(
            "taxTypeCode"  -> "314",
            "description"  -> "Other fermented products like fruit ciders from 1.3% to 3.4%",
            "rateType"     -> "Core",
            "rate"         -> 9.27,
            "rangeDetails" -> Json.arr(
              Json.obj(
                "alcoholRegime" -> "OtherFermentedProduct",
                "abvRanges"     -> Json.arr(
                  Json.obj(
                    "alcoholType" -> "OtherFermentedProduct",
                    "minABV"      -> 1.3,
                    "maxABV"      -> 3.4
                  )
                )
              )
            )
          ),
          "totalLitresVolume"  -> 100,
          "pureAlcoholVolume"  -> 10,
          "duty"               -> -92.7
        ),
        Json.obj(
          "adjustmentType"     -> "drawback",
          "period"             -> "2024-04",
          "rateBand"           -> Json.obj(
            "taxTypeCode"  -> "313",
            "description"  -> "Wine (including sparkling wine) from 1.3% to 3.4%",
            "rateType"     -> "Core",
            "rate"         -> 9.27,
            "rangeDetails" -> Json.arr(
              Json.obj(
                "alcoholRegime" -> "Wine",
                "abvRanges"     -> Json.arr(
                  Json.obj(
                    "alcoholType" -> "Wine",
                    "minABV"      -> 1.3,
                    "maxABV"      -> 3.4
                  )
                )
              )
            )
          ),
          "totalLitresVolume"  -> 210,
          "pureAlcoholVolume"  -> 21,
          "duty"               -> -194.67
        )
      ),
      AdjustmentTotalPage.toString                        -> -515.67,
      UnderDeclarationTotalPage.toString                  -> 1019.7,
      UnderDeclarationReasonPage.toString                 -> "Reason for under declaration",
      OverDeclarationTotalPage.toString                   -> -1854,
      OverDeclarationReasonPage.toString                  -> "Reason for over declaration",
      DeclareDutySuspendedDeliveriesQuestionPage.toString -> true,
      DutySuspendedBeerPage.toString                      -> Json.obj(
        "totalBeer"         -> 1000,
        "pureAlcoholInBeer" -> 100
      ),
      DutySuspendedCiderPage.toString                     -> Json.obj(
        "totalCider"         -> 2000,
        "pureAlcoholInCider" -> 200
      ),
      DutySuspendedSpiritsPage.toString                   -> Json.obj(
        "totalSpirits"         -> 1000,
        "pureAlcoholInSpirits" -> 100
      ),
      DutySuspendedWinePage.toString                      -> Json.obj(
        "totalWine"         -> 1000,
        "pureAlcoholInWine" -> 100
      ),
      DutySuspendedOtherFermentedPage.toString            -> Json.obj(
        "totalOtherFermented"         -> 1000,
        "pureAlcoholInOtherFermented" -> 100
      ),
      DeclareQuarterlySpiritsPage.toString                -> true,
      DeclareSpiritsTotalPage.toString                    -> 1234,
      WhiskyPage.toString                                 -> Json.obj(
        "scotchWhisky" -> 111,
        "irishWhiskey" -> 222
      ),
      SpiritTypePage.toString                             -> Json.arr(
        "neutralIndustrialOrigin",
        "other",
        "neutralAgriculturalOrigin",
        "ciderOrPerry",
        "wineOrMadeWine",
        "beer",
        "maltSpirits",
        "grainSpirits"
      ),
      OtherSpiritsProducedPage.toString                   -> "Other Type of Spirit",
      GrainsUsedPage.toString                             -> Json.obj(
        "maltedBarleyQuantity"     -> 1111,
        "wheatQuantity"            -> 2222,
        "maizeQuantity"            -> 3333,
        "ryeQuantity"              -> 4444,
        "unmaltedGrainQuantity"    -> 5555,
        "usedMaltedGrainNotBarley" -> true
      ),
      OtherMaltedGrainsPage.toString                      -> Json.obj(
        "otherMaltedGrainsTypes"    -> "Other malted grains",
        "otherMaltedGrainsQuantity" -> 6666
      ),
      AlcoholUsedPage.toString                            -> Json.obj(
        "beer"         -> 1111,
        "wine"         -> 2222,
        "madeWine"     -> 3333,
        "ciderOrPerry" -> 4444
      ),
      EthyleneGasOrMolassesUsedPage.toString              -> Json.obj(
        "ethyleneGas"      -> 6666,
        "molasses"         -> 7777,
        "otherIngredients" -> true
      ),
      OtherIngredientsUsedPage.toString                   -> Json.obj(
        "otherIngredientsUsedTypes"    -> "Other Ingredient",
        "otherIngredientsUsedUnit"     -> "Tonnes",
        "otherIngredientsUsedQuantity" -> 4321
      )
    )
  )

  val fullReturn =
    AdrReturnSubmission(
      AdrDutyDeclared(
        true,
        List(
          AdrDutyDeclaredItem(AdrAlcoholQuantity(100, 10), AdrDuty("315", 9.27, 92.7)),
          AdrDutyDeclaredItem(AdrAlcoholQuantity(110, 10), AdrDuty("313", 9.27, 92.7)),
          AdrDutyDeclaredItem(AdrAlcoholQuantity(1000, 100), AdrDuty("373", 10.01, 1001)),
          AdrDutyDeclaredItem(AdrAlcoholQuantity(200, 20), AdrDuty("312", 9.27, 185.4)),
          AdrDutyDeclaredItem(AdrAlcoholQuantity(300, 30), AdrDuty("372", 3.03, 90.9)),
          AdrDutyDeclaredItem(AdrAlcoholQuantity(100, 10), AdrDuty("314", 9.27, 92.7)),
          AdrDutyDeclaredItem(AdrAlcoholQuantity(100, 10), AdrDuty("311", 9.27, 92.7)),
          AdrDutyDeclaredItem(AdrAlcoholQuantity(100, 10), AdrDuty("361", 10.01, 100.1))
        )
      ),
      AdrAdjustments(
        true,
        Some("Reason for over declaration"),
        List(AdrAdjustmentItem("24AA", AdrAlcoholQuantity(2000, 200), AdrDuty("312", 9.27, -1854))),
        true,
        Some("Reason for under declaration"),
        List(
          AdrAdjustmentItem("23AL", AdrAlcoholQuantity(1000, 100), AdrDuty("311", 9.27, 927)),
          AdrAdjustmentItem("23AL", AdrAlcoholQuantity(100, 10), AdrDuty("371", 9.27, 92.7))
        ),
        true,
        List(AdrAdjustmentItem("24AC", AdrAlcoholQuantity(100, 10), AdrDuty("314", 9.27, -92.7))),
        true,
        List(AdrAdjustmentItem("24AD", AdrAlcoholQuantity(210, 21), AdrDuty("313", 9.27, -194.67))),
        true,
        List(AdrRepackagedDraughtAdjustmentItem("24AB", "371", 3.21, "311", 9.27, AdrAlcoholQuantity(1000, 100), 606))
      ),
      AdrDutySuspended(
        true,
        List(
          AdrDutySuspendedProduct(AdrDutySuspendedAlcoholRegime.Beer, AdrAlcoholQuantity(1000, 100)),
          AdrDutySuspendedProduct(AdrDutySuspendedAlcoholRegime.Cider, AdrAlcoholQuantity(2000, 200)),
          AdrDutySuspendedProduct(AdrDutySuspendedAlcoholRegime.Spirits, AdrAlcoholQuantity(1000, 100)),
          AdrDutySuspendedProduct(AdrDutySuspendedAlcoholRegime.Wine, AdrAlcoholQuantity(1000, 100)),
          AdrDutySuspendedProduct(AdrDutySuspendedAlcoholRegime.OtherFermentedProduct, AdrAlcoholQuantity(1000, 100))
        )
      ),
      Some(
        AdrSpirits(
          true,
          Some(
            AdrSpiritsProduced(
              AdrSpiritsVolumes(1234, 111, 222),
              Set(
                AdrTypeOfSpirit.Other,
                AdrTypeOfSpirit.CiderOrPerry,
                AdrTypeOfSpirit.NeutralAgricultural,
                AdrTypeOfSpirit.WineOrMadeWine,
                AdrTypeOfSpirit.Grain,
                AdrTypeOfSpirit.NeutralIndustrial,
                AdrTypeOfSpirit.Beer,
                AdrTypeOfSpirit.Malt
              ),
              Some("Other Type of Spirit"),
              true,
              AdrSpiritsGrainsQuantities(Some(1111), Some(6666), Some(2222), Some(3333), Some(4444), Some(5555)),
              Some("Other malted grains"),
              AdrSpiritsIngredientsVolumes(Some(6666), Some(7777), Some(1111), Some(2222), Some(3333), Some(4444)),
              Some(AdrOtherIngredient(4321, Tonnes, "Other Ingredient"))
            )
          )
        )
      ),
      AdrTotals(1748.2, -1854, 1019.7, -92.7, -194.67, 606, 1232.53)
    )

  val fullRepackageAdjustmentEntry: AdjustmentEntry = AdjustmentEntry(
    index = Some(0),
    adjustmentType = Some(AdjustmentType.RepackagedDraughtProducts),
    period = Some(returnPeriodMar.period),
    rateBand = Some(
      RateBand(
        taxTypeCode = "001",
        description = "",
        rateType = RateType.DraughtAndSmallProducerRelief,
        rate = Some(BigDecimal(1)),
        rangeDetails = Set(
          RangeDetailsByRegime(
            alcoholRegime = AlcoholRegime.Beer,
            abvRanges = NonEmptySeq.one(
              ABVRange(
                AlcoholType.Beer,
                minABV = AlcoholByVolume(BigDecimal(1)),
                maxABV = AlcoholByVolume(BigDecimal(1))
              )
            )
          )
        )
      )
    ),
    totalLitresVolume = Some(BigDecimal(0)),
    pureAlcoholVolume = Some(BigDecimal(1)),
    sprDutyRate = Some(BigDecimal(1)),
    repackagedRateBand = Some(
      RateBand(
        taxTypeCode = "002",
        description = "",
        rateType = RateType.Core,
        rate = Some(BigDecimal(1)),
        rangeDetails = Set(
          RangeDetailsByRegime(
            alcoholRegime = AlcoholRegime.Beer,
            abvRanges = NonEmptySeq.one(
              ABVRange(
                AlcoholType.Beer,
                minABV = AlcoholByVolume(BigDecimal(1)),
                maxABV = AlcoholByVolume(BigDecimal(1))
              )
            )
          )
        )
      )
    ),
    repackagedSprDutyRate = Some(BigDecimal(1)),
    duty = Some(BigDecimal(1)),
    repackagedDuty = Some(BigDecimal(1)),
    newDuty = Some(BigDecimal(1))
  )

  val fullAdjustmentEntry: AdjustmentEntry = fullRepackageAdjustmentEntry.copy(
    adjustmentType = Some(AdjustmentType.Underdeclaration),
    repackagedRateBand = None,
    repackagedSprDutyRate = None,
    repackagedDuty = None,
    newDuty = None
  )

  val coreRateBand: RateBand = RateBand(
    "123",
    "core description",
    RateType.Core,
    Some(BigDecimal(1.1)),
    Set(
      RangeDetailsByRegime(
        Beer,
        NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(1)), AlcoholByVolume(BigDecimal(2))))
      )
    )
  )

  val coreRateBand2: RateBand = RateBand(
    "124",
    "core description",
    RateType.Core,
    Some(BigDecimal(1.1)),
    Set(
      RangeDetailsByRegime(
        Beer,
        NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(1)), AlcoholByVolume(BigDecimal(2))))
      )
    )
  )

  val coreRateBand3: RateBand = RateBand(
    "125",
    "core description",
    RateType.Core,
    Some(BigDecimal(1.1)),
    Set(
      RangeDetailsByRegime(
        Beer,
        NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(1)), AlcoholByVolume(BigDecimal(2))))
      )
    )
  )

  val draughtReliefRateBand: RateBand = RateBand(
    "124",
    "draught relief description",
    RateType.DraughtRelief,
    Some(BigDecimal(2.1)),
    Set(
      RangeDetailsByRegime(
        Beer,
        NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(2)), AlcoholByVolume(BigDecimal(3))))
      )
    )
  )

  val smallProducerReliefRateBand: RateBand = RateBand(
    "125",
    "small producer relief description",
    RateType.SmallProducerRelief,
    Some(BigDecimal(3.1)),
    Set(
      RangeDetailsByRegime(
        Beer,
        NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(3)), AlcoholByVolume(BigDecimal(4))))
      )
    )
  )

  val smallProducerReliefRateBand2: RateBand = RateBand(
    "127",
    "small producer relief description",
    RateType.SmallProducerRelief,
    Some(BigDecimal(6.5)),
    Set(
      RangeDetailsByRegime(
        Beer,
        NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(6)), AlcoholByVolume(BigDecimal(7))))
      )
    )
  )

  val draughtAndSmallProducerReliefRateBand: RateBand = RateBand(
    "126",
    "draught and small producer relief description",
    RateType.DraughtAndSmallProducerRelief,
    Some(BigDecimal(4.1)),
    Set(
      RangeDetailsByRegime(
        Beer,
        NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(4)), AlcoholByVolume(BigDecimal(5))))
      )
    )
  )

  val draughtAndSmallProducerReliefRateBand2: RateBand = RateBand(
    "128",
    "draught and small producer relief description",
    RateType.DraughtAndSmallProducerRelief,
    Some(BigDecimal(1.3)),
    Set(
      RangeDetailsByRegime(
        Beer,
        NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(1)), AlcoholByVolume(BigDecimal(2))))
      )
    )
  )

  val allRateBands: Set[RateBand] =
    Set(coreRateBand, draughtReliefRateBand, smallProducerReliefRateBand, draughtAndSmallProducerReliefRateBand)

  val volumeAndRateByTaxType1 = VolumeAndRateByTaxType(
    taxType = "124",
    totalLitres = BigDecimal(100),
    pureAlcohol = BigDecimal(2.5),
    dutyRate = BigDecimal(1.26)
  )
  val volumeAndRateByTaxType2 = VolumeAndRateByTaxType(
    taxType = "125",
    totalLitres = BigDecimal(1000),
    pureAlcohol = BigDecimal(3.5),
    dutyRate = BigDecimal(1.46)
  )
  val volumeAndRateByTaxType3 = VolumeAndRateByTaxType(
    taxType = "126",
    totalLitres = BigDecimal(10000),
    pureAlcohol = BigDecimal(4.5),
    dutyRate = BigDecimal(1.66)
  )
  val volumeAndRateByTaxType4 = VolumeAndRateByTaxType(
    taxType = "126",
    totalLitres = BigDecimal(20000),
    pureAlcohol = BigDecimal(4.8),
    dutyRate = BigDecimal(1.66)
  )

  val allVolumeAndRateByTaxType =
    Seq(volumeAndRateByTaxType1, volumeAndRateByTaxType2, volumeAndRateByTaxType3, volumeAndRateByTaxType4)
}
