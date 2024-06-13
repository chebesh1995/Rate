package com.example.rate

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.rate.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    private var profileImageUrl: String =
        "https://firebasestorage.googleapis.com/v0/b/rateapp-1806f.appspot.com/o/icon-user.png?alt=media&token=c9591be9-cafb-4279-8d94-caffec120a11"
    private val PICK_IMAGE_REQUEST_CODE = 438


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarRegister)
        supportActionBar!!.title = "Регистрация"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbarRegister.setNavigationOnClickListener {
            val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        binding.registerBtn.setOnClickListener {
            registerUser()
        }

        binding.profilePhotoRegister.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data!!.data != null) {
            val selectedImageUri = data.data
            Toast.makeText(this, "Загрузка...", Toast.LENGTH_LONG).show()
            if (selectedImageUri != null) {
                uploadUserProfileImage(selectedImageUri)
            }
        }
    }

    private fun uploadUserProfileImage(filePath: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference.child("User Images")
        val filename = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")

        filename.putFile(filePath)
            .addOnSuccessListener {
                Toast.makeText(this, "Фото профиля успешно загружено", Toast.LENGTH_LONG).show()
                filename.downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        profileImageUrl = task.result.toString()
                        Picasso.get().load(profileImageUrl).into(binding.profilePhotoRegister)
                    } else {
                        Toast.makeText(this, "Ошибка загрузки URL фото профиля", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Ошибка загрузки фото профиля: $exception", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun registerUser() {
        val userName = binding.usernameRegister.text.toString()
        val email = binding.emailRegister.text.toString()
        val password = binding.passwordRegister.text.toString()

        if (userName.isEmpty()) {
            Toast.makeText(this@RegisterActivity, "Введите имя пользователя", Toast.LENGTH_LONG)
                .show()
        } else if (email.isEmpty()) {
            Toast.makeText(this@RegisterActivity, "Введите email", Toast.LENGTH_LONG).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this@RegisterActivity, "Введите пароль", Toast.LENGTH_LONG).show()
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUserId = mAuth.currentUser!!.uid
                        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")
                            .child(firebaseUserId)

                        val userHashMap = hashMapOf(
                            "uid" to firebaseUserId,
                            "username" to userName,
                            "profile" to profileImageUrl,
                            "cover" to "https://firebasestorage.googleapis.com/v0/b/rateapp-1806f.appspot.com/o/icon-user.png?alt=media&token=c9591be9-cafb-4279-8d94-caffec120a11",
                            "status" to "offline",
                            "search" to userName.toLowerCase(),
                            "facebook" to "https://m.facebook.com",
                            "instagram" to "https://m.instagram.com",
                            "website" to "https://www.google.com",
                            "rate" to 0.1,
                            "rateCount" to 1000,
                        )

                        refUsers.updateChildren(userHashMap as Map<String, Any>)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    startActivity(
                                        Intent(this@RegisterActivity, MainActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Ошибка: ${task.exception!!.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Ошибка" + task.exception!!.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}