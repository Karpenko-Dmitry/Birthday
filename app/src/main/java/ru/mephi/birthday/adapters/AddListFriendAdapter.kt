package ru.mephi.birthday.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.mephi.birthday.R

class AddListFriendAdapter(
    private val dataset: List<NewPerson>
) : RecyclerView.Adapter<AddListFriendAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon : ImageView = view.findViewById(R.id.icon)
        val name: TextView = view.findViewById(R.id.name)
        val phoneNumber: TextView = view.findViewById(R.id.phone_number)
        val email: TextView = view.findViewById(R.id.email)
        val checkBox : CheckBox = view.findViewById(R.id.check)

        fun bind(person : NewPerson) {
            name.text = person.name
            phoneNumber.text = person.phone
            email.text = person.email
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_list_item, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.bind(item)
    }

    override fun getItemCount() = dataset.size
}

class NewPerson(
    val name : String,
    val iconUri : String,
    val phone : String? = null,
    val email : String? = null
)