package com.park9eon.card

import android.database.Cursor
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    companion object val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val expendList = ArrayList<Expend>()
        val smsUri = Uri.parse("content://sms/inbox")
        val cur = contentResolver.query(smsUri, arrayOf("_id", "address", "body", "date"),
                "address = ? AND date >= ? AND date <= ?",
                arrayOf("15889955", DATE_FORMAT.parse("2017-10-19").time.toString(), DATE_FORMAT.parse("2017-11-20").time.toString()),
                null)
        while (cur != null && cur.moveToNext()) {
            expendList.add(curToExpend(cur))
        }
        cur?.close()
        expendList.sortedBy { -it.money }.forEach {
            Log.d("TEST", it.toString())
        }
        Log.d("TEST", "총 ${expendList.sumBy { it.money }}원")
    }
}

fun curToExpend(cur: Cursor): Expend {
    val address = cur.getString(cur.getColumnIndex("address"))
    val date = cur.getLong(cur.getColumnIndex("date"))
    val body = cur.getString(cur.getColumnIndexOrThrow("body"))
    val expendPair = """(\d{1,3}[,\d{3}]*)원.*\s.*\s(.*)""".toRegex().find(body, 0)?.groupValues?.let {
        Pair(DecimalFormat("#,##").parse(it[1].trim()).toInt(), it[2].trim())
    }
    return Expend(Date(date), expendPair?.first?:-1, expendPair?.second?:"unll")
}

data class Expend(val date: Date, val money: Int, val comment: String) {
    companion object val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.KOREA)
    override fun toString(): String {
        return "${DATE_FORMAT.format(date)} : ${money}원 : $comment"
    }
}
