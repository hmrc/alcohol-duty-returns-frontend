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

package forms.dutySuspended

import forms.behaviours.BigDecimalFieldBehaviours
import models.dutySuspended.DutySuspendedVolume
import org.mockito.MockitoSugar.mock
import play.api.data.FormError
import play.api.i18n.Messages

class DutySuspendedFormProviderSpec extends BigDecimalFieldBehaviours {
  val regime   = regimeGen.sample.value
  val messages = mock[Messages]
  val form     = new DutySuspendedFormProvider()(regime)(messages)

  ".volumes" - {
    "must bind valid data" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "1",
        "volumes.pureAlcoholVolume" -> "1"
      )
      form.bind(data).value.value mustBe DutySuspendedVolume(1, 1)
    }

    "must unbind valid data" in {
      val data = DutySuspendedVolume(1, 1)
      form.fill(data).data must contain theSameElementsAs Map(
        "volumes.totalLitresVolume" -> "1",
        "volumes.pureAlcoholVolume" -> "1"
      )
    }

    "fail to bind when no answers are selected" in {
      val data = Map.empty[String, String]
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitresVolume", "dutySuspendedVolume.error.noValue.totalLitresVolume", Seq("")),
        FormError("volumes_pureAlcoholVolume", "dutySuspendedVolume.error.noValue.pureAlcoholVolume", Seq(""))
      )
    }

    "fail to bind when blank answer provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "",
        "volumes.pureAlcoholVolume" -> ""
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitresVolume", "dutySuspendedVolume.error.noValue.totalLitresVolume", Seq("")),
        FormError("volumes_pureAlcoholVolume", "dutySuspendedVolume.error.noValue.pureAlcoholVolume", Seq(""))
      )
    }

    "fail to bind when values with too many decimal places are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "1.111",
        "volumes.pureAlcoholVolume" -> "1.11234"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitresVolume", s"dutySuspendedVolume.error.decimalPlaces.totalLitresVolume", Seq("")),
        FormError("volumes_pureAlcoholVolume", s"dutySuspendedVolume.error.decimalPlaces.pureAlcoholVolume", Seq(""))
      )
    }

    "fail to bind when invalid values are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "invalid",
        "volumes.pureAlcoholVolume" -> "invalid"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitresVolume", "dutySuspendedVolume.error.invalid.totalLitresVolume", List("")),
        FormError("volumes_pureAlcoholVolume", "dutySuspendedVolume.error.invalid.pureAlcoholVolume", List(""))
      )
    }

    "fail to bind when values below minimum are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "-100000000000",
        "volumes.pureAlcoholVolume" -> "-100000000000"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitresVolume", "dutySuspendedVolume.error.minimumValue.totalLitresVolume", List("")),
        FormError("volumes_pureAlcoholVolume", "dutySuspendedVolume.error.minimumValue.pureAlcoholVolume", List(""))
      )
    }

    "fail to bind when values exceed maximum are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "100000000000",
        "volumes.pureAlcoholVolume" -> "100000000000"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitresVolume", "dutySuspendedVolume.error.maximumValue.totalLitresVolume", List("")),
        FormError("volumes_pureAlcoholVolume", "dutySuspendedVolume.error.maximumValue.pureAlcoholVolume", List(""))
      )
    }

    "fail to bind when pure alcohol volume is higher than total litres value" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "0",
        "volumes.pureAlcoholVolume" -> "2"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_pureAlcoholVolume", "dutySuspendedVolume.error.lessThanExpected", List(""))
      )
    }

    "fail to bind when pure alcohol volume is negative and total litres value is positive" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "1",
        "volumes.pureAlcoholVolume" -> "-2"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_pureAlcoholVolume", "dutySuspendedVolume.error.incorrectSign", List(""))
      )
    }

    "fail to bind when total litres value is negative and pure alcohol volume is a higher negative" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "-11",
        "volumes.pureAlcoholVolume" -> "-2"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_pureAlcoholVolume", "dutySuspendedVolume.error.lessThanExpected", List(""))
      )
    }

    "fail to bind when total litres value is zero and pure alcohol volume is negative" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "0",
        "volumes.pureAlcoholVolume" -> "-2"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_pureAlcoholVolume", "dutySuspendedVolume.error.zeroTotalLitres", List(""))
      )
    }

  }
}
