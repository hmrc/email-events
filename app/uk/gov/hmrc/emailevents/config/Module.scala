/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.emailevents.config

import com.google.inject.AbstractModule
import uk.gov.hmrc.emailevents.service.{ EventsProcessing, EventsProcessingImpl }

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[AppConfig]).asEagerSingleton()
    bind(classOf[EventsProcessing]).to(classOf[EventsProcessingImpl]).asEagerSingleton()
  }
}
