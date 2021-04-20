package org.beangle.ems.core.config.model

import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.Updated

class DocTemplate  extends LongId with Updated {
  var path: String = _
  var contents: Array[Byte] = _
  var app: App = _
}
