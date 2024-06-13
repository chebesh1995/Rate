package com.example.rate.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.rate.MessageChatActivity
import com.example.rate.R
import com.example.rate.VisitUserProfileActivity
import com.example.rate.model.Chat
import com.example.rate.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.Calendar

class UserAdapter(
    mContext: Context,
    mUsers: ArrayList<User>,
    isChatCheck: Boolean
) : RecyclerView.Adapter<UserAdapter.ViewHolder?>() {

    private lateinit var mDbRef: DatabaseReference

    private val mContext: Context
    private val mUsers: ArrayList<User>
    private val isChatCheck: Boolean
    var lastMsg: String = ""

    init {
        this.mUsers = mUsers
        this.mContext = mContext
        this.isChatCheck = isChatCheck
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mDbRef = FirebaseDatabase.getInstance().reference
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: User = mUsers[position]
        holder.userNameTxt?.text = user.username
        holder.userRate.text = user.rate.toString()
        holder.rateCount.text = (user.rateCount - 1000).toString()
        Picasso.get().load(user.profile).placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.profileImageView)

        if (isChatCheck) {
            retrieveLastMessage(user.uid, holder.lastMessageTxt)
            holder.personalDataLl.visibility = View.GONE
        } else {
            holder.lastMessageLl.visibility = View.GONE
        }//

        if (isChatCheck) {
            if (user.status == "online") {
                holder.onlineTxt.visibility = View.VISIBLE
                holder.offlineTxt.visibility = View.GONE
            } else {
                holder.onlineTxt.visibility = View.GONE
                holder.offlineTxt.visibility = View.VISIBLE
            }
        } else {
            holder.onlineTxt.visibility = View.GONE
            holder.offlineTxt.visibility = View.GONE
        }

        holder.profileImageCv.setOnClickListener {
            val intent = Intent(mContext, VisitUserProfileActivity::class.java)
            intent.putExtra("visit_id", user.uid)
            mContext.startActivity(intent)
        }
        holder.lastMessageLl.setOnClickListener {
            val intent = Intent(mContext, MessageChatActivity::class.java)
            intent.putExtra("visit_id", user.uid)
            mContext.startActivity(intent)
        }
        holder.personalDataLl.setOnClickListener {
            showRatingDialog(user)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameTxt: TextView? = null
        var profileImageView: ImageView
        var rateCount: TextView
        var onlineTxt: TextView
        var offlineTxt: TextView
        var lastMessageTxt: TextView
        var userRate: TextView
        var lastMessageLl: LinearLayout
        var personalDataLl: LinearLayout
        var profileImageCv: CardView

        init {
            userNameTxt = itemView.findViewById(R.id.username)
            userRate = itemView.findViewById(R.id.user_rate)
            profileImageView = itemView.findViewById(R.id.profile_image)
            rateCount = itemView.findViewById(R.id.rateCount)
            onlineTxt = itemView.findViewById(R.id.image_online)
            offlineTxt = itemView.findViewById(R.id.image_offline)
            lastMessageTxt = itemView.findViewById(R.id.message_last)
            lastMessageLl = itemView.findViewById(R.id.message_last_ll)
            personalDataLl = itemView.findViewById(R.id.personal_data_user_ll)
            profileImageCv = itemView.findViewById(R.id.profile_image_cv)

        }

    }

    private fun retrieveLastMessage(chatUserId: String, lastMessageTxt: TextView) {

        lastMsg = "defaultMsg"
        val firebaseUsers = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children) {
                    val chat: Chat? = dataSnapshot.getValue(Chat::class.java)
                    if (firebaseUsers != null && chat != null) {
                        if (chat.getReceiver() == firebaseUsers!!.uid &&
                            chat.getSender() == chatUserId ||
                            chat.getReceiver() == chatUserId &&
                            chat.getSender() == firebaseUsers!!.uid
                        ) {
                            lastMsg = chat.getMessage()!!
                        }
                    }
                }
                when (lastMsg) {
                    "defaultMsg" -> lastMessageTxt.text = "No Message"
                    "sent you an image" -> lastMessageTxt.text = "image sent."
                    else -> lastMessageTxt.text = lastMsg
                }
                lastMsg = "defaultMsg"
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun showRatingDialog(user: User) {
        val dialogBuilder = AlertDialog.Builder(mContext)
        val dialogView = LayoutInflater.from(mContext).inflate(R.layout.rating_dialog, null)

        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val submitButton = dialogView.findViewById<Button>(R.id.submitButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        dialogBuilder.setView(dialogView)
        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(mContext.getDrawable(R.drawable.dialog_background))

        submitButton.isEnabled = false
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            submitButton.isEnabled = rating > 0.0
        }

        submitButton.setOnClickListener {
            val rating = ratingBar.rating.toDouble()

            val updatedUser = user.addRating(rating)

            val userIndex = mUsers.indexOfFirst { it.uid == user.uid }
            if (userIndex != -1) {
                mUsers[userIndex] = updatedUser
                notifyItemChanged(userIndex)
                updateRatingInFirebase(updatedUser)
            }
            dialog.dismiss()
        }
        cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun updateRatingInFirebase(user: User) {
        mDbRef.child("Users").child(user.uid).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(mContext, "Рейтинг обновлен", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    mContext,
                    "Рейтинг не сохранен: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    /* holder.itemView.setOnClickListener {

            val options = arrayOf<CharSequence>(
                "Написать сообщение",
                "Перейти в профиль",
                "Поставить рейтинг"
            )
            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("Что вы хотите?")
            builder.setItems(options, DialogInterface.OnClickListener { dialog,position ->
                if (position == 0) {
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.putExtra("visit_id", user.getUid())
                    mContext.startActivity(intent)
                }
                if (position == 1){
                    val intent = Intent(mContext, VisitUserProfileActivity::class.java)
                    intent.putExtra("visit_id", user.getUid())
                    mContext.startActivity(intent)
                }
                if (position == 2){
                    showRatingDialog(user)
                }
            })
            builder.show()
        }*/
}