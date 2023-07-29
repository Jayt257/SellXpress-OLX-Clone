package com.jaytaravia.olxcloneapp.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.jaytaravia.olxcloneapp.Utils
import com.jaytaravia.olxcloneapp.databinding.ActivityRegisterEmailBinding

class RegisterEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterEmailBinding

    private companion object{
        private const val TAG = "REGISTER_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //init/setup ProgressDialog to show while sign up
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.haveAccountTv.setOnClickListener {
            onBackPressed()
        }

        binding.registerBtn.setOnClickListener {
            validateData()
        }

    }

    private var email = ""
    private var password = ""
    private var cPassword = ""

    private fun validateData(){
        //input data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        cPassword = binding.cPasswordEt.text.toString().trim()

        Log.d(TAG, "validateData: email :$email")
        Log.d(TAG, "validateData: password : $password")
        Log.d(TAG, "validateData: confirm password :$cPassword")

        //validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            binding.emailEt.error = "Invalid Email Pattern"
            binding.emailEt.requestFocus()
        }
        else if (password.isEmpty()){

            binding.passwordEt.error = "Enter Password"
            binding.passwordEt.requestFocus()
        }
        else if (cPassword.isEmpty()){

            binding.cPasswordEt.error = "Enter Confirm Password"
            binding.cPasswordEt.requestFocus()
        }

        else if (password != cPassword){

            binding.cPasswordEt.error = "Password Doesn't Match"
            binding.cPasswordEt.requestFocus()
        }
        else{
            //all data is valid, start sign-up
            registerUser()
        }

    }

    private fun registerUser(){
        Log.d(TAG, "registerUser: ")
        //show progress
        progressDialog.setMessage("Creating account")
        progressDialog.show()

        //start user sign-up
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //User Register success, We also need to firebase db
                Log.d(TAG, "registerUser: Register Success")
                updateUserInfo()

            }
            .addOnFailureListener { e ->
                Log.e(TAG, "registerUser: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to create account due to ${e.message}")
            }
    }


    private fun updateUserInfo(){
        Log.d(TAG, "updateUserInfo: ")
        //change progress dialog message
        progressDialog.setMessage("Saving User Info")

        val timestamp = Utils.getTimestamp()
        val registredUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserUid = firebaseAuth.uid

        //setup data to save in firebase realtime db. most of the data will be empty and will set in edit profile
        val hashMap = HashMap<String, Any>()
        hashMap["name"] = ""
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Email"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = "$registredUserEmail"
        hashMap["uid"] = "$registeredUserUid"

        //set data to firebase db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //Firebase db save success
                Log.d(TAG, "updateUserInfo: User registered...")
                progressDialog.dismiss()

                //Start MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity() //finish current and all activities from back stack
            }
            .addOnFailureListener {e ->
                //Firebase db save failed
                Log.e(TAG, "updateUserInfo: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to save user info due to ${e.message}")

            }
    }

}