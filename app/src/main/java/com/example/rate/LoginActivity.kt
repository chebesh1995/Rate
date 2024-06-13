package com.example.rate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.rate.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarLogin)
        supportActionBar!!.title = "Логин"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbarLogin.setNavigationOnClickListener {
            val intent = Intent(this@LoginActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        binding.loginBtn.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val password = binding.passwordLogin.text.toString()
        val email = binding.emailLogin.text.toString()

        if (password.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Введите пароль", Toast.LENGTH_LONG)
                .show()
        } else if (email.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Введите email", Toast.LENGTH_LONG).show()
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Ошибка" + task.exception!!.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}