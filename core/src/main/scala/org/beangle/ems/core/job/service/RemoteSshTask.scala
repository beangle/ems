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

package org.beangle.ems.core.job.service

import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.{ClientChannel, ClientChannelEvent}
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier
import org.apache.sshd.common.channel.Channel
import org.apache.sshd.common.util.security.SecurityUtils

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.time.Duration
import java.util as ju
import java.util.concurrent.TimeUnit

/**
 * 使用 Apache MINA SSHD 执行远程 SSH 命令的任务。仅支持密钥认证，忽略主机 key 校验。
 *
 * @param target 目标，格式 user@host 或 user@host:port，port 可省略（默认 22）
 *
 */
class RemoteSshTask(val target: String) extends ShellTask {

  /** 私钥文件路径（如 ~/.ssh/id_rsa） */
  var keyPath: Path = _

  /** 命令执行超时，默认 30 秒 */
  var timeout: Duration = Duration.ofSeconds(30)

  /** 执行远程命令
   *
   * @param command 要执行的命令
   * @return
   */
  override def execute(command: String): (Int, String) = {
    if (command == null || command.isBlank) {
      return (-1, "command is empty")
    }
    val (user, host, port) = parseTarget(target)
    val client = SshClient.setUpDefaultClient()
    client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE)
    try {
      client.start()
      val session = client.connect(user, host, port)
        .verify(timeout.toSeconds, TimeUnit.SECONDS)
        .getSession

      try {
        val parser = SecurityUtils.getKeyPairResourceParser
        val keys = parser.loadKeyPairs(null, keyPath, null)
        val iter = keys.iterator()
        if (!iter.hasNext) {
          return (-1, "no key pair loaded from: " + keyPath)
        }
        session.addPublicKeyIdentity(iter.next())
        session.auth().verify(timeout.toSeconds, TimeUnit.SECONDS)

        val out = new ByteArrayOutputStream
        val err = new ByteArrayOutputStream
        val channel = session.createChannel(Channel.CHANNEL_EXEC, command).asInstanceOf[ClientChannel]
        channel.setOut(out)
        channel.setErr(err)

        try {
          channel.open().verify(timeout.toSeconds, TimeUnit.SECONDS)
          channel.waitFor(ju.EnumSet.of(ClientChannelEvent.CLOSED), timeout.toMillis)
          val exitStatus = channel.getExitStatus
          val outStr = new String(out.toByteArray, StandardCharsets.UTF_8)
          val errStr = new String(err.toByteArray, StandardCharsets.UTF_8)
          (exitStatus, if (errStr.nonEmpty) outStr + "\n" + errStr else outStr)
        } finally {
          channel.close(false)
        }
      } finally {
        session.close()
      }
    } finally {
      client.stop()
    }
  }

  private def parseTarget(t: String): (String, String, Int) = {
    if (t == null || t.isBlank)
      throw new IllegalArgumentException("target must be user@host or user@host:port")
    val at = t.indexOf('@')
    if (at <= 0)
      throw new IllegalArgumentException("invalid target format: " + t)
    val user = t.substring(0, at)
    val rest = t.substring(at + 1)
    val lastColon = rest.lastIndexOf(':')
    if (lastColon > 0 && rest.substring(lastColon + 1).forall(_.isDigit)) {
      val host = rest.substring(0, lastColon)
      val port = rest.substring(lastColon + 1).toInt
      (user, host, port)
    } else {
      (user, rest, 22)
    }
  }

}
