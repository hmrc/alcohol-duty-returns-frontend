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

import config.Constants.MappingFields._
import models.adjustment.SpoiltVolumeWithDuty
import play.api.data.FormError
import play.api.data.format.Formatter

class SpoiltVolumesAndDutyFormatter(
  lessOrEqualKey: String,
  volumeFormatter: BigDecimalFieldFormatter,
  pureAlcoholVolumeFormatter: BigDecimalFieldFormatter,
  dutyFormatter: BigDecimalFieldFormatter,
  args: Seq[String]
) extends Formatter[SpoiltVolumeWithDuty]
    with Formatters {

  private def bindFormatters(
    key: String,
    data: Map[String, String]
  ): Either[Seq[FormError], SpoiltVolumeWithDuty] = {
    val totalLitresEither = bindField(key, totalLitresField, volumeFormatter, data)
    val pureAlcoholEither = bindField(key, pureAlcoholField, pureAlcoholVolumeFormatter, data)
    val dutyEither        = bindField(key, dutyField, dutyFormatter, data)
    (totalLitresEither, pureAlcoholEither, dutyEither) match {
      case (Right(totalLitres), Right(pureAlcohol), Right(duty)) =>
        Right(SpoiltVolumeWithDuty(totalLitres, pureAlcohol, duty))
      case _                                                     =>
        Left(Seq(totalLitresEither, pureAlcoholEither, dutyEither).collect { case Left(error) =>
          error
        }.flatten)
    }
  }

  private def checkPureAlcoholIsNotGreaterThanTotalLitres(
    key: String,
    spoiltVolumeWithDuty: SpoiltVolumeWithDuty
  ): Either[Seq[FormError], SpoiltVolumeWithDuty] =
    if (spoiltVolumeWithDuty.totalLitresVolume < spoiltVolumeWithDuty.pureAlcoholVolume) {
      Left(
        Seq(
          FormError(nameToId(s"$key.$pureAlcoholField"), lessOrEqualKey, args)
        )
      )
    } else {
      Right(
        SpoiltVolumeWithDuty(
          spoiltVolumeWithDuty.totalLitresVolume,
          spoiltVolumeWithDuty.pureAlcoholVolume,
          spoiltVolumeWithDuty.duty
        )
      )
    }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], SpoiltVolumeWithDuty] =
    for {
      spoiltVolumeWithDuty <- bindFormatters(key, data)
      _                    <- checkPureAlcoholIsNotGreaterThanTotalLitres(key, spoiltVolumeWithDuty)
    } yield spoiltVolumeWithDuty
  override def unbind(key: String, value: SpoiltVolumeWithDuty): Map[String, String]                      =
    volumeFormatter.unbind(s"$key.$totalLitresField", value.totalLitresVolume) ++
      pureAlcoholVolumeFormatter.unbind(s"$key.$pureAlcoholField", value.pureAlcoholVolume) ++
      dutyFormatter.unbind(s"$key.$dutyField", value.duty)
}
