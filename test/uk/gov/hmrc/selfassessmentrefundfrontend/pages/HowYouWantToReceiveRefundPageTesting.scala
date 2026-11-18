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

package uk.gov.hmrc.selfassessmentrefundfrontend.pages

import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.mustBe
import support.PageContentTesting
import uk.gov.hmrc.selfassessmentrefundfrontend.controllers.refundRequestJourney.routes
import uk.gov.hmrc.selfassessmentrefundfrontend.model.BankDetailsMethod

trait HowYouWantToReceiveRefundPageTesting extends PageContentTesting {

  def checkPageContent(preselectedOption: Option[BankDetailsMethod])(doc: Document): Unit = {

    doc.checkHasBackLinkWithUrl("#")

    doc.select("#bankDetailsMethod-hint").text() mustBe "You can get your refund paid into a UK bank account."

    doc.checkHasRadioButtonOptionsWith(
      List(
        "Sign in to your bank to approve the refund (recommended)" -> Some(
          "You’ll be transferred to your bank to sign in and choose the account you want to use. " +
            "You won’t need to enter your account details, and it’s more secure."
        ),
        "Enter your bank account details"                          -> Some(
          "You’ll need to type in your account number and sort code."
        )
      )
    )

    doc.checkHasFormActionAsContinueButton(routes.HowYouWantToReceiveRefundController.onSubmit)

    doc.select("input[value=\"OpenBanking\"]").hasAttr("checked") mustBe preselectedOption.contains(
      BankDetailsMethod.OpenBanking
    )
    doc.select("input[value=\"ManualEntry\"]").hasAttr("checked") mustBe preselectedOption.contains(
      BankDetailsMethod.ManualEntry
    )
    ()
  }

  def checkPageWithFormError(
    errorMessage: String,
    errorLink:    String
  )(doc: Document): Unit = {
    checkPageContent(None)(doc)

    doc.checkHasErrorSummaryWith(errorMessage, errorLink)

    doc.select("#bankDetailsMethod-error").text() mustBe "Error: " + errorMessage
    ()
  }

}
