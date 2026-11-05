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
import support.PageContentTesting

trait HMRCUsesEcospendPageTesting extends PageContentTesting {

  def checkPageContent(doc: Document): Unit = {

    doc.checkHasBackLinkWithUrl("#")

    doc.checkHasParagraphs(
      List(
        "Ecospend checks bank account data for security purposes to ensure that the account belongs to the customer.",
        "Ecospend is an authorised open banking service provider and is regulated by the Financial Conduct Authority (FCA). Ecospend or HMRC staff never see your data, and it is not stored or shared.",
        "Signing in through your own bank confirms the account is yours, which keeps your refund safe.",
        "You can also get your refund by entering your account details. This option does not use Ecospend."
      )
    )

    doc.checkHasActionAsButton(
      uk.gov.hmrc.selfassessmentrefundfrontend.controllers.refundRequestJourney.routes.HowYouWantToReceiveRefundController.onPageLoad.url,
      "Continue"
    )
  }

}
