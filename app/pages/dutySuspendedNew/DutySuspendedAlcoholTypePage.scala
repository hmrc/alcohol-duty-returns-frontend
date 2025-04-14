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

package pages.dutySuspendedNew

import models.{AlcoholRegime, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case object DutySuspendedAlcoholTypePage extends QuestionPage[Set[AlcoholRegime]] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "dutySuspendedAlcoholType"

  override def cleanup(value: Option[Set[AlcoholRegime]], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(alcoholRegimes) =>
        val regimesToRemove = userAnswers.regimes.regimes.diff(alcoholRegimes)
        regimesToRemove.foldLeft(Try(userAnswers)) { (currentUserAnswers, regime) =>
          currentUserAnswers.flatMap(
            _.removeByKey(DutySuspendedQuantitiesPage, regime).flatMap(
              _.removeByKey(DutySuspendedFinalVolumesPage, regime)
            )
          )
        }
      case None                 => super.cleanup(value, userAnswers)
    }
}
