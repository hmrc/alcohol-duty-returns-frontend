/*
 * Copyright 2025 HM Revenue & Customs
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

package models.checkAndSubmit

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import models.SpiritType

object AdrTypeOfSpirit extends Enum[AdrTypeOfSpirit] with PlayJsonEnum[AdrTypeOfSpirit] {
  val values = findValues

  case object Malt extends AdrTypeOfSpirit
  case object Grain extends AdrTypeOfSpirit
  case object NeutralAgricultural extends AdrTypeOfSpirit
  case object NeutralIndustrial extends AdrTypeOfSpirit
  case object Beer extends AdrTypeOfSpirit
  case object CiderOrPerry extends AdrTypeOfSpirit
  case object WineOrMadeWine extends AdrTypeOfSpirit
  case object Other extends AdrTypeOfSpirit

  def fromSpiritsType(spiritsType: SpiritType): AdrTypeOfSpirit = spiritsType match {
    case SpiritType.Maltspirits               => Malt
    case SpiritType.Grainspirits              => Grain
    case SpiritType.NeutralAgriculturalOrigin => NeutralAgricultural
    case SpiritType.NeutralIndustrialOrigin   => NeutralIndustrial
    case SpiritType.Beer                      => Beer
    case SpiritType.CiderOrPerry              => CiderOrPerry
    case SpiritType.WineOrMadeWine            => WineOrMadeWine
    case SpiritType.Other                     => Other
  }
}

sealed trait AdrTypeOfSpirit extends EnumEntry
