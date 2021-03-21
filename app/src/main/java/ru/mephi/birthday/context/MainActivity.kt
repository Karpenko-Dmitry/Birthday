package ru.mephi.birthday.context

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.mephi.birthday.R


class MainActivity : AppCompatActivity() {

    val context : Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val navHost = supportFragmentManager.findFragmentById(R.id.fragment)
        navHost!!.childFragmentManager.fragments.get(0).onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }
}