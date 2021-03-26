package ru.mephi.birthday.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.mephi.birthday.R
import ru.mephi.birthday.context.MainFragmentDirections
import ru.mephi.birthday.database.Person
import ru.mephi.birthday.repository.Repository

@Suppress("DEPRECATION")
class PersonListAdapter() : ListAdapter<Person, PersonListAdapter.PersonViewHolder>(PersonComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        return PersonViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int)  {
        val current = getItem(position)
        holder.bind(current)
    }

    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name: TextView  = itemView.findViewById(R.id.person_name)
        val birthday: TextView = itemView.findViewById(R.id.person_birthday)
        var id  = 0

        fun bind(person: Person) {
            id = person.personId
            name.text = person.nickName
            birthday.text = Repository.getTimeForBirthdayString(person.birthday)
        }

        /*private fun getTimeForBirthday(birthday : Long) : String {
            val curMilliSec= System.currentTimeMillis()
            val birthCalendar = GregorianCalendar()
            birthCalendar.time = Date(birthday)
            val nowdayCalendar = GregorianCalendar()
            nowdayCalendar.time = Date(curMilliSec)
            birthCalendar.add(Calendar.YEAR,nowdayCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR))
            if (birthCalendar.before(nowdayCalendar)) {
                birthCalendar.add(Calendar.YEAR,1)
            }
          return Days.daysBetween(Instant(nowdayCalendar.time),
                Instant(birthCalendar.time)).days.toString() + " days"

        }*/

        companion object {
            fun create(parent: ViewGroup): PersonViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.person_card, parent, false)
                val holder = PersonViewHolder(view)
                view.setOnClickListener {view.findNavController().navigate(MainFragmentDirections.actionAddPerson(holder.id))}
                return holder
            }
        }

    }



    class PersonComparator : DiffUtil.ItemCallback<Person>() {
        override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean {
            return oldItem == newItem
        }
    }

}