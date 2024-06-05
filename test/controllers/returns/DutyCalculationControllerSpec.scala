package controllers.returns

import base.SpecBase
import play.api.test.Helpers._
import views.html.returns.DutyCalculationView

class DutyCalculationControllerSpec extends SpecBase {

  "DutyCalculation Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.returns.routes.DutyCalculationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutyCalculationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
