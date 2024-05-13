import models.AlcoholRegime.{Beer, Cider, Wine}
import models.{AlcoholByVolume, RateBand, RateType}

val bands = Seq(
  RateBand(
    "311",
    "some band",
    RateType.Core,
    Set(Beer),
    AlcoholByVolume(1.2),
    AlcoholByVolume(3.4),
    Some(BigDecimal(10))
  ),
  RateBand(
    "321",
    "some band",
    RateType.Core,
    Set(Beer),
    AlcoholByVolume(3.5),
    AlcoholByVolume(8.4),
    Some(BigDecimal(10))
  ),
  RateBand(
    "351",
    "some band",
    RateType.DraughtRelief,
    Set(Beer),
    AlcoholByVolume(1.2),
    AlcoholByVolume(3.4),
    Some(BigDecimal(10))
  ),
  RateBand(
    "361",
    "some band",
    RateType.SmallProducerRelief,
    Set(Cider),
    AlcoholByVolume(1.2),
    AlcoholByVolume(3.4),
    Some(BigDecimal(10))
  ),
  RateBand(
    "371",
    "some band",
    RateType.DraughtAndSmallProducerRelief,
    Set(Beer),
    AlcoholByVolume(1.2),
    AlcoholByVolume(3.4),
    Some(BigDecimal(10))
  )
)

bands.filter(_.alcoholRegime.contains(Beer)).groupBy(_.rateType)

val myMap = Map(
  ("a" -> 1),
  ("b" -> 2)
)

myMap.getOrElse("c", -1)