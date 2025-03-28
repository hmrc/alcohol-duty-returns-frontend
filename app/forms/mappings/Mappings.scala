/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.mappings

import models.declareDuty.{VolumeAndRateByTaxType, VolumesByTaxType}

import java.time.{LocalDate, YearMonth}
import play.api.data.FieldMapping
import play.api.data.Forms.of
import models.Enumerable
import models.adjustment.{AdjustmentVolume, AdjustmentVolumeWithSPR, SpoiltVolumeWithDuty}

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(stringFormatter(errorKey, args))

  protected def int(
    requiredKey: String = "error.required",
    wholeNumberKey: String = "error.wholeNumber",
    nonNumericKey: String = "error.nonNumeric",
    args: Seq[String] = Seq.empty
  ): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey, args))

  protected def bigDecimal(
    decimalPlaces: Int = 2,
    requiredKey: String = "error.required",
    nonNumericKey: String = "error.nonNumeric",
    decimalPlacesKey: String = "error.decimalPlaces",
    args: Seq[String] = Seq.empty
  ): FieldMapping[BigDecimal] =
    of(bigDecimalFormatter(decimalPlaces, requiredKey, nonNumericKey, decimalPlacesKey, args))

  protected def boolean(
    requiredKey: String = "error.required",
    invalidKey: String = "error.boolean",
    args: Seq[String] = Seq.empty
  ): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey, args))

  protected def enumerable[A](
    requiredKey: String = "error.required",
    invalidKey: String = "error.invalid",
    args: Seq[String] = Seq.empty
  )(implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey, args))

  protected def localDate(
    invalidKey: String,
    allRequiredKey: String,
    twoRequiredKey: String,
    requiredKey: String,
    args: Seq[String] = Seq.empty
  ): FieldMapping[LocalDate] =
    of(new LocalDateFormatter(invalidKey, allRequiredKey, twoRequiredKey, requiredKey, args))

  protected def yearMonth(
    invalidKey: String,
    allRequiredKey: String,
    requiredKey: String,
    invalidYear: String,
    args: Seq[String] = Seq.empty
  ): FieldMapping[YearMonth] =
    of(new YearMonthFormatter(invalidKey, allRequiredKey, requiredKey, invalidYear, args))

  protected def adjustmentVolumes(
    invalidKey: String,
    requiredKey: String,
    decimalPlacesKey: String,
    minimumValueKey: String,
    maximumValueKey: String,
    inconsistentKey: String,
    args: Seq[String] = Seq.empty
  ): FieldMapping[AdjustmentVolume] =
    of(
      new AdjustmentVolumesFormatter(
        invalidKey,
        requiredKey,
        decimalPlacesKey,
        minimumValueKey,
        maximumValueKey,
        inconsistentKey,
        args
      )
    )

  protected def adjustmentVolumesWithRate(
    invalidKey: String,
    requiredKey: String,
    decimalPlacesKey: String,
    minimumValueKey: String,
    maximumValueKey: String,
    inconsistentKey: String,
    args: Seq[String] = Seq.empty
  ): FieldMapping[AdjustmentVolumeWithSPR] =
    of(
      new AdjustmentVolumesAndRateFormatter(
        invalidKey,
        requiredKey,
        decimalPlacesKey,
        minimumValueKey,
        maximumValueKey,
        inconsistentKey,
        args
      )
    )

  protected def volumes(
    invalidKey: String,
    requiredKey: String,
    decimalPlacesKey: String,
    minimumValueKey: String,
    maximumValueKey: String,
    lessOrEqualKey: String,
    regimeName: String
  ): FieldMapping[VolumesByTaxType] =
    of(
      new VolumesFormatter(
        invalidKey,
        requiredKey,
        decimalPlacesKey,
        minimumValueKey,
        maximumValueKey,
        lessOrEqualKey,
        regimeName
      )
    )

  protected def volumesWithRate(
    invalidKey: String,
    requiredKey: String,
    decimalPlacesKey: String,
    minimumValueKey: String,
    maximumValueKey: String,
    lessOrEqualKey: String,
    regimeName: String
  ): FieldMapping[VolumeAndRateByTaxType] =
    of(
      new VolumesAndRateFormatter(
        invalidKey,
        requiredKey,
        decimalPlacesKey,
        minimumValueKey,
        maximumValueKey,
        lessOrEqualKey,
        regimeName
      )
    )

  protected def volumesWithRateMultipleSPRSelection(
    invalidKey: String,
    requiredKey: String,
    decimalPlacesKey: String,
    minimumValueKey: String,
    maximumValueKey: String,
    lessOrEqualKey: String,
    regimeName: String
  ): FieldMapping[VolumeAndRateByTaxType] =
    of(
      new VolumesAndRateFormatterMultipleSPRSelect(
        invalidKey,
        requiredKey,
        decimalPlacesKey,
        minimumValueKey,
        maximumValueKey,
        lessOrEqualKey,
        regimeName
      )
    )

  protected def spoiltVolumesWithDuty(
    invalidKey: String,
    requiredKey: String,
    decimalPlacesKey: String,
    minimumValueKey: String,
    maximumValueKey: String,
    inconsistentKey: String,
    args: Seq[String] = Seq.empty
  ): FieldMapping[SpoiltVolumeWithDuty] =
    of(
      new SpoiltVolumesAndDutyFormatter(
        invalidKey,
        requiredKey,
        decimalPlacesKey,
        minimumValueKey,
        maximumValueKey,
        inconsistentKey,
        args
      )
    )
}
