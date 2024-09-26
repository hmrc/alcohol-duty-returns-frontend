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

import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}

case class TableViewModel(
  head: Seq[HeadCell],
  rows: Seq[TableRowViewModel],
  total: Option[TableTotalViewModel] = None
)

object TableViewModel {
  def empty(): TableViewModel = TableViewModel(Seq.empty, Seq.empty, None)
}
case class TableRowViewModel(cells: Seq[TableRow], actions: Seq[TableRowActionViewModel] = Seq.empty)

case class TableTotalViewModel(legend: HeadCell, total: HeadCell) {
  def toHeadCells(): Seq[HeadCell] =
    Seq(legend, total)
}

case class TableRowActionViewModel(label: String, href: Call, visuallyHiddenText: Option[String] = None)
