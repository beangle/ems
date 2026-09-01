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

import org.beangle.commons.aot.{AotHintRegistrar, AotPolicy}

/** ems 应用侧 AotHintRegistrar 的公共基类。
 *
 * 实体类（TestUser）由 AotPlugin 依据 beangle.xml 的 jpa mapping 自动注册
 * （allPublic*），代理类由 ProxyPlugin 生成；这里只补插件不覆盖的应用面。
 * 各 registrar 按"被注册类所在包/模块"分组（app/core/portal/security/第三方支撑），
 * 放在对应模块（app/portal/native），共享本基类提供的注册策略。Scala 3 枚举经
 * 库侧 AotHints.registerEnum（classOf 直引，注册枚举类/伴生/值类 + 序列化）。
 * 基类随 app 模块发布，portal/native 的 registrar 经模块依赖引用。
 *
 * 模板模型/DTO/tag 组件的 BeanMeta 一律由各模块 MetaRegistrar（meta-registrars.txt）
 * 固化进 beanmeta.idx：BeanInfos.get 命中索引后只经 public 方法/构造器重建访问器，
 * 因此只需默认策略（public 方法 + public 构造器），无需 declared 成员——native 下
 * declared 查询（getDeclaredMethods/getDeclaredFields）只有注册 declared 类别才可见。
 */
abstract class EmsAotSupport extends AotHintRegistrar {

  /** Spring 依赖注入策略：BindingRegistry 经 getDeclaredConstructors 发现构造器、
   *  getDeclaredFields/getDeclaredMethods 注入依赖。 */
  protected val servicePolicy = AotPolicy(Set(
    AotPolicy.Category.PublicConstructors,
    AotPolicy.Category.DeclaredMethods,
    AotPolicy.Category.DeclaredFields))

}
