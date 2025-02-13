/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.i18n.{I18nSupport, Lang, Messages}
import play.api.mvc.RequestHeader

import java.io.PrintWriter

trait CoverageI18nSupport extends I18nSupport {

  /**
    * Converts from a request directly into a Messages.
    *
    * @param request the incoming request
    * @return The preferred [[Messages]] according to the given [[play.api.mvc.RequestHeader]]
    */
  implicit override def request2Messages(implicit request: RequestHeader): Messages = LoggedMessagesImpl(
    super.request2Messages(request)
  )
}

case class LoggedMessagesImpl(m: Messages) extends Messages {
  def lang: Lang = m.lang

  /**
    * Translates a message.
    *
    * Uses `java.text.MessageFormat` internally to format the message.
    *
    * @param key  the message key
    * @param args the message arguments
    * @return the formatted message or a default rendering if the key wasn’t defined
    */
  override def apply(key: String, args: Any*): String = {
    LoggedMessagesImpl.outputter.write(key + "\n")
    LoggedMessagesImpl.outputter.flush()
    m.apply(key, args: _*)
  }

  /**
    * Translates the first defined message.
    *
    * Uses `java.text.MessageFormat` internally to format the message.
    *
    * @param keys the message key
    * @param args the message arguments
    * @return the formatted message or a default rendering if the key wasn’t defined
    */
  override def apply(keys: Seq[String], args: Any*): String =
    m.apply(keys, args: _*)

  /**
    * Translates a message.
    *
    * Uses `java.text.MessageFormat` internally to format the message.
    *
    * @param key  the message key
    * @param args the message arguments
    * @return the formatted message, if this key was defined
    */
  override def translate(key: String, args: Seq[Any]): Option[String] =
    m.translate(key, args)

  /**
    * Check if a message key is defined.
    *
    * @param key the message key
    * @return a boolean
    */
  override def isDefinedAt(key: String): Boolean =
    m.isDefinedAt(key)

  /**
    * @return the Java version for this Messages.
    */
  override def asJava: play.i18n.Messages =
    m.asJava
}

object LoggedMessagesImpl {
  val outputter = new PrintWriter("messages.keys.output")
}
