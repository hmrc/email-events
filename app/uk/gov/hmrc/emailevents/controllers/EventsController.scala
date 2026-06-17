/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.emailevents.controllers

import play.api.Logging
import play.api.libs.json.{ JsPath, JsValue, Json }
import play.api.mvc.{ Action, ControllerComponents }
import uk.gov.hmrc.emailevents.models.controllers.RawEvent
import uk.gov.hmrc.emailevents.models.{ EventIgnored, EventSaveFailed, EventSaved }
import uk.gov.hmrc.emailevents.service.EventsProcessing
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext
import scala.util.matching.Regex

@Singleton
class EventsController @Inject() (cc: ControllerComponents, eventsProcessing: EventsProcessing)(implicit
  ec: ExecutionContext
) extends BackendController(cc) with Logging {

  def events(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[RawEvent] { event =>
      logEvent(event)
      eventsProcessing(event) map {
        case result: EventSaved => Ok(Json.toJson(result))
        case ignoredResult: EventIgnored =>
          Accepted(Json.toJson(ignoredResult.copy(message = deliveryStatus(request.body))))
        case e: EventSaveFailed => InternalServerError(Json.toJson(e))
      }
    }
  }

  private def logEvent(event: RawEvent): Unit = {
    val emailHidden = event.copy(
      deliveryInfoNotification = event.deliveryInfoNotification.copy(
        event.deliveryInfoNotification.deliveryInfo
          .copy(
            destination = "emailAddressHidden",
            description = scrapeEmails(event.deliveryInfoNotification.deliveryInfo.description),
            additionalInfo = scrapeEmails(event.deliveryInfoNotification.deliveryInfo.additionalInfo)
          )
      )
    )
    logger.warn(s"EmailEventReceived: ${Json.toJson(emailHidden)}")
  }

  private def deliveryStatus(body: JsValue) = {
    val status = (JsPath \\ "deliveryStatus").read[String].reads(body).getOrElse("")
    val message = s"Event with deliveryStatus $status is ignored and not processed"
    logger.warn(message)
    message
  }

  private[controllers] def scrapeEmails(text: String) = {
    val emailRegex: Regex = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b".r
    emailRegex.findAllIn(text).toList.foldLeft(text)((acc, email) => acc.replace(email, "emailHidden"))
  }
}
