package com.github.curzonj

import org.json4s._
import java.util.UUID

object JavaTypesSerializers {
  val all = List(UUIDSerializer)
}

case object UUIDSerializer extends CustomSerializer[UUID](format => (
  {
    case JString(s) => UUID.fromString(s)
    case JNull => null
  },
  {
    case x: UUID => JString(x.toString)
  }
  )
)
