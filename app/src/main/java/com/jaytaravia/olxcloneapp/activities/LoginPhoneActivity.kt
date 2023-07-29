package com.jaytaravia.olxcloneapp.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.FirebaseDatabase
import com.jaytaravia.olxcloneapp.Utils
import com.jaytaravia.olxcloneapp.databinding.ActivityLoginPhoneBinding
import java.util.concurrent.TimeUnit

class LoginPhoneActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginPhoneBinding

    private companion object{

        private const val TAG = "PHONE_LOGIN_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var forceRefreshingToken: ForceResendingToken? = null

    private lateinit var mCallbacks: OnVerificationStateChangedCallbacks

    private var mVerificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneInputRl.visibility = View.VISIBLE
        binding.otpInputRl.visibility = View.GONE


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        //listen for phone login callbacks. Hint: you may put here instead of creating a function
        phoneLoginCallBacks()

        //handle toolbarBackBtn clcik, go-back
        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.sendOtpBtn.setOnClickListener {
            validateData()
        }

        binding.resendOtpTv.setOnClickListener {
            resendVerificationCode(forceRefreshingToken)
        }

        binding.verifyOtpBtn.setOnClickListener {

            val otp = binding.otpEt.text.toString().trim()
            Log.d(TAG, "onCreate: otp: $otp")

            if (otp.isEmpty()){

                binding.otpEt.error = "Enter OTP"
                binding.otpEt.requestFocus()
            }
            else if (otp.length < 6){

                binding.otpEt.error = "OTP length must be 6 characters long"
                binding.otpEt.requestFocus()
            }
            else{
                verifyPhoneNumberWithCode(mVerificationId, otp)
            }
        }

    }




    private var phoneCode = ""
    private var phoneNumber = ""
    private var phoneNumberWithCode = ""

    private fun validateData(){

        phoneCode = binding.phoneCodeTil.selectedCountryCodeWithPlus
        phoneNumber = binding.phoneNumberEt.text.toString().trim()
        phoneNumberWithCode = phoneCode + phoneNumber

        Log.d(TAG, "validateData: phoneCode: $phoneCode")
        Log.d(TAG, "validateData: phoneNumber: $phoneNumber")
        Log.d(TAG, "validateData: phoneNumberWithCode: $phoneNumberWithCode")

        if (phoneNumber.isEmpty()){

            binding.phoneNumberEt.error = "Enter Phone Number"
            binding.phoneNumberEt.requestFocus()
        }
        else{
            startPhoneNumberVerification()
        }

    }

    private fun startPhoneNumberVerification(){
        Log.d(TAG, "startPhoneNumberVerification: ")
        //show progress
        progressDialog.setMessage("Sending OTP to $phoneNumberWithCode")
        progressDialog.show()
        //setup phone auth options
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumberWithCode) //Phone Number with country code e.g. +91**********
            .setTimeout(60L, TimeUnit.SECONDS) //Timeout and unit
            .setActivity(this) //Activity (for callback binding)
            .setCallbacks(mCallbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun phoneLoginCallBacks(){
        Log.d(TAG, "phoneLoginCallBacks: ")

        mCallbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Log.d(TAG, "OnVerificationStateChangedCallbacks: ")

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                Log.e(TAG, "onVerificationFailed: ", e)

                progressDialog.dismiss()

                Utils.toast(this@LoginPhoneActivity, "${e.message}")

            }

            override fun onCodeSent(verificationId: String, token: ForceResendingToken) {

                Log.d(TAG, "onCodeSent: verificationId: $verificationId")

                mVerificationId = verificationId
                forceRefreshingToken = token

                progressDialog.dismiss()

                binding.phoneInputRl.visibility = View.GONE
                binding.otpInputRl.visibility = View.VISIBLE

                //Show toast for success sending OTP
                Utils.toast(this@LoginPhoneActivity, "OTP is sent to $phoneNumberWithCode")
                //show user a message that Please type the verification code sent to the phone number user has input
                binding.loginLableTv.text = "Please type the verification code sent to $phoneNumberWithCode"

            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {

            }
        }
    }



    private fun verifyPhoneNumberWithCode(verificationId: String?, otp: String) {
        Log.d(TAG, "verifyPhoneNumberWithCode: verificationId: $verificationId")
        Log.d(TAG, "verifyPhoneNumberWithCode: otp: $otp")

        progressDialog.setMessage("Verifying OTP")
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(token: ForceResendingToken?){
        Log.d(TAG, "resendVerificationCode: ")

        //show progress
        progressDialog.setMessage("Resending OTP to $phoneNumberWithCode")
        progressDialog.show()
        //setup phone auth options
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumberWithCode) //Phone Number with country code e.g. +91**********
            .setTimeout(60L, TimeUnit.SECONDS) //Timeout and unit
            .setActivity(this) //Activity (for callback binding)
            .setCallbacks(mCallbacks)
            .setForceResendingToken(token!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.d(TAG, "signInWithPhoneAuthCredential: ")

        progressDialog.setMessage("Logging In")
        progressDialog.show()

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                Log.d(TAG, "signInWithPhoneAuthCredential: Success")

                if (authResult.additionalUserInfo!!.isNewUser){

                    Log.d(TAG, "signInWithPhoneAuthCredential: New User, Account Created")
                    updateUserInfoDb()
                }
                else{
                    Log.d(TAG, "signInWithPhoneAuthCredential: Existing User, Logged In")

                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {e->

                Log.e(TAG, "signInWithPhoneAuthCredential: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to login due to ${e.message}")
            }
    }

    private fun updateUserInfoDb(){
        Log.d(TAG, "updateUserInfoDb: ")

        progressDialog.setMessage("Saving User Info")
        progressDialog.show()

        val timestamp = Utils.getTimestamp()
        val registeredUserUid = firebaseAuth.uid

        val hashMap = HashMap<String, Any?>()
        hashMap["name"] = ""
        hashMap["phoneCode"] = "$phoneCode"
        hashMap["phoneNumber"] = "$phoneNumber"
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Phone" //possible values Email/Phone/Google
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = ""
        hashMap["uid"] = registeredUserUid

        //set data to firebase db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //User info save success
                Log.d(TAG, "updateUserInfoDb: User info saved")
                progressDialog.dismiss()
                //Start MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener {e ->
                //User info save failed
                Log.e(TAG, "updateUserInfoDb: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to save user info due to ${e.message}")
            }

    }

}