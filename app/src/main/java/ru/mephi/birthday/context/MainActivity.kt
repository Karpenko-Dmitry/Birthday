package ru.mephi.birthday.context

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MainFragment.REQUEST_CODE_READ_CONTACTS) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                MainFragment.READ_CONTACTS_GRANTED = true;
            }
        }
        if(MainFragment.READ_CONTACTS_GRANTED){
            MainFragment.showContactsList(this)
        }
        else{
            Toast.makeText(this, "Требуется установить разрешения", Toast.LENGTH_LONG).show();
        }
    }

}