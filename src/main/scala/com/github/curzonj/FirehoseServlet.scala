package com.github.curzonj

import org.eclipse.jetty.websocket.servlet.WebSocketServlet
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory
import org.eclipse.jetty.websocket.servlet._

class FirehoseServlet extends WebSocketServlet {

  val creator = new WebsocketManager()

  override def init(): Unit = {
    super.init()
  }

  override def configure(factory: WebSocketServletFactory) {
    factory.getPolicy.setIdleTimeout(10000)
    factory.setCreator(creator)
  }

  override def destroy(): Unit = {
    super.destroy()
  }
}



