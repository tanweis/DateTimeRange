package com.ytsk.datetimerangesample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.joda.time.DateTime
import skedgo.datetimerangepicker.DateTimeRangePickerActivity
import skedgo.datetimerangepicker.DateTimeRangePickerViewModel
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            val intent = DateTimeRangePickerActivity
                .newIntent(
                    this, TimeZone.getTimeZone("GMT+08:00"), null, null, null,
                    DateTime.now().getMillis(), DateTime.now().plusYears(1).getMillis(),null,null, false
                )
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK)
            return
        if (requestCode == 1 && data != null) {
            val startMillis = data.getLongExtra(DateTimeRangePickerViewModel.KEY_START_TIME_IN_MILLIS, -1)
            val endMillis = data.getLongExtra(DateTimeRangePickerViewModel.KEY_END_TIME_IN_MILLIS, -1)

            val beginTime = DateTime(startMillis).toString("yyyy-MM-dd HH:mm:ss")
            val endTime = DateTime(endMillis).toString("yyyy-MM-dd HH:mm:ss")
            Toast.makeText(this, "$beginTime -> $endTime", Toast.LENGTH_SHORT).show()

        }
    }
}
