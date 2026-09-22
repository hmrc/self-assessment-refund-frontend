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

package uk.gov.hmrc.selfassessmentrefundfrontend.controllers.action

import org.apache.pekko.stream.Materializer
import play.api.mvc.Results.Ok
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.ItSpec
import support.stubbing.AuthStub
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, ConfidenceLevel}
import uk.gov.hmrc.http.SessionId
import uk.gov.hmrc.selfassessmentrefundfrontend.controllers.action.request.PreAuthRequest
import uk.gov.hmrc.selfassessmentrefundfrontend.testdata.TdSupport.FakeRequestOps
import uk.gov.hmrc.selfassessmentrefundfrontent.util.CanEqualGivens.sessionIdCanEqual

import scala.concurrent.Future

class PreAuthSessionRefinerSpec extends ItSpec {

  implicit val materializer: Materializer = app.injector.instanceOf[Materializer]

  val defaultActionBuilder: DefaultActionBuilder   = app.injector.instanceOf[DefaultActionBuilder]
  val preAuthSessionRefiner: PreAuthSessionRefiner = app.injector.instanceOf[PreAuthSessionRefiner]

  val preAuthJourneyAction: ActionBuilder[PreAuthRequest, AnyContent] =
    defaultActionBuilder
      .andThen[PreAuthRequest](preAuthSessionRefiner)

  override def fakeAuthConnector: Option[AuthConnector] = None

  def doTest(request: Request[_], expectedSessionId: SessionId): Future[Result] = preAuthJourneyAction { request =>
    request.sessionId shouldBe expectedSessionId
    Ok
  }(request).run()

  def doTestNoSessionId(request: Request[_]): Future[Result] = preAuthJourneyAction { _ =>
    Ok
  }(request).run()

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/self-assessment-refund-frontend/auth/authorise")

  "PreAuthSessionRefiner" should {
    "allow logged in user to proceed" when {

      Seq(
        AffinityGroup.Individual,
        AffinityGroup.Organisation,
        AffinityGroup.Agent
      ).foreach { affinityGroup =>
        s"$affinityGroup has a valid session" in {
          stubBackendBusinessJourney()
          AuthStub.authorise(affinityGroup, ConfidenceLevel.L50)

          val result = doTest(fakeRequest.withSessionId().withAuthToken(), SessionId("session-deadbeef"))
          status(result) shouldBe OK
        }
      }
    }

    "redirect to log in page" when {
      Seq(
        AffinityGroup.Individual,
        AffinityGroup.Organisation,
        AffinityGroup.Agent
      ).foreach { affinityGroup =>
        s"$affinityGroup has no sessionId is found" in {
          stubBackendJourneyNoSessionId()
          AuthStub.authorise(affinityGroup, ConfidenceLevel.L50)

          val result = doTestNoSessionId(fakeRequest.withAuthToken())

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(
            "http://localhost:9949/auth-login-stub/gg-sign-in?continue=http://localhost:9081/manage-self-assessment/financials/money-in-your-account"
          )
        }
      }

      "user is not authenticated" in {
        stubBackendBusinessJourney()
        AuthStub.notAuthorized()

        val result = doTest(fakeRequest.withSessionId().withAuthToken(), SessionId("session-deadbeef"))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(
          "http://localhost:9949/auth-login-stub/gg-sign-in?continue=http://localhost:9081/manage-self-assessment/financials/money-in-your-account"
        )
      }

      "user is not authenticated and has no session" in {
        stubBackendJourneyNoSessionId()
        AuthStub.notAuthorized()

        val result = doTestNoSessionId(fakeRequest.withAuthToken())

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(
          "http://localhost:9949/auth-login-stub/gg-sign-in?continue=http://localhost:9081/manage-self-assessment/financials/money-in-your-account"
        )
      }
    }
  }
}
