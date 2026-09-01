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

package org.beangle.ems.nativeapp

import org.beangle.data.dao.OqlBuilder
import org.beangle.data.hibernate.HibernateEntityDao
import org.beangle.data.hibernate.LocalSessionFactoryBean
import org.beangle.commons.collection.page.PageLimit

/** GraalVM native-image integration test application for EMS.
 *
 * Performs Hibernate CRUD operations and prints SQL output.
 * Usage: java -cp ... org.beangle.ems.nativeapp.NativeApp
 */
object NativeApp {

  def main(args: Array[String]): Unit = {
    try {
      doMain(args)
    } catch {
      case e: Throwable =>
        System.err.println(s"FATAL: ${e.getClass.getName}: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  private def doMain(args: Array[String]): Unit = {
    val profile = sys.props.getOrElse("ems.profile", "default")
    println(s"=== Beangle EMS Native-Image Test (profile: $profile) ===")
    println()

    // 1. Setup H2 in-memory database
    val jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    val ds = new org.h2.jdbcx.JdbcDataSource()
    ds.setURL(jdbcUrl)
    ds.setUser("sa")
    ds.setPassword("")

    // 2. Build Hibernate SessionFactory
    val builder = new LocalSessionFactoryBean(ds)
    builder.ormLocation = "classpath*:beangle.xml"
    builder.properties.put("hibernate.show_sql", "true")
    builder.properties.put("hibernate.hbm2ddl.auto", "create")
    builder.properties.put("hibernate.cache.use_second_level_cache", "true")
    builder.properties.put("hibernate.cache.use_query_cache", "true")
    builder.properties.put("hibernate.cache.region.factory_class", "jcache")
    try {
      builder.init()
    } catch {
      case e: Exception =>
        System.err.println(s"BUILDER INIT FAILED: ${e.getClass.getName}: ${e.getMessage}")
        e.printStackTrace()
        return
    }
    val sf = builder.getObject

    // 3. Create DAO
    val entityDao = new HibernateEntityDao(sf)
    entityDao.init()

    val session = sf.getCurrentSession
    val tx = session.beginTransaction()

    try {
      // 4. Test basic CRUD operations
      println("--- INSERT ---")
      val user = new TestUser
      user.code = "admin"
      user.name = "Administrator"
      user.updatedAt = java.time.Instant.now()
      entityDao.saveOrUpdate(user)
      session.flush()
      println()

      // 5. SELECT
      println("--- SELECT ---")
      val query = OqlBuilder.from(classOf[TestUser], "u")
      query.where("u.code = :code", "admin")
      val users = entityDao.search(query)
      println(s"Found ${users.size} user(s) with code 'admin':")
      users.foreach(u => println(s"  [${u.code}] ${u.name} (id=${u.id})"))
      println()

      // 6. UPDATE
      println("--- UPDATE ---")
      user.name = "Super Administrator"
      user.updatedAt = java.time.Instant.now()
      entityDao.saveOrUpdate(user)
      session.flush()
      println()

      // 7. DELETE
      println("--- DELETE ---")
      entityDao.remove(user)
      session.flush()
      val remaining = entityDao.search(OqlBuilder.from(classOf[TestUser], "u"))
      println(s"After delete: ${remaining.size} user(s) remaining")
      println()

      tx.commit()
      println("=== All operations completed successfully ===")
    } catch {
      case e: Exception =>
        tx.rollback()
        println(s"ERROR: ${e.getMessage}")
        e.printStackTrace()
        System.exit(1)
    } finally {
      sf.close()
    }
  }
}
