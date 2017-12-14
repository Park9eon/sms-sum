package com.park9eon.card

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object

    val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

    val startDateEditText: EditText by lazy { findViewById(R.id.startDateEditText) as EditText }
    val endDateEditText: EditText by lazy { findViewById(R.id.endDateEditText) as EditText }
    val searchButton: Button by lazy { findViewById(R.id.searchButton) as Button }
    val resultTextView: TextView by lazy { findViewById(R.id.resultTextView) as TextView }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val todayText = DATE_FORMAT.format(Date())

        startDateEditText.hint = todayText
        endDateEditText.hint = todayText
        endDateEditText.setText(todayText)
        searchButton.setOnClickListener {

            val expendList = ArrayList<Expend>()
            val smsUri = Uri.parse("content://sms/inbox")
            val cur = contentResolver.query(smsUri, arrayOf("_id", "address", "body", "date"),
                    "address = ? AND date >= ? AND date <= ?",
                    arrayOf("15889955",
                            DATE_FORMAT.parse(startDateEditText.text.toString())
                                    .time
                                    .toString(),
                            DATE_FORMAT.parse(endDateEditText.text.toString())
                                    .time
                                    .toString()),
                    null)
            while (cur != null && cur.moveToNext()) {
                expendList.add(curToExpend(cur))
            }
            cur?.close()
            resultTextView.text = "${expendList.sumBy { it.money }}원\n${expendList.sortedBy { -it.money }
                    .joinToString("\n")}"
        }
    }
}

fun curToExpend(cur: Cursor): Expend {
    val date = cur.getLong(cur.getColumnIndex("date"))
    val body = cur.getString(cur.getColumnIndexOrThrow("body"))
    val expendPair = """(\d{1,3}[,\d{3}]*)원.*\s.*\s(.*)""".toRegex().find(body, 0)?.groupValues?.let {
        Pair(DecimalFormat("#,##").parse(it[1].trim()).toInt(), it[2].trim())
    }
    return Expend(Date(date), expendPair?.first ?: -1, expendPair?.second ?: "unll")
}

data class Expend(val date: Date, val money: Int, val comment: String) {
    companion object

    val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.KOREA)
    override fun toString(): String {
        return "${DATE_FORMAT.format(date)} : ${money}원 : $comment"
    }
}
