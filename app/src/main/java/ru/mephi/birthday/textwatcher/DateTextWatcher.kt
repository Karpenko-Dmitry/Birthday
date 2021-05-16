package ru.mephi.birthday.textwatcher

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class DateTextWatcher(private val editText: EditText, private val isDayWatcher : Boolean, private val textInput : TextInputLayout) : TextWatcher {

    private var updatedText = ""
    private var editing = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        textInput.isErrorEnabled = false
        if (s.toString().equals(updatedText) || editing) return
        val reg = Regex("[^0-9]")
        var digits = reg.replace(s.toString(), "")
        if (isDayWatcher) {
           digits = dayValidation(digits)
        } else {
            digits = yearValidation(digits)
        }
        updatedText = digits
    }

    private fun dayValidation(digits : String) : String {
        if (digits.length > 0) {
            val day : Int = digits.toInt()
            if (day > 31) {
                return digits.substring(0,digits.length-1)
            }
        }
        return digits
    }

    private fun yearValidation(digits : String) : String {
        if (digits.length > 0) {
            val year : Int = digits.toInt()
            val calendar = Calendar.getInstance()
            if (year > calendar.get(Calendar.YEAR)) {
                return digits.substring(0,digits.length-1)
            }
        }
        return digits
    }

    override fun afterTextChanged(s: Editable?) {
        if (editing) return
        editing = true
        s?.clear()
        s?.insert(0, updatedText)
        editing = false
    }
}