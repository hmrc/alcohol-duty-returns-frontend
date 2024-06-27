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

import models.UserAnswers
import models.productEntry.ProductEntry
import pages.productEntry.ProductEntryListPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, Text}
import viewmodels.{TableRowActionViewModel, TableRowViewModel, TableTotalViewModel, TableViewModel}

object ProductListSummaryHelper {

  def productEntryTable(userAnswers: UserAnswers)(implicit messages: Messages): TableViewModel = {

    val productEntries: Seq[ProductEntry] = getProductEntries(userAnswers)
    TableViewModel(
      head = Seq(
        HeadCell(content = Text(messages("productEntryList.name")), classes = "govuk-!-width-one-half"),
        HeadCell(content = Text(messages("productEntryList.duty")), classes = "govuk-!-width-one-quarter"),
        HeadCell(content = Text(messages("productEntryList.action")), classes = "govuk-!-width-one-quarter")
      ),
      rows = getProductEntryRows(productEntries),
      total = Some(
        TableTotalViewModel(
          HeadCell(content = Text(messages("productList.total")), classes = "govuk-!-width-one-half"),
          HeadCell(
            content = Text(messages("site.currency.2DP", productEntries.map(_.duty.getOrElse(BigDecimal(0))).sum)),
            classes = "govuk-!-width-one-half"
          )
        )
      )
    )
  }

  private def getProductEntries(userAnswers: UserAnswers): Seq[ProductEntry] =
    userAnswers.get(ProductEntryListPage).getOrElse(Seq.empty)

  private def getProductEntryRows(productEntries: Seq[ProductEntry])(implicit
    messages: Messages
  ): Seq[TableRowViewModel] =
    productEntries.zipWithIndex.map { case (productEntry, index) =>
      TableRowViewModel(
        cells = Seq(
          Text(productEntry.name.getOrElse("")),
          Text(messages("site.currency.2DP", productEntry.duty.getOrElse(BigDecimal(0))))
        ),
        actions = Seq(
          TableRowActionViewModel(
            label = messages("site.change"),
            href = controllers.productEntry.routes.CheckYourAnswersController.onPageLoad(Some(index)),
            visuallyHiddenText = Some(messages("productList.change.hidden"))
          ),
          TableRowActionViewModel(
            label = messages("site.remove"),
            href = controllers.productEntry.routes.DeleteProductController.onPageLoad(index: Int),
            visuallyHiddenText = Some(messages("productList.remove.hidden"))
          )
        )
      )
    }

}
