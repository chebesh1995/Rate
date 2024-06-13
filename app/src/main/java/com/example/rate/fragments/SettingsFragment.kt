package com.example.rate.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.rate.databinding.FragmentSettingsBinding
import com.example.rate.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private var usersReference: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null
    private val requestCodes = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = ""
    private var socialChecker: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersReference =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        usersReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: User? = snapshot.getValue(User::class.java)
                    if (context != null) {
                        binding.usernameSettings.text = user!!.username
                        binding.userRateDisplay.text = user.rate.toString()
                        binding.userRateCountDisplay.text = (user.rateCount - 1000).toString()
                        Picasso.get().load(user.profile).into(binding.profileImageSettings)
                        Picasso.get().load(user.cover).into(binding.coverImageSettings)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        binding.profileImageSettings.setOnClickListener {
            pickImage()
        }

        binding.coverImageSettings.setOnClickListener {
            coverChecker = "cover"
            pickImage()
        }

        binding.setFacebook.setOnClickListener {
            socialChecker = "facebook"
            setSocialLinks()
        }

        binding.setInstagram.setOnClickListener {
            socialChecker = "instagram"
            setSocialLinks()
        }

        binding.setWebsite.setOnClickListener {
            socialChecker = "website"
            setSocialLinks()
        }
    }

    private fun setSocialLinks() {

        val builder: AlertDialog.Builder = AlertDialog.Builder(
            requireContext()
        )

        if (socialChecker == "website") {
            builder.setTitle("Добавьте ссылку URL:")
        } else {
            builder.setTitle("Введите имя пользователя:")
        }

        val editText = EditText(context)

        if (socialChecker == "website") {
            editText.hint = "https://vk.com/chebesh2013"
        } else {
            editText.hint = "e.g alize438"
        }
        builder.setView(editText)
        builder.setNegativeButton("Добавить", DialogInterface.OnClickListener { dilog, which ->
            val str = editText.text.toString()
            if (str == "") {
                Toast.makeText(context, "Напишите что нибудь ...", Toast.LENGTH_LONG).show()
            } else {
                saveSocialLink(str)
            }
        })
        builder.setPositiveButton("Отмена", DialogInterface.OnClickListener { dilog, which ->
            dilog.cancel()
        })
        builder.show()

    }

    private fun saveSocialLink(str: String) {
        val mapSocial = HashMap<String, Any>()

        when (socialChecker) {
            "facebook" -> {
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }
            "instagram" -> {
                mapSocial["instagram"] = "https://m.instagram.com/$str"
            }
            "website" -> {
                mapSocial["website"] = "https://vk.com/$str"
            }
        }
        usersReference!!.updateChildren(mapSocial).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(context, "Update succesfulity ...", Toast.LENGTH_LONG).show()

            }
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, requestCodes)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestCodes && resultCode == Activity.RESULT_OK && data!!.data != null) {

            imageUri = data.data
            Toast.makeText(context, "Загрузка...", Toast.LENGTH_LONG).show()
            uploadImageToDatabase()

        }
    }

    private fun uploadImageToDatabase() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Фото загружается, подождите")
        progressBar.show()

        if (imageUri != null) {
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")

            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if (coverChecker == "cover") {

                        val mapCoverImg = HashMap<String, Any>()
                        mapCoverImg["cover"] = url
                        usersReference!!.updateChildren(mapCoverImg)
                        coverChecker = ""

                    } else {

                        val mapProfileImg = HashMap<String, Any>()
                        mapProfileImg["profile"] = url
                        usersReference!!.updateChildren(mapProfileImg)
                        coverChecker = ""

                    }

                    progressBar.dismiss()
                }
            }
        }
    }
}