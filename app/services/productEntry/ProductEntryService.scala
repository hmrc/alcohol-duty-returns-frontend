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
import models.adjustment.AdjustmentType.Spoilt
import models.productEntry.ProductEntry
import pages.productEntry.CurrentProductEntryPage
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductEntryServiceImpl @Inject() (
  alcoholDutyCalculatorConnector: AlcoholDutyCalculatorConnector
) extends ProductEntryService {

  override def createProduct(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ProductEntry] = {

    val productEntry =
      userAnswers
        .get(CurrentProductEntryPage)
        .getOrElse(throw new RuntimeException("Can't fetch product entry from cache"))
    val abv          = productEntry.abv.getOrElse(throw new RuntimeException("Can't fetch ABV from cache"))
    val volume       = productEntry.volume.getOrElse(throw new RuntimeException("Can't fetch volume from cache"))
    val rate         = productEntry.rate.getOrElse(throw getError(productEntry))

    for {
      taxDuty <-
        alcoholDutyCalculatorConnector
          .calculateTaxDuty(volume, rate, Spoilt) // removing abv and adding adjustment for code to compile
    } yield productEntry.copy(
      duty = Some(taxDuty.duty)
    )
  }

  private def getError(productEntry: ProductEntry): RuntimeException =
    (productEntry.taxRate, productEntry.sprDutyRate) match {
      case (Some(_), Some(_)) =>
        new RuntimeException("Failed to get rate, both tax rate and spr duty rate are defined.")
      case (None, None)       => new RuntimeException("Failed to get rate, neither tax rate nor spr duty rate are defined.")
      case (_, _)             => new RuntimeException("Failed to get rate.")
    }
}
@ImplementedBy(classOf[ProductEntryServiceImpl])
trait ProductEntryService {
  def createProduct(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ProductEntry]
}
