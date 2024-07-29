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

object Constants {
  val periodKeySessionKey: String    = "period-key"
  val maximumDecimalPlaces: Int      = 2
  val volumeMinimumValue: BigDecimal = BigDecimal(0.01)
  val volumeMaximumValue: BigDecimal = BigDecimal(999999999.99)
  val dutyMinimumValue: BigDecimal   = BigDecimal(0.00)
  val dutyMaximumValue: BigDecimal   = BigDecimal(999999999.99)

  val oneQuarterCssClass     = "govuk-!-width-one-quarter"
  val oneHalfCssClass        = "govuk-!-width-one-half"
  val threeQuartersCssClass  = "govuk-!-width-three-quarters"
  val textAlignRightCssClass = "text-align-right"
  val boldFontCssClass       = "govuk-!-font-weight-bold"

  val blueTagCssClass  = "govuk-tag--blue"
  val greenTagCssClass = "govuk-tag--green"
  val redTagCssClass   = "govuk-tag--red"

  val headingMCssClass = "govuk-heading-m"

  val tableCaptionMCssClass    = "govuk-table__caption--m"
  val tableRowNoBorderCssClass = "govuk-summary-list__row--no-border"
}
