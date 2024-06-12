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

package viewmodels.checkAnswers.returns

import models.{AlcoholRegime, Error, NormalMode, RateBand, UserAnswers}
import models.returns.VolumeAndRateByTaxType
import pages.returns.{MultipleSPRListPage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, Text}
import viewmodels.checkAnswers.returns.RateBandHelper.{rateBandContent, rateBandRecap}
import viewmodels.{TableRowActionViewModel, TableRowViewModel, TableViewModel}

object MultipleSPRListHelper {

  def sprTableViewModel(userAnswers: UserAnswers, regime: AlcoholRegime)(implicit
    messages: Messages
  ): Either[Error, TableViewModel] =
    getSprListEntries(userAnswers, regime) match {
      case Right(sprList) =>
        Right(
          TableViewModel(
            head = Seq(
              HeadCell(
                content = Text(messages("multipleSPRList.description.label")),
                classes = "govuk-!-width-half"
              ),
              HeadCell(
                content = Text(messages("multipleSPRList.totalLitres.label"))
              ),
              HeadCell(
                content = Text(messages("multipleSPRList.pureAlcohol.label"))
              ),
              HeadCell(
                content = Text(messages("multipleSPRList.dutyRate.label"))
              ),
              HeadCell(
                content = Text(messages("multipleSPRList.action.label")),
                classes = "govuk-!-width-one-quarter"
              )
            ),
            rows = getSPREntryRows(sprList, regime),
            total = BigDecimal(0)
          )
        )
      case Left(e: Error) => Left(e)
    }

  case class SprDutyRateEntry(dutyByTaxType: VolumeAndRateByTaxType, rateBand: RateBand)

  def getSprListEntries(userAnswers: UserAnswers, regime: AlcoholRegime): Either[Error, Seq[SprDutyRateEntry]] =
    (
      userAnswers.getByKey(MultipleSPRListPage, regime),
      userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime)
    ) match {
      case (Some(dutyByTaxTypes), Some(rateBands)) =>
        val sprEntries = dutyByTaxTypes.map { dutyByTaxType =>
          rateBands.find(_.taxType == dutyByTaxType.taxType) match {
            case Some(rateBand) => Right(SprDutyRateEntry(dutyByTaxType, rateBand))
            case None           => Left(dutyByTaxType.taxType)
          }
        }

        sprEntries.partitionMap(identity) match {
          case (Nil, sprDutyRateEntries) => Right(sprDutyRateEntries)
          case (taxTypes, _)             => Left(Error(s"Tax types not found: ${taxTypes.mkString(", ")}"))
        }
      case _                                       => Left(Error("error"))
    }

  def getSPREntryRows(sprList: Seq[SprDutyRateEntry], regime: AlcoholRegime)(implicit
    messages: Messages
  ): Seq[TableRowViewModel] =
    sprList.zipWithIndex.map { case (sprEntry, index) =>
      TableRowViewModel(
        cells = Seq(
          Text(rateBandRecap(sprEntry.rateBand)),
          Text(messages("multipleSPRList.totalLitres.value", sprEntry.dutyByTaxType.totalLitres)),
          Text(messages("multipleSPRList.pureAlcohol.value", sprEntry.dutyByTaxType.pureAlcohol)),
          Text(messages("multipleSPRList.dutyRate.value", sprEntry.dutyByTaxType.dutyRate))
        ),
        actions = Seq(
          TableRowActionViewModel(
            label = messages("site.change"),
            href = controllers.returns.routes.TellUsAboutMultipleSPRRateController
              .onPageLoad(NormalMode, regime, Some(index)),
            visuallyHiddenText = Some(messages("productList.change.hidden"))
          ),
          TableRowActionViewModel(
            label = messages("site.remove"),
            href = controllers.returns.routes.DeleteMultipleSPREntryController.onPageLoad(regime, index),
            visuallyHiddenText = Some(messages("productList.remove.hidden"))
          )
        )
      )
    }
}
