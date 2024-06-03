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

package controllers.returns

import base.SpecBase
import play.api.test.Helpers._
import viewmodels.checkAnswers.returns.CheckYourAnswersSPRSummaryListHelper
import views.html.returns.CheckYourAnswersSPRView

class CheckYourAnswersSPRControllerSpec extends SpecBase {

  "CheckYourAnswerSPR Controller" - {

    "must return OK and the correct view for a GET" in {

      val regime = regimeGen.sample.value

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.returns.routes.CheckYourAnswersSPRController.onPageLoad(regime).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersSPRView]

        val summaryList =
          CheckYourAnswersSPRSummaryListHelper.summaryList(regime, emptyUserAnswers, None)(messages(application)).get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(regime, summaryList, None)(request, messages(application)).toString
      }
    }
  }
}
