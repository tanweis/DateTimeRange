package skedgo.datetimerangepicker

import android.content.Intent
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.os.Bundle
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import rx.Observable
import rx.subjects.BehaviorSubject
import java.util.*

class DateTimeRangePickerViewModel(private val timeFormatter: TimeFormatter) {
    val startDateText = ObservableField<String>()
    val startTimeText = ObservableField<String>()
    val endDateText = ObservableField<String>()
    val endTimeText = ObservableField<String>()
    val hasStartDate = ObservableBoolean()
    val hasEndDate = ObservableBoolean()
    val isCompletable = ObservableBoolean()
    val onStartTimeSelected: com.wdullaer.materialdatetimepicker.time.TimePickerDialog.OnTimeSetListener
    val onEndTimeSelected: com.wdullaer.materialdatetimepicker.time.TimePickerDialog.OnTimeSetListener
    val dateFormatter: DateTimeFormatter = DateTimeFormat.mediumDate()

    internal var timeZone: TimeZone? = TimeZone.getDefault()
    internal val startDateTime: BehaviorSubject<DateTime> = BehaviorSubject.create()
    internal val endDateTime: BehaviorSubject<DateTime> = BehaviorSubject.create()
//  internal val minDate: Date by lazy {
//    DateTime.parse("2018-01-01T00:00:00").toDate()
//  }
//  internal val maxDate: Date by lazy {
//    DateTime.now(DateTimeZone.forTimeZone(timeZone))
//        .plusDays(1)
//        .toDate()
//  }

    var minDate: Date = DateTime.parse("2018-01-01T00:00:00").toDate()
    var maxDate: Date = DateTime.now(DateTimeZone.forTimeZone(timeZone)).plusDays(1).toDate()
    var minOutBoundMsg:String="开始时间小于最小时间"
    var maxOutBoundMsg:String="结束时间大于最大时间"

    var stackFromBottom = true

    init {
//    onStartTimeSelected = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
//      startDateTime.value!!.let {
//        val newValue = it.withHourOfDay(hourOfDay).withMinuteOfHour(minute)
//        startDateTime.onNext(newValue)
//      }
//    }
        onStartTimeSelected = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute, _ ->
            startDateTime.value!!.let {
                val newValue = it.withHourOfDay(hourOfDay).withMinuteOfHour(minute)
                startDateTime.onNext(newValue)
            }
        }
//    onEndTimeSelected = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
//      endDateTime.value!!.let {
//        val newValue = it.withHourOfDay(hourOfDay).withMinuteOfHour(minute)
//        endDateTime.onNext(newValue)
//      }
//    }
        onEndTimeSelected = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute, _ ->
            endDateTime.value!!.let {
                val newValue = it.withHourOfDay(hourOfDay).withMinuteOfHour(minute)
                endDateTime.onNext(newValue)
            }
        }
        startDateTime.subscribe({
            onDateTimeEmitted(it, startDateText, startTimeText, hasStartDate)
        })
        endDateTime.subscribe({
            onDateTimeEmitted(it, endDateText, endTimeText, hasEndDate)
        })
        Observable.combineLatest(startDateTime, endDateTime, { x, y ->
            x != null && y != null
        }).subscribe({ isCompletable.set(it) })
    }

    fun onDateTimeEmitted(
        dateTime: DateTime?,
        dateText: ObservableField<String>,
        timeText: ObservableField<String>,
        visibility: ObservableBoolean
    ) {
        if (dateTime != null) {
            visibility.set(true)
            dateText.set(dateFormatter.print(dateTime))
            timeText.set(timeFormatter.printTime(dateTime))
        } else {
            visibility.set(false)
            dateText.set(null)
            timeText.set(null)
        }
    }

    fun createResultIntent(): Intent {
        return Intent()
            .putExtra(KEY_START_TIME_IN_MILLIS, startDateTime.value!!.millis)
            .putExtra(KEY_END_TIME_IN_MILLIS, endDateTime.value!!.millis)
            .putExtra(KEY_TIME_ZONE, timeZone!!.id)
    }

    fun updateSelectedDates(selectedDates: List<Date>) {
        if (selectedDates.isEmpty()) {
            startDateTime.onNext(null)
            endDateTime.onNext(null)
            return
        }

        val dateTimeZone = DateTimeZone.forTimeZone(timeZone)
        val startDateTimeValue = { DateTime(selectedDates.first().time, dateTimeZone) }
        startDateTime.onNext(
            when {
                startDateTime.hasValue() -> {
                    startDateTimeValue()
                        .withTime(startDateTime.value!!.toLocalTime())
                }
                else -> startDateTimeValue()
            }
        )

        val endDateTimeValue = when {
            selectedDates.size == 1 -> startDateTimeValue()
            else -> DateTime(selectedDates.last().time, dateTimeZone)
        }
        endDateTime.onNext(
            when {
                endDateTime.hasValue() -> {
                    endDateTimeValue.withTime(endDateTime.value!!.toLocalTime())
                }
                else -> endDateTimeValue
            }
        )
    }

    fun handleArgs(arguments: Bundle) {
        val timeZoneId = arguments.getString(KEY_TIME_ZONE)
        timeZone = TimeZone.getTimeZone(timeZoneId)
        if (arguments.containsKey(KEY_VIEW_STACK_FROM_BOTTOM)) {
            stackFromBottom = arguments.getBoolean(KEY_VIEW_STACK_FROM_BOTTOM, true)
        }
        if (arguments.containsKey(KEY_MIN_DATE_IN_MILLIS)) {
            val minMillis = arguments.getLong(KEY_MIN_DATE_IN_MILLIS)
            minDate = DateTime(minMillis, DateTimeZone.forTimeZone(timeZone)).toDate()
        }
        if (arguments.containsKey(KEY_MAX_DATE_IN_MILLIS)) {
            val maxMills = arguments.getLong(KEY_MAX_DATE_IN_MILLIS)
            maxDate = DateTime(maxMills, DateTimeZone.forTimeZone(timeZone)).toDate()
        }
        if (arguments.containsKey(KEY_MAX_DATE_MSG)){
            maxOutBoundMsg=arguments.getString(KEY_MAX_DATE_MSG,maxOutBoundMsg)
        }
        if (arguments.containsKey(KEY_MIN_DATE_MSG)){
            minOutBoundMsg=arguments.getString(KEY_MIN_DATE_MSG,minOutBoundMsg)
        }

        if (arguments.containsKey(KEY_START_TIME_IN_MILLIS)) {
            val startInMillis = arguments.getLong(KEY_START_TIME_IN_MILLIS)
            startDateTime.onNext(DateTime(startInMillis, DateTimeZone.forTimeZone(timeZone)))
        }
        if (arguments.containsKey(KEY_END_TIME_IN_MILLIS)) {
            val endInMillis = arguments.getLong(KEY_END_TIME_IN_MILLIS)
            endDateTime.onNext(DateTime(endInMillis, DateTimeZone.forTimeZone(timeZone)))
        }
    }

    companion object {
        val KEY_START_TIME_IN_MILLIS = "startTimeInMillis"
        val KEY_END_TIME_IN_MILLIS = "endTimeInMillis"
        val KEY_TIME_ZONE = "timeZone"
        val KEY_TIME_RANGE = "timeRange"
        val KEY_MIN_DATE_IN_MILLIS = "minDate"
        val KEY_MAX_DATE_MSG = "maxDateMsg"
        val KEY_MIN_DATE_MSG = "minDateMsg"
        val KEY_MAX_DATE_IN_MILLIS = "maxDate"
        val KEY_VIEW_STACK_FROM_BOTTOM = "viewStackFromBottom"
    }
}
