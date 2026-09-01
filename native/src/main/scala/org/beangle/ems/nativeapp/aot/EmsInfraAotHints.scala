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

package org.beangle.ems.nativeapp.aot

import org.beangle.commons.aot.AotPolicy
import org.beangle.ems.app.aot.EmsAotSupport

/** 第三方/基础设施类的 GraalVM 反射提示。
 *
 * 不属于 ems 业务包（app/core/portal/security）且引用外部/第三方类
 * （tomcat），无对应仓库模块，保留在 native 模块。
 * 是 ems 自身声明依赖或支撑框架的运行时反射面：
 *  - FreeMarker 模板支撑类（Tomcat 请求/响应门面）。
 *
 *  caffeine/jedis 相关注册已移至 beangle-cache 模块的 CacheAotHints；
 *  JDK 序列化类型注册已移至 beangle-serializer 模块的 SerializerAotHints；
 *  Hibernate 查询缓存注册已移至 beangle-data-hibernate 模块的 BeangleAotHints；
 *  分页模型 SinglePage 已移至 beangle-data-model 模块的 ModelAotHints；
 *  beanmeta.idx 资源已由 beangle-commons 的 resource-config.json 静态注册。
 */
class EmsInfraAotHints extends EmsAotSupport {

  override def registering(): Unit = {
    // RequestFacade/ResponseFacade 供 grid.ftl/iframe.ftl 访问 request 属性
    val loader = getClass.getClassLoader
    List("org.apache.catalina.connector.RequestFacade",
      "org.apache.catalina.connector.ResponseFacade"
    ) foreach { n =>
      try hints.registerType(Class.forName(n, false, loader), AotPolicy.default)
      catch { case _: Throwable => () }
    }
  }
}
