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

package repositories

import common.TestData
import config.FrontendAppConfig
import generators.ModelGenerators
import models.{ReturnId, UserAnswers}
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[UserAnswers]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar
    with TestData
    with ModelGenerators {

  private val instant          = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1

  protected override val repository = new SessionRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )

  ".set" - {

    "must set the last updated time on the supplied user answers to `now`, and save them" in {

      val expectedResult = emptyUserAnswers.copy(lastUpdated = instant)

      val setResult     = repository.set(emptyUserAnswers).futureValue
      val updatedRecord = find(Filters.equal("_id", emptyUserAnswers.returnId)).futureValue.headOption.value

      setResult mustEqual true
      updatedRecord mustEqual expectedResult
    }
  }

  ".get" - {

    "when there is a record for this id" - {

      "must update the lastUpdated time and get the record" in {

        insert(emptyUserAnswers).futureValue

        val result         = repository.get(emptyUserAnswers.returnId).futureValue
        val expectedResult = emptyUserAnswers.copy(lastUpdated = instant)

        result.value mustEqual expectedResult
      }
    }

    "when there is no record for this id" - {

      "must return None" in {

        repository.get(ReturnId("id that does not exist", "invalid period")).futureValue must not be defined
      }
    }
  }

  ".clear" - {

    "must remove a record" in {

      insert(emptyUserAnswers).futureValue

      val result = repository.clear(emptyUserAnswers.returnId).futureValue

      result mustEqual true
      repository.get(emptyUserAnswers.returnId).futureValue must not be defined
    }

    "must return true when there is no record to remove" in {
      val result = repository.clear(ReturnId("id that does not exist", "invalid period")).futureValue

      result mustEqual true
    }
  }

  ".keepAlive" - {

    "when there is a record for this id" - {

      "must update its lastUpdated to `now` and return true" in {

        insert(emptyUserAnswers).futureValue

        val result = repository.keepAlive(emptyUserAnswers.returnId).futureValue

        val expectedUpdatedAnswers = emptyUserAnswers.copy(lastUpdated = instant)

        result mustEqual true
        val updatedAnswers = find(Filters.equal("_id", emptyUserAnswers.returnId)).futureValue.headOption.value
        updatedAnswers mustEqual expectedUpdatedAnswers
      }
    }

    "when there is no record for this id" - {

      "must return true" in {

        repository.keepAlive(ReturnId("id that does not exist", "invalid period")).futureValue mustEqual true
      }
    }
  }
}
