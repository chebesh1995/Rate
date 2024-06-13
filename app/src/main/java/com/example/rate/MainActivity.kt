package com.example.rate

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.rate.databinding.ActivityMainBinding
import com.example.rate.fragments.ChatsFragment
import com.example.rate.fragments.SearchFragment
import com.example.rate.fragments.SettingsFragment
import com.example.rate.model.Chat
import com.example.rate.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    var refUsers : DatabaseReference? = null
    var firebaseUsers : FirebaseUser? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUsers = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUsers!!.uid)

        setSupportActionBar(binding.toolbarMain)
        supportActionBar!!.title = ""

        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref!!.addValueEventListener(object : ValueEventListener {


            override fun onDataChange(p0: DataSnapshot) {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                var countUnreadMessages = 0

                for (dataSnapshot in p0.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUsers!!.uid) && !chat.isIsSeen()) {

                        countUnreadMessages += 1

                    }
                }
                viewPagerAdapter.addFragment(SearchFragment(), getString(R.string.search))
                val chatsTitle = getString(R.string.chats)
                if (countUnreadMessages == 0) {
                    viewPagerAdapter.addFragment(ChatsFragment(), chatsTitle)
                } else {
                   // val titleWithCount = "($countUnreadMessages) $chatsTitle"
                    viewPagerAdapter.addFragment(ChatsFragment(), chatsTitle)
                }
                viewPagerAdapter.addFragment(SettingsFragment(), getString(R.string.profile))
                binding.viewPager.adapter = viewPagerAdapter
                binding.tabLayout.setupWithViewPager(binding.viewPager)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        refUsers!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user: User? = snapshot.getValue(User::class.java)
                    binding.userName.text = user!!.username
                    Picasso.get().load(user.profile).placeholder(R.drawable.ic_launcher_foreground).into(binding.profileImage)
                    binding.userRate.text = user.rate.toString()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when (item.itemId) {
             R.id.action_logout -> {
                 FirebaseAuth.getInstance().signOut()
                 val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                 startActivity(intent)
                 finish()

                 return true
             }
            else -> super.onOptionsItemSelected(item)
        }
        return false
    }

    internal class ViewPagerAdapter(fragmentManager:FragmentManager) :
        FragmentPagerAdapter(fragmentManager) {

        private val fragments: ArrayList<Fragment> = ArrayList<Fragment>()
        private val titles: ArrayList<String> = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(i: Int): CharSequence? {
            return titles[i]
        }
    }

    private fun updateStatus(status: String){

        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUsers!!.uid)

        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        ref!!.updateChildren(hashMap)

    }

    override fun onResume() {
        super.onResume()
        updateStatus("online")
    }

    override fun onPause() {
        super.onPause()
        updateStatus("offline")
    }
}