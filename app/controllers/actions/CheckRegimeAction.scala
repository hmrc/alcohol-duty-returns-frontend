/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.actions

import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.requests.DataRequest
import models.AlcoholRegime
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait CheckRegimeAction extends ActionRefiner[DataRequest, DataRequest] with Logging {
  val regime: AlcoholRegime

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] =
    if (request.userAnswers.regimes.hasRegime(regime)) {
      Future.successful(Right(request))
    } else {
      Future.successful(Left(Redirect(controllers.routes.UnauthorisedController.onPageLoad)))
    }
}

class CheckBeerRegimeAction @Inject() (implicit val executionContext: ExecutionContext) extends CheckRegimeAction {
  val regime: AlcoholRegime = Beer
}

class CheckCiderRegimeAction @Inject() (implicit val executionContext: ExecutionContext) extends CheckRegimeAction {
  val regime: AlcoholRegime = Cider
}

class CheckWineRegimeAction @Inject() (implicit val executionContext: ExecutionContext) extends CheckRegimeAction {
  val regime: AlcoholRegime = Wine
}

class CheckSpiritsRegimeAction @Inject() (implicit val executionContext: ExecutionContext) extends CheckRegimeAction {
  val regime: AlcoholRegime = Spirits
}

class CheckOtherFermentedRegimeAction @Inject() (implicit val executionContext: ExecutionContext)
    extends CheckRegimeAction {
  val regime: AlcoholRegime = OtherFermentedProduct
}
