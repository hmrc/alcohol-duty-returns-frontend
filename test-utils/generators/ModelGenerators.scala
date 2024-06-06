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

package generators

import models._
import models.productEntry.ProductEntry
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen}
import enumeratum.scalacheck._
import models.returns.VolumeAndRateByTaxType

import java.time.YearMonth

trait ModelGenerators {

  implicit lazy val arbitraryGrainsUsed: Arbitrary[spiritsQuestions.GrainsUsed] =
    Arbitrary {
      for {
        maltedBarleyQuantity     <- arbitrary[BigDecimal]
        wheatQuantity            <- arbitrary[BigDecimal]
        maizeQuantity            <- arbitrary[BigDecimal]
        ryeQuantity              <- arbitrary[BigDecimal]
        unmaltedGrainQuantity    <- arbitrary[BigDecimal]
        usedMaltedGrainNotBarley <- arbitrary[Boolean]
      } yield spiritsQuestions.GrainsUsed(
        maltedBarleyQuantity,
        wheatQuantity,
        maizeQuantity,
        ryeQuantity,
        unmaltedGrainQuantity,
        usedMaltedGrainNotBarley
      )
    }

  implicit lazy val arbitraryOtherMaltedGrains: Arbitrary[spiritsQuestions.OtherMaltedGrains] =
    Arbitrary {
      for {
        otherMaltedGrainsTypes    <- arbitrary[String]
        otherMaltedGrainsQuantity <- arbitrary[BigDecimal]
      } yield spiritsQuestions.OtherMaltedGrains(otherMaltedGrainsTypes, otherMaltedGrainsQuantity)
    }

  implicit lazy val arbitraryEthyleneGasOrMolassesUsed: Arbitrary[spiritsQuestions.EthyleneGasOrMolassesUsed] =
    Arbitrary {
      for {
        ethyleneGas      <- arbitrary[BigDecimal]
        molasses         <- arbitrary[BigDecimal]
        otherIngredients <- arbitrary[Boolean]
      } yield spiritsQuestions.EthyleneGasOrMolassesUsed(ethyleneGas, molasses, otherIngredients)
    }

  implicit lazy val arbitraryOtherIngredientsUsed: Arbitrary[spiritsQuestions.OtherIngredientsUsed] =
    Arbitrary {
      for {
        otherIngredientsTypes    <- arbitrary[String]
        otherIngredientsUnit     <- arbitrary[UnitsOfMeasure]
        otherIngredientsQuantity <- arbitrary[BigDecimal]
      } yield spiritsQuestions.OtherIngredientsUsed(
        otherIngredientsTypes,
        otherIngredientsUnit,
        otherIngredientsQuantity
      )
    }

  implicit lazy val arbitraryWhisky: Arbitrary[spiritsQuestions.Whisky] =
    Arbitrary {
      for {
        scotchWhisky <- arbitrary[BigDecimal]
        irishWhiskey <- arbitrary[BigDecimal]
      } yield spiritsQuestions.Whisky(scotchWhisky, irishWhiskey)
    }

  implicit lazy val arbitraryAlcoholUsed: Arbitrary[spiritsQuestions.AlcoholUsed] =
    Arbitrary {
      for {
        beer         <- arbitrary[BigDecimal]
        wine         <- arbitrary[BigDecimal]
        madeWine     <- arbitrary[BigDecimal]
        ciderOrPerry <- arbitrary[BigDecimal]
      } yield spiritsQuestions.AlcoholUsed(beer, wine, madeWine, ciderOrPerry)
    }

  implicit lazy val arbitraryDutySuspendedBeer: Arbitrary[dutySuspended.DutySuspendedBeer] =
    Arbitrary {
      for {
        totalBeer         <- arbitrary[BigDecimal]
        pureAlcoholInBeer <- arbitrary[BigDecimal]
      } yield dutySuspended.DutySuspendedBeer(totalBeer, pureAlcoholInBeer)
    }

  implicit lazy val arbitraryDutySuspendedCider: Arbitrary[dutySuspended.DutySuspendedCider] =
    Arbitrary {
      for {
        totalCider         <- arbitrary[BigDecimal]
        pureAlcoholInCider <- arbitrary[BigDecimal]
      } yield dutySuspended.DutySuspendedCider(totalCider, pureAlcoholInCider)
    }

  implicit lazy val arbitraryDutySuspendedWine: Arbitrary[dutySuspended.DutySuspendedWine] =
    Arbitrary {
      for {
        totalWine         <- arbitrary[BigDecimal]
        pureAlcoholInWine <- arbitrary[BigDecimal]
      } yield dutySuspended.DutySuspendedWine(totalWine, pureAlcoholInWine)
    }

  implicit lazy val arbitraryDutySuspendedSpirits: Arbitrary[dutySuspended.DutySuspendedSpirits] =
    Arbitrary {
      for {
        totalSpirits         <- arbitrary[BigDecimal]
        pureAlcoholInSpirits <- arbitrary[BigDecimal]
      } yield dutySuspended.DutySuspendedSpirits(totalSpirits, pureAlcoholInSpirits)
    }

  implicit lazy val arbitraryDutySuspendedOtherFermented: Arbitrary[dutySuspended.DutySuspendedOtherFermented] =
    Arbitrary {
      for {
        totalOtherFermented         <- arbitrary[BigDecimal]
        pureAlcoholInOtherFermented <- arbitrary[BigDecimal]
      } yield dutySuspended.DutySuspendedOtherFermented(totalOtherFermented, pureAlcoholInOtherFermented)
    }

  implicit lazy val arbitraryAdjustmentType: Arbitrary[adjustment.AdjustmentType] =
    Arbitrary {
      Gen.oneOf(adjustment.AdjustmentType.values.toSeq)
    }

  implicit lazy val arbitrarySpiritType: Arbitrary[SpiritType] =
    Arbitrary {
      Gen.oneOf(SpiritType.values)
    }

  implicit val arbitraryYearMonth: Arbitrary[YearMonth] = Arbitrary {
    for {
      year  <- Gen.choose(1900, 2200)
      month <- Gen.choose(1, 12)
    } yield YearMonth.of(year, month)
  }

  implicit val arbitraryRateType: Arbitrary[RateType] = Arbitrary {
    Gen.oneOf(RateType.Core, RateType.DraughtRelief)
  }

  implicit val arbitraryAlcoholRegime: Arbitrary[AlcoholRegime]       = Arbitrary {
    Gen.oneOf(
      AlcoholRegime.Beer,
      AlcoholRegime.Cider,
      AlcoholRegime.Wine,
      AlcoholRegime.Spirits
    )
  }
  implicit val arbitraryRateTypeResponse: Arbitrary[RateTypeResponse] = Arbitrary {
    Gen.oneOf(
      RateTypeResponse(RateType.DraughtRelief),
      RateTypeResponse(RateType.SmallProducerRelief),
      RateTypeResponse(RateType.DraughtAndSmallProducerRelief),
      RateTypeResponse(RateType.Core)
    )
  }

  implicit val arbitrarySetOfAlcoholRegimes: Arbitrary[Set[AlcoholRegime]] = Arbitrary {
    Gen.containerOf[Set, AlcoholRegime](arbitraryAlcoholRegime.arbitrary)
  }

  val genAlcoholByVolumeValue: Gen[BigDecimal] =
    for {
      value <- Gen.choose(0.001, 100.00)
      scale <- Gen.oneOf(0, 1)
    } yield BigDecimal(value).setScale(scale, BigDecimal.RoundingMode.UP)

  val genAlcoholByVolumeValueTooBigScale: Gen[BigDecimal] =
    for {
      value <- Gen.choose(0.001, 100.00)
      scale <- Gen.choose(2, 10)
    } yield BigDecimal(value).setScale(scale, BigDecimal.RoundingMode.UP)

  val genAlcoholByVolumeValueNegative: Gen[BigDecimal] =
    Gen
      .chooseNum(Double.MinValue, -0.1)
      .map(d => BigDecimal(d).setScale(1, BigDecimal.RoundingMode.HALF_UP))

  val genAlcoholByVolumeValueMoreThan100: Gen[BigDecimal] =
    Gen
      .chooseNum(100.01, Double.MaxValue)
      .map(d => BigDecimal(d).setScale(1, BigDecimal.RoundingMode.UP))

  val genAlcoholByVolumeValueOutOfRange: Gen[BigDecimal] =
    Gen.oneOf(genAlcoholByVolumeValueNegative, genAlcoholByVolumeValueMoreThan100)

  implicit val arbitraryAlcoholByVolume: Arbitrary[AlcoholByVolume] = Arbitrary {
    genAlcoholByVolumeValue.map(AlcoholByVolume.apply)
  }

  implicit val chooseBigDecimal: Choose[BigDecimal] =
    Choose.xmap[Double, BigDecimal](d => BigDecimal(d), bd => bd.toDouble)(implicitly[Choose[Double]])

  implicit val arbitraryRateBand: Arbitrary[RateBand] = Arbitrary {
    for {
      taxType       <- Gen.alphaStr
      description   <- Gen.alphaStr
      rateType      <- arbitraryRateType.arbitrary
      alcoholRegime <- arbitrarySetOfAlcoholRegimes.arbitrary
      minABV        <- arbitraryAlcoholByVolume.arbitrary
      maxABV        <- arbitraryAlcoholByVolume.arbitrary
      rate          <- Gen.option(Gen.chooseNum(-99999.99, 99999.99).map(BigDecimal(_)))
    } yield RateBand(taxType, description, rateType, alcoholRegime, minABV, maxABV, rate)
  }

  implicit val arbitraryRatePeriod: Arbitrary[RatePeriod] = Arbitrary {
    for {
      name              <- Gen.alphaStr
      isLatest          <- Gen.oneOf(true, false)
      validityStartDate <- Arbitrary.arbitrary[YearMonth]
      validityEndDate   <- Gen.option(Arbitrary.arbitrary[YearMonth])
      rateBands         <- Gen.nonEmptyListOf(arbitraryRateBand.arbitrary)
    } yield RatePeriod(name, isLatest, validityStartDate, validityEndDate, rateBands)
  }

  implicit val arbitraryListOfRatePeriod: Arbitrary[Seq[RatePeriod]] = Arbitrary {
    Gen.listOf(arbitraryRatePeriod.arbitrary)
  }

  implicit val arbitraryProductEntry: Arbitrary[ProductEntry] = Arbitrary {
    productEntryGen
  }

  implicit val arbitraryProductEntryList: Arbitrary[List[ProductEntry]] = Arbitrary {
    Gen.listOf(productEntryGen)
  }

  def productEntryGen: Gen[ProductEntry] = for {
    name                <- Gen.alphaStr
    abv                 <- arbitrary[AlcoholByVolume]
    rateType            <- arbitrary[RateType]
    volume              <- Gen.posNum[BigDecimal]
    draughtRelief       <- Gen.oneOf(true, false)
    smallProducerRelief <- Gen.oneOf(true, false)
    taxCode             <- Gen.alphaStr
    regime              <- arbitrary[AlcoholRegime]
    taxRate             <- Gen.posNum[BigDecimal]
    sprDutyRate         <- Gen.posNum[BigDecimal]
    duty                <- Gen.posNum[BigDecimal]
    pureAlcoholVolume   <- Gen.posNum[BigDecimal]
  } yield ProductEntry(
    name = Some(name),
    abv = Some(abv),
    rateType = Some(rateType),
    volume = Some(volume),
    draughtRelief = Some(draughtRelief),
    smallProducerRelief = Some(smallProducerRelief),
    taxCode = Some(taxCode),
    regime = Some(regime),
    taxRate = if (!smallProducerRelief) Some(taxRate) else None,
    sprDutyRate = if (smallProducerRelief) Some(sprDutyRate) else None,
    duty = Some(duty),
    pureAlcoholVolume = Some(pureAlcoholVolume)
  )

  def periodKeyGen: Gen[String] = for {
    year  <- Gen.chooseNum(23, 50)
    month <- Gen.chooseNum(0, 11)
  } yield s"${year}A${(month + 'A').toChar}"

  def invalidPeriodKeyGen: Gen[String] = Gen.alphaStr
    .suchThat(_.nonEmpty)
    .suchThat(!_.matches(ReturnPeriod.returnPeriodPattern.toString()))

  def returnPeriodGen: Gen[ReturnPeriod] = periodKeyGen.map(ReturnPeriod.fromPeriodKey(_).get)

  implicit val arbitraryReturnPeriod: Arbitrary[ReturnPeriod] = Arbitrary {
    returnPeriodGen
  }

  def appaIdGen: Gen[String] = Gen.listOfN(10, Gen.numChar).map(id => s"XMADP${id.mkString}")

  def regimeGen: Gen[AlcoholRegime] = Gen.oneOf(AlcoholRegime.values)

  def rateBandGen(regime: AlcoholRegime): Gen[RateBand] = for {
    taxType     <- Gen.chooseNum(300, 400).map(_.toString)
    description <- Gen.alphaStr
    rateType    <- arbitrary[RateType]
    minABV      <- arbitrary[AlcoholByVolume]
    maxABV      <- arbitrary[AlcoholByVolume]
    rate        <- Gen.option(Gen.chooseNum(-99999.99, 99999.99).map(BigDecimal(_)))
  } yield RateBand(taxType, description, rateType, Set(regime), minABV, maxABV, rate)

  def arbitraryRateBandList(regime: AlcoholRegime): Arbitrary[List[RateBand]] = Arbitrary {
    Gen.listOfN(10, rateBandGen(regime))
  }

  def genDutyTaxTypesFromRateBand(rateBand: RateBand): Arbitrary[VolumeAndRateByTaxType] = Arbitrary {
    for {
      totalLitres <- genAlcoholByVolumeValue
      pureAlcohol <- genAlcoholByVolumeValue
      sprDutyRate <- Gen.chooseNum(-99999.99, 99999.99).map(BigDecimal(_))
    } yield VolumeAndRateByTaxType(
      rateBand.taxType,
      totalLitres,
      pureAlcohol,
      rateBand.rate.getOrElse(sprDutyRate)
    )
  }

}
