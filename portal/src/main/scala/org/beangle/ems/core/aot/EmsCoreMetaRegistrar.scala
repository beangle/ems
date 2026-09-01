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

package org.beangle.ems.core.aot

import org.beangle.commons.bean.meta.MetaRegistrar
import org.beangle.ems.core.config.model.EmbedMode
import org.beangle.ems.core.log.model.AppLogEntry
import org.beangle.ems.core.oa.model.{FlowStatus, NoticeStatus}
import org.beangle.ems.core.security.model.SessionInfo
import org.beangle.ems.core.security.service.{AppMenus, DomainMenus, GroupMenus}

/** org.beangle.ems.core.* 非 JPA 实体的 BeanMeta 注册器（随 portal 模块发布）。
 *
 * 经 src/main/resources/META-INF/beangle/meta-registrars.txt 声明（与 beangle.xml
 * 的 jpa mapping/cdi module 合并生成 portal 的 beanmeta.idx）。这些类不在 jpa
 * mapping 清单里、也不在实体 idx 中，若只在 native 注册 public 方法，运行时
 * BeanInfos.get 回退 MetaLoader 时 getDeclaredMethods 取不到（GraalVM allPublicMethods
 * 不支撑 declared 查询）；register()（inline 宏静态 dig，classOf 直引）把 BeanMeta
 * 固化进 idx，运行时命中索引后仅需 public 成员，无需 fullPolicy 的 declared 注册：
 *  - 菜单域 JSON 序列化 DTO：DomainMenus/GroupMenus/AppMenus（MenuWS 菜单接口）；
 *  - 非实体模板会话模型：SessionInfo（JDBC 存储）与 AppLogEntry（日志模型父类，
 *    具体子类 BusinessLog/ErrorLog 已在 jpa mapping 中）。
 */
class EmsCoreMetaRegistrar extends MetaRegistrar {

  override def registering(): Unit = {
    register(
      classOf[DomainMenus], classOf[GroupMenus], classOf[AppMenus],
      classOf[SessionInfo], classOf[AppLogEntry])

    hints.registerEnum(classOf[EmbedMode])
    hints.registerEnum(classOf[FlowStatus])
    hints.registerEnum(classOf[NoticeStatus])

    /** 注册内部 bean LogDbAppender。
     *
     * LogModule 经 `property("appenders", list(classOf[LogDbAppender]))` 绑定，list() 非
     * bind 宏路径，构建期不注册 BeanMeta/反射；BindingRegistry 对具体类 BeanInfos.get
     * 走 MetaLoader 回退，并需 getDeclaredConstructors 发现 (LogPersistBuffer) 构造器
     * 以自动注入构造参数，因此保留反射注册。
     */
    hints.registerType(classOf[org.beangle.ems.core.log.service.LogDbAppender])
  }
}
