package org.beangle.ems.core.config.model

import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.Updated

class TextTemplate extends LongId with Updated {
  var path: String = _
  var contents: String = _
  var app: App = _
}
