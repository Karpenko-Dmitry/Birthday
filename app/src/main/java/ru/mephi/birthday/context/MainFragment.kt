package ru.mephi.birthday.context

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.math.MathUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import ru.mephi.birthday.PersonViewModel
import ru.mephi.birthday.PersonViewModelFactory
import ru.mephi.birthday.R
import ru.mephi.birthday.adapters.PersonListAdapter
import ru.mephi.birthday.database.Person
import java.util.concurrent.TimeUnit


class MainFragment : Fragment() {

    private val RC_SIGN_IN : Int = 1627


    lateinit var navController : NavController
    lateinit var userIcon : ImageView
    lateinit var userName : TextView
    lateinit var userMail : TextView

    val adapter = PersonListAdapter()
    private val personViewModel: PersonViewModel by viewModels {
        PersonViewModelFactory((requireActivity().application as MyApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        personViewModel.people.observe(this, Observer { people -> people?.let { adapter.submitList(it)}})
    }



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        val navigationView = view.findViewById<NavigationView>(R.id.navigation_view)
        val bottomSheetBehavior = BottomSheetBehavior.from(navigationView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        val bottomAppBar = view.findViewById<BottomAppBar>(R.id.bottom_app_bar)
        val scrim = view.findViewById<FrameLayout>(R.id.scrim)
        scrim.setOnClickListener {
            scrim.visibility = View.GONE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN }
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val baseColor = Color.BLACK
                val baseAlpha = ResourcesCompat.getFloat(resources, R.dimen.material_emphasis_medium)
                val offset = (slideOffset - (-1f)) / (1f - (-1f)) * (1f - 0f) + 0f
                val alpha = MathUtils.lerp(0f, 255f, offset * baseAlpha).toInt()
                val color = Color.argb(alpha, baseColor.red, baseColor.green, baseColor.blue)
                scrim.setBackgroundColor(color)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }
        })
        bottomAppBar.setNavigationOnClickListener {
            //val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            //inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken,0)
            scrim.visibility = View.VISIBLE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED }
        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.title.equals(getString(R.string.account))) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                auth()
            }
            true
        }
        val header = navigationView.getHeaderView(0)
        userIcon = header.findViewById(R.id.imageView)
        userName = header.findViewById(R.id.textView)
        userMail = header.findViewById(R.id.textView2)
        val fab = view.findViewById<FloatingActionButton>(R.id.floating_action_button)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && fab.isShown()) {
                    fab.hide()
                } else if (dy < 0 && !fab.isShown()) {
                    fab.show()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                /*if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show()
                }*/
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        val list = listOf<ToolsToAddPerson>( ToolsToAddPerson(R.drawable.ic_hand, getString(R.string.by_hand)),
                ToolsToAddPerson(R.drawable.ic_contact, getString(R.string.contacts)),
                ToolsToAddPerson(R.drawable.ic_vk, getString(R.string.vk)),
                ToolsToAddPerson(R.drawable.ic_facebook, getString(R.string.facebook)))
        fab.setOnClickListener { showDialogAddFriends(list) }
        val controller = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        navController = controller.navController
        return view
    }

    fun showDialogAddFriends(list: List<ToolsToAddPerson>) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.add_dialog_list)
        if (dialog.getWindow() != null) {
            dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        val listView: ListView = dialog.findViewById(R.id.add_list_variants)
        val arrayAdapter = ToolListAdapter(requireContext(), R.layout.add_item_dialog_layout,list)
        listView.adapter = arrayAdapter
        listView.setOnItemClickListener { adapterView, view, which,
                                          l ->
            when (which) {
                0 -> navController.navigate(MainFragmentDirections.actionAddPerson(-1))
                2 -> VK.login(requireActivity(),arrayListOf(VKScope.WALL, VKScope.FRIENDS))
                else -> Toast.makeText(requireContext(),"${list[which].name} was clicked",Toast.LENGTH_SHORT).show()
            }
            dialog.hide()
        }
        dialog.show()
    }

    fun showDialogAddVKFriends(list: List<Person>) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.vk_friends)
        if (dialog.getWindow() != null) {
            dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        val recyclerView: RecyclerView = dialog.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //val friendsAdapter = VkFriendsAdapter(list)
        //recyclerView.adapter = friendsAdapter
        val button : Button = dialog.findViewById(R.id.button)
        button.setOnClickListener {dialog.hide()}
        dialog.show()
    }

    fun auth() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build())
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers).setTheme(R.style.Theme_Birthday)
                .build(),
            RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val callback = getVkCallback()
        if (!VK.onActivityResult(requestCode, resultCode, data, callback)) {
            when(requestCode) {
                RC_SIGN_IN -> updateAccountUI(resultCode)
            }
        }
    }

    private fun getVkCallback () : VKAuthCallback = object: VKAuthCallback {

        val context = requireContext().applicationContext

        override fun onLogin(token: VKAccessToken) {
            Toast.makeText(context,"VK auth succeses",Toast.LENGTH_LONG).show()

        }
        override fun onLoginFailed(errorCode: Int) {
            Toast.makeText(context,"VK ERROR",Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAccountUI(resultCode: Int) {
        //val response = IdpResponse.fromResultIntent(data)
        if (resultCode == Activity.RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            userName.text =  user!!.displayName
            userMail.text = user.email
            Glide.with(requireActivity()).load(user.photoUrl).into(userIcon)
        } else {
            Toast.makeText(requireContext(),"ERROR",Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePersonUI(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            userName.text =  user!!.displayName
            userMail.text = user.email
            Glide.with(requireActivity()).load(user.photoUrl).into(userIcon)
        } else {
            Toast.makeText(requireContext(),"ERROR",Toast.LENGTH_SHORT).show()
        }
    }





    /*class VkFriendsAdapter(private val dataSet: List<Person>) :
            RecyclerView.Adapter<VkFriendsAdapter.VkFriendViewHolder>() {

        class VkFriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView
            //val birthday: TextView

            init {
                name = view.findViewById(R.id.person_name)
                //birthday = view.findViewById(R.id.person_birthday)
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): VkFriendViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.item_vk_friend, viewGroup, false)
            view.setOnClickListener {
                Toast.makeText(viewGroup.context, "Click", Toast.LENGTH_SHORT).show()
            }
            return VkFriendViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: VkFriendViewHolder, position: Int) {
            viewHolder.name.text = dataSet[position].name
            //viewHolder.birthday.text = dataSet[position].birthday
        }

        override fun getItemCount() = dataSet.size

    }*/

    class ToolsToAddPerson(val resId: Int, val name: String)

    class ToolListAdapter(context: Context, resource: Int, objects: List<ToolsToAddPerson>)
        : ArrayAdapter<ToolsToAddPerson?>(context, resource, objects) {

        private val userList: List<ToolsToAddPerson>

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater = LayoutInflater.from(context)
            val view: View = inflater.inflate(R.layout.add_item_dialog_layout, parent, false)
            val tools = userList[position]
            val icon = view.findViewById<ImageView>(R.id.add_item_image)
            icon.setImageResource(tools.resId)
            val name = view.findViewById<TextView>(R.id.add_item_name)
            name.text = tools.name
            return view
        }

        init {
            userList = objects
        }
    }

}