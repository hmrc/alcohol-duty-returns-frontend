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

package navigation

import base.SpecBase
import controllers.routes
import models._
import pages._

class DeclareDutySuspendedDeliveriesNavigatorSpec extends SpecBase {

  val navigator = new DeclareDutySuspendedDeliveriesNavigator

  "DeclareDutySuspendedDeliveriesNavigator" - {

    "in Normal mode" - {

      "must go from the Declare duty suspended deliveries question page to Duty suspended deliveries guidance page if the answer is Yes" in {

        navigator.nextPage(
          DeclareDutySuspendedDeliveriesQuestionPage,
          NormalMode,
          UserAnswers("id").set(DeclareDutySuspendedDeliveriesQuestionPage, true).success.value
        ) mustBe routes.DutySuspendedDeliveriesGuidanceController.onPageLoad()
      }

      "must go from the Declare duty suspended deliveries question page to task list page if the answer is No" in {

        navigator.nextPage(
          DeclareDutySuspendedDeliveriesQuestionPage,
          NormalMode,
          UserAnswers("id").set(DeclareDutySuspendedDeliveriesQuestionPage, false).success.value
        ) mustBe routes.IndexController.onPageLoad
      }

      "must go from the Declare duty suspended deliveries question page to journey recovery page if the answer there is not an answer" in {

        navigator.nextPage(
          DeclareDutySuspendedDeliveriesQuestionPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.JourneyRecoveryController.onPageLoad(None)
      }

      "must go from the Declare duty suspended deliveries outside UK page to Declare duty suspended deliveries inside UK page" in {

        navigator.nextPage(
          DeclareDutySuspendedDeliveriesOutsideUkPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.DutySuspendedDeliveriesController.onPageLoad(NormalMode)
      }

      "must go from the Declare duty suspended deliveries inside UK page to Declare duty suspended received page" in {

        navigator.nextPage(
          DutySuspendedDeliveriesPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.DeclareDutySuspendedReceivedController.onPageLoad(NormalMode)
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad
      }
    }
  }
}
