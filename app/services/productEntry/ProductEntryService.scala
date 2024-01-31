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

package services.productEntry

import com.google.inject.{ImplementedBy, Inject, Singleton}
import connectors.AlcoholDutyCalculatorConnector
import models.UserAnswers
import models.productEntry.{ProductEntry, TaxType}
import pages.QuestionPage
import pages.productEntry.{AlcoholByVolumeQuestionPage, DeclareSmallProducerReliefDutyRatePage, DraughtReliefQuestionPage, ProductVolumePage, SmallProducerReliefQuestionPage, TaxTypePage}
import play.api.libs.json.Reads
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class ProductEntryServiceImpl @Inject() (
  alcoholDutyCalculatorConnector: AlcoholDutyCalculatorConnector
) extends ProductEntryService {

  override def createProduct(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ProductEntry]       =
    for {
      abv                <- Future.fromTry(extractValueForPage(userAnswers, AlcoholByVolumeQuestionPage))
      volume             <- Future.fromTry(extractValueForPage(userAnswers, ProductVolumePage))
      draughtRelief      <- Future.fromTry(extractValueForPage(userAnswers, DraughtReliefQuestionPage))
      smallProduceRelief <- Future.fromTry(extractValueForPage(userAnswers, SmallProducerReliefQuestionPage))
      taxType            <- Future.fromTry(extractValueForPage(userAnswers, TaxTypePage))
      rate               <- Future.fromTry(getRate(taxType, userAnswers.get(DeclareSmallProducerReliefDutyRatePage)))
      taxDuty            <- alcoholDutyCalculatorConnector.calculateTaxDuty(abv, volume, rate)
    } yield ProductEntry(
      abv = abv,
      volume = volume,
      rate = rate,
      draughtRelief = draughtRelief,
      smallProduceRelief = smallProduceRelief,
      pureAlcoholVolume = taxDuty.pureAlcoholVolume,
      duty = taxDuty.duty,
      taxCode = taxType.code
    )
  def getRate(taxType: TaxType, sprDutyRate: Option[BigDecimal]): Try[BigDecimal] = Try {
    (taxType.taxRate, sprDutyRate) match {
      case (Some(value), None)       => value
      case (None, Some(sprDutyRate)) => sprDutyRate
      case (Some(_), Some(_))        =>
        throw new RuntimeException("Failed to get rate, both tax rate and spr duty rate are defined")
      case _                         => throw new RuntimeException("Failed to get rate, neither tax rate nor spr duty rate are defined")
    }
  }

  private def extractValueForPage[T](userAnswers: UserAnswers, page: QuestionPage[T])(implicit rds: Reads[T]): Try[T] =
    Try {
      userAnswers.get(page) match {
        case Some(value) => value
        case _           => throw new RuntimeException(s"Failed to get value for page ${page.toString}")
      }
    }
}

@ImplementedBy(classOf[ProductEntryServiceImpl])
trait ProductEntryService {
  def createProduct(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ProductEntry]
}
