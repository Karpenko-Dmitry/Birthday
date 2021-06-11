package ru.mephi.birthday.context

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.*
import com.facebook.AccessToken
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.math.MathUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import org.json.JSONArray
import org.json.JSONException
import ru.mephi.birthday.PersonViewModel
import ru.mephi.birthday.PersonViewModelFactory
import ru.mephi.birthday.R
import ru.mephi.birthday.adapters.AddListFriendAdapter
import ru.mephi.birthday.adapters.NewPerson
import ru.mephi.birthday.adapters.PersonListAdapter
import ru.mephi.birthday.database.Person
import ru.mephi.birthday.database.UserState


class MainFragment : Fragment() {


    companion object {
        val REQUEST_CODE_READ_CONTACTS = 1233
        var READ_CONTACTS_GRANTED = false


        fun showFriendList(context: Context, list : List<NewPerson>) {
            val dialog = Dialog(context, android.R.style.ThemeOverlay_Material_Light)
            //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.friend_list)
            if (dialog.getWindow() != null) {
                dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            val recyclerView: RecyclerView = dialog.findViewById(R.id.recycler_view)
            val adapter = AddListFriendAdapter(list)
            recyclerView.adapter = adapter
            val button: FloatingActionButton = dialog.findViewById(R.id.floatingActionButton)
            button.setOnClickListener {
                val list = adapter.dataset
                val rep = (context.applicationContext as MyApplication).repository
                for (el in list) {
                    if (el.add) {
                        val person  = Person(el.id,el.name,1,0,null,UserState.INVALID,null)
                        rep.insert(person)
                    }
                }
                dialog.hide()
            }
            dialog.show()
        }

        public fun loadContacts(context: Context): List<NewPerson> {
            val listPerson = ArrayList<NewPerson>()
            val cursor = context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            if (cursor != null && cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val name =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val isUser = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.IS_USER_PROFILE))

                    var phone: String? = null
                    var email: String? = null
                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        val cur = context.getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        if (cur != null) {
                            while (cur.moveToNext()) {
                                phone = cur.getString(
                                    cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                )
                                email = cur.getString(
                                    cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME)
                                )
                            }
                            cur.close()
                        }
                        listPerson.add(NewPerson(null,name,phone,true))
                    }
                }
            }
            cursor?.close()
            return listPerson
        }
    }

    private lateinit var navController: NavController
    private lateinit var userIcon: ImageView
    private lateinit var userName: TextView
    private lateinit var userMail: TextView
    private val callbackManager = CallbackManager.Factory.create()

    private var auth: FirebaseAuth = Firebase.auth

    val adapter = PersonListAdapter()
    private val personViewModel: PersonViewModel by viewModels {
        PersonViewModelFactory((requireActivity().application as MyApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        personViewModel.people.observe(this, Observer { people ->
            people?.let {
                adapter.submitList(
                    it
                )
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateAccountUI(currentUser)
        }
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
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val baseColor = Color.BLACK
                val baseAlpha = ResourcesCompat.getFloat(
                    resources,
                    R.dimen.material_emphasis_medium
                )
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
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.title.equals(getString(R.string.account))) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            when (menuItem.title) {
                getString(R.string.account) -> {
                    navController.navigate(R.id.to_accountFragment)
                }
                getString(R.string.notification) -> navController.navigate(R.id.actionNotificationSettings)
            }
            true
        }
        val header = navigationView.getHeaderView(0)
        userIcon = header.findViewById(R.id.imageView)
        userName = header.findViewById(R.id.textView)
        userMail = header.findViewById(R.id.textView2)
        val fab = view.findViewById<FloatingActionButton>(R.id.add_new_person)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            layoutManager.orientation
        )
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
        val list = listOf(
            ToolsToAddPerson(R.drawable.ic_hand, getString(R.string.by_hand)),
            ToolsToAddPerson(R.drawable.ic_contact, getString(R.string.contacts)),
            ToolsToAddPerson(R.drawable.ic_facebook, getString(R.string.facebook))
        )
        fab.setOnClickListener { showDialogAddFriends(list) }
        val controller =
            requireActivity().supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        navController = controller.navController
        return view
    }

    fun showDialogAddFriends(list: List<ToolsToAddPerson>) {
        val dialog = Dialog(requireContext())
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.add_dialog_list)
        if (dialog.getWindow() != null) {
            dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        val listView: ListView = dialog.findViewById(R.id.add_list_variants)
        val arrayAdapter = ToolListAdapter(requireContext(), R.layout.add_item_dialog_layout, list)
        listView.adapter = arrayAdapter
        listView.setOnItemClickListener { adapterView, view, which,
                                          l ->
            when (which) {
                0 -> navController.navigate(MainFragmentDirections.actionAddPerson())
                1 -> showContacts()
                2 -> {
                    dialog.hide()
                    showSocialNetworkList()
                }
                //2 -> VK.login(requireActivity(), arrayListOf(VKScope.WALL, VKScope.FRIENDS))
                else -> Toast.makeText(
                    requireContext(),
                    "${list[which].name} was clicked",
                    Toast.LENGTH_SHORT
                ).show()
            }
            dialog.hide()
        }
        dialog.show()
    }

    private fun showSocialNetworkList() {
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        if (isLoggedIn) {
            /*GraphRequest(
                accessToken,
                "/{friend-list-id}",
                null,
                HttpMethod.GET
            ) { response ->
                Log.d("FacebookFriends","Array ${response.jsonArray}; Error: ${response.error.errorCode}")
                Log.d("FacebookFriends","Permissions ${accessToken.permissions}")
            }.executeAsync()*/
            val graphReq = GraphRequest.newMyFriendsRequest(accessToken,object:GraphRequest.GraphJSONArrayCallback {
                override fun onCompleted(objects: JSONArray?, response: GraphResponse?) {
                    //Log.d("FacebookFriends","Array: $objects")
                    val list = ArrayList<NewPerson>();
                    if (objects != null) {
                        for (i in 0..objects.length()-1) {
                            val obj = objects.getJSONObject(i)
                            list.add(NewPerson(obj.getString("id"),obj.getString("name")))
                        }
                    }
                    showFriendList(requireContext(),list)
                }
            })
            graphReq.executeAsync()
        } else {
            facebookLogin()
        }
    }

    /*
    //showFriendList(requireContext(),getFacebookFriends())
            val graphRequest = GraphRequest.newMeRequest(
                accessToken
            ) { jsonObject, graphResponse ->
                try {
                    val jsonArrayFriends =
                        jsonObject.getJSONObject("friendlist").getJSONArray("data")
                    val friendlistObject = jsonArrayFriends.getJSONObject(0)
                    val friendListID = friendlistObject.getString("id")
                    myNewGraphReq(friendListID)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            val param = Bundle()
            param.putString("fields", "friendlist")
            graphRequest.parameters = param
            graphRequest.executeAsync()

     */

    private fun myNewGraphReq(friendlistId: String) {
        val graphPath = "/$friendlistId/members/"
        val token = AccessToken.getCurrentAccessToken()
        val request = GraphRequest(
            token, graphPath, null, HttpMethod.GET
        ) { graphResponse ->
            val `object` = graphResponse.jsonObject
            try {
                val arrayOfUsersInFriendList = `object`.getJSONArray("data")
                /* Do something with the user list */
                /* ex: get first user in list, "name" */
                val user = arrayOfUsersInFriendList.getJSONObject(0)
                val usersName = user.getString("name")
                Toast.makeText(requireContext(),"$usersName",Toast.LENGTH_SHORT).show()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val param = Bundle()
        param.putString("fields", "name")
        request.parameters = param
        request.executeAsync()
    }

    private fun getFacebookFriends() : List<NewPerson> {
        return java.util.ArrayList()
    }

    private fun facebookLogin() {
        val dialog = Dialog(requireContext())
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.add_friend_social_net)
        val loginButton: LoginButton = dialog.findViewById(R.id.facebook_login_button)
        loginButton.setPermissions("user_friends")
        loginButton.setFragment(this)
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                Toast.makeText(requireContext(),"onSuccess",Toast.LENGTH_SHORT).show()
                dialog.hide()
            }

            override fun onCancel() {
                Toast.makeText(requireContext(),"onCancel()",Toast.LENGTH_SHORT).show()
                dialog.hide()
            }

            override fun onError(exception: FacebookException) {
                Toast.makeText(requireContext(),"onError",Toast.LENGTH_SHORT).show()
                dialog.hide()
            }
        })
        dialog.show()
    }

    private fun showContacts() {
        val context = requireContext()
        val hasReadContactsPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        )
        if (hasReadContactsPermission == PackageManager.PERMISSION_GRANTED) {
            READ_CONTACTS_GRANTED = true;
            showFriendList(context, loadContacts(context))
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_CODE_READ_CONTACTS
            )
        }
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
        val button: Button = dialog.findViewById(R.id.button)
        button.setOnClickListener { dialog.hide() }
        dialog.show()
    }

    private fun facebookGetFriends(context: Context) {
        FacebookSdk.sdkInitialize(context)
        val callback = GraphRequest.Callback { res ->
            Toast.makeText(context, "Facebook Callback",Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "Error: ${res.error.errorCode}",Toast.LENGTH_LONG).show()
        }
        GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/{friend-list-id}",
            null,
            HttpMethod.GET,
            callback
        ).executeAsync()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode,resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        val callback = getVkCallback()
        if (!VK.onActivityResult(requestCode, resultCode, data, callback)) {
            when (requestCode) {
                //RC_SIGN_IN -> updateAccount(resultCode)
            }
        }
    }

    private fun getVkCallback(): VKAuthCallback = object : VKAuthCallback {

        val context = requireContext().applicationContext

        override fun onLogin(token: VKAccessToken) {
            Toast.makeText(context, "VK auth succeses", Toast.LENGTH_LONG).show()

        }

        override fun onLoginFailed(errorCode: Int) {
            Toast.makeText(context, "VK ERROR", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAccount(resultCode: Int) {
        //val response = IdpResponse.fromResultIntent(data)
        if (resultCode == Activity.RESULT_OK) {

        } else {
            Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAccountUI(user: FirebaseUser) {
        //val user = FirebaseAuth.getInstance().currentUser
        userName.text = user.displayName
        userMail.text = user.email
        Glide.with(requireActivity()).load(user.photoUrl).into(userIcon)
    }

    private fun updatePersonUI(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            userName.text = user!!.displayName
            userMail.text = user.email
            Glide.with(requireActivity()).load(user.photoUrl).into(userIcon)
        } else {
            Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_SHORT).show()
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

    class ToolListAdapter(context: Context, resource: Int, objects: List<ToolsToAddPerson>) :
        ArrayAdapter<ToolsToAddPerson?>(context, resource, objects) {

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