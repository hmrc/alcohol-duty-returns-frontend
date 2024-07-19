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

import generators.ModelGenerators
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.returns.{ReturnAdjustments, ReturnAdjustmentsRow, ReturnAlcoholDeclared, ReturnAlcoholDeclaredRow, ReturnDetails, ReturnDetailsIdentification, ReturnTotalDutyDue}
import models.{AlcoholRegimes, ObligationData, ObligationStatus, ReturnId, ReturnPeriod, UserAnswers}
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails

import java.time.{Clock, Instant, LocalDate, ZoneId}

trait TestData extends ModelGenerators {
  val clock              = Clock.fixed(Instant.ofEpochMilli(1718118467838L), ZoneId.of("UTC"))
  val appaId: String     = appaIdGen.sample.get
  val periodKey: String  = periodKeyGen.sample.get
  val groupId: String    = "groupid"
  val internalId: String = "id"
  val returnId: ReturnId = ReturnId(appaId, periodKey)

  val returnAndUserDetails = ReturnAndUserDetails(returnId, groupId, internalId)

  val returnPeriod = returnPeriodGen.sample.get

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

  val quarterPeriodKeys    = Seq(periodKeyMar, periodKeyJun, periodKeySep, periodKeyDec)
  val nonQuarterPeriodKeys =
    Seq(periodKeyJan, periodKeyFeb, periodKeyApr, periodKeyMay, periodKeyJul, periodKeyAug, periodKeyOct, periodKeyNov)

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

  def exampleReturnDetails(periodKey: String, now: Instant): ReturnDetails =
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
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(150),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("3151.50")
            ),
            ReturnAdjustmentsRow(
              adjustmentTypeKey = ReturnAdjustments.spoiltKey,
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(1150),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("-24161.50")
            ),
            ReturnAdjustmentsRow(
              adjustmentTypeKey = ReturnAdjustments.spoiltKey,
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(75),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("-1575.50")
            ),
            ReturnAdjustmentsRow(
              adjustmentTypeKey = ReturnAdjustments.repackagedDraughtKey,
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(150),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("3151.50")
            )
          )
        ),
        total = BigDecimal("-19434")
      ),
      totalDutyDue = ReturnTotalDutyDue(totalDue = BigDecimal("55815"))
    )

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
      totalDutyDue = ReturnTotalDutyDue(totalDue = BigDecimal("0"))
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
      totalDutyDue = ReturnTotalDutyDue(totalDue = BigDecimal("0"))
    )

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

  val multipleFulfilledObligations = Seq(
    obligationDataSingleFulfilled,
    obligationDataSingleFulfilled.copy(dueDate = LocalDate.of(2023, 6, 30), periodKey = "24AE"),
    obligationDataSingleFulfilled.copy(dueDate = LocalDate.of(2023, 7, 30), periodKey = "24AF"),
    obligationDataSingleFulfilled.copy(dueDate = LocalDate.of(2023, 5, 30), periodKey = "24AD")
  )
}
