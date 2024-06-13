package com.example.rate.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rate.adapter.UserAdapter
import com.example.rate.databinding.FragmentSearchBinding
import com.example.rate.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private var userAdapter: UserAdapter? = null
    private var mUsers: ArrayList<User>? = null
    private var recyclerView: RecyclerView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.searchList
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        mUsers = ArrayList()
        retrieveAllUsers()

        binding.searchUsersEt!!.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchForUsers(p0.toString().toLowerCase())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

    }

    private fun retrieveAllUsers() {

        val firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")

        refUsers.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<User>).clear()
                if (binding.searchUsersEt!!.text.toString() == ""){
                    for (snapshot in snapshot.children) {
                        val user: User? = snapshot.getValue(User::class.java)
                        if ((user!!.uid) != firebaseUserId){
                            (mUsers as ArrayList<User>).add(user)
                        }
                    }
                    mUsers!!.sortWith(compareByDescending { it.rate })
                    if (context != null && mUsers != null) {
                        userAdapter = UserAdapter(context!!, mUsers!!, false)
                    }
                    recyclerView!!.adapter = userAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun searchForUsers(str:String){

        val firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUsers = FirebaseDatabase.getInstance().reference
            .child("Users").orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")

        queryUsers.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<User>).clear()
                for (snapshot in snapshot.children) {
                    val user: User? = snapshot.getValue(User::class.java)
                    if ((user!!.uid) != firebaseUserId){
                        (mUsers as ArrayList<User>).add(user)
                    }
                }
                if (context != null) {
                    userAdapter = UserAdapter(context!!, mUsers!!, false);
                }
                recyclerView!!.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}