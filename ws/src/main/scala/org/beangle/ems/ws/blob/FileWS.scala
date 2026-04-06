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

package org.beangle.ems.ws.blob

import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.json.{Json, JsonObject}
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.Ems
import org.beangle.ems.core.blob.model.{BlobMeta, Profile}
import org.beangle.ems.core.config.model.Domain
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.webmvc.annotation.{body, response}
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}

class FileWS(entityDao: EntityDao) extends ActionSupport, ServletSupport {

  var domainService: DomainService = _
  var appService: AppService = _

  private def getProfile(domain: Domain, profileDir: String): Profile = {
    val q = OqlBuilder.from(classOf[Profile], "p")
    q.where("p.domain=:domain", domain)
    q.where("p.base=:base", profileDir)
    q.cacheable()
    entityDao.unique(q)
  }

  /** 登记一条 Blob 元数据（文件已落到存储后由调用方上报）。
   *
   * 请求体为 JSON 字符串；可选在查询参数中携带 `digest`，其值应为 `MD5(app.secret + jsonBody 原始字符串)`，
   * 用于校验请求完整性（与具体校验逻辑以服务端实现为准）。
   *
   * `jsonBody` 字段说明：
   *  - `appName`：应用名称，用于解析 `App` 与 `secret`（做 digest 等）
   *  - `profile`：存储配置名（对应域下 `Profile.base`）
   *  - `owner`：文件属主标识
   *  - `name`：展示名
   *  - `fileSize`：字节数
   *  - `sha`：内容摘要
   *  - `mediaType`：MIME 类型
   *  - `filePath`：在 profile 下的相对路径
   *  - `updatedAt`：更新时间（ISO-8601 瞬时时间字符串，如 `2025-01-01T12:00:00Z`）
   *
   * `jsonBody` 样例：
   * {{{
   * {
   *   "appName": "my-app",
   *   "profile": "/platform",
   *   "owner": "user-1001",
   *   "name": "report.pdf",
   *   "fileSize": 102400,
   *   "sha": "a1b2c3d4e5f6...",
   *   "mediaType": "application/pdf",
   *   "filePath": "/2025/01/report.pdf",
   *   "updatedAt": "2025-01-15T08:30:00Z"
   * }
   * }}}
   *
   * @param jsonBody 原始 JSON 字符串（HTTP body）
   * @return `code` / `msg` 的 JsonObject
   */
  @response
  def register(@body jsonBody: String): JsonObject = {
    val rs = new JsonObject()
    val jo = Json.parseObject(jsonBody)
    val appName = jo.getString("appName", "--")
    val appOpt = appService.getApp(appName)
    if (appOpt.isEmpty) {
      rs.add("code", 500)
      rs.add("msg", s"cannot find app ${appName}")
    } else {
      val app = appOpt.get
      if (get("digest").contains(Digests.md5Hex(Ems.key + jsonBody))) {
        val meta = new BlobMeta
        meta.domain = domainService.getDomain
        meta.profile = getProfile(meta.domain, jo.getString("profile"))

        meta.owner = jo.getString("owner")
        meta.name = jo.getString("name")
        meta.fileSize = jo.getInt("fileSize")
        meta.sha = jo.getString("sha")
        meta.mediaType = jo.getString("mediaType")
        meta.filePath = jo.getString("filePath")
        meta.updatedAt = jo.getInstant("updatedAt")
        entityDao.saveOrUpdate(meta)
        rs.add("code", 200)
        rs.add("msg", "ok")
      } else {
        rs.add("code", 500)
        rs.add("msg", s"digest verify failed")
      }
    }
    rs
  }

  /** 按域 + profile + filePath 取消已登记的 Blob 元数据（不删除物理文件，仅删库中记录）。
   *
   * 请求体为 JSON 字符串；可选查询参数 `digest` 含义同 [[register]]。
   *
   * `jsonBody` 字段说明：
   *  - `appName`：应用名称
   *  - `profile`：存储配置名
   *  - `filePath`：与登记时一致的相对路径
   *
   * `jsonBody` 样例：
   * {{{
   * {
   *   "appName": "my-app",
   *   "profile": "/platform",
   *   "filePath": "/2025/01/report.pdf"
   * }
   * }}}
   *
   * @param jsonBody 原始 JSON 字符串（HTTP body）
   * @return `code` / `msg` 的 JsonObject（未匹配到记录时仍可能返回 200 且 msg 为 not found，以当前实现为准）
   */
  @response
  def unregister(@body jsonBody: String): JsonObject = {
    val rs = new JsonObject()
    val jo = Json.parseObject(jsonBody)
    val appName = jo.getString("appName", "--")
    val appOpt = appService.getApp(appName)
    if (appOpt.isEmpty) {
      rs.add("code", 500)
      rs.add("msg", s"cannot find app ${appName}")
    } else {
      val app = appOpt.get
      if (get("digest").contains(Digests.md5Hex(Ems.key + jsonBody))) {
        val meta = new BlobMeta
        val domain = domainService.getDomain
        val profile = getProfile(domain, jo.getString("profile"))
        val filePath = jo.getString("filePath")
        val q = OqlBuilder.from(classOf[BlobMeta], "m")
        q.where("m.domain=:domain and m.profile=:profile and m.filePath=:filePath", domain, profile, filePath)
        val metas = entityDao.search(q)
        rs.add("code", 200)
        if (metas.size == 1) {
          entityDao.remove(metas)
          rs.add("msg", "ok")
        } else {
          rs.add("code", 200)
          rs.add("msg", "not found")
        }
      } else {
        rs.add("code", 500)
        rs.add("msg", s"digest verify failed")
      }
    }
    rs
  }
}
