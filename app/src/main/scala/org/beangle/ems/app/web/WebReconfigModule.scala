package org.beangle.ems.app.web

import org.beangle.cdi.bind.ReconfigModule
import org.beangle.ems.app.{Ems, EmsApp}

class WebReconfigModule extends ReconfigModule {
  override protected def config(): Unit = {
    //support load remote freemarker template files
    update("mvc.FreemarkerConfigurer.default")
      .set("templatePath", s"${Ems.api}/platform/config/files/${EmsApp.name}/{path},class://")
  }
}
