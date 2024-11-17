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

package org.beangle.ems.portal.helper

import org.beangle.commons.bean.Properties
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.EntityDao
import org.beangle.ems.core.security.service.ProfileService
import org.beangle.ems.core.user.model.{Dimension, IProfile, Profile}
import org.beangle.ems.core.user.service.impl.CsvDataResolver
import org.beangle.ems.core.user.service.{DataResolver, DimensionService}
import org.beangle.security.Securities
import org.beangle.webmvc.context.{ActionContext, Params}

class ProfileHelper(entityDao: EntityDao, profileService: ProfileService, dimensionService: DimensionService) {
  var dataResolver: DataResolver = CsvDataResolver

  /**
   * 查看限制资源界面
   */
  def populateInfo(profiles: Seq[_ <: IProfile]): Unit = {
    val fieldMaps = new collection.mutable.HashMap[String, Map[String, AnyRef]]
    for (profile <- profiles) {
      val aoDimensions = new collection.mutable.HashMap[String, AnyRef]
      for ((field, value) <- profile.properties) {
        val fieldName = field.name
        if (Strings.isNotEmpty(value)) {
          if (field.source.startsWith("text")) {
            aoDimensions.put(fieldName, value)
          } else if (value.equals("*")) {
            aoDimensions.put(fieldName, "不限")
          } else {
            aoDimensions.put(fieldName, getProperty(profile, field))
          }
        }
      }
      fieldMaps.put(Properties.get[Any](profile, "id").toString, aoDimensions.toMap)
    }
    ActionContext.current.attribute("profiles", profiles)
    ActionContext.current.attribute("fieldMaps", fieldMaps)
  }

  def fillEditInfo(profile: IProfile, isAdmin: Boolean): Unit = {
    //val me = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    val mngDimensions = new collection.mutable.HashMap[String, Object]
    val userDimensions = new collection.mutable.HashMap[String, Object]

    val myProfiles = entityDao.findBy(classOf[Profile], "user.code", List(Securities.user))
    val ignores = getIgnoreDimensions(myProfiles)
    ActionContext.current.attribute("ignoreDimensions", ignores)
    val userIgnoreDimensions = new collection.mutable.HashSet[Dimension]
    ActionContext.current.attribute("userIgnoreDimensions", userIgnoreDimensions)
    val fields = dimensionService.getAll()
    ActionContext.current.attribute("fields", fields)
    for (field <- fields) {
      var mngDimensionValues = new collection.mutable.ListBuffer[Any]
      mngDimensionValues ++= profileService.getDimensionValues(field)
      if (!isAdmin) {
        mngDimensionValues --= getMyProfileValues(myProfiles, field)
      } else ignores += field

      var fieldValue = ""
      profile.getProperty(field) foreach { p => fieldValue = p }
      if ("*".equals(fieldValue)) userIgnoreDimensions.add(field)

      mngDimensions.put(field.name, mngDimensionValues)
      if (null == field.source) {
        userDimensions.put(field.name, fieldValue)
      } else {
        val p = getProperty(profile, field)
        if (null != p) userDimensions.put(field.name, p)
      }
    }
    ActionContext.current.attribute("mngDimensions", mngDimensions)
    ActionContext.current.attribute("userDimensions", userDimensions)
    ActionContext.current.attribute("profile", profile)
  }

  private def getMyProfileValues(profiles: Seq[IProfile], field: Dimension): collection.Seq[AnyRef] = {
    val values = new collection.mutable.ListBuffer[AnyRef]
    for (profile <- profiles) {
      profile.getProperty(field) foreach { _ =>
        if (field.multiple) {
          values ++= getProperty(profile, field).asInstanceOf[Seq[AnyRef]]
        } else {
          values += getProperty(profile, field)
        }
      }
    }
    values
  }

  private def getIgnoreDimensions(profiles: Seq[IProfile]): collection.mutable.Set[Dimension] = {
    val ignores = new collection.mutable.HashSet[Dimension]
    for (profile <- profiles) {
      for ((field, value) <- profile.properties) {
        if ("*".equals(value)) ignores.add(field)
      }
    }
    ignores
  }

  private def getProperty(profile: IProfile, field: Dimension): AnyRef = {
    profile.getProperty(field) match {
      case Some(p) =>
        if ("*" == p) {
          profileService.getDimensionValues(field)
        } else {
          field.keyName match {
            case None => Strings.split(p, ",").toList
            case Some(keyname) =>
              var values = profileService.getDimensionValues(field)
              val myValues = Strings.split(p, ",").toSet
              values = values.filter { v =>
                myValues.contains(Properties.get(v, keyname))
              }
              values
          }
        }
      case None => null
    }
  }

  def populateSaveInfo(profile: IProfile, isAdmin: Boolean): Unit = {
    val myProfiles = profileService.getProfiles(Securities.user)
    val ignoreDimensions = getIgnoreDimensions(myProfiles)
    for (field <- dimensionService.getAll()) {
      val values = Params.getAll(field.name).asInstanceOf[Iterable[String]]
      if ((ignoreDimensions.contains(field) || isAdmin) && Params.getBoolean("ignoreDimension" + field.id).getOrElse(false)) {
        profile.setProperty(field, "*")
      } else {
        if (null == values || values.isEmpty) {
          profile.setProperty(field, null)
        } else {
          profile.setProperty(field, Strings.join(values.toSeq: _*))
        }
      }
    }
  }
}
