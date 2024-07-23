package controllers.checkAndSubmit

import base.SpecBase
import play.api.test.Helpers._
import views.html.checkAndSubmit.ReturnSubmittedView

class ReturnSubmittedControllerSpec extends SpecBase {

  "ReturnSubmitted Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.checkAndSubmit.routes.ReturnSubmittedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReturnSubmittedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
