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

package forms.mappings

import base.SpecBase
import org.scalatest.EitherValues
import play.api.data.FormError

class BigDecimalFieldFormatterSpec extends SpecBase with EitherValues {

  val requiredKey = "error.required"
  val invalidKey = "error.invalid"
  val decimalPlacesKey = "error.decimalPlaces"
  val minimumValueKey = "error.minimum"
  val maximumValueKey = "error.maximum"
  val fieldKey = "testField"

  val formatter = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    fieldKey
  )

  "BigDecimalFieldFormatter" - {

    "must bind a valid BigDecimal value" in {
      val result = formatter.bind("field", Map("field" -> "123.45"))
      result.value mustEqual BigDecimal(123.45)
    }

    "must return an error for a missing value" in {
      val result = formatter.bind("field", Map.empty)
      result.left.value mustEqual Seq(FormError("field", s"$requiredKey.$fieldKey"))
    }

    "must return an error for an invalid BigDecimal value" in {
      val result = formatter.bind("field", Map("field" -> "invalid"))
      result.left.value mustEqual Seq(FormError("field", s"$invalidKey.$fieldKey"))
    }

    "must return an error when value is below the minimum" in {
      val result = formatter.bind("field", Map("field" -> "0.009"))
      result.left.value mustEqual Seq(FormError("field", s"$minimumValueKey.$fieldKey"))
    }

    "must return an error when value exceeds the maximum" in {
      val result = formatter.bind("field", Map("field" -> "1000000000.00"))
      result.left.value mustEqual Seq(FormError("field", s"$maximumValueKey.$fieldKey"))
    }

    "must return an error when value has too many decimal places" in {
      val result = formatter.bind("field", Map("field" -> "123.456"))
      result.left.value mustEqual Seq(FormError("field", s"$decimalPlacesKey.$fieldKey"))
    }

    "must unbind a BigDecimal value" in {
      val result = formatter.unbind("field", BigDecimal(123.45))
      result mustEqual Map("field" -> "123.45")
    }
  }
}
