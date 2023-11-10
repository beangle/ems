package org.beangle.ems.core.config.model

import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.Named

import java.util.Locale

class TextBundle extends LongId, Named {

  var app: App = _

  var locale: Locale = _

  var texts: String = _
}
