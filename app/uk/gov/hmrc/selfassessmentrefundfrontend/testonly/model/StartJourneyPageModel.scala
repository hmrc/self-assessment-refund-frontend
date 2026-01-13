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

package uk.gov.hmrc.selfassessmentrefundfrontend.testonly.model

import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.selfassessmentrefundfrontend.model.start.StartRequest
import uk.gov.hmrc.selfassessmentrefundfrontend.model.start.StartRequest.{StartRefund, ViewHistory}

import scala.reflect.ClassTag

final case class StartJourneyPageModel(
  form:    Form[StartJourneyOptions],
  presets: List[Preset]
) {

  lazy val journeyType: StartJourneyType = form.value.map(_.`type`).getOrElse(StartJourneyType.StartRefund)

  def submitStartJourney: Call =
    uk.gov.hmrc.selfassessmentrefundfrontend.testonly.controllers.routes.StartJourneyController.submitStartJourneyForm()

  def selectPreset: Call =
    uk.gov.hmrc.selfassessmentrefundfrontend.testonly.controllers.routes.StartJourneyController.selectPreset()

  def hideInputsClass: String = journeyType match {
    case StartJourneyType.StartRefund => ""
    case StartJourneyType.ViewHistory => "govuk-visually-hidden"
  }

  def errorSummary(implicit messages: Messages): ErrorSummary =
    ErrorSummary(
      errorList = form.errors.asTextErrorLinks,
      title = Text("Issues")
    )

  def makePresetSelect[T <: StartRequest: ClassTag]: Select = {

    val typedPresets: List[(Preset, Int)] =
      presets.zipWithIndex.collect {
        case (p @ Preset(_: StartRefund, _), idx) => p -> idx
        case (p @ Preset(_: ViewHistory, _), idx) => p -> idx
      }

    val selectItems: Seq[SelectItem] =
      typedPresets.map {
        case (Preset(sr: StartRefund, description), idx) =>
          makeSelectItem(Left(sr), description, idx)

        case (Preset(vh: ViewHistory, description), idx) =>
          makeSelectItem(Right(vh), description, idx)
      }

    Select(
      id = "presets",
      name = "index",
      items = selectItems,
      label = Label(content = HtmlContent("Presets"))
    )
  }

  def makeSelectItem(
    req:         Either[StartRefund, ViewHistory],
    description: String,
    n:           Int
  ): SelectItem =
    req.fold(
      sr => {
        val paymentType =
          if (sr.lastPaymentViaCard.getOrElse(false)) "CARD" else "BACS"
        SelectItem(
          text = s"Start Refund: ${sr.nino} - £${sr.fullAmount} - $paymentType - $description",
          value = Some(n.toString)
        )
      },
      vh => SelectItem(text = s"View History: ${vh.nino} - $description", value = Some(n.toString))
    )

}

object StartJourneyPageModel {

  def apply(form: Form[StartJourneyOptions]): StartJourneyPageModel =
    StartJourneyPageModel(form, presets = StartJourneyPresets.presets)

}
