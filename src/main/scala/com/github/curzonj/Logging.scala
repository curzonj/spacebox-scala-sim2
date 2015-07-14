package com.github.curzonj

import org.slf4j.LoggerFactory

trait Logging {
  protected lazy val logger = LoggerFactory.getLogger(getClass.getName)

  def logfmt(list: (String, Any)*): String = {
    list map {case (key, value) => "" + key + "=" + value} mkString " "
  }
}
