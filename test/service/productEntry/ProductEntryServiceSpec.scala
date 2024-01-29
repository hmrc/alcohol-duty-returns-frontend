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
import models.AlcoholRegime
import models.productEntry.{ProductEntry, TaxDuty, TaxType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.QuestionPage
import services.productEntry.ProductEntryServiceImpl
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class ProductEntryServiceSpec extends SpecBase {

  "ProductEntryService" - {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val userAnswers = emptyUserAnswers
      .set(AlcoholByVolumeQuestionPage, BigDecimal(3.5))
      .success
      .value
      .set(DraughtReliefQuestionPage, true)
      .success
      .value
      .set(SmallProducerReliefQuestionPage, false)
      .success
      .value
      .set(ProductVolumePage, BigDecimal(1))
      .success
      .value

    "must create a product entry when TaxType contains rate" in {

      val userAnswerWithRate = userAnswers
        .set(TaxTypePage, TaxType("ALC", AlcoholRegime.Beer, Some(BigDecimal(1))))
        .success
        .value

      val mockConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockConnector.calculateTaxDuty(any(), any(), any())(any()))
        .thenReturn(Future.successful(TaxDuty(BigDecimal(1), BigDecimal(1))))

      val service = new ProductEntryServiceImpl(mockConnector)

      val result = service.createProduct(userAnswerWithRate).futureValue

      result                    shouldBe a[ProductEntry]
      result.abv                shouldBe BigDecimal(3.5)
      result.rate               shouldBe BigDecimal(1)
      result.volume             shouldBe BigDecimal(1)
      result.draughtRelief      shouldBe true
      result.smallProduceRelief shouldBe false
      result.pureAlcoholVolume  shouldBe BigDecimal(1)
      result.duty               shouldBe BigDecimal(1)

    }

    "must create a product entry when TaxType does not contain rate but the Small Producer Relief duty rate is present" in {

      val userAnswerWithRate = userAnswers
        .set(TaxTypePage, TaxType("ALC", AlcoholRegime.Beer, None))
        .success
        .value
        .set(DeclareSmallProducerReliefDutyRatePage, BigDecimal(2))
        .success
        .value

      val mockConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockConnector.calculateTaxDuty(any(), any(), any())(any()))
        .thenReturn(Future.successful(TaxDuty(BigDecimal(1), BigDecimal(1))))

      val service = new ProductEntryServiceImpl(mockConnector)

      val result = service.createProduct(userAnswerWithRate).futureValue

      result                    shouldBe a[ProductEntry]
      result.abv                shouldBe BigDecimal(3.5)
      result.rate               shouldBe BigDecimal(2)
      result.volume             shouldBe BigDecimal(1)
      result.draughtRelief      shouldBe true
      result.smallProduceRelief shouldBe false
      result.pureAlcoholVolume  shouldBe BigDecimal(1)
      result.duty               shouldBe BigDecimal(1)
    }

    "must throw an Exception" - {

      "if both, TaxType and DeclareSmallProducerReliefDutyRatePage contain rate" in {

        val userAnswerWithRate = userAnswers
          .set(TaxTypePage, TaxType("ALC", AlcoholRegime.Beer, Some(BigDecimal(1))))
          .success
          .value
          .set(DeclareSmallProducerReliefDutyRatePage, BigDecimal(2))
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

        val userAnswerWithRate = userAnswers
          .set(TaxTypePage, TaxType("ALC", AlcoholRegime.Beer, None))
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

      val pagesToRemove: Seq[QuestionPage[_]] = Seq(
        AlcoholByVolumeQuestionPage,
        ProductVolumePage,
        DraughtReliefQuestionPage,
        SmallProducerReliefQuestionPage,
        TaxTypePage
      )

      pagesToRemove.foreach { page =>
        s"if UserAnswer doesn't contain value for $page" in {

          val userAnswerWithoutPage = userAnswers
            .remove(page)
            .success
            .value

          val mockConnector = mock[AlcoholDutyCalculatorConnector]
          when(mockConnector.calculateTaxDuty(any(), any(), any())(any()))
            .thenReturn(Future.successful(TaxDuty(BigDecimal(1), BigDecimal(1))))

          val service = new ProductEntryServiceImpl(mockConnector)

          val exception = intercept[RuntimeException] {
            service.createProduct(userAnswerWithoutPage).futureValue
          }

          exception.getLocalizedMessage must include(s"Failed to get value for page $page.")
        }
      }
    }

  }
}
