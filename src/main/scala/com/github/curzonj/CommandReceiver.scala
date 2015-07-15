package com.github.curzonj

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}
import org.json4s._

import org.json4s.jackson.JsonMethods._

object CommandReceiver extends Logging {

  lazy val redis = RedisFactory.get
  implicit val formats = DefaultFormats ++ JavaTypesSerializers.all

  type ApiCommand = String
  var lastCommandFetch: Future[List[ApiCommand]] = Future { List() }
  def fetchCommands(thisTick: Long): Future[List[ApiCommand]] = {
    val name = "command_at_"+thisTick
    val commandFetch = lastCommandFetch

    logger.trace("at=commandFetch:start ts={}", thisTick)
    lastCommandFetch = redis.withTransaction { t =>
      t.rename("commands", name)
      t.lRange[ApiCommand](name)
    }

    lastCommandFetch onComplete { _ =>
      logger.trace("at=commandFetch:complete ts={}", thisTick)
    }

    commandFetch
  }

  case class DiffCommand(uuid: UUID) {
    var sourceJson: Option[JValue] = None
  }

  def commands(tick: Long): Future[List[DiffCommand]] = {
    CommandReceiver.fetchCommands(tick) map { list =>
      list map { value =>
        val parsed = Try(parse(value))
        val extracted = parsed flatMap {
          v => Try(v.extract[DiffCommand])
        } map { diff =>
          // parsed.get can't throw an exception here because
          // Try.map only looks at successes
          diff.sourceJson = Some(parsed.get)
          diff
        }

        if (extracted.isFailure) logger.error("Failed to parse `"+value.toString+"`", extracted.failed.get)

        extracted
      } collect {
        case Success(v) => v
      }
    }
  }
}
