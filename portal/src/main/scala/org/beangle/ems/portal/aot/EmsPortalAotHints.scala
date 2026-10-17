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

package org.beangle.ems.portal.aot

import org.beangle.commons.aot.{AotHintRegistrar, AotPolicy}

/** org.beangle.ems.portal.* 包的 GraalVM 反射提示。
 *
 * 随 portal 模块发布（AotPlugin 按模块独立收集，native 构建时各 jar 配置自动
 * 合并）。除 portal 辅助类 DomainSupport（Spring 依赖注入反射）外，还登记本镜像
 * 对外提供但不在本模块的会话模型。
 */
class EmsPortalAotHints extends AotHintRegistrar {

  override def registering(): Unit = {
    hints.registerType(classOf[org.beangle.ems.portal.action.admin.DomainSupport],AotPolicy.bean)
    // 会话模型：ids 的 `/session/{id}` 是 webmvc `@response` 接口，文本格式（json）
    // 走 BeanMarshaller → BeanInfos 反射逐属性取值。native 下类未登记时 getMethods
    // 返回空 → 属性集为空 → 序列化成 `{}`（现网 CAS 的 JSON 正是空对象，protobuf 才完整）。
    // AotPolicy.default（public 方法 + public 构造器）即 MetaLoaderLite 在 native 下的所需粒度。
    hints.registerType(
      classOf[org.beangle.security.session.DefaultSession],
      classOf[org.beangle.security.session.Session.Agent])
    // AvatarWS 默认头像：由 classpath 直读，native 下需显式登记才进镜像
    hints.registerPattern("org/beangle/ems/ws/default_avatar.jpg")
    // 隐私安全政策页（SafetyAction 提供），同上
    hints.registerPattern("cas/safety.html")
  }
}
