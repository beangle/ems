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

package org.beangle.ems.ws.oa

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.codec.binary.Base64
import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.{Ems, EmsApp}
import org.beangle.ems.app.log.{BusinessLogEvent, BusinessLogger, Level}
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.oa.model.Signature
import org.beangle.ems.core.user.model.User
import org.beangle.web.servlet.util.RequestUtils
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.{Status, View}

import java.io.ByteArrayInputStream
import java.time.{Instant, LocalDate}
import scala.compiletime.uninitialized

/** 查询用户的签名文件
 *
 * @param entityDao
 */
class SignatureWS(entityDao: EntityDao) extends ActionSupport, ServletSupport {
  var domainService: DomainService = uninitialized
  var appService: AppService = uninitialized
  var businessLogger: BusinessLogger = uninitialized

  @mapping("{userCode}")
  def info(@param("userCode") userCode: String): View = {
    loadSignaturePath(userCode) match {
      case Some(path) =>
        val p = EmsApp.getBlobRepository().uri(path).toString
        response.addHeader("Access-Control-Allow-Origin", "*")
        redirect(to(p), "")
      case None => Status(404)
    }
  }

  /** 保存用户默认签名，供受信应用在用户授权提交签名时调用。
   *
   * 请求参数 `appName` 标识调用应用，服务端会校验其存在；`digest` 为
   * `MD5(Ems.key + "&appName=" + app.name + "&userCode=" + userCode)`。
   */
  @mapping(value = "{userCode}/default", methods = "post")
  def saveDefault(@param("userCode") userCode: String): View = {
    val appName = get("appName", "").trim
    if (Strings.isBlank(appName) || Strings.isBlank(userCode)) return Status.BadRequest
    val appOpt = appService.getApp(appName)
    if (appOpt.isEmpty) return Status.Forbidden
    val app = appOpt.get

    val digest = Digests.md5Hex(Ems.key + s"&appName=${app.name}&userCode=${userCode}")
    if (!get("digest").contains(digest)) return Status.Forbidden

    val signature = get("signature", "")
    if (Strings.isBlank(signature)) return Status.BadRequest
    val bytes = Base64.decode(Strings.substringAfter(signature, ";base64,"))
    if (bytes.isEmpty) return Status.BadRequest

    val userQuery = OqlBuilder.from(classOf[User], "u")
    userQuery.where("u.org=:org", domainService.getOrg)
    userQuery.where("u.code=:code", userCode)
    val userOpt = entityDao.search(userQuery).headOption
    if (userOpt.isEmpty) return Status.NotFound
    val user = userOpt.get

    val sig = entityDao.findBy(classOf[Signature], "user", user).headOption.getOrElse(new Signature(user))
    val blob = EmsApp.getBlobRepository(true)
    if (Strings.isNotBlank(sig.filePath)) {
      blob.remove(sig.filePath)
    }
    val meta = blob.upload("/oa/signature/",
      new ByteArrayInputStream(bytes),
      s"${user.code}_${LocalDate.now()}.png",
      s"${user.code} ${user.name}")
    sig.filePath = meta.filePath
    sig.mediaType = if (Strings.isEmpty(meta.mediaType)) MediaTypes.png.toString else meta.mediaType
    sig.fileSize = meta.fileSize
    sig.updatedAt = Instant.now
    entityDao.saveOrUpdate(sig)
    recordModification(app, user, sig)
    Status.Ok
  }

  private def recordModification(app: App, user: User, sig: Signature): Unit = {
    val event = new BusinessLogEvent()
    event.appName = app.name
    event.operator = user.code
    event.operateAt = Instant.now
    val appTitle = if (Strings.isBlank(app.title)) app.name else app.title
    event.summary = Strings.abbreviate(s"${appTitle}修改了${user.code}的默认签名", 1000)
    val details = s"appName=${app.name}, appTitle=${appTitle}, userCode=${user.code}, " +
      s"filePath=${sig.filePath}, fileSize=${sig.fileSize}, mediaType=${sig.mediaType}"
    event.details = Strings.abbreviate(details, 4000)
    event.resources = Strings.abbreviate(s"oa_signatures:${user.code}", 300)
    event.ip = Strings.abbreviate(RequestUtils.getIpAddr(request), 50)
    event.agent = Strings.abbreviate(Option(request.getHeader("User-Agent")).getOrElse(""), 100)
    event.entry = Strings.abbreviate(request.getRequestURI, 100)
    event.level = Level.Info
    businessLogger.publish(event)
  }

  private def loadSignaturePath(userCode: String): Option[String] = {
    val query = OqlBuilder.from[String](classOf[Signature].getName, "a")
    query.where("a.user.org=:org", domainService.getOrg)
    query.where("a.user.code = :code", userCode)
    query.select("a.filePath")
    entityDao.search(query).headOption
  }

}
