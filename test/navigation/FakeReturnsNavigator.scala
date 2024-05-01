package navigation

import play.api.mvc.Call
import pages._
import models.{Mode, UserAnswers}

class FakeReturnsNavigator(desiredRoute: Call) extends ReturnsNavigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    desiredRoute
}
