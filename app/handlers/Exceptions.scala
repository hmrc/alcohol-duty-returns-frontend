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

package handlers

/**
  * Use these for when we cannot progress because of a fault, but may be retried.
  * Users who are now in the wrong place (e.g. navigation failed due to inconsistent data with the
  * page), use redirect to journey recovery instead which will change the url and can optionally
  * provide a continuation url to navigate to.
  *
  * If potentially user error is at fault (data not found or incorrect) but navigating to
  * journey recovery is the wrong solution, use ADRBadRequest or ADRNotFound as these are
  * logged at warn and passed to the client error handler as if Play detected them
  * before they hit the controller. Other errors are passed to the server error handler which
  * logs at error level
  */
abstract class ADRException(message: String) extends RuntimeException(message)

/**
  * Got to the controller, but the request cannot be processed
  */
case class ADRBadRequest(message: String) extends ADRException(message)

/**
  * Got to the controller, but a requested resource (e.g. return) was not available
  */
case class ADRNotFound(message: String) extends ADRException(message)

/**
  * A service (ours or further downstream returned an error or was unavailable)
  */
case class ADRServerException(message: String) extends ADRException(message)
