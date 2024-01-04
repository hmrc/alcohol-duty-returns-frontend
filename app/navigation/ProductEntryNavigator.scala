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

package navigation

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call
import controllers.routes
import pages._
import models._

@Singleton
class ProductEntryNavigator @Inject() () extends BaseNavigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case ProductNamePage                        => _ => routes.AlcoholByVolumeQuestionController.onPageLoad(NormalMode)
    case AlcoholByVolumeQuestionPage            => _ => routes.DraughtReliefQuestionController.onPageLoad(NormalMode)
    case DraughtReliefQuestionPage              => _ => routes.SmallProducerReliefQuestionController.onPageLoad(NormalMode)
    case DeclareSmallProducerReliefDutyRatePage => _ => routes.ProductVolumeController.onPageLoad(NormalMode)
    case _                                      =>
      _ => routes.IndexController.onPageLoad

  }

  override val checkRouteMap: Page => UserAnswers => Call = { case _ =>
    _ => routes.CheckYourAnswersController.onPageLoad
  }
}
