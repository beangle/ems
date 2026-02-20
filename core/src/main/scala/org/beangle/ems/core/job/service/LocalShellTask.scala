/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.ems.core.job.service

import org.beangle.commons.os.{LinuxBash, Platform, WinCmd}

/** 本地执行 Shell 命令的任务。
 */
class LocalShellTask extends ShellTask {

  /** 执行 Shell 命令
   *
   * @param command 要执行的命令（通过系统 shell 执行，支持管道、重定向等）
   * @return
   */
  override def execute(command: String): (Int, String) = {
    if (Platform.isLinux) {
      val rs = LinuxBash.exec(command)
      (rs._1, rs._2.mkString("\n"))
    } else if (Platform.isWin) {
      val rs = WinCmd.exec(command)
      (rs._1, rs._2.mkString("\n"))
    } else {
      throw new RuntimeException(s"Cannot support platform ${Platform.osName}")
    }
  }
}
