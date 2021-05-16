package ru.mephi.birthday.context

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.work.*
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vk.api.sdk.VK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.mephi.birthday.R
import ru.mephi.birthday.workers.BirthdayWorker
import ru.mephi.birthday.workers.NotificationWorker
import ru.mephi.birthday.workers.SynchronizationWorker
import java.util.concurrent.TimeUnit

class AccountFragment : Fragment() {

    private val RC_SIGN_IN: Int = 1927

    private lateinit var userIcon: ImageView
    private lateinit var userName: TextView
    private lateinit var userMail: TextView
    private lateinit var account: Button
    private var hasAuth: Boolean = false
    private var currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onStart() {
        super.onStart()
        updateAccount()
    }

    private fun updateAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            hasAuth = true
            account.setText(R.string.sign_out)
            if(!user.uid.equals(currentUser?.uid)) {
                currentUser = user
                val rep = (requireActivity().application as MyApplication).repository
                GlobalScope.launch(Dispatchers.IO) {
                    rep.delete()
                    val synchronizationWorkRequest : OneTimeWorkRequest =
                        OneTimeWorkRequestBuilder<SynchronizationWorker>().build()
                    WorkManager.getInstance(requireContext()).beginUniqueWork("synchronization",
                        ExistingWorkPolicy.REPLACE,synchronizationWorkRequest).enqueue()
                }
            }
            updateAccountUI(user)
        } else {
            hasAuth = false
            account.setText(R.string.sign_in)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        userIcon = view.findViewById(R.id.user_icon)
        userName = view.findViewById(R.id.user_name)
        userMail = view.findViewById(R.id.user_email)
        account = view.findViewById(R.id.enter_to_account)
        account.setOnClickListener {
            if (hasAuth) {
                hasAuth = false
                account.setText(R.string.sign_in)
                cleanUI()
                AuthUI.getInstance().signOut(requireContext())
            } else {
                hasAuth = true
                account.setText(R.string.sign_out)
                auth()
            }
        }
        return view
    }

    private fun updateAccountUI(user: FirebaseUser) {
        userName.visibility = View.VISIBLE
        userMail.visibility = View.VISIBLE
        userName.text = user.displayName
        userMail.text = user.email
        Glide.with(requireContext()).
            load(user.photoUrl).
            centerCrop().
            placeholder(R.drawable.ic_user).
            error(R.drawable.ic_user).
            into(userIcon)
    }

    private fun cleanUI() {
        userName.visibility = View.INVISIBLE
        userMail.visibility = View.INVISIBLE
        userIcon.setImageResource(R.drawable.ic_user)
    }

    private fun auth() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers).setTheme(R.style.Theme_Birthday)
                .build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_SIGN_IN -> {
                    updateAccount()}
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = AccountFragment()
    }
}