/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.emailevents.connector

import play.api.Logging
import play.api.http.Status
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.emailevents.models.connector.Event
import uk.gov.hmrc.emailevents.models.{ EventSaveError, EventSaveFailed, EventSaveResult, EventSaved }
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse, StringContextOps }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import scala.language.implicitConversions

@Singleton
class EmailConnector @Inject() (servicesConfig: ServicesConfig, httpClient: HttpClientV2)(implicit ec: ExecutionContext)
    extends Logging {

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  val serviceUrl: String = servicesConfig.baseUrl("email")

  def send(event: Event): Future[EventSaveResult] = {
    val eventualResponse: Future[HttpResponse] =
      httpClient.post(url"$serviceUrl/events").withBody(Json.toJson(event)).execute[HttpResponse]
    eventualResponse
      .map { response =>
        response.status match {
          case Status.CREATED => EventSaved("true")
          case status =>
            EventSaveFailed(EventSaveError(status.intValue, s"Event save failed - status code: ${status.intValue}"))
        }
      }
      .recover { case e =>
        EventSaveFailed(
          EventSaveError(Status.INTERNAL_SERVER_ERROR, s"HTTP request to email failed with error: ${e.getMessage}")
        )
      }
  }
}
