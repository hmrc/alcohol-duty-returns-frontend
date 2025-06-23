/*
 * Copyright 2023 HM Revenue & Customs
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

package pages.dutySuspended

import models.UserAnswers
import play.api.libs.json.JsPath
import pages.QuestionPage

import scala.util.Try

case object DeclareDutySuspendedDeliveriesQuestionPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "declareDutySuspendedDeliveriesQuestion"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(false)) {
      userAnswers.remove(
        List(
          DutySuspendedBeerPage,
          DutySuspendedCiderPage,
          DutySuspendedWinePage,
          DutySuspendedSpiritsPage,
          DutySuspendedOtherFermentedPage
        )
      )
    } else {
      super.cleanup(value, userAnswers)
    }
}
