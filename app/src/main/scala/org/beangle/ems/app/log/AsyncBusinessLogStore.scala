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

package org.beangle.ems.app.log

import org.beangle.commons.bean.{Disposable, Initializing}
import org.beangle.commons.logging.Logging
import org.beangle.ems.app.log.AsyncBusinessLogStore.Worker

import java.util as ju
import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

object AsyncBusinessLogStore {
  class Worker(store: AsyncBusinessLogStore) extends Thread {
    var stopped: Boolean = false

    override def run(): Unit = {
      while (store.started && !stopped) {
        try {
          val elements = new ju.ArrayList[BusinessLogEntry]
          val e0 = store.queue.take()
          elements.add(e0)
          store.queue.drainTo(elements)
          val iter = elements.iterator()
          while (iter.hasNext) {
            val e = iter.next()
            store.appenders foreach (ap => ap.append(e))
          }
        } catch {
          case _: InterruptedException => stopped = true
        }
      }
    }
  }
}

class AsyncBusinessLogStore extends BusinessLogStore with Initializing with Disposable {
  var appenders: List[Appender] = _
  var queueSize: Int = 512
  private var queue: BlockingQueue[BusinessLogEntry] = _
  private var started: Boolean = _
  private var worker: Worker = _

  override def publish(entry: BusinessLogEntry): Unit = {
    queue.offer(entry)
  }

  override def init(): Unit = {
    queue = new ArrayBlockingQueue[BusinessLogEntry](queueSize)
    started = true
    worker = new Worker(this)
    worker.setDaemon(true)
    worker.setName("AsyncBusinessLogStore-Worker")
    worker.start()
  }

  override def destroy(): Unit = {
    worker.interrupt()
  }
}
