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

package org.beangle.ems.portal.admin.action.config

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.{Strings, Throwables}
import org.beangle.data.dao.OqlBuilder
import org.beangle.data.jdbc.engine.{Drivers, Engines, UrlFormat}
import org.beangle.ems.app.util.AesEncryptor
import org.beangle.ems.core.config.model.{Credential, Db}
import org.beangle.ems.core.config.service.{CredentialService, DomainService}
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.action.RestfulAction

import java.sql.DriverManager

class DbAction extends RestfulAction[Db] {

  override def simpleEntityName = "db"

  var domainService: DomainService = _

  var credentialService: CredentialService = _

  override protected def getQueryBuilder: OqlBuilder[Db] = {
    val builder = super.getQueryBuilder
    builder.where("db.domain=:domain", domainService.getDomain)
    builder
  }

  def testSetting(): View = {
    val entityType = entityDao.domain.getEntity(entityClass).get
    val entities = getModels[Db](entityType, getIds(simpleEntityName, entityType.id.clazz))
    put("credentials", entityDao.getAll(classOf[Credential]))
    put("datasource", entities.head)
    forward()
  }

  override def saveAndRedirect(db: Db): View = {
    val keys = Collections.newSet[String]
    get("properties") foreach { p =>
      var props = Strings.replace(p, "\r", "\n")
      props = Strings.replace(p, ";", "\n")
      val propArray = Strings.split(props, "\n")
      propArray foreach { kv =>
        val k = Strings.substringBefore(kv, "=").trim
        val v = Strings.substringAfter(kv, "=").trim
        keys.add(k)
        db.properties.put(k, v)
      }
    }
    val removedKey = db.properties.keys.toSet -- keys
    db.properties.subtractAll(removedKey)
    db.domain = domainService.getDomain
    super.saveAndRedirect(db)
  }

  def test(): View = {
    var username = get("username", "")
    var password = get("password", "")
    put("credentials", credentialService.getAll())
    val entityType = entityDao.domain.getEntity(entityClass).get
    val entities = getModels[Db](entityType, getIds(simpleEntityName, entityType.id.clazz))
    val cfg = entities.head

    val useCredential = getBoolean("use_credential", false)
    try {
      if (useCredential) {
        val credential = entityDao.get(classOf[Credential], getIntId("credential"))
        val key = get("key").orNull
        username = credential.username
        password = new AesEncryptor(key).decrypt(credential.password)
      }
      if (Strings.isNotBlank(username) && Strings.isNotBlank(password)) {

        val url =
          cfg.url match {
            case None =>
              val driverInfo = Drivers.get(cfg.driver).get
              Class.forName(driverInfo.className)
              val format = driverInfo.urlformats.head
              val params =
                Map("host" -> cfg.serverName, "port" -> cfg.portNumber.toString, "database_name" -> cfg.databaseName,
                  "server_name" -> cfg.serverName) ++ cfg.properties
              "jdbc:" + cfg.driver + ":" + new UrlFormat(format).fill(params)
            case Some(u) => u
          }
        val conn = DriverManager.getConnection(url, username, password)
        val msg = new StringBuilder
        val meta = conn.getMetaData
        val version = s"${meta.getDatabaseMajorVersion}.${meta.getDatabaseMinorVersion}"
        msg.append("DatabaseProductName:").append(meta.getDatabaseProductName)
        msg.append("<br>DatabaseProductVersion:").append(meta.getDatabaseProductVersion)
        msg.append("<br>DatabaseVersion:").append(version)
        val engine = Engines.forName(meta.getDatabaseProductName, version)
        msg.append("<br>Supported Engine:").append(if null == engine then "NULL" else s"${engine.name} ${engine.version}")

        put("msg", msg.toString)
        conn.close()
        put("passed", true)

      }
    } catch {
      case t: Throwable =>
        put("msg", Throwables.stackTrace(t))
        put("passed", false)
    }
    forward()
  }

  protected override def editSetting(entity: Db): Unit = {
    val drivers = Map("postgresql" -> "PostgreSQL", "oracle" -> "Oracle", "mysql" -> "MySQL",
      "db2" -> "DB2", "sqlserver" -> "Microsoft SQL Server", "jtds" -> "Jtds(SQL Server)")
    put("drivers", drivers)
  }

}
