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

package service.productEntry

import base.SpecBase
import connectors.AlcoholDutyCalculatorConnector
import pages.productEntry._
import models.{AlcoholByVolume, AlcoholRegimeName}
import models.productEntry.{ProductEntry, TaxDuty}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import services.productEntry.ProductEntryServiceImpl
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProductEntryServiceSpec extends SpecBase {

  "ProductEntryService" - {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val productEntry = ProductEntry(
      abv = Some(AlcoholByVolume(3.5)),
      draughtRelief = Some(true),
      smallProducerRelief = Some(false),
      volume = Some(BigDecimal(1))
    )

    "must create a product entry when TaxType contains rate" in {

      val updatedProductEntry = productEntry.copy(
        taxCode = Some("ALC"),
        regime = Some(AlcoholRegimeName.Beer),
        taxRate = Some(BigDecimal(1))
      )

      val userAnswerWithRate = emptyUserAnswers
        .set(CurrentProductEntryPage, updatedProductEntry)
        .success
        .value

      val mockConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockConnector.calculateTaxDuty(any(), any(), any())(any()))
        .thenReturn(Future.successful(TaxDuty(BigDecimal(1), BigDecimal(1))))

      val service = new ProductEntryServiceImpl(mockConnector)

      val result = service.createProduct(userAnswerWithRate).futureValue

      result                     shouldBe a[ProductEntry]
      result.abv                 shouldBe Some(AlcoholByVolume(3.5))
      result.rate                shouldBe Some(BigDecimal(1))
      result.volume              shouldBe Some(BigDecimal(1))
      result.draughtRelief       shouldBe Some(true)
      result.smallProducerRelief shouldBe Some(false)
      result.pureAlcoholVolume   shouldBe Some(BigDecimal(1))
      result.duty                shouldBe Some(BigDecimal(1))

    }

    "must create a product entry when TaxType does not contain rate but the Small Producer Relief duty rate is present" in {

      val updatedProductEntry = productEntry.copy(
        taxCode = Some("ALC"),
        regime = Some(AlcoholRegimeName.Beer),
        smallProducerRelief = Some(true),
        sprDutyRate = Some(BigDecimal(2))
      )

      val userAnswerWithRate = emptyUserAnswers
        .set(CurrentProductEntryPage, updatedProductEntry)
        .success
        .value

      val mockConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockConnector.calculateTaxDuty(any(), any(), any())(any()))
        .thenReturn(Future.successful(TaxDuty(BigDecimal(1), BigDecimal(1))))

      val service = new ProductEntryServiceImpl(mockConnector)

      val result = service.createProduct(userAnswerWithRate).futureValue

      result                     shouldBe a[ProductEntry]
      result.abv                 shouldBe Some(AlcoholByVolume(3.5))
      result.rate                shouldBe Some(BigDecimal(2))
      result.volume              shouldBe Some(BigDecimal(1))
      result.draughtRelief       shouldBe Some(true)
      result.smallProducerRelief shouldBe Some(true)
      result.pureAlcoholVolume   shouldBe Some(BigDecimal(1))
      result.duty                shouldBe Some(BigDecimal(1))
    }

    "must throw an Exception" - {

      "if user answers object is empty" in {

        val mockConnector = mock[AlcoholDutyCalculatorConnector]

        val service = new ProductEntryServiceImpl(mockConnector)

        val exception = intercept[RuntimeException] {
          service.createProduct(emptyUserAnswers).futureValue
        }

        exception.getLocalizedMessage must include(
          "Can't fetch product entry from cache"
        )
      }

      "if both, TaxType and SmallProducerReliefDuty contain rate" in {

        val updatedProductEntry = productEntry.copy(
          taxCode = Some("ALC"),
          regime = Some(AlcoholRegimeName.Beer),
          taxRate = Some(BigDecimal(1)),
          smallProducerRelief = Some(true),
          sprDutyRate = Some(BigDecimal(2))
        )

        val userAnswerWithRate = emptyUserAnswers
          .set(CurrentProductEntryPage, updatedProductEntry)
          .success
          .value

        val mockConnector = mock[AlcoholDutyCalculatorConnector]
        when(mockConnector.calculateTaxDuty(any(), any(), any())(any()))
          .thenReturn(Future.successful(TaxDuty(BigDecimal(1), BigDecimal(1))))

        val service = new ProductEntryServiceImpl(mockConnector)

        val exception = intercept[RuntimeException] {
          service.createProduct(userAnswerWithRate).futureValue
        }

        exception.getLocalizedMessage must include("Failed to get rate, both tax rate and spr duty rate are defined.")
      }

      "if neither TaxType or DeclareSmallProducerReliefDutyRatePage contain rate" in {

        val updatedProductEntry = productEntry.copy(
          taxCode = Some("ALC"),
          regime = Some(AlcoholRegimeName.Beer),
          smallProducerRelief = Some(true)
        )

        val userAnswerWithRate = emptyUserAnswers
          .set(CurrentProductEntryPage, updatedProductEntry)
          .success
          .value

        val mockConnector = mock[AlcoholDutyCalculatorConnector]
        when(mockConnector.calculateTaxDuty(any(), any(), any())(any()))
          .thenReturn(Future.successful(TaxDuty(BigDecimal(1), BigDecimal(1))))

        val service = new ProductEntryServiceImpl(mockConnector)

        val exception = intercept[RuntimeException] {
          service.createProduct(userAnswerWithRate).futureValue
        }

        exception.getLocalizedMessage must include(
          "Failed to get rate, neither tax rate nor spr duty rate are defined."
        )
      }

      "if Product Entry doesn't contain ABV value" in {

        val userAnswers = emptyUserAnswers
          .set(CurrentProductEntryPage, productEntry.copy(abv = None))
          .success
          .value

        val mockConnector = mock[AlcoholDutyCalculatorConnector]
        when(mockConnector.calculateTaxDuty(any(), any(), any())(any()))
          .thenReturn(Future.successful(TaxDuty(BigDecimal(1), BigDecimal(1))))

        val service = new ProductEntryServiceImpl(mockConnector)

        val exception = intercept[RuntimeException] {
          service.createProduct(userAnswers).futureValue
        }

        exception.getLocalizedMessage must include(s"Can't fetch ABV from cache")
      }

      "if Product Entry doesn't contain Volume value" in {

        val userAnswers = emptyUserAnswers
          .set(CurrentProductEntryPage, productEntry.copy(volume = None))
          .success
          .value

        val mockConnector = mock[AlcoholDutyCalculatorConnector]
        when(mockConnector.calculateTaxDuty(any(), any(), any())(any()))
          .thenReturn(Future.successful(TaxDuty(BigDecimal(1), BigDecimal(1))))

        val service = new ProductEntryServiceImpl(mockConnector)

        val exception = intercept[RuntimeException] {
          service.createProduct(userAnswers).futureValue
        }

        exception.getLocalizedMessage must include(s"Can't fetch volume from cache")
      }
    }
  }
}
