package com.github.curzonj

import org.eclipse.jetty.websocket.servlet.{ServletUpgradeResponse, ServletUpgradeRequest, WebSocketCreator}
import scala.collection.mutable

class WebsocketManager extends WebSocketCreator {

  var list: mutable.HashSet[WebsocketHandle] = new mutable.HashSet[WebsocketHandle]()

  override def createWebSocket(req: ServletUpgradeRequest, resp: ServletUpgradeResponse): WebsocketHandle = {
    val handle = new WebsocketHandle(this)
    storeHandle(handle)
    handle
  }

  def remove(handle: WebsocketHandle): Unit = {
    list.remove(handle)
  }

  def send(str: String): Unit = synchronized {
    list.foreach { f =>
      f.send(str)
    }
  }

  def storeHandle(handle: WebsocketHandle): Unit = synchronized {
    list += handle
  }
}
