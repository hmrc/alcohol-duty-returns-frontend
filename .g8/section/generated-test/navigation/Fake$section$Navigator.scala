package navigation

import play.api.mvc.Call
import pages._
import models.{Mode, UserAnswers}

class Fake$section$Navigator(desiredRoute: Call) extends $section$Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    desiredRoute
}
