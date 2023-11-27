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

package forms.behaviours

import play.api.data.{Form, FormError}

trait BigDecimalFieldBehaviours extends FieldBehaviours {

  def bigDecimalField(
    form: Form[_],
    fieldName: String,
    nonNumericError: FormError,
    twoDecimalPlacesError: FormError
  ): Unit = {

    "not bind non-numeric numbers" in {

      forAll(nonNumerics -> "nonNumeric") { nonNumeric =>
        val result = form.bind(Map(fieldName -> nonNumeric)).apply(fieldName)
        result.errors must contain only nonNumericError
      }
    }

    "not bind decimals that are not two decimal places" in {

      forAll(decimalsNotTwoDecimalPlaces -> "decimal") { decimal =>
        val result = form.bind(Map(fieldName -> decimal)).apply(fieldName)
        result.errors must contain only twoDecimalPlacesError
      }
    }
  }

  def bigDecimalFieldWithMinimum(
    form: Form[_],
    fieldName: String,
    minimum: BigDecimal,
    expectedError: FormError
  ): Unit =
    s"not bind big decimals below $minimum" in {

      forAll(bigDecimalsBelowValue(minimum) -> "bigDecimalBelowMin") { number: BigDecimal =>
        val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
        result.errors must contain only expectedError
      }
    }

  def bigDecimalFieldWithMaximum(
    form: Form[_],
    fieldName: String,
    maximum: BigDecimal,
    expectedError: FormError
  ): Unit =
    s"not bind big decimals above $maximum" in {

      forAll(bigDecimalsAboveValue(maximum) -> "bigDecimalAboveMax") { number: BigDecimal =>
        val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
        result.errors must contain only expectedError
      }

      def bigDecimalFieldWithRange(
        form: Form[_],
        fieldName: String,
        minimum: BigDecimal,
        maximum: BigDecimal,
        expectedError: FormError
      ): Unit =
        s"not bind big decimals outside the range $minimum to $maximum" in {

          forAll(bigDecimalsOutsideRange(minimum, maximum) -> "bigDecimalOutsideRange") { number =>
            val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
            result.errors must contain only expectedError
          }
        }

    }
}
