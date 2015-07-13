package com.github.curzonj

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketListener
import org.eclipse.jetty.websocket.api.{Session, WebSocketListener}

class WebsocketHandle(manager: WebsocketManager) extends WebSocketListener {

  var outbound: Session = null

  override def onWebSocketBinary(payload: Array[Byte], offset: Int, len: Int) {
    // We have to implement this but don't use it
  }

  override def onWebSocketClose(statusCode: Int, reason: String) {
    this.outbound = null
  }

  override def onWebSocketConnect(session: Session) {
    this.outbound = session
  }

  override def onWebSocketError(cause: Throwable) {
    cause.printStackTrace(System.err)
  }

  def send(str: String): Unit = {
    if (outbound != null && outbound.isOpen) {
      val remote = outbound.getRemote
      remote.sendStringByFuture(str)
    }
  }

  override def onWebSocketText(message: String) {
    if (outbound != null && outbound.isOpen) {
      System.out.printf("Echoing back message again [%s]%n", message)
      manager.send(message)
    }
  }
}
