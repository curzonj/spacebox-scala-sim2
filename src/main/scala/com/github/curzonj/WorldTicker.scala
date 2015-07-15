package com.github.curzonj

import java.util.TimerTask
import java.util.concurrent.{TimeUnit, ScheduledThreadPoolExecutor}
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

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

  val interval = 80

  var currentTick: Long = 0

  def worldTick: Unit = {
    currentTick += interval
    val startedAt= System.currentTimeMillis
    val jitter = startedAt - currentTick

    logger.trace("at=worldTick:start tick={} jitter={}", currentTick, jitter)

    val future = CommandReceiver.commands(currentTick) map { list =>
      list map { value =>
        logger.debug(value.toString)
      }
    }

    future onFailure {
      case t => logger.error("at=worldTick:error", t)
    }

    future onComplete { _ =>
      val endedAt = System.currentTimeMillis
      logger.trace("at=worldTick:end duration={}", endedAt - startedAt)

      val delay = currentTick + interval - endedAt
      pool.schedule(task, delay, TimeUnit.MILLISECONDS)
    }
  }
}
