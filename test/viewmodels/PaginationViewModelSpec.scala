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

package viewmodels

import base.SpecBase
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{PaginationItem, PaginationLink}

class PaginationViewModelSpec extends SpecBase {

  def createUrl(page: Int): String = s"/adjustment-list?page=$page"

  "PaginationViewModel should" - {

    "create correct pagination with multiple pages and middle current page" in {
      val currentPage = 2
      val totalPages = 5
      val paginationViewModel = PaginationViewModel(currentPage, totalPages, createUrl)
      val pagination = paginationViewModel.paginate
      pagination.previous shouldBe Some(PaginationLink(href = createUrl(1)))
      pagination.next shouldBe Some(PaginationLink(href = createUrl(3)))
      pagination.items shouldBe Some(Seq(
        PaginationItem(href = createUrl(1), number = Some("1"), current = Some(false)),
        PaginationItem(href = createUrl(2), number = Some("2"), current = Some(true)),
        PaginationItem(href = createUrl(3), number = Some("3"), current = Some(false)),
        PaginationItem(href = createUrl(4), number = Some("4"), current = Some(false)),
        PaginationItem(href = createUrl(5), number = Some("5"), current = Some(false))
      ))
    }

    "handle the first page correctly" in {
      val currentPage = 1
      val totalPages = 5
      val paginationViewModel = PaginationViewModel(currentPage, totalPages, createUrl)
      val pagination = paginationViewModel.paginate

      pagination.previous shouldBe None
      pagination.next shouldBe Some(PaginationLink(href = createUrl(2)))
      pagination.items shouldBe Some(Seq(
        PaginationItem(href = createUrl(1), number = Some("1"), current = Some(true)),
        PaginationItem(href = createUrl(2), number = Some("2"), current = Some(false)),
        PaginationItem(href = createUrl(3), number = Some("3"), current = Some(false)),
        PaginationItem(href = createUrl(4), number = Some("4"), current = Some(false)),
        PaginationItem(href = createUrl(5), number = Some("5"), current = Some(false))
      ))
    }

    "handle the last page correctly" in {
      val currentPage = 5
      val totalPages = 5
      val paginationViewModel = PaginationViewModel(currentPage, totalPages, createUrl)
      val pagination = paginationViewModel.paginate
      pagination.previous shouldBe Some(PaginationLink(href = createUrl(4)))
      pagination.next shouldBe None
      pagination.items shouldBe Some(Seq(
        PaginationItem(href = createUrl(1), number = Some("1"), current = Some(false)),
        PaginationItem(href = createUrl(2), number = Some("2"), current = Some(false)),
        PaginationItem(href = createUrl(3), number = Some("3"), current = Some(false)),
        PaginationItem(href = createUrl(4), number = Some("4"), current = Some(false)),
        PaginationItem(href = createUrl(5), number = Some("5"), current = Some(true))
      ))
    }

    "handle a single page scenario" in {
      val currentPage = 1
      val totalPages = 1
      val paginationViewModel = PaginationViewModel(currentPage, totalPages, createUrl)
      val pagination = paginationViewModel.paginate
      pagination.previous shouldBe None
      pagination.next shouldBe None
      pagination.items shouldBe Some(Seq(
        PaginationItem(href = createUrl(1), number = Some("1"), current = Some(true))
      ))
    }

    "handle two pages with first page selected" in {
      val currentPage = 1
      val totalPages = 2
      val paginationViewModel = PaginationViewModel(currentPage, totalPages, createUrl)
      val pagination = paginationViewModel.paginate
      pagination.previous shouldBe None
      pagination.next shouldBe Some(PaginationLink(href = createUrl(2)))
      pagination.items shouldBe Some(Seq(
        PaginationItem(href = createUrl(1), number = Some("1"), current = Some(true)),
        PaginationItem(href = createUrl(2), number = Some("2"), current = Some(false))
      ))
    }

    "handle two pages with second page selected" in {
      val currentPage = 2
      val totalPages = 2
      val paginationViewModel = PaginationViewModel(currentPage, totalPages, createUrl)
      val pagination = paginationViewModel.paginate
      pagination.previous shouldBe Some(PaginationLink(href = createUrl(1)))
      pagination.next shouldBe None
      pagination.items shouldBe Some(Seq(
        PaginationItem(href = createUrl(1), number = Some("1"), current = Some(false)),
        PaginationItem(href = createUrl(2), number = Some("2"), current = Some(true))
      ))
    }
  }
}
