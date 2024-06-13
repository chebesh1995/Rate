package com.example.rate

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rate.databinding.ActivityVisitUserProfileBinding
import com.example.rate.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class VisitUserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVisitUserProfileBinding
    private var userVisitId: String = ""
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarProfile)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbarProfile.setNavigationOnClickListener {
            val intent = Intent(this@VisitUserProfileActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        userVisitId = intent.getStringExtra("visit_id").toString()
        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(userVisitId)
        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    user = p0.getValue(User::class.java)
                    binding.usernameDisplay.text = user!!.username
                    supportActionBar!!.title = user!!.username
                    binding.userRateDisplay.text = user!!.rate.toString()
                    binding.userRateCountDisplay.text = (user!!.rateCount - 1000).toString()
                    Picasso.get().load(user!!.profile).into(binding.profileDisplay)
                    Picasso.get().load(user!!.cover).into(binding.coverDisplay)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        binding.facebookDisplay.setOnClickListener {
            val uri = Uri.parse(user!!.facebook)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        binding.instagramDisplay.setOnClickListener {
            val uri = Uri.parse(user!!.instagram)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        binding.websiteDisplay.setOnClickListener {
            val uri = Uri.parse(user!!.website)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        binding.sendMsgBtn.setOnClickListener {
            val intent = Intent(this@VisitUserProfileActivity, MessageChatActivity::class.java)
            intent.putExtra("visit_id",user!!.uid)
            startActivity(intent)
        }
    }
}