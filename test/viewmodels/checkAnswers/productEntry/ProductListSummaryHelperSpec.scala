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

package viewmodels.checkAnswers.productEntry

import base.SpecBase
import generators.ModelGenerators
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.productEntry.ProductEntryListPage
import play.api.Application
import play.api.i18n.Messages

class ProductListSummaryHelperSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {
  val application: Application    = applicationBuilder().build()
  implicit val messages: Messages = messages(application)
  "ProductListSummaryHelper" - {

    "must return a table with the correct head" in {
      forAll(arbitraryProductEntryList) { productEntryList =>
        val productList = productEntryList.arbitrary.sample.value
        val userAnswers = emptyUserAnswers.set(ProductEntryListPage, productList).success.value
        val table       = ProductListSummaryHelper.productEntryTable(userAnswers)

        table.head.size shouldBe 3
      }
    }

    "must return a table with the correct rows" in {
      forAll(arbitraryProductEntryList) { productEntryList =>
        val productList = productEntryList.arbitrary.sample.value
        val userAnswers = emptyUserAnswers.set(ProductEntryListPage, productList).success.value
        val table       = ProductListSummaryHelper.productEntryTable(userAnswers)
        table.rows.size shouldBe productList.size
        table.rows.zipWithIndex.foreach { case (row, index) =>
          row.actions.head.href shouldBe controllers.productEntry.routes.CheckYourAnswersController
            .onPageLoad(Some(index))
          row.actions(1).href   shouldBe controllers.productEntry.routes.DeleteProductController.onPageLoad(index)
        }
      }
    }

    "must return the correct total" in {
      forAll(arbitraryProductEntryList) { productEntryList =>
        val productList = productEntryList.arbitrary.sample.value
        val userAnswers = emptyUserAnswers.set(ProductEntryListPage, productList).success.value
        val table       = ProductListSummaryHelper.productEntryTable(userAnswers)
        table.total shouldBe productList.map(_.duty.get).sum
      }
    }

    "must return the correct total if one of the product entries has an undefined duty" in {
      forAll(arbitraryProductEntryList) { productEntryList =>
        val productList               = productEntryList.arbitrary.sample.value
        val expectedSum               = productList.map(_.duty.get).sum
        val undefinedDutyProductEntry = productEntryGen.sample.get.copy(duty = None)

        val userAnswers =
          emptyUserAnswers.set(ProductEntryListPage, productList :+ undefinedDutyProductEntry).success.value
        val table       = ProductListSummaryHelper.productEntryTable(userAnswers)

        table.total shouldBe expectedSum
      }
    }

    "must return the correct total if the list is empty" in {
      val userAnswers = emptyUserAnswers.set(ProductEntryListPage, Seq.empty).success.value
      val table       = ProductListSummaryHelper.productEntryTable(userAnswers)
      table.total shouldBe BigDecimal(0)
    }

  }

}
