/*
 * Copyright 2025 HM Revenue & Customs
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

import models.{AlcoholRegime, RateBand, UserAnswers}
import pages.declareDuty.{DoYouHaveMultipleSPRDutyRatesPage, MultipleSPRListPage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

object MissingSPRRateBandHelper {
  def findMissingSPRRateBands(regime: AlcoholRegime, userAnswers: UserAnswers): Option[Set[RateBand]] =
    if (userAnswers.getByKey(DoYouHaveMultipleSPRDutyRatesPage, regime).contains(true)) {
      for {
        selectedSPRRateBands <- userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime).map(_.filter(_.rateType.isSPR))
        declaredSPRTaxTypes  <- userAnswers.getByKey(MultipleSPRListPage, regime).map(_.map(_.taxType))
      } yield selectedSPRRateBands.filter(rateBand => !declaredSPRTaxTypes.contains(rateBand.taxTypeCode))
    } else {
      None
    }

  def getMissingRateBandDescriptions(regime: AlcoholRegime, missingRateBands: Set[RateBand])(implicit
    messages: Messages
  ): Seq[HtmlContent] =
    missingRateBands.toSeq.sortBy(_.taxTypeCode).map { rateBand =>
      HtmlContent(RateBandDescription.toDescription(rateBand, Some(regime)).capitalize)
    }

}
