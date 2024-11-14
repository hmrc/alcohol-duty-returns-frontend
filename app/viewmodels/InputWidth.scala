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

package viewmodels

import config.Constants.Css

sealed trait InputWidth

object InputWidth {

  case object Fixed2 extends WithCssClass(Css.inputWidth2CssClass) with InputWidth
  case object Fixed3 extends WithCssClass(Css.inputWidth3CssClass) with InputWidth
  case object Fixed4 extends WithCssClass(Css.inputWidth4CssClass) with InputWidth
  case object Fixed5 extends WithCssClass(Css.inputWidth5CssClass) with InputWidth
  case object Fixed10 extends WithCssClass(Css.inputWidth10CssClass) with InputWidth
  case object Fixed20 extends WithCssClass(Css.inputWidth20CssClass) with InputWidth
  case object Fixed30 extends WithCssClass(Css.inputWidth30CssClass) with InputWidth

  case object Full extends WithCssClass(Css.fullWidthCssClass) with InputWidth
  case object ThreeQuarters extends WithCssClass(Css.threeQuartersCssClass) with InputWidth
  case object TwoThirds extends WithCssClass(Css.twoThirdsCssClass) with InputWidth
  case object OneHalf extends WithCssClass(Css.oneHalfCssClass) with InputWidth
  case object OneThird extends WithCssClass(Css.oneThirdCssClass) with InputWidth
  case object OneQuarter extends WithCssClass(Css.oneQuarterCssClass) with InputWidth
}
