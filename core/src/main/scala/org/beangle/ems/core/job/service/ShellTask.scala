package org.beangle.ems.core.job.service

trait ShellTask {

  def execute(command: String): (Int, String)
}
