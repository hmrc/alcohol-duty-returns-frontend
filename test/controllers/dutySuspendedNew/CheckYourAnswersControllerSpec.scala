package controllers.dutySuspendedNew

import base.SpecBase
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.AlcoholRegimes
import play.api.inject.bind
import org.mockito.ArgumentMatchers.any
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.{SummaryList, SummaryListRow, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, Value}
import viewmodels.checkAnswers.dutySuspendedNew.CheckYourAnswersSummaryListHelper
import viewmodels.govuk.SummaryListFluency
import views.html.dutySuspendedNew.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  lazy val displayCYARoute = controllers.dutySuspendedNew.routes.CheckYourAnswersController.onPageLoad().url

  val expectedAlcoholTypeSummary = SummaryList(
    Seq(
      SummaryListRow(key = Key(Text("Type of alcohol")), value = Value(Text("Beer"))),
      SummaryListRow(key = Key(Text("Type of alcohol")), value = Value(Text("Cider"))),
      SummaryListRow(key = Key(Text("Type of alcohol")), value = Value(Text("Wine"))),
      SummaryListRow(key = Key(Text("Type of alcohol")), value = Value(Text("Other fermented products"))),
      SummaryListRow(key = Key(Text("Type of alcohol")), value = Value(Text("Spirits")))
    )
  )
  val expectedAmountSummary      = SummaryList(
    Seq(
      SummaryListRow(key = Key(Text("Type of alcohol")), value = Value(Text("Beer"))),
      SummaryListRow(key = Key(Text("Total litres of alcohol")), value = Value(Text("112 litres of total product"))),
      SummaryListRow(key = Key(Text("Litres of pure alcohol")), value = Value(Text("10 litres of pure alcohol"))),
      SummaryListRow(key = Key(Text("Type of alcohol")), value = Value(Text("Cider"))),
      SummaryListRow(key = Key(Text("Total litres of alcohol")), value = Value(Text("112 litres of total product"))),
      SummaryListRow(key = Key(Text("Litres of pure alcohol")), value = Value(Text("10 litres of pure alcohol"))),
      SummaryListRow(key = Key(Text("Type of alcohol")), value = Value(Text("Wine"))),
      SummaryListRow(key = Key(Text("Total litres of alcohol")), value = Value(Text("112 litres of total product"))),
      SummaryListRow(key = Key(Text("Litres of pure alcohol")), value = Value(Text("10 litres of pure alcohol"))),
      SummaryListRow(key = Key(Text("Type of alcohol")), value = Value(Text("Other fermented products"))),
      SummaryListRow(key = Key(Text("Total litres of alcohol")), value = Value(Text("112 litres of total product"))),
      SummaryListRow(key = Key(Text("Litres of pure alcohol")), value = Value(Text("10 litres of pure alcohol"))),
      SummaryListRow(key = Key(Text("Type of alcohol")), value = Value(Text("Spirits"))),
      SummaryListRow(key = Key(Text("Total litres of alcohol")), value = Value(Text("112 litres of total product"))),
      SummaryListRow(key = Key(Text("Litres of pure alcohol")), value = Value(Text("10 litres of pure alcohol")))
    )
  )

  "Display CheckYourAnswers Controller" - {
    "must return OK and render the CheckYourAnswersView when all required data is present" in {
      val mockHelper = mock[CheckYourAnswersSummaryListHelper]
      when(mockHelper.alcoholTypeSummaryList(any())(any()))
        .thenReturn(Some(expectedAlcoholTypeSummary))
      when(mockHelper.dutySuspendedAmountsSummaryList(any())(any()))
        .thenReturn(Some(expectedAmountSummary))

      val allRegimeUserAnswers = emptyUserAnswers.copy(
        regimes = AlcoholRegimes(Set(Beer, Cider, Wine, Spirits, OtherFermentedProduct))
      )

      val application = applicationBuilder(userAnswers = Some(allRegimeUserAnswers))
        .overrides(bind[CheckYourAnswersSummaryListHelper].toInstance(mockHelper))
        .build()

      running(application) {
        val request = FakeRequest(GET, displayCYARoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val expectedRenderedView =
          if (allRegimeUserAnswers.regimes.regimes.size > 4) {
            view(Some(expectedAlcoholTypeSummary), expectedAmountSummary)(request, getMessages(application))
          } else {
            view(None, expectedAmountSummary)(request, getMessages(application))
          }

        status(result)          mustEqual OK
        contentAsString(result) mustEqual expectedRenderedView.toString
      }
    }
  }

  "must render the view with None for alcoholTypeSummary when there is only one regime" in {
    val mockHelper = mock[CheckYourAnswersSummaryListHelper]
    when(mockHelper.alcoholTypeSummaryList(any())(any()))
      .thenReturn(Some(expectedAlcoholTypeSummary))
    when(mockHelper.dutySuspendedAmountsSummaryList(any())(any()))
      .thenReturn(Some(expectedAmountSummary))

    val oneRegimeUserAnswers = emptyUserAnswers.copy(
      regimes = AlcoholRegimes(Set(Beer))
    )

    val application = applicationBuilder(userAnswers = Some(oneRegimeUserAnswers))
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
    val application = applicationBuilder(Some(userAnswersWithAllRegimes)).build()
    running(application) {
      val request =
        FakeRequest(GET, displayCYARoute)

      val result = route(application, request).value

      status(result)                 mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}
