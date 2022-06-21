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

package org.beangle.ems.app.log

import org.beangle.commons.bean.{Disposable, Initializing}
import org.beangle.commons.io.Dirs
import org.beangle.commons.lang.Charsets

import java.io.{File, FileOutputStream}

trait Appender {

  def append(entry: BusinessLogEntry): Unit
}

class ConsoleAppender(layout: Layout) extends Appender {
  def append(entry: BusinessLogEntry): Unit = {
    println(layout.mkString(entry))
  }
}

class FileAppender(val fileName: String, layout: Layout) extends Appender with Initializing with Disposable {

  private var fos: FileOutputStream = _

  override def init(): Unit = {
    val file = new File(fileName)
    Dirs.on(file.getParentFile).mkdirs()
    fos = new FileOutputStream(file)
  }

  override def destroy(): Unit = {
    if null != fos then fos.close()
  }

  def append(entry: BusinessLogEntry): Unit = {
    fos.write(layout.mkString(entry).getBytes(Charsets.UTF_8))
  }
}
