package com.example.rate.notification

import com.example.rate.model.Token
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseInstanceId : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refreshToken = FirebaseMessaging.getInstance().token

        if (firebaseUser != null){

            updateToken(refreshToken)
        }
    }

    private fun updateToken(refreshToken: Task<String>) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val tokenString = await(refreshToken)
        val token = Token(tokenString)
        ref.child(firebaseUser!!.uid).setValue(token)
    }
}