package skedgo.datetimerangepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.squareup.timessquare.CalendarPickerView
import org.joda.time.DateTime
import skedgo.datetimerangepicker.databinding.DateTimeRangePickerBinding
import java.util.*

class DateTimeRangePickerActivity : AppCompatActivity() {
    companion object {
        fun newIntent(
                context: Context?,
                timeZone: TimeZone?,
                startTimeInMillis: Long?,
                endTimeInMillis: Long?,
                range: Int? = -1,
                minTimeInMillis: Long? = null,
                maxTimeInMillis: Long? = null,
                stackFromBottom: Boolean? = true
        ): Intent {
            val intent = Intent(context!!, DateTimeRangePickerActivity::class.java)
            startTimeInMillis?.let { intent.putExtra(DateTimeRangePickerViewModel.KEY_START_TIME_IN_MILLIS, it) }
            endTimeInMillis?.let { intent.putExtra(DateTimeRangePickerViewModel.KEY_END_TIME_IN_MILLIS, it) }
            intent.putExtra(DateTimeRangePickerViewModel.KEY_TIME_ZONE, timeZone!!.id)
            minTimeInMillis?.let { intent.putExtra(DateTimeRangePickerViewModel.KEY_MIN_DATE_IN_MILLIS, it) }
            maxTimeInMillis?.let { intent.putExtra(DateTimeRangePickerViewModel.KEY_MAX_DATE_IN_MILLIS, it) }
            range?.let { intent.putExtra(DateTimeRangePickerViewModel.KEY_TIME_RANGE, it) }
            stackFromBottom?.let { intent.putExtra(DateTimeRangePickerViewModel.KEY_VIEW_STACK_FROM_BOTTOM, it) }
            return intent
        }
    }

    private val viewModel: DateTimeRangePickerViewModel by lazy {
        DateTimeRangePickerViewModel(TimeFormatter(applicationContext))
    }
    private val binding: DateTimeRangePickerBinding by lazy {
        DataBindingUtil.setContentView<DateTimeRangePickerBinding>(
                this,
                R.layout.date_time_range_picker
        )
    }

    private var range: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.handleArgs(intent.extras)
        binding.setViewModel(viewModel)
        binding.calendarPickerView.isStackFromBottom = viewModel.stackFromBottom
        if (intent.hasExtra(DateTimeRangePickerViewModel.KEY_TIME_RANGE)) {
            range = intent.getIntExtra(DateTimeRangePickerViewModel.KEY_TIME_RANGE, -1)
        }
        val toolbar = binding.toolbar
        toolbar.inflateMenu(R.menu.date_time_range_picker)
        toolbar.setNavigationOnClickListener { _ -> finish() }
        toolbar.setOnMenuItemClickListener { item ->
            when {
                item.itemId == R.id.dateTimeRangePickerDoneItem -> {
                    val min = viewModel.minDate.time
                    val max = viewModel.maxDate.time
                    val start = viewModel.startDateTime.value?.millis ?: 0
                    val end = viewModel.endDateTime.value?.millis ?: 0
                    if (start < min) {
                        Toast.makeText(this, "开始时间小于最小时间", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }
                    if (end > max) {
                        Toast.makeText(this, "结束时间大于最大时间", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }
                    if (end <= start) {
                        Toast.makeText(this, "结束时间需大于开始时间", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }
                    if (range != -1) {
                        if (end - start > 1000 * 60 * 60 * 24 * range) {
                            Toast.makeText(this, "所选时间段不能长于${range}天", Toast.LENGTH_SHORT).show()
                            return@setOnMenuItemClickListener true
                        }
                    }
                    setResult(Activity.RESULT_OK, viewModel.createResultIntent())
                    finish()
                }
            }
            true
        }

        val calendarPickerView = binding.calendarPickerView
        calendarPickerView.init(viewModel.minDate, viewModel.maxDate)
                .inMode(CalendarPickerView.SelectionMode.RANGE)
        viewModel.startDateTime.value?.let {
            calendarPickerView.selectDate(it.toDate())
        }
        viewModel.endDateTime.value?.let {
            calendarPickerView.selectDate(it.toDate())
        }

        calendarPickerView.setOnDateSelectedListener(object : CalendarPickerView.OnDateSelectedListener {
            override fun onDateSelected(date: Date) {
                viewModel.updateSelectedDates(calendarPickerView.selectedDates)
            }

            override fun onDateUnselected(date: Date) {
                viewModel.updateSelectedDates(calendarPickerView.selectedDates)
            }
        })

        binding.pickStartTimeView.setOnClickListener { _ ->
            showTimePicker(viewModel.startDateTime.value, viewModel.onStartTimeSelected)
        }
        binding.pickEndTimeView.setOnClickListener { _ ->
            showTimePicker(viewModel.endDateTime.value, viewModel.onEndTimeSelected)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun showTimePicker(
            initialTime: DateTime,
            listener: com.wdullaer.materialdatetimepicker.time.TimePickerDialog.OnTimeSetListener) {
//        TimePickerDialog(
//                this,
//                listener,
//                initialTime.hourOfDay,
//                initialTime.minuteOfHour,
//                DateFormat.is24HourFormat(this)
//        ).show()
        com.wdullaer.materialdatetimepicker.time.TimePickerDialog.newInstance(listener, initialTime.hourOfDay, initialTime.minuteOfHour, true).show(fragmentManager, "time")
    }
}
