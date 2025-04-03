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

package org.beangle.ems.portal.action.admin.oa

import jakarta.servlet.http.Part
import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.codec.binary.Base64
import org.beangle.commons.collection.Collections
import org.beangle.commons.file.zip.Zipper
import org.beangle.commons.io.{Files, IOs}
import org.beangle.commons.lang.{Strings, SystemInfo}
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.model.{Credential, Db}
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.oa.model.Signature
import org.beangle.ems.core.user.model.{Depart, User}
import org.beangle.ems.core.user.service.UserService
import org.beangle.jdbc.ds.{AesEncryptor, DataSourceFactory}
import org.beangle.jdbc.engine.{Drivers, UrlFormat}
import org.beangle.jdbc.query.JdbcExecutor
import org.beangle.webmvc.support.action.RestfulAction
import org.beangle.webmvc.view.{Stream, View}

import java.io.{ByteArrayInputStream, File, FileOutputStream, InputStream}
import java.time.{Instant, LocalDate}

class SignatureAction extends RestfulAction[Signature] {

  var domainService: DomainService = _
  var userService: UserService = _

  override protected def indexSetting(): Unit = {
    super.indexSetting()
    put("categories", userService.getCategories())
    put("departs", entityDao.findBy(classOf[Depart], "org", domainService.getOrg))
  }

  override def search(): View = {
    val signatures = entityDao.search(getQueryBuilder)
    put("signatures", signatures)
    val blob = EmsApp.getBlobRepository(true)
    val paths = signatures.map { s => (s, blob.url(s.filePath)) }.toMap
    put("paths", paths)
    forward()
  }

  override def saveAndRedirect(s: Signature): View = {
    val parts = getAll("signature_file", classOf[Part])
    if (parts.nonEmpty && parts.head.getSize > 0) {
      val exists = entityDao.findBy(classOf[Signature], "user", s.user)
      val sig = if exists.isEmpty then s else exists.head
      sig.user = entityDao.get(classOf[User], sig.user.id)
      val part = parts.head
      val ext = Strings.substringAfterLast(part.getSubmittedFileName, ".")
      upload(sig, part.getInputStream, ext)
      entityDao.saveOrUpdate(sig)
    }
    super.saveAndRedirect(s)
  }

  private def upload(sig: Signature, is: InputStream, ext: String): Unit = {
    val user = sig.user
    val blob = EmsApp.getBlobRepository(true)
    if (Strings.isNotBlank(sig.filePath)) {
      blob.remove(sig.filePath)
    }
    val rs = blob.upload("/oa/signature/",
      is, user.code + "_" + LocalDate.now().toString + s".${ext}", user.code + " " + user.name)
    if (Strings.isEmpty(rs.mediaType)) {
      rs.mediaType = MediaTypes.get(ext, MediaTypes.ImagePng).toString
    } else {
      rs.mediaType = rs.mediaType
    }
    sig.filePath = rs.filePath
    sig.mediaType = rs.mediaType
    sig.fileSize = rs.fileSize
    sig.updatedAt = Instant.now
  }

  /** 批量下载
   *
   * @return
   */
  def download(): View = {
    val signatures = entityDao.find(classOf[Signature], getLongIds("signature"))
    val blob = EmsApp.getBlobRepository(true)
    val docRoot = new File(SystemInfo.tmpDir + "/signatures" + System.currentTimeMillis())
    docRoot.mkdirs()
    val innerFiles = Collections.newBuffer[File]
    signatures foreach { sig =>
      blob.url(sig.filePath) foreach { url =>
        val user = sig.user
        val fileName = user.code + "_" + user.name + "." + Strings.substringAfterLast(sig.mediaType, "/")
        val localFile = new File(docRoot.getAbsolutePath + Files./ + fileName)
        IOs.copy(url.openStream(), new FileOutputStream(localFile))
        if (localFile.exists()) innerFiles.addOne(localFile)
      }
    }
    val zipFile = new File(SystemInfo.tmpDir + Files./ + s"签名${innerFiles.size}人.zip")
    Zipper.zip(docRoot, innerFiles, zipFile, "utf-8")
    Files.travel(docRoot, f => f.delete())
    docRoot.delete()
    Stream(zipFile).cleanup { () => zipFile.delete() }
  }

  def uploadDbSetting(): View = {
    val domain = domainService.getDomain
    val dbs = entityDao.findBy(classOf[Db], "domain", domain)
    val credentials = entityDao.findBy(classOf[Credential], "domain", domain)
    put("dbs", dbs)
    put("credentials", credentials)
    forward()
  }

  def uploadFromDb(): View = {
    val db = entityDao.get(classOf[Db], getIntId("db"))
    val credential = entityDao.get(classOf[Credential], getIntId("credential"))
    val sql = get("sql").get
    val key = get("key").orNull
    val username = credential.username
    val password = new AesEncryptor(key).decrypt(credential.password)

    val dsf = new DataSourceFactory
    dsf.user = username
    dsf.password = password
    dsf.driver = db.driver
    dsf.url = db.url match
      case None =>
        val driverInfo = Drivers.get(db.driver).get
        Class.forName(driverInfo.className)
        val format = driverInfo.urlformats.head
        val params =
          Map("host" -> db.serverName, "port" -> db.portNumber.toString, "database_name" -> db.databaseName,
            "server_name" -> db.serverName) ++ db.properties
        "jdbc:" + db.driver + ":" + new UrlFormat(format).fill(params)
      case Some(url) => url
    dsf.props.put("url", dsf.url)
    dsf.init()
    val ds = dsf.result

    val jdbcExecutor = new JdbcExecutor(ds)
    println(sql)
    val dataIter = jdbcExecutor.iterate(sql)
    var count = 0
    dataIter.foreach { data =>
      val code = data(0).toString
      var contents = data(1).toString
      userService.get(code) foreach { user =>
        val exists = entityDao.findBy(classOf[Signature], "user", user)
        val sig = if exists.isEmpty then new Signature(user) else exists.head
        contents = Strings.substringAfter(contents, ";base64,")
        val bytes = Base64.decode(contents)
        upload(sig, new ByteArrayInputStream(bytes), "png")
        entityDao.saveOrUpdate(sig)
        count += 1
      }
    }
    dsf.destroy()
    redirect("search", s"导入更新${count}个签名")
  }
}
