/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import scala.annotation.tailrec

object ListHelpers {
  implicit class ListWithHelpers[+A](l: List[A]) {
    @tailrec
    private def nextItem[B >: A](items: List[B], current: B): Option[B] =
      items match {
        case h :: t if h == current => t.headOption
        case _ :: t                 => nextItem(t, current)
        case _                      => None
      }

    def nextItem[B >: A](current: B): Option[B] = nextItem(l, current)
  }
}
