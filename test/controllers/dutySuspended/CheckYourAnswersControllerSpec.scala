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

package controllers.dutySuspended

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.{SummaryList, SummaryListRow, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, Value}
import viewmodels.checkAnswers.dutySuspended.CheckYourAnswersSummaryListHelper
import viewmodels.govuk.SummaryListFluency
import views.html.dutySuspended.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  lazy val displayCYARoute = controllers.dutySuspended.routes.CheckYourAnswersController.onPageLoad().url

  override def configOverrides: Map[String, Any] = Map(
    "features.duty-suspended-new-journey" -> true
  )

  "CheckYourAnswers Controller" - {
    "must return OK and render the CheckYourAnswersView when all required data is present" in {
      val expectedAlcoholTypeSummary = SummaryList(
        Seq(
          SummaryListRow(
            key = Key(Text("Type of alcohol")),
            value = Value(HtmlContent("Beer<br>Cider<br>Wine<br>Spirits<br>Other fermented products"))
          )
        )
      )

      val expectedAmountSummary = SummaryList(
        Seq(
          SummaryListRow(
            key = Key(Text("Beer")),
            value = Value(HtmlContent("112 litres of total product<br>10 litres of pure alcohol"))
          ),
          SummaryListRow(
            key = Key(Text("Cider")),
            value = Value(HtmlContent("112 litres of total product<br>10 litres of pure alcohol"))
          ),
          SummaryListRow(
            key = Key(Text("Wine")),
            value = Value(HtmlContent("112 litres of total product<br>10 litres of pure alcohol"))
          ),
          SummaryListRow(
            key = Key(Text("Spirits")),
            value = Value(HtmlContent("112 litres of total product<br>10 litres of pure alcohol"))
          ),
          SummaryListRow(
            key = Key(Text("Other fermented products")),
            value = Value(HtmlContent("112 litres of total product<br>10 litres of pure alcohol"))
          )
        )
      )

      val mockHelper = mock[CheckYourAnswersSummaryListHelper]
      when(mockHelper.alcoholTypeSummaryList(any())(any()))
        .thenReturn(Some(expectedAlcoholTypeSummary))
      when(mockHelper.dutySuspendedAmountsSummaryList(any())(any()))
        .thenReturn(Some(expectedAmountSummary))

      val application = applicationBuilder(userAnswers = Some(userAnswersAllDSDRegimesSelected))
        .overrides(bind[CheckYourAnswersSummaryListHelper].toInstance(mockHelper))
        .build()

      running(application) {
        val request = FakeRequest(GET, displayCYARoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val expectedRenderedView =
          view(Some(expectedAlcoholTypeSummary), expectedAmountSummary)(request, getMessages(application))

        status(result)          mustEqual OK
        contentAsString(result) mustEqual expectedRenderedView.toString
      }
    }
  }

  "must render the view with None for alcoholTypeSummary when there is only one regime" in {
    val expectedAlcoholTypeSummary = SummaryList(
      Seq(
        SummaryListRow(key = Key(Text("Type of alcohol")), value = Value(HtmlContent("Beer")))
      )
    )

    val expectedAmountSummary = SummaryList(
      Seq(
        SummaryListRow(
          key = Key(Text("Beer")),
          value = Value(HtmlContent("112 litres of total product<br>10 litres of pure alcohol"))
        )
      )
    )

    val mockHelper = mock[CheckYourAnswersSummaryListHelper]
    when(mockHelper.alcoholTypeSummaryList(any())(any()))
      .thenReturn(Some(expectedAlcoholTypeSummary))
    when(mockHelper.dutySuspendedAmountsSummaryList(any())(any()))
      .thenReturn(Some(expectedAmountSummary))

    val application = applicationBuilder(userAnswers = Some(userAnswersWithBeer))
      .overrides(bind[CheckYourAnswersSummaryListHelper].toInstance(mockHelper))
      .build()

    running(application) {
      val request = FakeRequest(GET, displayCYARoute)
      val result  = route(application, request).value

      val view                 = application.injector.instanceOf[CheckYourAnswersView]
      val expectedRenderedView =
        view(None, expectedAmountSummary)(request, getMessages(application))

      status(result)          mustEqual OK
      contentAsString(result) mustEqual expectedRenderedView.toString
    }
  }

  "must redirect to Journey Recovery when alcohol types or duty suspended volumes are missing" in {
    val mockHelper = mock[CheckYourAnswersSummaryListHelper]
    when(mockHelper.alcoholTypeSummaryList(any())(any()))
      .thenReturn(None)
    when(mockHelper.dutySuspendedAmountsSummaryList(any())(any()))
      .thenReturn(None)

    val application = applicationBuilder(Some(userAnswersWithAllRegimes))
      .overrides(bind[CheckYourAnswersSummaryListHelper].toInstance(mockHelper))
      .build()

    running(application) {
      val request = FakeRequest(GET, displayCYARoute)

      val result = route(application, request).value

      status(result)                 mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}
