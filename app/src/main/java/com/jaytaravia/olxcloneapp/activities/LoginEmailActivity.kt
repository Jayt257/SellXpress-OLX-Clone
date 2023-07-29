package com.jaytaravia.olxcloneapp.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.jaytaravia.olxcloneapp.Utils
import com.jaytaravia.olxcloneapp.databinding.ActivityLoginEmailBinding

class LoginEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginEmailBinding

    private companion object{
        private const val TAG = "LOGIN_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get instance of firebase auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance()

        //init/setup ProgressDialog to show while sign-in
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle toolbarBackBtn click, go-back
        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        //handle noAccount click, open RegisterActivity to register user with email & Password
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterEmailActivity::class.java))
        }

        binding.forgotPasswordTv.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        //handle loginBtn click, start login
        binding.loginBtn.setOnClickListener {
            validateData()
        }

    }

    private var email = ""
    private var password = ""

    private fun validateData(){
        //input data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        Log.d(TAG, "validateData: email: $email")
        Log.d(TAG, "validateData: password: $password")

        //validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //email pattern is invalid, show error
            binding.emailEt.error = "Invalid Email Format"
            binding.emailEt.requestFocus()
        }
        else if(password.isEmpty()){
            //password is not entered, show error
            binding.passwordEt.error = "Enter Password"
            binding.passwordEt.requestFocus()
        }
        else{
            //email pattern is valid and password is entered. start login
            loginUser()
        }
    }

    private fun loginUser(){
        Log.d(TAG, "loginUser: ")

        //show progress
        progressDialog.setMessage("Logging In")
        progressDialog.show()

        //start user login
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //User login success
                Log.d(TAG, "loginUser: Logged In...")
                progressDialog.dismiss()

                //Start MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity() //finish current and all activities from back stack
            }
            .addOnFailureListener {e ->
                //User login failed
                Log.e(TAG, "loginUser:", e)
                progressDialog.dismiss()

                Utils.toast(this, "Unable to login due to ${e.message}")
            }
    }

}