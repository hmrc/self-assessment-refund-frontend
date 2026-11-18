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

import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.selfassessmentrefundfrontend.config.AppConfig
import uk.gov.hmrc.selfassessmentrefundfrontend.connectors.JourneyConnector
import uk.gov.hmrc.selfassessmentrefundfrontend.controllers.action.Actions
import uk.gov.hmrc.selfassessmentrefundfrontend.controllers.action.request.BarsVerifiedRequest
import uk.gov.hmrc.selfassessmentrefundfrontend.model.{BankDetailsMethod, PaymentMethod}
import uk.gov.hmrc.selfassessmentrefundfrontend.views.html.refundrequestjourney.HowYouWantToReceiveRefundPage

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HowYouWantToReceiveRefundController @Inject() (
  messagesControllerComponents:  MessagesControllerComponents,
  appConfig:                     AppConfig,
  actions:                       Actions,
  i18n:                          I18nSupport,
  howYouWantToReceiveRefundPage: HowYouWantToReceiveRefundPage,
  journeyConnector:              JourneyConnector
)(using ExecutionContext)
    extends FrontendController(messagesControllerComponents) {

  import i18n.*

  val onPageLoad: Action[AnyContent] = actions.authenticatedRefundJourneyAction { implicit request =>
    if (appConfig.aisEnabled) {
      val form = request.journey.bankDetailsMethod match {
        case Some(existingValue) => HowYouWantToReceiveRefundController.form.fill(existingValue)
        case None                => HowYouWantToReceiveRefundController.form
      }
      Ok(howYouWantToReceiveRefundPage(form))
    } else {
      NotImplemented
    }
  }

  val onSubmit: Action[AnyContent] = actions.authenticatedRefundJourneyAction.async { implicit request =>
    if (appConfig.aisEnabled) {
      HowYouWantToReceiveRefundController.form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(howYouWantToReceiveRefundPage(formWithErrors))),
          handleBankDetailsMethod(_)
        )
    } else {
      Future.successful(NotImplemented)
    }
  }

  def handleBankDetailsMethod(
    bankDetailsMethod: BankDetailsMethod
  )(using request: BarsVerifiedRequest[_]): Future[Result] =
    for {
      _             <- storeBankDetailsMethod(bankDetailsMethod)
      paymentMethod <- paymentMethod()
    } yield {
      val redirectTo = bankDetailsMethod match {
        case BankDetailsMethod.OpenBanking =>
          paymentMethod match {
            case PaymentMethod.Card                              => routes.HowYouWillGetYourRefundController.onPageLoadAis
            case PaymentMethod.BACS | PaymentMethod.PaymentOrder => routes.WeNeedBankDetailsController.onPageLoadAis
          }
        case BankDetailsMethod.ManualEntry =>
          paymentMethod match {
            case PaymentMethod.Card                              => routes.HowYouWillGetYourRefundController.onPageLoad
            case PaymentMethod.BACS | PaymentMethod.PaymentOrder => routes.WeNeedBankDetailsController.onPageLoad
          }
      }
      Redirect(redirectTo)
    }

  private def storeBankDetailsMethod(
    bankDetailsMethod: BankDetailsMethod
  )(using request: BarsVerifiedRequest[_]): Future[Unit] = {
    val valueUnchanged = request.journey.bankDetailsMethod match {
      case Some(existingValue) => existingValue == bankDetailsMethod
      case None                => false
    }

    if (valueUnchanged) {
      Future.successful(())
    } else {
      journeyConnector.setJourney(
        request.journey.id,
        request.journey.copy(bankDetailsMethod = Some(bankDetailsMethod))
      )
    }
  }

  private def paymentMethod()(using request: BarsVerifiedRequest[_]): Future[PaymentMethod] =
    request.journey.paymentMethod.map(Future.successful).getOrElse {
      journeyConnector.lastPaymentMethod(request.journey.id)
    }
}

object HowYouWantToReceiveRefundController {

  val form: Form[BankDetailsMethod] = Form(
    "bankDetailsMethod" -> Forms.of[BankDetailsMethod](new Formatter[BankDetailsMethod] {
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BankDetailsMethod] =
        data.get(key) match {
          case Some("OpenBanking") => Right(BankDetailsMethod.OpenBanking)
          case Some("ManualEntry") => Right(BankDetailsMethod.ManualEntry)
          case Some(_)             => Left(Seq(FormError(key, "error.invalid")))
          case None                => Left(Seq(FormError(key, "error.required")))
        }

      override def unbind(key: String, value: BankDetailsMethod): Map[String, String] = {
        val stringValue = value match {
          case BankDetailsMethod.OpenBanking => "OpenBanking"
          case BankDetailsMethod.ManualEntry => "ManualEntry"
        }
        Map(key -> stringValue)
      }
    })
  )

}
