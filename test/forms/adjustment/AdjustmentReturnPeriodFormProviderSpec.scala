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

package forms.adjustment

import forms.behaviours.{DateBehaviours, IntFieldBehaviours}
import play.api.data.FormError

import java.time.{LocalDate, YearMonth}

class AdjustmentReturnPeriodFormProviderSpec extends DateBehaviours with IntFieldBehaviours {

  val returnPeriod                = returnPeriodGen.sample.get.period
  val form                        = new AdjustmentReturnPeriodFormProvider()(returnPeriod)
  val invalidYearMonth: YearMonth = YearMonth.of(2023, 7)
  val inputKey                    = "adjustment-return-period-input"

  "input" - {
    val validData = datesBetween(
      min = LocalDate.of(2023, 9, 1),
      max = returnPeriod.minusMonths(1).atEndOfMonth()
    ).map(YearMonth.from(_))

    behave like yearMonthField(form, inputKey, validData)

    behave like mandatoryField(
      form,
      inputKey,
      FormError(inputKey, "adjustmentReturnPeriod.date.error.required.all")
    )

    behave like yearMonthMandatoryMonthField(
      form,
      inputKey,
      FormError(s"$inputKey.month", "adjustmentReturnPeriod.date.error.required.month")
    )

    behave like yearMonthMandatoryYearField(
      form,
      inputKey,
      FormError(s"$inputKey.year", "adjustmentReturnPeriod.date.error.required.year")
    )

    behave like yearMonthFieldInFuture(
      form,
      inputKey,
      returnPeriod,
      FormError(inputKey, "adjustmentReturnPeriod.date.error.invalid.future")
    )

    behave like yearMonthFieldWithMin(
      form,
      inputKey,
      invalidYearMonth,
      FormError(inputKey, "adjustmentReturnPeriod.date.error.invalid.past")
    )

    behave like yearMonthInvalidMonth(
      form,
      inputKey,
      validData,
      FormError(s"$inputKey.month", "adjustmentReturnPeriod.date.error.invalid.nonNumeric.month")
    )

    behave like yearMonthWithMonthOutOfMinRange(
      form,
      inputKey,
      validData,
      FormError(s"$inputKey.month", "adjustmentReturnPeriod.date.error.invalid.month")
    )

    behave like yearMonthWithMonthOutOfMaxRange(
      form,
      inputKey,
      validData,
      FormError(s"$inputKey.month", "adjustmentReturnPeriod.date.error.invalid.month")
    )

    behave like yearMonthInvalidYear(
      form,
      inputKey,
      validData,
      FormError(s"$inputKey.year", "adjustmentReturnPeriod.date.error.invalid.nonNumeric.year")
    )

    behave like yearMonthWithYearOutOfMinRange(
      form,
      inputKey,
      validData,
      FormError(s"$inputKey.year", "adjustmentReturnPeriod.date.error.invalidYear.year")
    )

    behave like yearMonthWithYearOutOfMaxRange(
      form,
      inputKey,
      validData,
      FormError(s"$inputKey.year", "adjustmentReturnPeriod.date.error.invalidYear.year")
    )

  }
}
