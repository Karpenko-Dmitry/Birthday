package ru.mephi.birthday.context

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.mephi.birthday.PersonViewModel
import ru.mephi.birthday.PersonViewModelFactory
import ru.mephi.birthday.R
import ru.mephi.birthday.database.Person
import java.text.SimpleDateFormat
import java.util.*

class AddPersonFragment : Fragment() {

    private var id : Long = -2
    private lateinit var firstName : EditText
    private lateinit var lastName : EditText
    private lateinit var button : Button
    private var birthday : Long? = null
    private var person : Person? = null
    private val datePickerBuilder = MaterialDatePicker.Builder.datePicker().
                                setTitleText(R.string.set_birthday)

     private val personViewModel: PersonViewModel by viewModels {
        PersonViewModelFactory((requireActivity().application as MyApplication).repository)
     }

    enum class State {
        INSERT, UPDATE;
    }

    private lateinit var state : State



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args : AddPersonFragmentArgs by navArgs()
        id = args.argPersonId
        if (id < 0) {
            state = State.INSERT
        } else {
            state = State.UPDATE
        }
        personViewModel.getPersonById(id)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_person, container, false)
        val appBar : BottomAppBar = view.findViewById(R.id.bottom_app_bar)
        appBar.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.delete -> {if (person == null) {
                                        false
                                } else {
                                    deletePerson()
                                    button.findNavController().popBackStack()
                                    true
                                }
                                }
                else -> false
            }
        }
        lastName  = view.findViewById(R.id.lastname)
        firstName = view.findViewById(R.id.firstname)
        val okButton : FloatingActionButton = view.findViewById(R.id.floating_action_button)
        personViewModel.person.observe(viewLifecycleOwner, Observer {prs -> updateUI(prs)})
        val datePicker = datePickerBuilder.build()
        button = view.findViewById(R.id.button)
        button.setOnClickListener {
            datePicker.show(requireActivity().supportFragmentManager,"DATE_PICKER")
        }
        datePicker.addOnPositiveButtonClickListener {
            birthday = datePicker.selection
            button.text = getStringDateByLong(birthday)
        }
        okButton.setOnClickListener {
            confirmChanging()
            hideKeyBoard()
            button.findNavController().popBackStack()
        }
        return view
    }

    private fun updateUI(person: Person?) {
        firstName.setText(person?.firstName)
        lastName.setText(person?.lastName)
        birthday = person?.birthday
        datePickerBuilder.setSelection(person?.birthday)
        button.text = getStringDateByLong(person?.birthday)
        if (person != null) {
            this.person = person
        }
    }

    private fun getStringDateByLong(l : Long?) : String {
        if (l != null) {
            val dateFormat = SimpleDateFormat("d MMM y")
            val date : Calendar = Calendar.getInstance()
            date.time = l?.let { Date(it) }
            return dateFormat.format(date.time)
        }
        return getString(R.string.choose_birthday)
    }

    private fun confirmChanging() {
        if (person != null ) {
            person!!.birthday = birthday!!
            person!!.lastName = lastName.text.toString()
            person!!.firstName = firstName.text.toString()
            personViewModel.update(person!!)
        } else {
            person = Person(firstName.text.toString(),lastName.text.toString(),birthday!!)
            personViewModel.insert(person!!)
        }
    }

    private fun deletePerson() {
        personViewModel.delete(person!!)
    }

    private fun hideKeyBoard() {
        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken,0)
    }


    override fun onResume() {
        super.onResume()

    }

}