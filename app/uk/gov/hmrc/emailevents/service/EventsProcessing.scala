/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.emailevents.service

import play.api.Logging
import uk.gov.hmrc.emailevents.connector.EmailConnector
import uk.gov.hmrc.emailevents.models.{ EventIgnored, EventSaveResult }
import uk.gov.hmrc.emailevents.models.connector.Event
import uk.gov.hmrc.emailevents.models.controllers.DeliveryStatus.UnInterested
import uk.gov.hmrc.emailevents.models.controllers.RawEvent

import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future

trait EventsProcessing {
  def apply(rawEvent: RawEvent): Future[EventSaveResult]
}

@Singleton
class EventsProcessingImpl @Inject() (emailConnector: EmailConnector) extends EventsProcessing with Logging {
  override def apply(rawEvent: RawEvent): Future[EventSaveResult] =
    rawEvent.deliveryInfoNotification.deliveryInfo.deliveryStatus match {
      case UnInterested =>
        Future.successful(EventIgnored(s"Event is ignored and its not processed"))
      case _ =>
        emailConnector.send(
          Event(
            rawEvent.deliveryInfoNotification.transId,
            rawEvent.deliveryInfoNotification.correlationId,
            rawEvent.deliveryInfoNotification.deliveryInfo.deliveryStatus,
            rawEvent.deliveryInfoNotification.deliveryInfo.timeStamp,
            rawEvent.deliveryInfoNotification.deliveryInfo.code,
            rawEvent.deliveryInfoNotification.deliveryInfo.description,
            rawEvent.deliveryInfoNotification.deliveryInfo.additionalInfo,
            rawEvent.deliveryInfoNotification.deliveryInfo.destination,
            rawEvent.deliveryInfoNotification.callbackData
          )
        )
    }
}
