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

import forms.behaviours.BigDecimalFieldBehaviours
import play.api.data.FormError
import scala.collection.immutable.ArraySeq

class AlcoholByVolumeFormProviderSpec extends BigDecimalFieldBehaviours {

  val form = new AlcoholByVolumeFormProvider()()

  ".value" - {

    val fieldName = "alcoholByVolume-input"

    val minimum = 0.01
    val maximum = 100

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "alcoholByVolume.error.nonNumeric"),
      twoDecimalPlacesError = FormError(fieldName, "alcoholByVolume.error.twoDecimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum       = minimum,
      expectedError = FormError(fieldName, "alcoholByVolume.error.minimumRequired", ArraySeq(minimum))
    )
    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      expectedError = FormError(fieldName, "alcoholByVolume.error.maximumRequired", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "alcoholByVolume.error.required")
    )
  }
}
