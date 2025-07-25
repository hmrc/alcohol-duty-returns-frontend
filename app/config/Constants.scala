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

package config

import java.time.Month
import java.time.Month._

object Constants {
  val returnCreatedDetailsKey: String = "return-created-details"
  val periodKeySessionKey: String     = "period-key"
  val pastPaymentsSessionKey: String  = "past-payment-amount"

  val noDetailsValue: String = "no-details"

  object MappingFields {
    val taxTypeField: String             = "taxType"
    val totalLitresField: String         = "totalLitres"
    val totalLitresVolumeField: String   = "totalLitresVolume"
    val pureAlcoholField: String         = "pureAlcohol"
    val pureAlcoholVolumeField: String   = "pureAlcoholVolume"
    val dutyField: String                = "duty"
    val dutyRateField: String            = "dutyRate"
    val sprDutyRateField: String         = "sprDutyRate"
    val rateBandDescriptionField: String = "rateBandDescription"
  }

  val maximumTwoDecimalPlaces: Int                = 2
  val volumeMinimumValueIncZero: BigDecimal       = BigDecimal(0.00)
  val volumeMinimumValue: BigDecimal              = BigDecimal(0.01)
  val volumeMaximumValue: BigDecimal              = BigDecimal(999999999.99)
  val dutySuspendedVolumeMinimumValue: BigDecimal = BigDecimal(-999999999.99)
  val dutySuspendedVolumeMaximumValue: BigDecimal = BigDecimal(999999999.99)
  val lpaMaximumDecimalPlaces: Int                = 4
  val lpaMinimumValue: BigDecimal                 = BigDecimal(0.0001)
  val lpaMaximumValue: BigDecimal                 = BigDecimal(999999999.9999)
  val dutySuspendedLpaMinimumValue: BigDecimal    = BigDecimal(-999999999.9999)
  val dutySuspendedLpaMaximumValue: BigDecimal    = BigDecimal(999999999.9999)
  val dutyMaximumDecimalPlaces: Int               = 2
  val dutyMinimumValue: BigDecimal                = BigDecimal(0.00)
  val dutyMaximumValue: BigDecimal                = BigDecimal(999999999.99)
  val spoiltDutyMinimumValue: BigDecimal          = BigDecimal(0.01)
  val spoiltDutyMaximumValue: BigDecimal          = BigDecimal(99999999999.99)

  val dutySuspendedVolumeNewMinimumValue: BigDecimal = BigDecimal(0.00)
  val dutySuspendedLpaNewMinimumValue: BigDecimal    = BigDecimal(0.0000)

  val dayOfSubmissionDeadline: Int = 15

  private val oneThousand: Int = 1000

  val overUnderDeclarationThreshold: BigDecimal = BigDecimal(oneThousand) // When a reason is required
  val overUnderDeclarationReasonLength: Int     = 250

  val adjustmentTooEarlyMonth: Int = 7 // Only dates afterward we allow to be adjusted via the service
  val adjustmentTooEarlyYear: Int  = 2023

  val minTaxType = 100 // Although the boundaries will not be valid codes
  val maxTaxType = 999

  val maxABV: Int = 100

  val otherSpiritsProducedMaxLength = 150

  val rowsPerPage = 15

  val quarterlySpiritsMonths: Set[Month] = Set(MARCH, JUNE, SEPTEMBER, DECEMBER)

  val ukTimeZoneStringId = "Europe/London"

  object Format {
    val numeric = "numeric"
  }

  object Css {
    val bodyCssClass = "govuk-body"

    val hintCssClass = "govuk-hint"

    val linkNoVisitedStateCssClass = "govuk-link govuk-link--no-visited-state"

    val sectionBreakCssClass        = "govuk-section-break"
    val sectionBreakLCssClass       = "govuk-section-break--l"
    val sectionBreakVisibleCssClass = "govuk-section-break--visible"

    val oneQuarterCssClass = "govuk-!-width-one-quarter"
    val twoThirdsCssClass  = "govuk-!-width-two-thirds"

    val textAlignRightCssClass     = "text-align-right"
    val textAlignRightWrapCssClass = "text-align-right-wrap"
    val noWrap                     = "no-wrap"

    val boldFontCssClass = "govuk-!-font-weight-bold"

    val lightBlueTagCssClass = "govuk-tag--light-blue"
    val blueTagCssClass      = "govuk-tag--blue"
    val greenTagCssClass     = "govuk-tag--green"
    val redTagCssClass       = "govuk-tag--red"
    val greyTagCssClass      = "govuk-tag--grey"

    val cannotStartYetCssClass = "govuk-task-list__status govuk-task-list__status--cannot-start-yet"

    val headingXLCssClass = "govuk-heading-xl"
    val headingLCssClass  = "govuk-heading-l"
    val headingMCssClass  = "govuk-heading-m"

    val labelXLCssClass = "govuk-label--xl"
    val labelLCssClass  = "govuk-label--l"
    val labelMCssClass  = "govuk-label--m"

    val captionXLCssClass = "govuk-caption-xl"

    val fieldsetLegendXLCssClass = "govuk-fieldset__legend--xl"
    val fieldsetLegendLCssClass  = "govuk-fieldset__legend--l"
    val fieldsetLegendMCssClass  = "govuk-fieldset__legend--m"
    val fieldsetLegendSCssClass  = "govuk-fieldset__legend--s"

    val tableCaptionMCssClass = "govuk-table__caption--m"
    val tableCaptionLCssClass = "govuk-table__caption--l"

    val gridRowCssClass        = "govuk-grid-row"
    val gridColumnOneThird     = "govuk-grid-column-one-third"
    val gridColumnTwoThirds    = "govuk-grid-column-two-thirds"
    val gridColumnFullCssClass = "govuk-grid-column-full"

    val numericCellClass = "govuk-table__cell--numeric"

    val summaryListNoBorderCssClass        = "govuk-summary-list--no-border"
    val summaryListRowNoBorderCssClass     = "govuk-summary-list__row--no-border"
    val summaryListActionsListCssClass     = "govuk-summary-list__actions-list"
    val summaryListActionsListItemCssClass = "govuk-summary-list__actions-list-item"

    val marginBottom0CssClass = "govuk-!-margin-bottom-0"
    val marginBottom8CssClass = "govuk-!-margin-bottom-8"

    val reportIssueLinkMarginCssClass = "govuk-!-margin-bottom-1 govuk-!-margin-top-8"

    val paddingBottomCssClass = "govuk-body govuk-!-padding-bottom-3"

    val inputWidth2CssClass  = "govuk-input--width-2"
    val inputWidth4CssClass  = "govuk-input--width-4"
    val inputWidth10CssClass = "govuk-input--width-10"

    val secondaryButtonCssClass = "govuk-button--secondary"

    val inlineRadiosCssClass = "govuk-radios--inline"

    val numberListCssClass = "govuk-list govuk-list--number"
    val bulletListCssClass = "govuk-list govuk-list--bullet"

    val visuallyHiddenCssClass = "govuk-visually-hidden"

    val inputErrorCssClass = "govuk-input--error"

    val jsVisibleCssClass = "hmrc-!-js-visible"

    val displayNonePrintCssClass = "govuk-!-display-none-print"

    val formGroupCssClass      = "govuk-form-group"
    val formGroupErrorCssClass = "govuk-form-group--error"

    val errorMessageCssClass = "govuk-error-message"
  }
}
