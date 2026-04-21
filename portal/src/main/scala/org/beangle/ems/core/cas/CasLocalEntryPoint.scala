package org.beangle.ems.core.cas

import jakarta.servlet.http.HttpServletRequest
import org.beangle.commons.lang.Strings
import org.beangle.security.authc.AuthenticationException
import org.beangle.security.realm.cas.CasConfig
import org.beangle.security.web.UrlEntryPoint
import org.beangle.web.servlet.url.UrlBuilder

import java.net.URLEncoder
import java.util as ju

class CasLocalEntryPoint(url: String) extends UrlEntryPoint(url) {
  /**
   * Allows subclasses to modify the login form URL that should be applicable
   * for a given request.
   */
  protected override def determineUrl(req: HttpServletRequest, ae: AuthenticationException): String = {
    if (this.url.contains("${goto}")) {
      Strings.replace(this.url, "${goto}", UrlBuilder.url(req))
    } else {
      val service = serviceUrl(req)
      val sb = new StringBuilder(url)
      sb.append(if (url.indexOf("?") != -1) "&" else "?")
      sb.append(CasConfig.ServiceName + "=" + URLEncoder.encode(service, "UTF-8"))
      sb.toString()
    }
  }

  private def serviceUrl(req: HttpServletRequest): String = {
    val buffer = new StringBuilder()
    val serverName = CasConfig.getLocalServer(req)
    buffer.append(serverName).append(req.getRequestURI)
    val queryString = req.getQueryString
    if (Strings.isNotBlank(queryString)) {
      val parts = Strings.split(queryString, '&')
      //这里的排序，保证请求和验证的使用的service是一样的
      ju.Arrays.sort(parts.asInstanceOf[Array[AnyRef]])
      val paramBuf = new StringBuilder
      parts foreach { part =>
        val equIdx = part.indexOf('=')
        if (equIdx > 0) {
          val key = part.substring(0, equIdx)
          paramBuf.append("&").append(key).append(part.substring(equIdx))
        }
      }
      if (paramBuf.nonEmpty) {
        paramBuf.setCharAt(0, '?')
        buffer.append(paramBuf)
      }
    }
    buffer.toString
  }

}
