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
import org.beangle.commons.config.{Config, ConfigFactory, XmlConfigs}
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ClassLoaders, Strings}
import org.beangle.commons.net.Networks
import org.beangle.commons.net.http.HttpUtils
import org.beangle.commons.xml.{Document, Node}
import org.beangle.ems.app.Ems.env
import org.beangle.ems.app.blob.{LocalRepository, RemoteRepository, Repository}

import java.io.{File, FileInputStream}
import java.net.URL

object EmsApp {

  val properties: Map[String, String] = readProperties()
  val name: String = properties("name")
  val path: String = properties("path")

  def getBlobRepository(remote: Boolean = true): Repository = {
    val dir = "/" + Strings.substringBefore(name, "-")
    if remote then new RemoteRepository(env.blob, dir, name, secret)
    else new LocalRepository(Ems.home + "/micdn/blob", dir)
  }

  def secret: String = {
    properties.getOrElse("secret", name)
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

  private def readProperties(): Map[String, String] = {
    try {
      val configLocation = "classpath*:beangle.xml"
      val doc = XmlConfigs.load(configLocation)
      var appManifest: Map[String, String] = null
      (doc \ "ems").headOption foreach { ems =>
        appManifest = parseEmsXml(ems)
      }
      if (null == appManifest || appManifest.isEmpty) {
        throw new RuntimeException("cannot find beangle.xml,contains <ems> element.")
      } else if (!appManifest.contains("name")) {
        throw new RuntimeException("<ems> missing app-name attribute.")
      }
      val name = appManifest("name")

      //app path starts with /
      var appPath = Strings.replace(name, "-", "/")
      appPath = "/" + Strings.replace(appPath, ".", "/")

      val result = new collection.mutable.HashMap[String, String]
      result ++= appManifest
      result.put("path", appPath)

      val appFile = new File(Ems.home + appPath + ".xml")
      if (appFile.exists()) {
        val rootNode = Document.parse(appFile)
        result ++= rootNode.attrs
        rootNode \ "props" \ "prop" foreach { pNode =>
          result.put((pNode \ "@key").text.trim, pNode.text.trim())
        }
      }
      EmsApp.encryptor match {
        case None => result.toMap
        case Some(encryptor) => result.map(kv => (kv._1, encryptor.process(kv._1, kv._2))).toMap
      }
    } catch {
      case e: Throwable => e.printStackTrace(); System.exit(1); Map.empty
    }
  }

  private def parseEmsXml(ems: Node): Map[String, String] = {
    val appName = (ems \ "@app-name").text
    val props = Collections.newMap[String, String]
    (ems \ "props" \ "prop").map { p =>
      props.put((p \ "@key").text, p.text.trim())
    }
    if (Strings.isNotBlank(appName)) {
      props.put("name", appName)
    }
    props.toMap
  }

  def getResource(path: String): Option[URL] = {
    val p = if path.startsWith("/") then path.substring(1) else path
    ClassLoaders.getResources(path).headOption match
      case None =>
        val url = s"${Ems.innerApi}/platform/config/files/$name/$p"
        val status = HttpUtils.access(url)
        if status.isOk then Some(Networks.url(url)) else None
      case a@Some(url) => a
  }

  def encryptor: Option[Config.Processor] = {
    val key = "beangle.encryptor.password"
    var pwd = System.getProperty(key)
    if (null == pwd) {
      pwd = ConfigFactory.SystemEnvironment.get(key, null).asInstanceOf[String]
    }
    if (null == pwd) None else Some(Config.pbe(pwd))
  }
}
