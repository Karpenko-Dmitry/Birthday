package ru.mephi.birthday.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.mephi.birthday.R
import ru.mephi.birthday.context.AddPersonFragment.*
import ru.mephi.birthday.context.MainFragmentDirections
import ru.mephi.birthday.database.Person
import ru.mephi.birthday.database.UserState
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
        val icon: ImageView = itemView.findViewById(R.id.person_icon)
        var id : String = ""

        fun bind(person: Person) {
            id = person.uuid.toString()
            name.text = person.nickName
            if (person.state == UserState.INVALID) {
                val res = itemView.resources
                birthday.text = res.getString(R.string.invalid_birthday)
            } else {
                birthday.text = Repository.getTimeForBirthdayString(DateWithNullYear(person.day,person.month,person.year))
            }
            val uriStr = person.uri
            if (person.facebookId != null) {
                Glide.with(itemView.context).
                    load("https://graph.facebook.com/${person.facebookId}/picture?type=large").
                    centerCrop().
                    placeholder(R.drawable.ic_user).
                    error(R.drawable.ic_user).
                    into(icon)
            } else if (uriStr != null) {
                Glide.with(itemView.context).
                    load(Uri.parse(uriStr)).
                    centerCrop().
                    placeholder(R.drawable.ic_user).
                    error(R.drawable.ic_user).
                    into(icon)
            }
        }

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