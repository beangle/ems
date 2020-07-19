/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.app

import java.io.{File, FileInputStream}

import org.beangle.commons.conversion.converter.String2DateConverter
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ClassLoaders, Strings}
import org.beangle.commons.logging.Logging
import org.beangle.commons.net.http.HttpUtils
import org.beangle.ems.app.Ems.env
import org.beangle.ems.app.blob.{LocalRepository, RemoteRepository, Repository}
import org.beangle.ems.app.util.JSON

object EmsApp extends Logging {

  val properties: Map[String, Any] = readProperties()
  val name: String = properties("name").asInstanceOf[String]
  val path: String = properties("path").asInstanceOf[String]

  private var _token: Token = _

  def secret: String = {
    properties.getOrElse("secret", name).asInstanceOf[String]
  }

  def getBlobRepository(remote: Boolean = true): Repository = {
    var dir = Strings.substringBeforeLast(name, "-");
    dir = "/" + Strings.replace(dir, "-", "/");
    if (remote) {
      new RemoteRepository(env.blob, dir, name,secret)
    } else {
      new LocalRepository(Ems.home + "/blob", dir)
    }
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
        val token = JSON.parse(res.getText).asInstanceOf[Map[String, _]]
        _token = Token(token("token").asInstanceOf[String], (new String2DateConverter).convert(token("expiredAt"), classOf[java.util.Date]).asInstanceOf[java.util.Date].getTime)
      } else {
        throw new RuntimeException("cannot find token")
      }
    }
    _token.token
  }

  private def readProperties(): Map[String, Any] = {
    try {
      val configs = ClassLoaders.getResources("META-INF/beangle/ems-app.properties")
      val appManifest = if (configs.isEmpty) {
        Map.empty[String, String]
      } else {
        IOs.readJavaProperties(configs.head)
      }
      val name = appManifest.get("name") match {
        case Some(n) => n
        case None => throw new RuntimeException("cannot find META-INF/beangle/ems-app.properties")
      }

      //app path starts with /
      var appPath = Strings.replace(name, "-", "/")
      appPath = "/" + Strings.replace(appPath, ".", "/")

      val result = new collection.mutable.HashMap[String, Any]
      result ++= appManifest
      val appconf = new File(Ems.home + appPath + "/conf.properties")
      if (appconf.exists) result ++= IOs.readJavaProperties(appconf.toURI.toURL)
      result.put("path", appPath)

      val appFile = new File(Ems.home + appPath + ".xml")
      if (appFile.exists()) {
        val is = new FileInputStream(appFile)
        scala.xml.XML.load(is) \\ "app" foreach { app =>
          result ++= app.attributes.asAttrMap
        }
        IOs.close(is)
      }
      result.toMap
    } catch {
      case e: Throwable => logger.error("Issue exception when read property", e); System.exit(1); Map.empty
    }
  }

  def getAppFile: Option[File] = {
    val homefile = new File(Ems.home + path + ".xml")
    if (homefile.exists) Some(homefile)
    else None
  }

  def getFile(file: String): Option[File] = {
    val homefile =
      if (file.startsWith("/")) new File(Ems.home + path + file)
      else new File(Ems.home + path + "/" + file)

    if (homefile.exists) Some(homefile)
    else None
  }

}

case class Token(token: String, expiredAt: Long)
