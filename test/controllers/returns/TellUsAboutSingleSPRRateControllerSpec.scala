package controllers.returns

import base.SpecBase
import forms.returns.TellUsAboutSingleSPRRateFormProvider
import models.NormalMode
import models.returns.TellUsAboutSingleSPRRate
import navigation.{FakeReturnsNavigator, ReturnsNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.returns.TellUsAboutSingleSPRRatePage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.CacheConnector
import uk.gov.hmrc.http.HttpResponse
import views.html.returns.TellUsAboutSingleSPRRateView

import scala.concurrent.Future

class TellUsAboutSingleSPRRateControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new TellUsAboutSingleSPRRateFormProvider()
  val form         = formProvider()

  lazy val tellUsAboutSingleSPRRateRoute = routes.TellUsAboutSingleSPRRateController.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(
    returnId,
    groupId,
    userAnswersId,
    Json.obj(
      TellUsAboutSingleSPRRatePage.toString -> Json.obj(
        "field1" -> "value 1",
        "field2" -> "value 2"
      )
    )
  )

  "TellUsAboutSingleSPRRate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, tellUsAboutSingleSPRRateRoute)

        val view = application.injector.instanceOf[TellUsAboutSingleSPRRateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, tellUsAboutSingleSPRRateRoute)

        val view = application.injector.instanceOf[TellUsAboutSingleSPRRateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(TellUsAboutSingleSPRRate("value 1", "value 2")), NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(new FakeReturnsNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, tellUsAboutSingleSPRRateRoute)
            .withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, tellUsAboutSingleSPRRateRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[TellUsAboutSingleSPRRateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, tellUsAboutSingleSPRRateRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, tellUsAboutSingleSPRRateRoute)
            .withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
