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

package uk.gov.hmrc.selfassessmentrefundfrontend.controllers.trackRefundJourney

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import play.mvc.Http.Status
import support.{ItSpec, PageContentTesting, TestMessagesApiProvider}
import uk.gov.hmrc.selfassessmentrefundfrontend.connectors.RepaymentsConnector.Response
import uk.gov.hmrc.selfassessmentrefundfrontend.model.customer.Nino
import uk.gov.hmrc.selfassessmentrefundfrontend.model.repayment.RequestNumber
import uk.gov.hmrc.selfassessmentrefundfrontend.testdata.TdAll.{nino, no1, request, welshRequest}
import uk.gov.hmrc.selfassessmentrefundfrontend.testdata.TdSupport._

import scala.concurrent.Future

class RefundApprovedControllerSpec extends ItSpec with PageContentTesting {

  val refundApprovedController: RefundApprovedController =
    fakeApplication().injector.instanceOf[RefundApprovedController]

  class TestSetup(isAgent: Boolean = false) {
    if (isAgent) givenTheUserIsAuthorisedAsAgent() else givenTheUserIsAuthorised()

    stubBackendPersonalJourney(Some(nino))
    stubBarsVerifyStatus()
  }

  "onPageLoad" should {
    "render 'Refund Approved' page with correct content - Individual or Organisation - BACS" in new TestSetup() {
      givenTheApprovedRefundExists(no1, nino, 12000, Some("BACS"))

      val result: Future[Result] = refundApprovedController.showApprovedPage(no1)(request)
      val doc: Document          = Jsoup.parse(contentAsString(result))

      doc.checkHasHyperlink(
        "More details about this refund",
        "http://localhost:9081/report-quarterly/income-and-expenses/view/refund-to-taxpayer/1"
      )

      result.checkPageIsDisplayed(
        expectedHeading = "Your refund of £12,000 has been approved",
        expectedServiceLink = "http://localhost:9171/track-a-self-assessment-refund/refund-request-tracker",
        contentChecks = checkApprovedPageContent(welsh = false, "BACS"),
        expectedStatus = Status.OK,
        journey = "track"
      )
    }

    "render 'Refund Approved' page with correct content - Individual or Organisation - in Welsh - BACS" in new TestSetup() {
      givenTheApprovedRefundExists(no1, nino, 12000, Some("BACS"))

      val result: Future[Result] = refundApprovedController.showApprovedPage(no1)(welshRequest)
      val doc: Document          = Jsoup.parse(contentAsString(result))

      doc.checkHasHyperlink(
        "Rhagor o fanylion am yr ad-daliad hwn",
        "http://localhost:9081/report-quarterly/income-and-expenses/view/refund-to-taxpayer/1"
      )

      result.checkPageIsDisplayed(
        expectedHeading = "Mae’ch ad-daliad o £12,000 wedi’i gymeradwyo",
        expectedServiceLink = "http://localhost:9171/track-a-self-assessment-refund/refund-request-tracker",
        contentChecks = checkApprovedPageContent(welsh = true, "BACS"),
        expectedStatus = Status.OK,
        journey = "track",
        welsh = true
      )
    }

    "render 'Refund Approved' page with correct content - Individual or Organisation - CARD" in new TestSetup() {
      givenTheApprovedRefundExists(no1, nino, 12000, Some("CARD"))

      val result: Future[Result] = refundApprovedController.showApprovedPage(no1)(request)
      val doc: Document          = Jsoup.parse(contentAsString(result))

      doc.checkHasHyperlink(
        "More details about this refund",
        "http://localhost:9081/report-quarterly/income-and-expenses/view/refund-to-taxpayer/1"
      )

      result.checkPageIsDisplayed(
        expectedHeading = "Your refund of £12,000 has been approved",
        expectedServiceLink = "http://localhost:9171/track-a-self-assessment-refund/refund-request-tracker",
        contentChecks = checkApprovedPageContent(welsh = false, "CARD"),
        expectedStatus = Status.OK,
        journey = "track"
      )
    }

    "render 'Refund Approved' page with correct content - Individual or Organisation - in Welsh - CARD" in new TestSetup() {
      givenTheApprovedRefundExists(no1, nino, 12000, Some("CARD"))

      val result: Future[Result] = refundApprovedController.showApprovedPage(no1)(welshRequest)
      val doc: Document          = Jsoup.parse(contentAsString(result))

      doc.checkHasHyperlink(
        "Rhagor o fanylion am yr ad-daliad hwn",
        "http://localhost:9081/report-quarterly/income-and-expenses/view/refund-to-taxpayer/1"
      )

      result.checkPageIsDisplayed(
        expectedHeading = "Mae’ch ad-daliad o £12,000 wedi’i gymeradwyo",
        expectedServiceLink = "http://localhost:9171/track-a-self-assessment-refund/refund-request-tracker",
        contentChecks = checkApprovedPageContent(welsh = true, "CARD"),
        expectedStatus = Status.OK,
        journey = "track",
        welsh = true
      )
    }

    "render 'Refund Approved' page with correct content - Individual or Organisation - PO" in new TestSetup() {
      givenTheApprovedRefundExists(no1, nino, 12000, Some("PO"))

      val result: Future[Result] = refundApprovedController.showApprovedPage(no1)(request)
      val doc: Document          = Jsoup.parse(contentAsString(result))

      doc.checkHasHyperlink(
        "More details about this refund",
        "http://localhost:9081/report-quarterly/income-and-expenses/view/refund-to-taxpayer/1"
      )

      result.checkPageIsDisplayed(
        expectedHeading = "Your refund of £12,000 has been approved",
        expectedServiceLink = "http://localhost:9171/track-a-self-assessment-refund/refund-request-tracker",
        contentChecks = checkApprovedPageContent(welsh = false, "PO"),
        expectedStatus = Status.OK,
        journey = "track"
      )
    }

    "render 'Refund Approved' page with correct content - Individual or Organisation - in Welsh - PO" in new TestSetup() {
      givenTheApprovedRefundExists(no1, nino, 12000, Some("PO"))

      val result: Future[Result] = refundApprovedController.showApprovedPage(no1)(welshRequest)
      val doc: Document          = Jsoup.parse(contentAsString(result))

      doc.checkHasHyperlink(
        "Rhagor o fanylion am yr ad-daliad hwn",
        "http://localhost:9081/report-quarterly/income-and-expenses/view/refund-to-taxpayer/1"
      )

      result.checkPageIsDisplayed(
        expectedHeading = "Mae’ch ad-daliad o £12,000 wedi’i gymeradwyo",
        expectedServiceLink = "http://localhost:9171/track-a-self-assessment-refund/refund-request-tracker",
        contentChecks = checkApprovedPageContent(welsh = true, "PO"),
        expectedStatus = Status.OK,
        journey = "track",
        welsh = true
      )
    }

    "render 'Refund Approved' page with correct content - Individual or Organisation - Unknown payment method" in new TestSetup() {
      givenTheApprovedRefundExists(no1, nino, 12000, Some("Unknown"))

      val result: Future[Result] = refundApprovedController.showApprovedPage(no1)(request)
      val doc: Document          = Jsoup.parse(contentAsString(result))

      doc.checkHasHyperlink(
        "More details about this refund",
        "http://localhost:9081/report-quarterly/income-and-expenses/view/refund-to-taxpayer/1"
      )

      result.checkPageIsDisplayed(
        expectedHeading = "Your refund of £12,000 has been approved",
        expectedServiceLink = "http://localhost:9171/track-a-self-assessment-refund/refund-request-tracker",
        contentChecks = checkApprovedPageContent(welsh = false, "Unknown"),
        expectedStatus = Status.OK,
        journey = "track"
      )
    }

    "render 'Refund Approved' page with correct content - Individual or Organisation - in Welsh - Unknown payment method" in new TestSetup() {
      givenTheApprovedRefundExists(no1, nino, 12000, Some("Unknown"))

      val result: Future[Result] = refundApprovedController.showApprovedPage(no1)(welshRequest)
      val doc: Document          = Jsoup.parse(contentAsString(result))

      doc.checkHasHyperlink(
        "Rhagor o fanylion am yr ad-daliad hwn",
        "http://localhost:9081/report-quarterly/income-and-expenses/view/refund-to-taxpayer/1"
      )

      result.checkPageIsDisplayed(
        expectedHeading = "Mae’ch ad-daliad o £12,000 wedi’i gymeradwyo",
        expectedServiceLink = "http://localhost:9171/track-a-self-assessment-refund/refund-request-tracker",
        contentChecks = checkApprovedPageContent(welsh = true, "Unknown"),
        expectedStatus = Status.OK,
        journey = "track",
        welsh = true
      )
    }

    "render 'Refund Approved' page with correct content - Agent" in new TestSetup(isAgent = true) {
      givenTheApprovedRefundExists(no1, nino, 12000)

      // The standard fakeApplication has an overridden AuthConnector that returns an Individual affinityGroup
      val refundControllerAuth: RefundApprovedController = new GuiceApplicationBuilder()
        .configure(
          "metrics.jvm"                                               -> false,
          "metrics.enabled"                                           -> false,
          "microservice.services.self-assessment-refund-backend.port" -> wireMockServer.port(),
          "microservice.services.auth.port"                           -> wireMockServer.port(),
          "microservice.services.bank-account-reputation.port"        -> wireMockServer.port()
        )
        .overrides(bind[MessagesApi].toProvider[TestMessagesApiProvider])
        .build()
        .injector
        .instanceOf[RefundApprovedController]

      val result: Future[Result] = refundControllerAuth.showApprovedPage(no1)(request)
      val doc: Document          = Jsoup.parse(contentAsString(result))

      doc.checkHasHyperlink(
        "More details about this refund",
        "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/refund-to-taxpayer/1"
      )

      result.checkPageIsDisplayed(
        expectedHeading = "Your refund of £12,000 has been approved",
        expectedServiceLink = "http://localhost:9171/track-a-self-assessment-refund/refund-request-tracker",
        contentChecks = checkApprovedPageContent(welsh = false),
        expectedStatus = Status.OK,
        journey = "track"
      )
    }

    "render 'Refund Approved' page with correct content - Agent - in Welsh" in new TestSetup(isAgent = true) {
      givenTheApprovedRefundExists(no1, nino, 12000)

      // The standard fakeApplication has an overridden AuthConnector that returns an Individual affinityGroup
      val refundControllerAuth: RefundApprovedController = new GuiceApplicationBuilder()
        .configure(
          "metrics.jvm"                                               -> false,
          "metrics.enabled"                                           -> false,
          "microservice.services.self-assessment-refund-backend.port" -> wireMockServer.port(),
          "microservice.services.auth.port"                           -> wireMockServer.port(),
          "microservice.services.bank-account-reputation.port"        -> wireMockServer.port()
        )
        .overrides(bind[MessagesApi].toProvider[TestMessagesApiProvider])
        .build()
        .injector
        .instanceOf[RefundApprovedController]

      val result: Future[Result] = refundControllerAuth.showApprovedPage(no1)(welshRequest)
      val doc: Document          = Jsoup.parse(contentAsString(result))

      doc.checkHasHyperlink(
        "Rhagor o fanylion am yr ad-daliad hwn",
        "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/refund-to-taxpayer/1"
      )

      result.checkPageIsDisplayed(
        expectedHeading = "Mae’ch ad-daliad o £12,000 wedi’i gymeradwyo",
        expectedServiceLink = "http://localhost:9171/track-a-self-assessment-refund/refund-request-tracker",
        contentChecks = checkApprovedPageContent(welsh = true),
        expectedStatus = Status.OK,
        journey = "track",
        welsh = true
      )
    }

    "redirect to error page if GET repayments returned error" in new TestSetup() {
      val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest("GET", "/track-a-self-assessment-refund/refund-approved")
          .withAuthToken()
          .withRequestId()
          .withSessionId()

      stubGetRepaymentError()

      val result: Future[Result] = refundApprovedController.showApprovedPage(no1)(fakeRequest)

      result.checkPageIsDisplayed(
        expectedHeading = "Sorry, there is a problem with the service",
        expectedServiceLink = "http://localhost:9171/track-a-self-assessment-refund/refund-request-tracker",
        journey = "track",
        expectedStatus = Status.INTERNAL_SERVER_ERROR,
        withBackButton = false
      )
    }

    "render the error page if the Approved refund exists but doesn't include a completed date" in new TestSetup() {
      val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest("GET", "/track-a-self-assessment-refund/refund-approved")
          .withAuthToken()
          .withRequestId()
          .withSessionId()

      givenTheApprovedRefundExists(no1, nino, 12000)

      stubFor(
        get(urlEqualTo(s"/self-assessment-refund-backend/repayments/${nino.value}/${no1.value}"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(
                Json.prettyPrint(
                  Json.toJson(Response(no1, nino, 12000, "Approved", "2021-08-14", None, None, Some("PO")))
                )
              )
          )
      )

      val result: Future[Result] = refundApprovedController.showApprovedPage(no1)(fakeRequest)

      result.checkPageIsDisplayed(
        expectedHeading = "Sorry, there is a problem with the service",
        expectedServiceLink = "http://localhost:9171/track-a-self-assessment-refund/refund-request-tracker",
        journey = "track",
        expectedStatus = Status.INTERNAL_SERVER_ERROR,
        withBackButton = false
      )
    }
  }

  def givenTheApprovedRefundExists(
    key:        RequestNumber,
    nino:       Nino,
    cashAmount: Long,
    method:     Option[String] = None
  ): StubMapping =
    stubFor(
      get(urlEqualTo(s"/self-assessment-refund-backend/repayments/${nino.value}/${key.value}"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(
              Json.prettyPrint(
                Json.toJson(Response(key, nino, cashAmount, "Approved", "2021-08-14", Some("2021-08-31"), None, method))
              )
            )
        )
    )

  private def checkApprovedPageContent(welsh: Boolean, method: String = "")(doc: Document): Unit = {
    if (welsh) {
      doc.checkHasParagraphs(approvedParagraphsWelsh(method))
      doc.checkHasHyperlink(
        "A yw’r dudalen hon yn gweithio’n iawn? (yn agor tab newydd)",
        "/contact/report-technical-problem?service=self-assessment-repayment"
      )
    } else {
      doc.checkHasParagraphs(approvedParagraphs(method))
      doc.checkHasHyperlink(
        "Is this page not working properly? (opens in new tab)",
        "/contact/report-technical-problem?service=self-assessment-repayment"
      )
    }
    doc.checkHasBackLinkWithUrl("#")
  }

  private def approvedParagraphs(method: String): List[String] = List(
    "Refund reference: 1",
    if (method == "BACS") "We have approved your refund of £12,000 and sent it to the bank account you shared with us."
    else if (method == "CARD")
      "We have approved your refund of £12,000 and sent it to the card you used to pay your last Simple Assessment bill."
    else if (method == "PO") "We have approved your refund of £12,000 and sent it to to you by cheque."
    else "We have approved your refund of £12,000 and sent it for payment.",
    "You should receive your refund by 31 August 2021."
  )

  private def approvedParagraphsWelsh(method: String): List[String] = List(
    "Cyfeirnod yr ad-daliad: 1",
    if (method == "BACS")
      "Rydym wedi cymeradwyo’ch ad-daliad o £12,000, ac wedi ei anfon i’r cyfrif banc y gwnaethoch ei rannu gyda ni."
    else if (method == "CARD")
      "Rydym wedi cymeradwyo’ch ad-daliad o £12,000, ac wedi ei anfon i’r cerdyn y gwnaethoch ei ddefnyddio er mwyn talu’ch bil Hunanasesiad diwethaf."
    else if (method == "PO") "Rydym wedi cymeradwyo’ch ad-daliad o £12,000, ac wedi ei anfon atoch drwy siec."
    else "Rydym wedi cymeradwyo’ch ad-daliad o £12,000 ac wrthi’n prosesu’r taliad.",
    "Dylech gael eich ad-daliad erbyn 31 Awst 2021."
  )
}
