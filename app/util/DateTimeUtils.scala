package util

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

object DateTimeUtils {

  val formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd MM yyyy HH:mm:ss.SSS Z")

}
