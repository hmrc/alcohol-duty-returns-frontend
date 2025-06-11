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

package controllers.declareDuty

import base.SpecBase
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import viewmodels.declareDuty.{CheckYourAnswersSummaryListHelper, ReturnSummaryList}
import views.html.declareDuty.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase {
  "CheckYourAnswers Controller" - {
    "must return OK and the correct view for a GET" in new SetUp {
      when(mockCheckYourAnswersSummaryListHelper.createSummaryList(regime, emptyUserAnswers))
        .thenReturn(Some(returnSummaryList))

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(regime, returnSummaryList)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the Journey Recovery page when no summary can be returned" in new SetUp {
      when(mockCheckYourAnswersSummaryListHelper.createSummaryList(regime, emptyUserAnswers)).thenReturn(None)

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime).url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    class SetUp {
      val mockCheckYourAnswersSummaryListHelper: CheckYourAnswersSummaryListHelper =
        mock[CheckYourAnswersSummaryListHelper]

      val application                 = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CheckYourAnswersSummaryListHelper].toInstance(mockCheckYourAnswersSummaryListHelper))
        .build()
      implicit val messages: Messages = getMessages(application)

      val regime = regimeGen.sample.value

      val summaryList1 = SummaryList(rows =
        Seq(SummaryListRow(key = Key(content = Text("Key1")), value = Value(content = Text("Value1"))))
      )
      val summaryList2 = SummaryList(rows =
        Seq(SummaryListRow(key = Key(content = Text("Key2")), value = Value(content = Text("Value2"))))
      )
      val summaryList3 = SummaryList(rows =
        Seq(SummaryListRow(key = Key(content = Text("Key3")), value = Value(content = Text("Value3"))))
      )

      val returnSummaryList = ReturnSummaryList(
        whatDoYouNeedToDeclareSummary = summaryList1,
        howMuchDoYouNeedToDeclareSummary = Some(summaryList2),
        smallProducerReliefSummary = Some(summaryList3)
      )
    }
  }
}
