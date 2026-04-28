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

package uk.gov.hmrc.selfassessmentrefundfrontend.views.refundrequestjourney

import cats.implicits.catsSyntaxEq
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.Request
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import uk.gov.hmrc.selfassessmentrefundfrontend.model.{AccountType, Amount, BankAccountInfo}
import uk.gov.hmrc.selfassessmentrefundfrontend.util.AmountFormatter

import javax.inject.{Inject, Singleton}

@Singleton
class CheckDetailsHelper @Inject() (i18n: I18nSupport) {
  import i18n.*

  def buildSummaryList(amount: Amount, accountType: AccountType, bankAccountInfo: BankAccountInfo)(implicit
    request: Request[_]
  ): SummaryList = {
    val rows = Seq(
      accountTypeRow(accountType),
      nameOnTheAccountRow(bankAccountInfo),
      accountSortCodeRow(bankAccountInfo),
      accountNumberRow(bankAccountInfo)
    )

    SummaryList(
      card = Some(
        Card(
          title =
            Some(CardTitle(Text(Messages("check-details.bank-account-details")), classes = "govuk-card__header__text")),
          actions = Some(
            Actions(items =
              Seq(
                ActionItem(
                  href =
                    uk.gov.hmrc.selfassessmentrefundfrontend.controllers.refundRequestJourney.routes.CheckDetailsPageController.changeAccount.url,
                  classes = "change-link",
                  content = Text(Messages("check-details.change.bank.details")),
                  visuallyHiddenText = Some(Messages("check-details.change.bank.details"))
                )
              )
            )
          )
        )
      ),
      rows = bankAccountInfo.rollNumber.fold(rows)(rollNumber => rows :+ rollNumberRow(bankAccountInfo))
    )
  }

  def amountRow(amount: Amount)(implicit request: Request[_]): SummaryList = {
    val amountKey = Key(Text(s"""${Messages("check-details.amount")}"""))
    SummaryList(rows =
      Seq(
        SummaryListRow(
          key = amountKey,
          value = Value(HtmlContent(AmountFormatter.formatAmount(amount.repay))),
          actions = Some(
            uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions(items =
              Seq(
                ActionItem(
                  href =
                    s"${uk.gov.hmrc.selfassessmentrefundfrontend.controllers.refundRequestJourney.routes.CheckDetailsPageController.changeAmount.url}",
                  content = Text(Messages("check-details.change")),
                  visuallyHiddenText = Some(Messages("check-details.amount"))
                )
              )
            )
          )
        )
      )
    )
  }

  private def accountTypeRow(accountType: AccountType)(implicit request: Request[_]): SummaryListRow =
    buildSummaryListRow(
      key = Messages("check-details.bank-account-type"),
      value = if (accountType.name === "Personal") {
        Messages("check-details.personal")
      } else Messages("check-details.business")
    )

  private def nameOnTheAccountRow(bankAccountInfo: BankAccountInfo)(implicit request: Request[_]): SummaryListRow =
    buildSummaryListRow(
      key = Messages("check-details.account.name"),
      value = bankAccountInfo.name
    )

  private def accountSortCodeRow(bankAccountInfo: BankAccountInfo)(implicit request: Request[_]): SummaryListRow =
    buildSummaryListRow(
      key = Messages("check-details.account.sortCode"),
      value = bankAccountInfo.sortCode.displayFormat
    )

  private def accountNumberRow(bankAccountInfo: BankAccountInfo)(implicit request: Request[_]): SummaryListRow =
    buildSummaryListRow(
      key = Messages("check-details.account.number"),
      value = bankAccountInfo.accountNumber.value
    )

  private def rollNumberRow(bankAccountInfo: BankAccountInfo)(implicit request: Request[_]): SummaryListRow =
    buildSummaryListRow(
      key = Messages("check-details.account.building.society.roll.number"),
      value = bankAccountInfo.rollNumber.map(_.value).getOrElse("")
    )

  private def buildSummaryListRow(key: String, value: String): SummaryListRow = SummaryListRow(
    key = Key(Text(s"""$key""")),
    value = Value(HtmlContent(value))
  )
}
