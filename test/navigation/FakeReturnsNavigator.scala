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

package navigation

import play.api.mvc.Call
import pages._
import models.{Mode, UserAnswers}
import org.scalatest.Assertions.fail

class FakeReturnsNavigator(desiredRoute: Call, hasAnswerChangeValue: Option[Boolean]) extends ReturnsNavigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, boolean: Option[Boolean]): Call =
    desiredRoute

  override def nextPageWithRegime(
    page: Page,
    mode: Mode,
    userAnswers: UserAnswers,
    regime: models.AlcoholRegime,
    hasAnswerChanged: Boolean,
    index: Option[Int]
  ): Call =
    if (hasAnswerChanged == hasAnswerChangeValue.getOrElse(hasAnswerChanged)) {
      desiredRoute
    } else {
      fail("Answer has not changed")
    }
}
