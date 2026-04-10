package org.beangle.ems.cas.action

import org.beangle.ids.cas.web.action.LoginAction
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.View

class IndexAction extends ActionSupport {

  def index(): View = {
    redirect(to(classOf[LoginAction], "index"))
  }

}
