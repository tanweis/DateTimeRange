package skedgo.datetimerangepicker

import android.content.Context
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

open class TimeFormatter(val context: Context) {
  open fun printTime(dateTime: DateTime): String {
//    val formatter = if (DateFormat.is24HourFormat(context))
//      DateTimeFormat.forPattern("H:mm")
//              .withLocale(Locale.SIMPLIFIED_CHINESE)
//    else
//      val formatter=DateTimeFormat.forPattern("h:mm a")
//              .withLocale(Locale.SIMPLIFIED_CHINESE)
      val formatter=DateTimeFormat.forPattern("H:mm")
              .withLocale(Locale.SIMPLIFIED_CHINESE)
    return formatter.print(dateTime)
  }
}
