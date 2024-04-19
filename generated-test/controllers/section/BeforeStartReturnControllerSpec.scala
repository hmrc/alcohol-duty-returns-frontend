package controllers.section

import base.SpecBase
import play.api.test.Helpers._
import views.html.section.BeforeStartReturnView

class BeforeStartReturnControllerSpec extends SpecBase {

  "BeforeStartReturn Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.section.routes.BeforeStartReturnController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BeforeStartReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
