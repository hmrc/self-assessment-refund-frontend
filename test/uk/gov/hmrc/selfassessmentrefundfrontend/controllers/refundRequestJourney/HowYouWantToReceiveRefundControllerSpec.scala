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
import uk.gov.hmrc.selfassessmentrefundfrontend.model.{BankDetailsMethod, PaymentMethod}
import uk.gov.hmrc.selfassessmentrefundfrontend.pages.HowYouWantToReceiveRefundPageTesting
import uk.gov.hmrc.selfassessmentrefundfrontend.testdata.TdAll
import uk.gov.hmrc.selfassessmentrefundfrontend.testdata.TdSupport.FakeRequestOps

import scala.concurrent.Future

class HowYouWantToReceiveRefundControllerSpec extends ItSpec with HowYouWantToReceiveRefundPageTesting {

  val controller = app.injector.instanceOf[HowYouWantToReceiveRefundController]

  "GET /how-you-will-receive-the-refund" must {

    "display the page" in {
      stubBackendJourneyId()

      val response: Future[Result] = controller.onPageLoad(TdAll.request)
      response.checkPageIsDisplayed(
        expectedHeading = "How would you like to receive your refund?",
        expectedServiceLink = "http://localhost:9092/manage-self-assessment/financials/money-in-your-account",
        contentChecks = checkPageContent(preselectedOption = None),
        expectedStatus = Status.OK,
        journey = "request"
      )
    }

    BankDetailsMethod.values.foreach { bankDetailsMethod =>
      s"preselect the ${bankDetailsMethod.entryName} option if the user has previously selected it" in {
        stubBackendJourneyId(
          journey = TdAll.refundJourney.copy(bankDetailsMethod = Some(bankDetailsMethod))
        )

        val response: Future[Result] = controller.onPageLoad(TdAll.request)
        response.checkPageIsDisplayed(
          expectedHeading = "How would you like to receive your refund?",
          expectedServiceLink = "http://localhost:9092/manage-self-assessment/financials/money-in-your-account",
          contentChecks = checkPageContent(preselectedOption = Some(bankDetailsMethod)),
          expectedStatus = Status.OK,
          journey = "request"
        )
      }
    }
  }

  "POST /how-you-will-receive-the-refund" must {

    "show a form error" when {

      "no option is selected" in {
        stubBackendJourneyId()

        val response: Future[Result] = controller.onSubmit(
          FakeRequest().withSessionId().withAuthToken().withFormUrlEncodedBody()
        )
        response.checkPageIsDisplayed(
          expectedHeading = "How would you like to receive your refund?",
          expectedServiceLink = "http://localhost:9092/manage-self-assessment/financials/money-in-your-account",
          contentChecks = checkPageWithFormError(
            errorMessage = "Select how you want to receive your refund",
            errorLink = "#bankDetailsMethod"
          )(_),
          expectedStatus = Status.BAD_REQUEST,
          journey = "request"
        )
      }

      "the option selected is not understood" in {
        stubBackendJourneyId()

        val response: Future[Result] = controller.onSubmit(
          FakeRequest().withSessionId().withAuthToken().withFormUrlEncodedBody("bankDetailsMethod" -> "invalid-option")
        )
        response.checkPageIsDisplayed(
          expectedHeading = "How would you like to receive your refund?",
          expectedServiceLink = "http://localhost:9092/manage-self-assessment/financials/money-in-your-account",
          contentChecks = checkPageWithFormError(
            errorMessage = "Select how you want to receive your refund",
            errorLink = "#bankDetailsMethod"
          )(_),
          expectedStatus = Status.BAD_REQUEST,
          journey = "request"
        )
      }

    }

    "redirect to the 'how you will get your refund (AIS)' page" when {

      "the user selects the 'Open Banking' option and the payment method is 'Card'" in {
        stubBackendJourneyId()
        stubPOSTJourney()
        stubBackendLastPaymentMethod(PaymentMethod.Card)

        val response: Future[Result] = controller.onSubmit(
          TdAll.request
            .withMethod("POST")
            .withFormUrlEncodedBody("bankDetailsMethod" -> "OpenBanking")
        )
        redirectLocation(response) shouldBe Some(routes.HowYouWillGetYourRefundController.onPageLoadAis.url)
        verifyUpdateJourneyCalled(
          TdAll.refundJourney.copy(
            bankDetailsMethod = Some(BankDetailsMethod.OpenBanking)
          )
        )
      }
    }

    "redirect to the 'choose your bank account' page" when {

      Seq(PaymentMethod.BACS, PaymentMethod.PaymentOrder).foreach { paymentMethod =>
        s"the user selects the 'Open Banking' option and the payment method is '${paymentMethod.entryName}'" in {
          stubBackendJourneyId()
          stubPOSTJourney()
          stubBackendLastPaymentMethod(paymentMethod)

          val response: Future[Result] = controller.onSubmit(
            TdAll.request
              .withMethod("POST")
              .withFormUrlEncodedBody("bankDetailsMethod" -> "OpenBanking")
          )
          redirectLocation(response) shouldBe Some(routes.WeNeedBankDetailsController.onPageLoadAis.url)
          verifyUpdateJourneyCalled(
            TdAll.refundJourney.copy(
              bankDetailsMethod = Some(BankDetailsMethod.OpenBanking)
            )
          )
        }
      }
    }

    "redirect to the 'how you will get your refund (non-AIS)' page" when {

      "the user selects the 'Manual Entry' option and the payment method is 'Card'" in {
        stubBackendJourneyId()
        stubPOSTJourney()
        stubBackendLastPaymentMethod(PaymentMethod.Card)

        val response: Future[Result] = controller.onSubmit(
          TdAll.request
            .withMethod("POST")
            .withFormUrlEncodedBody("bankDetailsMethod" -> "ManualEntry")
        )
        redirectLocation(response) shouldBe Some(routes.HowYouWillGetYourRefundController.onPageLoad.url)
        verifyUpdateJourneyCalled(
          TdAll.refundJourney.copy(
            bankDetailsMethod = Some(BankDetailsMethod.ManualEntry)
          )
        )
      }
    }

    "redirect to the 'we need your bank details' page" when {

      Seq(PaymentMethod.BACS, PaymentMethod.PaymentOrder).foreach { paymentMethod =>
        s"the user selects the 'Manual Entry' option and the payment method is '${paymentMethod.entryName}'" in {
          stubBackendJourneyId()
          stubPOSTJourney()
          stubBackendLastPaymentMethod(paymentMethod)

          val response: Future[Result] = controller.onSubmit(
            TdAll.request
              .withMethod("POST")
              .withFormUrlEncodedBody("bankDetailsMethod" -> "ManualEntry")
          )
          redirectLocation(response) shouldBe Some(routes.WeNeedBankDetailsController.onPageLoad.url)
          verifyUpdateJourneyCalled(
            TdAll.refundJourney.copy(
              bankDetailsMethod = Some(BankDetailsMethod.ManualEntry)
            )
          )
        }
      }

    }

    "not retrieve the payment method again if there is already one in the journey" in {
      stubBackendJourneyId(
        TdAll.refundJourney.copy(
          paymentMethod = Some(PaymentMethod.Card)
        )
      )
      stubPOSTJourney()

      val response: Future[Result] = controller.onSubmit(
        TdAll.request
          .withMethod("POST")
          .withFormUrlEncodedBody("bankDetailsMethod" -> "OpenBanking")
      )
      redirectLocation(response) shouldBe Some(routes.HowYouWillGetYourRefundController.onPageLoadAis.url)
      verifyUpdateJourneyCalled(
        TdAll.refundJourney.copy(
          paymentMethod = Some(PaymentMethod.Card),
          bankDetailsMethod = Some(BankDetailsMethod.OpenBanking)
        )
      )
      verifyGetPaymentMethodNotCalled()
    }

    "not update the journey if the bank details method is unchanged" in {
      stubBackendJourneyId(
        TdAll.refundJourney.copy(
          bankDetailsMethod = Some(BankDetailsMethod.OpenBanking)
        )
      )
      stubBackendLastPaymentMethod(PaymentMethod.Card)

      val response: Future[Result] = controller.onSubmit(
        TdAll.request
          .withMethod("POST")
          .withFormUrlEncodedBody("bankDetailsMethod" -> "OpenBanking")
      )
      redirectLocation(response) shouldBe Some(routes.HowYouWillGetYourRefundController.onPageLoadAis.url)
      verifyUpdateJourneyNotCalled(TdAll.journeyId)
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
