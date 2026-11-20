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

package org.beangle.ems.app.oa

import org.beangle.commons.codec.binary.Base64
import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.net.http.{HttpUtils, Request}
import org.beangle.ems.app.{Ems, EmsApp}

/** 用户默认签名服务。
 *
 * 封装签名读取、保存的地址和 digest 计算，调用方不需要感知 HTTP 细节。
 */
object Signatures {

  private val DataUrlPrefix = "data:image/png;base64,"

  /** 读取用户默认签名，返回 data URL。 */
  def readDefault(userCode: String): Option[String] = {
    try {
      val response = HttpUtils.get(s"${baseUrl(userCode)}.png")
      response.content match {
        case bytes: Array[Byte] if response.isOk && bytes.nonEmpty => Some(toDataUrl(bytes))
        case _ => None
      }
    } catch {
      case _: Exception => None
    }
  }

  /** 保存用户默认签名，`bytes` 为图片内容。 */
  def saveDefault(userCode: String, bytes: Array[Byte]): Boolean = {
    saveDefault(userCode, toDataUrl(bytes))
  }

  /** 保存用户默认签名，`signature` 为 data URL。 */
  def saveDefault(userCode: String, signature: String): Boolean = {
    val appName = EmsApp.name
    val params = Seq(
      "appName" -> appName,
      "signature" -> signature,
      "digest" -> digest(appName, userCode))
    val response = HttpUtils.post(s"${baseUrl(userCode)}/default", Request.asForm(params))
    response.isOk
  }

  def toDataUrl(bytes: Array[Byte]): String = DataUrlPrefix + Base64.encode(bytes)

  private def digest(appName: String, userCode: String): String = {
    Digests.md5Hex(Ems.key + s"&appName=${appName}&userCode=${userCode}")
  }

  private def baseUrl(userCode: String): String = {
    s"${Ems.innerApi}/platform/oa/signatures/${userCode}"
  }
}
