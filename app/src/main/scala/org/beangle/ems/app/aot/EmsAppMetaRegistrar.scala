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

package org.beangle.ems.app.aot

import org.beangle.commons.bean.meta.MetaRegistrar
import org.beangle.ems.app.Ems
import org.beangle.ems.app.log.Level
import org.beangle.ems.app.oa.Flows
import org.beangle.ems.app.web.App
import org.beangle.ems.app.web.NavContext
import org.beangle.ems.app.web.tag.AvatarTag
import org.beangle.ems.app.web.tag.EmsModels
import org.beangle.ems.app.web.tag.UserTag

/** org.beangle.ems.app.* 的 BeanMeta 注册器（随 app 模块发布）。
 *
 * 经 src/main/resources/META-INF/beangle/meta-registrars.txt 声明：MetaPlugin 在
 * 构建期调用 register()（inline 宏静态 dig，classOf 直引）生成 beanmeta.idx 随 jar
 * 发布，运行时 BeanInfos.get 直接命中索引，不再回退 MetaLoader 反射——native 下
 * MetaLoader 依赖 getDeclaredMethods/getDeclaredFields（GraalVM 仅注册 public 方法
 * 时取不到），这是本 registrar 存在的根本原因。AotPlugin 同时读取
 * meta-registrars.txt，把 register() 经 addMetas 登记的默认策略（public 方法 +
 * public 构造器）写入 reflect-config：
 *  - 模板模型/标签库：NavContext/App/Ems 伴生及内嵌类型、EmsModels（BeansWrapper
 *    经 getMethods 内省，${nav.principal}/${ems.webapp} 取值的反射面）；
 *  - ems 自有 tag 组件：AvatarTag/UserTag（DefaultTagTemplateEngine 经
 *    getConstructor(ComponentContext) 实例化，Properties.set 经 BeanInfos 写属性，
 *    BeanMeta 入索引后仅需 public 成员，无需 declared 策略）；
 *  - OA 工作流 DTO：Flows 的 case class（Payload.toJson 经 JsonSerializer 序列化，
 *    入索引后同样只需 public 成员）；
 *  - 应用日志枚举 Level。
 */
class EmsAppMetaRegistrar extends MetaRegistrar {

  override def registering(): Unit = {
    register(
      classOf[NavContext], classOf[App],
      classOf[Ems.Org], classOf[Ems.Domain], classOf[Ems.Theme], classOf[Ems.type],
      classOf[EmsModels],
      classOf[AvatarTag], classOf[UserTag],
      classOf[Flows.Flow], classOf[Flows.Activity], classOf[Flows.User],
      classOf[Flows.Group], classOf[Flows.Task], classOf[Flows.Process],
      classOf[Flows.Comment], classOf[Flows.Attachment], classOf[Flows.Payload])
    // Scala 3 枚举（库侧 AotHints.registerEnum：枚举类/伴生/值类 + 序列化）
    hints.registerEnum(classOf[Level])
  }
}
