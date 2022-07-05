package org.beangle.ems.core.config.model

trait LocaleTitle {

  def title: String

  def enTitle: String

  def getTitle(isEn: Boolean): String = {
    if isEn then enTitle else title
  }
}
