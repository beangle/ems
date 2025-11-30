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

package org.beangle.ems.app

import org.beangle.commons.collection.Collections
import org.beangle.commons.conversion.string.DateConverter
import org.beangle.commons.io.IOs
import org.beangle.commons.json.Json
import org.beangle.commons.lang.{ClassLoaders, Strings}
import org.beangle.commons.logging.Logging
import org.beangle.commons.net.Networks
import org.beangle.commons.net.http.HttpUtils
import org.beangle.ems.app.Ems.env
import org.beangle.ems.app.blob.{LocalRepository, RemoteRepository, Repository}

import java.io.{File, FileInputStream}
import java.net.URL

object EmsApp extends Logging {

  val properties: Map[String, Any] = readProperties()
  val name: String = properties("name").asInstanceOf[String]
  val path: String = properties("path").asInstanceOf[String]

  private var _token: Token = _

  def getBlobRepository(remote: Boolean = true): Repository = {
    val dir = "/" + Strings.substringBefore(name, "-")
    if remote then new RemoteRepository(env.blob, dir, name, secret)
    else new LocalRepository(Ems.home + "/micdn/blob", dir)
  }

  def secret: String = {
    properties.getOrElse("secret", name).asInstanceOf[String]
  }

  def token: String = {
    if (null != _token) {
      _token.expiredAt < System.currentTimeMillis()
      _token = null
    }

    if (null == _token) {
      val tokenUrl = Ems.cas + "/oauth/token/login?app=" + name + "&secret=" + secret
      val res = HttpUtils.getText(tokenUrl)
      if (res.status == 200) {
        val token = Json.parseObject(res.getText)
        _token = Token(token.getString("token"), DateConverter.convert(token.getString("expiredAt"), classOf[java.util.Date]).getTime)
      } else {
        throw new RuntimeException("cannot find token")
      }
    }
    _token.token
  }

  def getAppFile: Option[File] = {
    val homefile = new File(Ems.home + path + ".xml")
    if (homefile.exists) Some(homefile) else None
  }

  def getFile(file: String): Option[File] = {
    val homefile =
      if (file.startsWith("/")) new File(Ems.home + path + file)
      else new File(Ems.home + path + "/" + file)

    if (homefile.exists) Some(homefile)
    else None
  }

  private def readProperties(): Map[String, Any] = {
    try {
      val configs = ClassLoaders.getResources("beangle.xml")
      var appManifest: Map[String, String] = null
      val iter = configs.iterator
      while (iter.hasNext && null == appManifest) {
        appManifest = parseEmsXml(iter.next())
      }
      if (null == appManifest) {
        throw new RuntimeException("cannot find beangle.xml,contains <ems> element.")
      } else if (!appManifest.contains("name")) {
        throw new RuntimeException("<ems> missing app-name attribute.")
      }
      val name = appManifest("name")

      //app path starts with /
      var appPath = Strings.replace(name, "-", "/")
      appPath = "/" + Strings.replace(appPath, ".", "/")

      val result = new collection.mutable.HashMap[String, Any]
      result ++= appManifest
      result.put("path", appPath)

      val appFile = new File(Ems.home + appPath + ".xml")
      if (appFile.exists()) {
        val is = new FileInputStream(appFile)
        val rootNode = scala.xml.XML.load(is)
        rootNode \\ "app" foreach { app =>
          result ++= app.attributes.asAttrMap
        }
        rootNode \ "props" \ "prop" foreach { pNode =>
          result.put((pNode \ "@key").text.trim, pNode.text.trim())
        }
        IOs.close(is)
      }
      result.toMap
    } catch {
      case e: Throwable => logger.error("Issue exception when read property", e); System.exit(1); Map.empty
    }
  }

  private def parseEmsXml(url: URL): Map[String, String] = {
    val is = url.openStream()
    val emsOption = (scala.xml.XML.load(is) \ "ems").headOption
    IOs.close(is)
    emsOption match {
      case Some(ems) =>
        val appName = (ems \ "@app-name").text
        val props = Collections.newMap[String, String]
        (ems \ "props" \ "prop").map { p =>
          props.put((p \ "@key").text, p.text.trim())
        }
        if (Strings.isNotBlank(appName)) {
          props.put("name", appName)
        }
        props.toMap
      case None => Map.empty
    }
  }

  def getResource(path: String): Option[URL] = {
    val p = if path.startsWith("/") then path.substring(1) else path
    ClassLoaders.getResources(path).headOption match
      case None =>
        val url = Networks.url(s"${Ems.innerApi}/platform/config/files/$name/$p")
        val status = HttpUtils.access(url)
        if status.isOk then Some(url) else None
      case a@Some(url) => a
  }
}

case class Token(token: String, expiredAt: Long)
