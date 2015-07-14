package com.github.curzonj

import java.util.TimerTask
import java.util.concurrent.{TimeUnit, ScheduledFuture, ScheduledThreadPoolExecutor}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Success, Failure}
import ExecutionContext.Implicits.global
import org.json4s._
import org.json4s.jackson.JsonMethods._


object WorldTicker extends Logging {
  def schedule(): Unit = {
    currentTick = System.currentTimeMillis
    pool.schedule(task, interval, TimeUnit.MILLISECONDS)

    logger.info("scheduled")
  }

  val task = new TimerTask {
    def run() =  worldTick
  }

  val interval = 80
  var currentTick: Long = 0

  lazy val pool = new ScheduledThreadPoolExecutor(1)
  lazy val redis = RedisFactory.get

  def worldTick: Unit = {
    currentTick += interval
    val startedAt= System.currentTimeMillis
    val jitter = startedAt - currentTick

    logger.info("world tick "+logfmt("tick" -> currentTick, "jitter" -> jitter, "ts" -> startedAt))

    val name = "command_at_"+currentTick
    val future = redis.withTransaction { t =>
      t.rename("commands", name)
      t.lRange[String](name)
    } map { value =>
      def optionalJson(s: String): Any = Try(parse(s)) getOrElse s
      val obj = value map { v => optionalJson(v) }

      logger.trace(obj.toString)
    }

    future onFailure {
      case t => logger.error("error in worldTick", t)
    }

    future onComplete { _ =>
      val endedAt = System.currentTimeMillis
      logger.info("end tick "+logfmt("duration" -> (endedAt - startedAt)))

      val delay = currentTick + interval - endedAt
      pool.schedule(task, delay, TimeUnit.MILLISECONDS)
    }
  }

}
