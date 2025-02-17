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

import cats.data.NonEmptySeq
import models._
import models.declareDuty.{DutyByTaxType, VolumeAndRateByTaxType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen}

import java.time.YearMonth

trait ModelGenerators {

  implicit lazy val arbitraryAdjustmentVolumeWithSpr: Arbitrary[adjustment.AdjustmentVolumeWithSPR] =
    Arbitrary {
      for {
        totalLitresVolume <- arbitrary[BigDecimal]
        pureAlcoholVolume <- arbitrary[BigDecimal]
        sprDutyRate       <- arbitrary[BigDecimal]
      } yield adjustment.AdjustmentVolumeWithSPR(totalLitresVolume, pureAlcoholVolume, sprDutyRate)
    }

  implicit lazy val arbitraryAdjustmentVolume: Arbitrary[adjustment.AdjustmentVolume] =
    Arbitrary {
      for {
        totalLitresVolume <- arbitrary[BigDecimal]
        pureAlcoholVolume <- arbitrary[BigDecimal]
      } yield adjustment.AdjustmentVolume(totalLitresVolume, pureAlcoholVolume)
    }

  implicit lazy val arbitraryWhisky: Arbitrary[spiritsQuestions.Whisky] =
    Arbitrary {
      for {
        scotchWhisky <- arbitrary[BigDecimal]
        irishWhiskey <- arbitrary[BigDecimal]
      } yield spiritsQuestions.Whisky(scotchWhisky, irishWhiskey)
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
      Gen.oneOf(adjustment.AdjustmentType.values)
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

  implicit val arbitraryAlcoholRegimeName: Arbitrary[AlcoholRegime] = Arbitrary {
    Gen.oneOf(
      AlcoholRegime.Beer,
      AlcoholRegime.Cider,
      AlcoholRegime.Wine,
      AlcoholRegime.Spirits
    )
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

  implicit val arbitraryABVIntervalLabel: Arbitrary[AlcoholType] = Arbitrary {
    Gen.oneOf(AlcoholType.values)
  }

  implicit val abvIntervalGen: Arbitrary[ABVRange] = Arbitrary {
    (for {
      label  <- arbitrary[AlcoholType]
      minABV <- arbitrary[AlcoholByVolume]
      maxABV <- arbitrary[AlcoholByVolume]
    } yield ABVRange(label, minABV, maxABV)).suchThat(interval => interval.minABV.value < interval.maxABV.value)
  }

  implicit val arbitraryABVIntervals: Arbitrary[List[ABVRange]] = Arbitrary {
    Gen.listOfN(2, abvIntervalGen.arbitrary)
  }

  implicit val arbitraryAlcoholRegime: Arbitrary[RangeDetailsByRegime] = Arbitrary {
    for {
      name      <- arbitraryAlcoholRegimeName.arbitrary
      abvRanges <- arbitraryABVIntervals.arbitrary
    } yield RangeDetailsByRegime(name, NonEmptySeq.fromSeqUnsafe(abvRanges))
  }

  implicit val arbitrarySetOfAlcoholRegimes: Arbitrary[Set[RangeDetailsByRegime]] = Arbitrary {
    Gen.containerOf[Set, RangeDetailsByRegime](arbitraryAlcoholRegime.arbitrary)
  }

  implicit val arbitraryRateBand: Arbitrary[RateBand] = Arbitrary {
    for {
      taxType        <- Gen.alphaStr.suchThat(_.nonEmpty)
      description    <- Gen.alphaStr
      rateType       <- arbitraryRateType.arbitrary
      alcoholRegimes <- arbitrarySetOfAlcoholRegimes.arbitrary
      rate           <- Gen.option(Gen.chooseNum(-99999.99, 99999.99).map(BigDecimal(_)))
    } yield RateBand(taxType, description, rateType, rate, alcoholRegimes)
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

  def periodKeyGen: Gen[String] = for {
    year  <- Gen.chooseNum(24, 50)
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

  def genAlcoholRegime(alcoholRegime: AlcoholRegime): Gen[RangeDetailsByRegime] =
    arbitraryABVIntervals.arbitrary.map(abvRanges =>
      RangeDetailsByRegime(alcoholRegime, NonEmptySeq.fromSeqUnsafe(abvRanges))
    )

  def genRateBandForRegime(alcoholRegime: AlcoholRegime): Gen[RateBand] =
    for {
      taxType        <- Gen.alphaStr.suchThat(_.nonEmpty)
      description    <- Gen.alphaStr
      rateType       <- arbitraryRateType.arbitrary
      alcoholRegimes <- genAlcoholRegime(alcoholRegime)
      rate           <- Gen.chooseNum(0, 99999.99).map(BigDecimal(_).setScale(1, BigDecimal.RoundingMode.UP))
    } yield RateBand(taxType, description, rateType, Some(rate), Set(alcoholRegimes))

  def genRateBandForRegimeWithSPR(alcoholRegime: AlcoholRegime): Gen[RateBand] =
    for {
      taxType        <- Gen.alphaStr.suchThat(_.nonEmpty)
      description    <- Gen.alphaStr
      rateType       <- arbitraryRateType.arbitrary
      alcoholRegimes <- genAlcoholRegime(alcoholRegime)
    } yield RateBand(taxType, description, rateType, None, Set(alcoholRegimes))

  def genListOfRateBandForRegime(alcoholRegime: AlcoholRegime): Gen[List[RateBand]] =
    Gen.listOfN(3, genRateBandForRegime(alcoholRegime))

  def genListOfRateBandForRegimeWithSPR(alcoholRegime: AlcoholRegime): Gen[List[RateBand]] =
    Gen.listOfN(3, genRateBandForRegimeWithSPR(alcoholRegime))

  def genVolumeAndRateByTaxTypeRateBand(rateBand: RateBand): Arbitrary[VolumeAndRateByTaxType] = Arbitrary {
    for {
      rateBandRecap <- Gen.alphaStr
      totalLitres   <- genAlcoholByVolumeValue
      sprDutyRate   <- Gen.chooseNum(0, 99999.99).map(BigDecimal(_).setScale(1, BigDecimal.RoundingMode.UP))
    } yield VolumeAndRateByTaxType(
      rateBand.taxTypeCode,
      totalLitres,
      (totalLitres * BigDecimal(0.1)).setScale(4, BigDecimal.RoundingMode.UP),
      rateBand.rate.getOrElse(sprDutyRate)
    )
  }

  def arbitraryVolumeAndRateByTaxType(rateBands: Seq[RateBand]): Arbitrary[Seq[VolumeAndRateByTaxType]] = Arbitrary {
    Gen.sequence[Seq[VolumeAndRateByTaxType], VolumeAndRateByTaxType](
      rateBands.map(genVolumeAndRateByTaxTypeRateBand(_).arbitrary)
    )
  }

  def genDutyByTaxTypeFromVolumeAndRateByTaxType(volumeAndRate: VolumeAndRateByTaxType): DutyByTaxType =
    DutyByTaxType(
      volumeAndRate.taxType,
      volumeAndRate.totalLitres,
      volumeAndRate.pureAlcohol,
      volumeAndRate.dutyRate,
      volumeAndRate.dutyRate * volumeAndRate.pureAlcohol
    )

  def arbitraryDutyByTaxType(rateBands: Seq[RateBand]): Arbitrary[Seq[DutyByTaxType]] = Arbitrary {
    arbitraryVolumeAndRateByTaxType(rateBands).arbitrary.map(_.map(genDutyByTaxTypeFromVolumeAndRateByTaxType))
  }

  def chargeReferenceGen: Gen[String] = Gen.listOfN(13, Gen.numChar).map(id => s"XA${id.mkString}")
}
