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

package org.beangle.ems.app.blob

import java.io.{File, FileOutputStream, InputStream}
import java.net.URL
import java.time.Instant

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.file.digest.Sha1
import org.beangle.commons.io.IOs

class LocalRepository(val base: String, val dir: String) extends Repository {

  override def remove(p: String): Boolean = {
    require(p.startsWith("/"))
    val path = s"$base$dir${p}"
    val file = new File(path)
    if (file.exists()) {
      file.delete()
    } else {
      false
    }
  }

  override def path(p: String): Option[String] = {
    require(p.startsWith("/"))
    val file = new File(s"$base$dir${p}")
    if (file.exists()) {
      Some(file.getAbsolutePath)
    } else {
      None
    }
  }

  override def url(path: String): Option[URL] = {
    require(path.startsWith("/"))
    val file = new File(s"$base$dir${path}")
    if (file.exists()) {
      Some(file.toURI.toURL)
    } else {
      None
    }
  }

  override def upload(folder: String, is: InputStream, fileName: String, owner: String): BlobMeta = {
    require(folder.startsWith("/"))
    val time = System.currentTimeMillis();
    val file = new File(s"$base$dir${folder}/$time")
    val os = new FileOutputStream(file)
    IOs.copy(is, os)
    IOs.close(os)
    val sha = Sha1.digest(file)
    val target = new File(s"$base$dir${folder}/$sha")
    file.renameTo(target)
    val ext = getExt(fileName)
    val meta = new BlobMeta()
    meta.sha = sha
    meta.updatedAt = Instant.now
    meta.fileSize = target.length().asInstanceOf[Int]
    meta.mediaType = MediaTypes.get(ext, MediaTypes.ApplicationOctetStream).toString()
    meta.filePath = s"$folder/${sha}"
    meta
  }

}
