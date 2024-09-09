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

package org.beangle.ems.portal.action.admin.user

import jakarta.servlet.http.Part
import org.apache.commons.compress.archivers.zip.ZipFile
import org.beangle.commons.collection.Collections
import org.beangle.commons.concurrent.Workers
import org.beangle.commons.file.zip.Zipper
import org.beangle.commons.io.{Files, IOs}
import org.beangle.commons.lang.{Strings, SystemInfo, Throwables}
import org.beangle.commons.net.http.HttpUtils
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.model.{Avatar, User}
import org.beangle.ems.core.user.service.{AvatarService, UserService}
import org.beangle.web.action.annotation.{mapping, param}
import org.beangle.web.action.support.{ActionSupport, ServletSupport}
import org.beangle.web.action.view.{Status, Stream, View}
import org.beangle.webmvc.support.helper.QueryHelper

import java.io.*
import scala.jdk.javaapi.CollectionConverters.asScala

class AvatarAction extends ActionSupport with ServletSupport {

  var entityDao: EntityDao = _

  var avatarService: AvatarService = _

  var domainService: DomainService = _

  var userService: UserService = _

  def index(): View = {
    val query = OqlBuilder.from(classOf[User], "user")
    query.where("user.org=:org", domainService.getOrg)
    QueryHelper.populate(query)
    get("user") foreach { u =>
      query.where("user.code like :u or user.name like :u", "%" + u + "%")
    }
    query.where("user.avatarId is not null")
    query.limit(QueryHelper.pageIndex, 60)
    query.orderBy("user.code")
    put("users", entityDao.search(query))
    forward()
  }

  def view(): View = {
    forward()
  }

  @mapping("{userId}")
  def info(@param("userId") userId: String): View = {
    val user = entityDao.get(classOf[User], userId.toLong)
    user.avatarId match {
      case None => Status.NotFound
      case Some(avatarId) =>
        loadAvatar(avatarId) match {
          case None => Status.NotFound
          case Some(avatar) =>
            if (null == avatar.filePath) {
              Status.NotFound
            } else {
              EmsApp.getBlobRepository().path(avatar.filePath) match {
                case Some(url) => response.sendRedirect(url); null
                case None => Status.NotFound
              }
            }
        }
    }
  }

  private def loadAvatar(avatarId: String): Option[Avatar] = {
    val avatar = entityDao.get(classOf[Avatar], avatarId)
    Option(avatar)
  }

  def uploadSetting(): View = {
    forward()
  }

  def upload(): View = {
    getAll("zipfile", classOf[Part]) foreach { zipFile =>
      val tmpFile = new File(SystemInfo.tmpDir + "/photo" + System.currentTimeMillis())
      IOs.copy(zipFile.getInputStream, new FileOutputStream(tmpFile))
      put("total", processZip(tmpFile, "GBK"))
    }
    get("dirInServer") foreach { dirInServer =>
      put("total", processDir(new File(dirInServer)))
    }
    forward()
  }

  def processDir(dir: File): Int = {
    if (!dir.exists()) return 0
    var i = 0
    dir.list() foreach { name =>
      val file = new File(dir.getAbsolutePath + "/" + name)
      if (name.indexOf(".") < 1) {
        logger.warn(name + " without suffix,skipped")
      } else if (file.isDirectory) {
        logger.info(name + " is dir,skipped")
      } else {
        val usercode = Strings.substringBeforeLast(name, ".")
        val users = userService.getIgnoreCase(usercode)
        if (users.isEmpty) {
          logger.warn("Cannot find user info of " + usercode)
        } else {
          i += 1
          avatarService.save(users.head, name, new FileInputStream(dir.getAbsolutePath + "/" + name))
        }
      }
    }
    i
  }

  def processZip(zipfile: File, encoding: String): Int = {
    val zipBuilder = ZipFile.builder().setFile(zipfile)
    if (null != encoding) zipBuilder.setCharset(encoding)

    val file = zipBuilder.get()
    var i = 0
    try {
      val en = file.getEntries()
      asScala(en) foreach { ze =>
        i = i + 1
        if (!ze.isDirectory) {
          val photoname = if (ze.getName.contains("/")) Strings.substringAfterLast(ze.getName, "/") else ze.getName
          if (photoname.indexOf(".") < 1) {
            logger.warn(photoname + " format is error")
          } else {
            val usercode = Strings.substringBeforeLast(photoname, ".")
            val users = userService.getIgnoreCase(usercode)
            if (users.isEmpty) {
              logger.warn("Cannot find user info of " + usercode)
            } else {
              avatarService.save(users.head, photoname, file.getInputStream(ze))
            }
          }
        }
      }
      file.close()
    } catch {
      case e: IOException => Throwables.propagate(e)
    }
    i
  }

  def downloadSetting(): View = {
    forward()
  }

  /** 批量下载
   *
   * @return
   */
  def download(): View = {
    var userCodeString = get("code", "")
    userCodeString = Strings.replace(userCodeString, "\r", "")
    userCodeString = Strings.replace(userCodeString, "，", ",")

    val userCodes = Strings.split(userCodeString).toSet
    val codesList = Collections.split(userCodes.toList, 500)
    val userFiles = Collections.newBuffer[Array[Any]]
    codesList foreach { codes =>
      val q = OqlBuilder.from[Array[Any]](classOf[User].getName + " user," + classOf[Avatar].getName + " avatar")
      q.where("user.org=:org", domainService.getOrg)
      q.where("user.avatarId=avatar.id")
      q.where("user.code in(:codes)", userCodes)
      q.select("user.code,avatar.filePath")
      userFiles.addAll(entityDao.search(q))
    }

    val tmpDir = System.getProperty("java.io.tmpdir") + "/avatar"
    new File(tmpDir).mkdirs()
    val blob = EmsApp.getBlobRepository()
    val avatarFiles = Collections.newBuffer[File]
    val exists = Collections.newSet[String]

    Workers.work(userFiles, (userFile: Array[Any]) => {
      blob.url(userFile(1).toString) foreach { url =>
        val userCode = userFile(0).toString
        val fileName = userCode + "." + Strings.substringAfterLast(userFile(1).toString, ".")
        val localFile = new File(tmpDir + Files./ + fileName)
        HttpUtils.download(url.openConnection(), localFile)
        if (localFile.exists()) {
          avatarFiles.addOne(localFile)
          exists.addOne(userCode)
        }
      }
    })

    val missings = userCodes -- exists
    if (missings.nonEmpty) {
      val missingFile = new File(tmpDir + Files./ + "未找到照片的名单.txt")
      val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(missingFile)))
      writer.write(missings.mkString("\n"))
      writer.close()
      avatarFiles.addOne(missingFile)
    }
    val zipFile = new File(tmpDir + Files./ + s"照片(${exists.size}人).zip")
    Zipper.zip(new File(tmpDir), avatarFiles, zipFile, "utf-8")
    Stream(zipFile, "application/zip", s"照片(${exists.size}人).zip").cleanup { () =>
      zipFile.delete()
      Files.travel(new File(tmpDir), f => f.delete())
      new File(tmpDir).delete()
    }
  }

}
