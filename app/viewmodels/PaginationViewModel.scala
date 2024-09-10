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

import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}

case class PaginationViewModel(
                                currentPage: Int,
                                totalPages: Int,
                                baseUrl: Int => String
                              ) {
  def paginate: Pagination = {
    Pagination(
      previous = if (currentPage > 1) Some(PaginationLink(href = baseUrl(currentPage - 1))) else None,
      next = if (currentPage < totalPages) Some(PaginationLink(href = baseUrl(currentPage + 1))) else None,
      items = Some((1 to totalPages).map { page =>
        PaginationItem(
          href = baseUrl(page),
          number = Some(page.toString),
          current = Some(page == currentPage)
        )
      })
    )
  }
}
