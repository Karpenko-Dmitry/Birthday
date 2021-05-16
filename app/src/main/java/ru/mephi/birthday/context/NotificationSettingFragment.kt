package ru.mephi.birthday.context

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.mephi.birthday.R
import java.util.*

class NotificationSettingFragment : Fragment() {


    private lateinit var switchSound: SwitchMaterial
    private lateinit var switchLight: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification_setting, container, false)
        val pref = requireContext().getSharedPreferences(notificationPreference, Context.MODE_PRIVATE)
        switchSound = view.findViewById(R.id.notification_sound)
        switchSound.isChecked = pref.getBoolean(notificationSound,false)
        switchSound.setOnCheckedChangeListener { buttonView, isChecked ->
            pref.edit().putBoolean(notificationSound, isChecked).commit()}
        switchLight = view.findViewById(R.id.notification_light)
        switchLight.isChecked = pref.getBoolean(notificationLight,false)
        switchLight.setOnCheckedChangeListener { buttonView, isChecked ->
            pref.edit().putBoolean(notificationLight, isChecked).commit()}
        return view
    }


    companion object {
        val notificationPreference = "NotificationManager"
        val notificationSound = "Notification sound"
        val notificationLight = "Notification light"
        @JvmStatic
        fun newInstance() = NotificationSettingFragment()
    }


}