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

import java.time.{Clock, LocalDate, YearMonth, ZoneOffset}

class WhenDidYouPayDutyFormProviderSpec extends DateBehaviours with IntFieldBehaviours {

  private val clock    = Clock.fixed(LocalDate.of(2024, 2, 1).atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
  val form             = new WhenDidYouPayDutyFormProvider()()
  val invalidYearMonth = YearMonth.of(2023, 8)

  "input" - {
    val validData = datesBetween(
      min = LocalDate.of(2023, 9, 1),
      max = LocalDate.now(clock).minusMonths(1)
    ).map(YearMonth.from(_))

    behave like yearMonthField(form, "when-did-you-pay-duty-input", validData)

    behave like mandatoryField(
      form,
      "when-did-you-pay-duty-input",
      FormError("when-did-you-pay-duty-input", "whenDidYouPayDuty.date.error.required.all")
    )

    behave like yearMonthFieldInFuture(
      form,
      "when-did-you-pay-duty-input",
      FormError("when-did-you-pay-duty-input", "whenDidYouPayDuty.date.error.invalid.future")
    )

    behave like yearMonthFieldWithMin(
      form,
      "when-did-you-pay-duty-input",
      invalidYearMonth,
      FormError("when-did-you-pay-duty-input", "whenDidYouPayDuty.date.error.invalid.past")
    )

    behave like yearMonthInvalidMonth(
      form,
      "when-did-you-pay-duty-input",
      validData,
      FormError("when-did-you-pay-duty-input.month", "whenDidYouPayDuty.date.error.invalid.month")
    )

    behave like yearMonthWithMonthOutOfMinRange(
      form,
      "when-did-you-pay-duty-input",
      validData,
      FormError("when-did-you-pay-duty-input.month", "whenDidYouPayDuty.date.error.invalid.month")
    )

    behave like yearMonthWithMonthOutOfMaxRange(
      form,
      "when-did-you-pay-duty-input",
      validData,
      FormError("when-did-you-pay-duty-input.month", "whenDidYouPayDuty.date.error.invalid.month")
    )

    behave like yearMonthInvalidYear(
      form,
      "when-did-you-pay-duty-input",
      validData,
      FormError("when-did-you-pay-duty-input.year", "whenDidYouPayDuty.date.error.invalid.year")
    )

    behave like yearMonthWithYearOutOfMinRange(
      form,
      "when-did-you-pay-duty-input",
      validData,
      FormError("when-did-you-pay-duty-input.year", "whenDidYouPayDuty.date.error.invalid.year")
    )

    behave like yearMonthWithYearOutOfMaxRange(
      form,
      "when-did-you-pay-duty-input",
      validData,
      FormError("when-did-you-pay-duty-input.year", "whenDidYouPayDuty.date.error.invalid.year")
    )

  }
}
