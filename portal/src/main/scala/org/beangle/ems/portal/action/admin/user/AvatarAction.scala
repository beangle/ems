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
import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.collection.Collections
import org.beangle.commons.concurrent.Workers
import org.beangle.commons.file.zip.Zipper
import org.beangle.commons.io.{Files, IOs}
import org.beangle.commons.lang.{Strings, SystemInfo, Throwables}
import org.beangle.commons.net.http.HttpUtils
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.EmsLogger
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.model.{Avatar, User}
import org.beangle.ems.core.user.service.{AvatarService, UserService}
import org.beangle.she.webmvc.QueryHelper
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.{Status, Stream, View}

import java.io.*
import scala.jdk.javaapi.CollectionConverters.asScala

class AvatarAction extends ActionSupport, ServletSupport {

  var entityDao: EntityDao = _

  var avatarService: AvatarService = _

  var domainService: DomainService = _

  var userService: UserService = _

  def index(): View = {
    val query = OqlBuilder.from(classOf[User], "user")
    query.where("user.org=:org", domainService.getOrg)
    QueryHelper.populate(entityDao, query)
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
              val url = EmsApp.getBlobRepository().uri(avatar.filePath).toString
              redirect(to(url), "")
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

  /** zip 包和单张照片的上限，防止批量导入把内存/磁盘撑满。 */
  private val MaxZipSize: Long = 100 * 1024 * 1024
  private val MaxPhotoSize: Long = 20 * 1024 * 1024
  private val PhotoSuffixes = Set("jpg", "jpeg", "png", "gif", "bmp", "webp")

  def upload(): View = {
    getAll("zipfile", classOf[Part]) foreach { zipFile =>
      if (zipFile.getSize > MaxZipSize) {
        EmsLogger.warn(s"photo zip is too large: ${zipFile.getSize} bytes,skipped")
        put("total", 0)
      } else {
        val tmpFile = new File(SystemInfo.tmpDir + "/photo" + System.currentTimeMillis())
        try {
          val out = new FileOutputStream(tmpFile)
          try IOs.copy(zipFile.getInputStream, out)
          finally out.close()
          put("total", processZip(tmpFile, "GBK"))
        } finally {
          tmpFile.delete()
        }
      }
    }
    forward()
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
          if (!isPhoto(photoname)) {
            EmsLogger.warn(photoname + " format is error")
          } else if (ze.getSize > MaxPhotoSize) {
            EmsLogger.warn(s"$photoname is too large: ${ze.getSize} bytes,skipped")
          } else {
            val usercode = Strings.substringBeforeLast(photoname, ".")
            val users = userService.getIgnoreCase(usercode)
            if (users.isEmpty) {
              EmsLogger.warn("Cannot find user info of " + usercode)
            } else {
              val is = file.getInputStream(ze)
              try avatarService.save(users.head, photoname, is)
              finally is.close()
            }
          }
        }
      }
    } catch {
      case e: IOException => Throwables.propagate(e)
    } finally {
      file.close()
    }
    i
  }

  private def isPhoto(name: String): Boolean = {
    val dot = name.lastIndexOf('.')
    dot > 0 && PhotoSuffixes.contains(name.substring(dot + 1).toLowerCase)
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

    Workers.workOn(userFiles, 0) { userFile =>
      val uri = blob.uri(userFile(1).toString)
      val userCode = userFile(0).toString
      val fileName = userCode + "." + Strings.substringAfterLast(userFile(1).toString, ".")
      val localFile = new File(tmpDir + Files./ + fileName)
      HttpUtils.download(uri.toString, localFile)
      if (localFile.exists()) {
        avatarFiles.addOne(localFile)
        exists.addOne(userCode)
      }
    }

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
    Stream(zipFile, MediaTypes.zip, s"照片(${exists.size}人).zip").cleanup { () =>
      zipFile.delete()
      Files.travel(new File(tmpDir), f => f.delete())
      new File(tmpDir).delete()
    }
  }

}
