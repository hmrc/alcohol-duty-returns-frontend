package controllers.adjustment

import base.SpecBase
import forms.adjustment.AdjustmentRepackagedTaxTypeWithSPRFormProvider
import models.NormalMode
import models.adjustment.AdjustmentRepackagedTaxTypeWithSPR
import navigation.{FakeAdjustmentNavigator, AdjustmentNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.adjustment.AdjustmentRepackagedTaxTypeWithSPRPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.CacheConnector
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.AdjustmentRepackagedTaxTypeWithSPRView

import scala.concurrent.Future

class AdjustmentRepackagedTaxTypeWithSPRControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new AdjustmentRepackagedTaxTypeWithSPRFormProvider()
  val form = formProvider()

  lazy val adjustmentRepackagedTaxTypeWithSPRRoute = routes.AdjustmentRepackagedTaxTypeWithSPRController.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(
    returnId,
    groupId,
    userAnswersId,
    Json.obj(
      AdjustmentRepackagedTaxTypeWithSPRPage.toString -> Json.obj(
        "tax-type-code" -> "value 1",
        "new-spr-duty-rate" -> "value 2"
      )
    )
  )

  "AdjustmentRepackagedTaxTypeWithSPR Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentRepackagedTaxTypeWithSPRRoute)

        val view = application.injector.instanceOf[AdjustmentRepackagedTaxTypeWithSPRView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentRepackagedTaxTypeWithSPRRoute)

        val view = application.injector.instanceOf[AdjustmentRepackagedTaxTypeWithSPRView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(AdjustmentRepackagedTaxTypeWithSPR("value 1", "value 2")), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentRepackagedTaxTypeWithSPRRoute)
            .withFormUrlEncodedBody(("tax-type-code", "value 1"), ("new-spr-duty-rate", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentRepackagedTaxTypeWithSPRRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AdjustmentRepackagedTaxTypeWithSPRView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentRepackagedTaxTypeWithSPRRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentRepackagedTaxTypeWithSPRRoute)
            .withFormUrlEncodedBody(("tax-type-code", "value 1"), ("new-spr-duty-rate", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
