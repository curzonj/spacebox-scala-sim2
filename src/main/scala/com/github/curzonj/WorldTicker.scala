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

    logger.info("at=scheduled")
  }

  val task = new TimerTask {
    def run() =  worldTick
  }

  lazy val pool = new ScheduledThreadPoolExecutor(1)
  lazy val redis = RedisFactory.get

  val interval = 80

  var currentTick: Long = 0

  type ApiCommand = String
  var lastCommandFetch: Future[List[ApiCommand]] = Future { List() }
  def fetchCommands: Future[List[ApiCommand]] = {
    val thisTick = currentTick
    val name = "command_at_"+thisTick
    val commandFetch = lastCommandFetch

    logger.info("at=commandFetch:start ts={}", thisTick)
    lastCommandFetch = redis.withTransaction { t =>
      t.rename("commands", name)
      t.lRange[ApiCommand](name)
    }

    lastCommandFetch onComplete { _ =>
      logger.info("at=commandFetch:complete ts={}", thisTick)
    }

    commandFetch
  }

  def worldTick: Unit = {
    currentTick += interval
    val startedAt= System.currentTimeMillis
    val jitter = startedAt - currentTick

    logger.info("at=worldTick:start tick={} jitter={}", currentTick, jitter)

    val future = fetchCommands map { value =>
      def optionalJson(s: ApiCommand): Any = Try(parse(s)) getOrElse s
      val obj = value map { v => optionalJson(v) }

      logger.trace(obj.toString)
    }

    future onFailure {
      case t => logger.error("at=worldTick:error", t)
    }

    future onComplete { _ =>
      val endedAt = System.currentTimeMillis
      logger.info("at=worldTick:end duration={}", endedAt - startedAt)

      val delay = currentTick + interval - endedAt
      pool.schedule(task, delay, TimeUnit.MILLISECONDS)
    }
  }

}
