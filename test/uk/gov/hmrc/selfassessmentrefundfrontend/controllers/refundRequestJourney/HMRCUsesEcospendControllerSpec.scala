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
import uk.gov.hmrc.selfassessmentrefundfrontend.pages.HMRCUsesEcospendPageTesting
import uk.gov.hmrc.selfassessmentrefundfrontend.testdata.TdSupport.FakeRequestOps

import scala.concurrent.Future

class HMRCUsesEcospendControllerSpec extends ItSpec, HMRCUsesEcospendPageTesting {

  val controller = app.injector.instanceOf[HMRCUsesEcospendController]

  "GET /hmrc-uses-ecospend" when {

    val fakeRequest = FakeRequest().withSessionId().withAuthToken()

    "called" should {
      "display the 'hmrc uses ecospend' page with correct content" in {
        stubBackendJourneyId()

        val result: Future[Result] = controller.onPageLoad(fakeRequest)

        result.checkPageIsDisplayed(
          expectedHeading = "HMRC uses Ecospend to help keep your refund secure",
          expectedServiceLink = "http://localhost:9092/manage-self-assessment/financials/money-in-your-account",
          contentChecks = checkPageContent,
          expectedStatus = Status.OK,
          journey = "request"
        )
      }
    }
  }

}

class HMRCUsesEcospendControllerAISDisabledSpec extends ItSpec {

  override lazy val configOverrides: Map[String, Any] = Map(
    "features.ais" -> false
  )

  val controller = app.injector.instanceOf[HMRCUsesEcospendController]

  "GET /hmrc-uses-ecospend" when {

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
