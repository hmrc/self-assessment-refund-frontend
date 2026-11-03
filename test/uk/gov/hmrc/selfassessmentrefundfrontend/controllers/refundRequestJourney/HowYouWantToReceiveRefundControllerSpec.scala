/*
 * Copyright 2026 HM Revenue & Customs
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

/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentrefundfrontend.controllers.refundRequestJourney

import play.api.http.Status
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import support.ItSpec
import uk.gov.hmrc.selfassessmentrefundfrontend.testdata.TdSupport.FakeRequestOps

import scala.concurrent.Future

class HowYouWantToReceiveRefundControllerSpec extends ItSpec {

  val controller = app.injector.instanceOf[HowYouWantToReceiveRefundController]

  "GET /how-receive-refund " when {

    val fakeRequest = FakeRequest().withSessionId().withAuthToken()

    "called" should {

      "return Ok" in {
        stubBackendJourneyId()

        val result: Future[Result] = controller.onPageLoad(fakeRequest)

        status(result) shouldBe Status.OK
        contentAsString(result) should include("How would you like to receive your refund?")
      }
    }
  }

}

class HowYouWantToReceiveRefundControllerISDisabledSpec extends ItSpec {

  override lazy val configOverrides: Map[String, Any] = Map(
    "features.ais" -> false
  )

  val controller = app.injector.instanceOf[HowYouWantToReceiveRefundController]

  "GET /how-receive-refund " when {

    val fakeRequest = FakeRequest().withSessionId().withAuthToken()

    "called" should {

      "return NotImplemented" in {
        stubBackendJourneyId()

        val result: Future[Result] = controller.onPageLoad(fakeRequest)

        status(result) shouldBe Status.NOT_IMPLEMENTED
      }
    }
  }

}
