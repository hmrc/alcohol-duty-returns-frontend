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
import models.dutySuspended.{DutySuspendedRegimeSpecificKey, DutySuspendedVolume}
import org.mockito.MockitoSugar.mock
import play.api.data.FormError
import play.api.i18n.Messages

class DutySuspendedFormProviderSpec extends BigDecimalFieldBehaviours {
  val regime         = regimeGen.sample.value
  val messages       = mock[Messages]
  val form           = new DutySuspendedFormProvider()(regime)(messages)
  val totalVolumeKey = DutySuspendedRegimeSpecificKey.totalVolumeKey(regime)
  val pureAlcoholKey = DutySuspendedRegimeSpecificKey.pureAlcoholKey(regime)
  ".volumes" - {
    "must bind valid data" in {
      val data = Map(
        s"volumes.$totalVolumeKey" -> "1",
        s"volumes.$pureAlcoholKey" -> "1"
      )
      form.bind(data).value.value mustBe DutySuspendedVolume(1, 1)
    }

    "must unbind valid data" in {
      val data = DutySuspendedVolume(1, 1)
      form.fill(data).data must contain theSameElementsAs Map(
        s"volumes.$totalVolumeKey" -> "1",
        s"volumes.$pureAlcoholKey" -> "1"
      )
    }

    "fail to bind when no answers are selected" in {
      val data = Map.empty[String, String]
      form.bind(data).errors must contain allElementsOf List(
        FormError(s"volumes_$totalVolumeKey", s"dutySuspendedVolume.error.noValue.$totalVolumeKey", Seq("")),
        FormError(s"volumes_$pureAlcoholKey", s"dutySuspendedVolume.error.noValue.$pureAlcoholKey", Seq(""))
      )
    }

    "fail to bind when blank answer provided" in {
      val data = Map(
        s"volumes.$totalVolumeKey" -> "",
        s"volumes.$pureAlcoholKey" -> ""
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError(s"volumes_$totalVolumeKey", s"dutySuspendedVolume.error.noValue.$totalVolumeKey", Seq("")),
        FormError(s"volumes_$pureAlcoholKey", s"dutySuspendedVolume.error.noValue.$pureAlcoholKey", Seq(""))
      )
    }

    "fail to bind when values with too many decimal places are provided" in {
      val data = Map(
        s"volumes.$totalVolumeKey" -> "1.111",
        s"volumes.$pureAlcoholKey" -> "1.11234"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError(s"volumes_$totalVolumeKey", s"dutySuspendedVolume.error.decimalPlaces.$totalVolumeKey", Seq("")),
        FormError(s"volumes_$pureAlcoholKey", s"dutySuspendedVolume.error.decimalPlaces.$pureAlcoholKey", Seq(""))
      )
    }

    "fail to bind when invalid values are provided" in {
      val data = Map(
        s"volumes.$totalVolumeKey" -> "invalid",
        s"volumes.$pureAlcoholKey" -> "invalid"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError(s"volumes_$totalVolumeKey", s"dutySuspendedVolume.error.invalid.$totalVolumeKey", List("")),
        FormError(s"volumes_$pureAlcoholKey", s"dutySuspendedVolume.error.invalid.$pureAlcoholKey", List(""))
      )
    }

    "fail to bind when values below minimum are provided" in {
      val data = Map(
        s"volumes.$totalVolumeKey" -> "-100000000000",
        s"volumes.$pureAlcoholKey" -> "-100000000000"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError(s"volumes_$totalVolumeKey", s"dutySuspendedVolume.error.minimumValue.$totalVolumeKey", List("")),
        FormError(s"volumes_$pureAlcoholKey", s"dutySuspendedVolume.error.minimumValue.$pureAlcoholKey", List(""))
      )
    }

    "fail to bind when values exceed maximum are provided" in {
      val data = Map(
        s"volumes.$totalVolumeKey" -> "100000000000",
        s"volumes.$pureAlcoholKey" -> "100000000000"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError(s"volumes_$totalVolumeKey", s"dutySuspendedVolume.error.maximumValue.$totalVolumeKey", List("")),
        FormError(s"volumes_$pureAlcoholKey", s"dutySuspendedVolume.error.maximumValue.$pureAlcoholKey", List(""))
      )
    }

    "fail to bind when pure alcohol volume is higher than total litres value" in {
      val data = Map(
        s"volumes.$totalVolumeKey" -> "1",
        s"volumes.$pureAlcoholKey" -> "2"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError(s"volumes_$pureAlcoholKey", "dutySuspendedVolume.error.lessThanExpected", List(""))
      )
    }

    "fail to bind when pure alcohol volume is negative and total litres value is positive" in {
      val data = Map(
        s"volumes.$totalVolumeKey" -> "1",
        s"volumes.$pureAlcoholKey" -> "-2"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError(s"volumes_$pureAlcoholKey", "dutySuspendedVolume.error.incorrectSign", List(""))
      )
    }

    "fail to bind when total litres value is negative and pure alcohol volume is a higher negative" in {
      val data = Map(
        s"volumes.$totalVolumeKey" -> "-11",
        s"volumes.$pureAlcoholKey" -> "-2"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError(s"volumes_$pureAlcoholKey", "dutySuspendedVolume.error.lessThanExpected", List(""))
      )
    }

    "fail to bind when total litres value is zero and pure alcohol volume is negative" in {
      val data = Map(
        s"volumes.$totalVolumeKey" -> "0",
        s"volumes.$pureAlcoholKey" -> "-2"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError(s"volumes_$pureAlcoholKey", "dutySuspendedVolume.error.zeroTotalLitres", List(""))
      )
    }

  }
}
