package ru.mephi.birthday.context

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.lifecycle.Observer
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ru.mephi.birthday.PersonViewModel
import ru.mephi.birthday.PersonViewModelFactory
import ru.mephi.birthday.R
import ru.mephi.birthday.database.Person
import ru.mephi.birthday.database.UserState
import ru.mephi.birthday.textwatcher.DateTextWatcher
import java.text.SimpleDateFormat
import java.util.*

class AddPersonFragment : Fragment() {

    private val PICK_IMAGE = 1

    private lateinit var personId : UUID
    private lateinit var icon: ImageView
    private lateinit var nickName: EditText
    private lateinit var dayEditText: EditText
    private lateinit var monthSpinner: Spinner
    private lateinit var yearEditText: EditText
    private lateinit var nameTextInput: TextInputLayout
    private lateinit var dayTextInput: TextInputLayout
    private lateinit var yearTextInput: TextInputLayout
    private var uriIcon: Uri? = null
    private var person: Person? = null


    private val personViewModel: PersonViewModel by viewModels {
        PersonViewModelFactory((requireActivity().application as MyApplication).repository)
    }

    enum class State {
        INSERT, UPDATE;
    }

    private lateinit var state: State


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args: AddPersonFragmentArgs by navArgs()
        if (args.argPersonId == null) {
            state = State.INSERT
        } else {
            personId = UUID.fromString(args.argPersonId)
            state = State.UPDATE
            personViewModel.getPersonById(personId)
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_person, container, false)
        val appBar: BottomAppBar = view.findViewById(R.id.bottom_app_bar)
        appBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete -> {
                    if (person == null) {
                        false
                    } else {
                        deletePerson()
                        nickName.findNavController().popBackStack()
                        true
                    }
                }
                else -> false
            }
        }
        icon = view.findViewById(R.id.icon)
        icon.setOnClickListener {
            val iconIntent = Intent(Intent.ACTION_PICK)
            iconIntent.setType("image/*")
            startActivityForResult(iconIntent, PICK_IMAGE)
        }
        monthSpinner = view.findViewById(R.id.month)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.months,
            R.layout.month_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        }
        monthSpinner.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.months,
            R.layout.month_spinner_item
        )
        nameTextInput = view.findViewById(R.id.nickname_layout)
        dayTextInput = view.findViewById(R.id.day_layout)
        yearTextInput = view.findViewById(R.id.year_layout)
        nickName = view.findViewById(R.id.nickname)
        nickName.setOnClickListener { nameTextInput.isErrorEnabled = false }
        val okButton: FloatingActionButton = view.findViewById(R.id.floating_action_button)
        personViewModel.person.observe(viewLifecycleOwner, Observer {
                prs -> updateUI(prs) })
        dayEditText = view.findViewById(R.id.dayForm)
        val dayTextWatcher: TextWatcher = DateTextWatcher(dayEditText, true, dayTextInput)
        dayEditText.addTextChangedListener(dayTextWatcher)
        yearEditText = view.findViewById(R.id.yearForm)
        val yearTextWatcher: TextWatcher = DateTextWatcher(dayEditText, false, yearTextInput)
        yearEditText.addTextChangedListener(yearTextWatcher)
        okButton.setOnClickListener {
            if (checkPersonData()) {
                confirmChanging()
                hideKeyBoard()
                nickName.findNavController().popBackStack()
            }
        }
        return view
    }

    private fun addNewPerson() {
        val user = Firebase.auth.currentUser;
        val userData = hashMapOf("id" to user.uid)
        //db.collection("users").document("user").set(userData)
    }

    private fun updateUI(prs: Person?) {
        person = prs
        nickName.setText(prs?.nickName)
        //val birthday = prs?.birthday
        val uriStr = prs?.uri
        /*if (uriStr != null) {
            uriIcon = Uri.parse(uriStr)
            if (uriIcon != null) {
                Glide.with(requireContext()).load(uriIcon).centerCrop()
                    .placeholder(R.drawable.ic_user).error(R.drawable.ic_user).into(icon)
            }
        }*/
        if (person?.facebookId != null) {
            Glide.with(requireContext()).
            load("https://graph.facebook.com/${person?.facebookId}/picture?type=large").
            centerCrop().
            placeholder(R.drawable.ic_user).
            error(R.drawable.ic_user).
            into(icon)
        } else if (uriStr != null) {
            Glide.with(requireContext()).
            load(Uri.parse(uriStr)).
            centerCrop().
            placeholder(R.drawable.ic_user).
            error(R.drawable.ic_user).
            into(icon)
        }
        if (prs?.day != null && prs.state != UserState.INVALID) {
            dayEditText.setText(prs.day.toString())
            monthSpinner.setSelection(prs.month!!.toInt())
            if (prs.year != null) {
                yearEditText.setText(prs.year.toString())
            }
        }
    }

    private fun getStringDateByLong(l: Long?): String {
        if (l != null) {
            val dateFormat = SimpleDateFormat("d MMM y")
            val date: Calendar = Calendar.getInstance()
            date.time = l?.let { Date(it) }
            return dateFormat.format(date.time)
        }
        return getString(R.string.choose_day)
    }

    private fun checkPersonData(): Boolean {
        if (!checkDay()) return false
        if (!checkYear()) return false
        if (!checkNickname()) return false
        return true
    }

    private fun checkDay(): Boolean {
        if (dayEditText.text.toString().isNullOrEmpty()) {
            dayTextInput.isErrorEnabled = true
            dayTextInput.error = "Empty"
            return false
        }
        return true
    }

    private fun checkYear(): Boolean {
        /*if (yearEditText.text.toString().isNullOrEmpty()) {
            yearTextInput.isErrorEnabled = true
            yearTextInput.error = "Empty"
            return false
        }*/
        return true
    }

    private fun checkNickname(): Boolean {
        if (nickName.text.toString().isNullOrEmpty()) {
            nameTextInput.isErrorEnabled = true
            nameTextInput.error = "Empty"
            return false
        }
        return true
    }

    private fun confirmChanging() {
        val birthday = getBirthdayFromEditText()
        if (person != null) {
            person!!.year = birthday.year
            person!!.month = birthday.month
            person!!.day = birthday.day
            person!!.nickName = nickName.text.toString().trim()
            person!!.uri = uriIcon.toString()
            person!!.state = UserState.NOT_SYNCHRONIZED
            personViewModel.update(person!!)
        } else {
            person = Person(null,
                nickName.text.toString(), birthday.day, birthday.month,
                birthday.year, UserState.NOT_SYNCHRONIZED, uriIcon.toString()
            )
            personViewModel.insert(person!!)
        }
    }

    private fun getBirthdayFromEditText(): DateWithNullYear {
        val day = dayEditText.text.toString().toInt()
        val month = monthSpinner.selectedItemPosition
        val yearStr = yearEditText.text.toString()
        val date = DateWithNullYear(
            day.toByte(), month.toByte(),
            if (yearStr.isNullOrEmpty()) null else yearStr.toShort()
        )
        return date
    }

    public class DateWithNullYear(val day: Byte, val month: Byte, val year: Short?)

    private fun deletePerson() {
        personViewModel.delete(person!!)
    }

    private fun hideKeyBoard() {
        val inputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE) {
            val uri = data?.data
            if (uri != null) {
                uriIcon = uri
                Glide.with(requireContext()).load(uri).centerCrop().placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user).into(icon)
            }
        }
    }
}