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

package viewmodels.declareDuty

import models.{AlcoholRegime, NormalMode, RateBand, UserAnswers}
import models.declareDuty.VolumeAndRateByTaxType
import pages.declareDuty.{MultipleSPRListPage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import RateBandDescription.toDescription
import config.Constants.{Css, Format}
import viewmodels.{TableRowActionViewModel, TableRowViewModel, TableViewModel}

object MultipleSPRListHelper {

  def sprTableViewModel(userAnswers: UserAnswers, regime: AlcoholRegime)(implicit
    messages: Messages
  ): Either[String, TableViewModel] =
    getSprListEntries(userAnswers, regime) match {
      case Right(sprList) =>
        Right(
          TableViewModel(
            head = Seq(
              HeadCell(
                content = Text(messages("multipleSPRList.description.label"))
              ),
              HeadCell(
                format = Some(Format.numeric),
                content = Text(messages("multipleSPRList.totalLitres.label"))
              ),
              HeadCell(
                format = Some(Format.numeric),
                content = Text(messages("multipleSPRList.pureAlcohol.label"))
              ),
              HeadCell(
                format = Some(Format.numeric),
                content = Text(messages("multipleSPRList.dutyRate.label"))
              ),
              HeadCell(
                content = Text(messages("multipleSPRList.action.label")),
                classes = Css.oneQuarterCssClass
              )
            ),
            rows = getSPREntryRows(sprList, regime)
          )
        )
      case Left(e)        => Left(e)
    }

  private case class SprDutyRateEntry(dutyByTaxType: VolumeAndRateByTaxType, rateBand: RateBand)

  private def getSprListEntries(
    userAnswers: UserAnswers,
    regime: AlcoholRegime
  ): Either[String, Seq[SprDutyRateEntry]] =
    (
      userAnswers.getByKey(MultipleSPRListPage, regime),
      userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime)
    ) match {
      case (Some(dutyByTaxTypes), Some(rateBands)) =>
        val sprEntries = dutyByTaxTypes.map { dutyByTaxType =>
          rateBands.find(_.taxTypeCode == dutyByTaxType.taxType) match {
            case Some(rateBand) => Right(SprDutyRateEntry(dutyByTaxType, rateBand))
            case None           => Left(dutyByTaxType.taxType)
          }
        }

        sprEntries.partitionMap(identity) match {
          case (Nil, sprDutyRateEntries) => Right(sprDutyRateEntries)
          case (taxTypes, _)             => Left(s"Tax types not found: ${taxTypes.mkString(", ")}")
        }
      case _                                       => Left("Error retrieving SPR entries and rate bands")
    }

  private def getSPREntryRows(sprList: Seq[SprDutyRateEntry], regime: AlcoholRegime)(implicit
    messages: Messages
  ): Seq[TableRowViewModel] =
    sprList.zipWithIndex.map { case (sprEntry, index) =>
      val rateBandDescription = toDescription(sprEntry.rateBand, Some(regime))
      TableRowViewModel(
        cells = Seq(
          TableRow(Text(rateBandDescription.capitalize)),
          TableRow(Text(messages("site.2DP", sprEntry.dutyByTaxType.totalLitres)), format = Some(Format.numeric)),
          TableRow(Text(messages("site.4DP", sprEntry.dutyByTaxType.pureAlcohol)), format = Some(Format.numeric)),
          TableRow(Text(messages("site.currency.2DP", sprEntry.dutyByTaxType.dutyRate)), format = Some(Format.numeric))
        ),
        actions = Seq(
          TableRowActionViewModel(
            label = messages("site.change"),
            href = controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController
              .onPageLoad(NormalMode, regime, Some(index)),
            visuallyHiddenText = Some(rateBandDescription)
          ),
          TableRowActionViewModel(
            label = messages("site.remove"),
            href = controllers.declareDuty.routes.DeleteMultipleSPREntryController.onPageLoad(regime, Some(index)),
            visuallyHiddenText = Some(rateBandDescription)
          )
        )
      )
    }
}
