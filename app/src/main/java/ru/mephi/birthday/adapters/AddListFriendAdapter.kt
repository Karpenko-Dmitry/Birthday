package ru.mephi.birthday.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.mephi.birthday.R

class AddListFriendAdapter(
    val dataset: List<NewPerson>
) : RecyclerView.Adapter<AddListFriendAdapter.ItemViewHolder>() {

    inner class ItemViewHolder : RecyclerView.ViewHolder {
        val icon : ImageView
        val name: TextView
        val phoneNumber: TextView
        val checkBox : CheckBox

        constructor(view: View) : super(view) {
            icon = view.findViewById(R.id.icon)
            name = view.findViewById(R.id.name)
            phoneNumber = view.findViewById(R.id.phone_number)
            checkBox = view.findViewById(R.id.check)
            checkBox.setOnCheckedChangeListener{
                    buttonView, isChecked ->
                if(isChecked) {
                    dataset[position].add = true
                } else {
                    dataset[position].add = false
                }
            }
        }


        fun bind(person : NewPerson, position: Int) {
            name.text = person.name
            phoneNumber.text = person.phone
            checkBox.isChecked = person.add
            if (person.id != null) {
                Glide.with(itemView.context).
                load("https://graph.facebook.com/${person.id}/picture?type=large").
                centerCrop().
                placeholder(R.drawable.ic_user).
                error(R.drawable.ic_user).
                into(icon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_list_item, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.bind(item,position)
    }

    override fun getItemCount() = dataset.size


}

class NewPerson(
    val id : String?,
    val name : String,
    val phone : String? = null,
    var add : Boolean = true
)