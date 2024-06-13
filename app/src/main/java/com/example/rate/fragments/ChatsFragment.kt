package com.example.rate.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.rate.adapter.UserAdapter
import com.example.rate.databinding.FragmentChatsBinding
import com.example.rate.model.ChatList
import com.example.rate.model.Token
import com.example.rate.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class ChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding
    private var userAdapter: UserAdapter? = null
    private var mUsers: List<User>? = null
    private var usersChatList: List<ChatList>? = null
    private var firebaseUser: FirebaseUser? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewChatList.setHasFixedSize(true)
        binding.recyclerViewChatList.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersChatList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                (usersChatList as ArrayList).clear()
                for (dataSnapshot in p0.children){
                    val chatList = dataSnapshot.getValue(ChatList::class.java)
                    (usersChatList as ArrayList).add(chatList!!)
                }
                retrieveChatLists()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        updateToken(FirebaseMessaging.getInstance().token)

    }

    private fun updateToken(token: Task<String>) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val tokenString = task.result
                val token1 = Token(tokenString)
                ref.child(firebaseUser!!.uid).setValue(token1)
            }
        }
    }

    private fun retrieveChatLists() {
        mUsers = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                (mUsers as ArrayList).clear()
                for (dataSnapshot in p0.children) {
                    val user = dataSnapshot.getValue(User::class.java)

                    for (eachChatList in usersChatList!!) {
                        if (user!!.uid == eachChatList.id) {
                            (mUsers as ArrayList).add(user!!)
                        }
                    }
                }
                userAdapter = context?.let { UserAdapter(it, (mUsers as ArrayList<User>), true) }
                binding.recyclerViewChatList.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}