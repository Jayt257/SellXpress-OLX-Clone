package com.jaytaravia.olxcloneapp.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jaytaravia.olxcloneapp.Utils
import com.jaytaravia.olxcloneapp.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding : ActivityChangePasswordBinding


    private companion object {

        private const val TAG = "CHANGE_PASSWORD_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)


        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var currentPassword = ""
    private var newPassword = ""
    private var confirmNewPassword = ""

    private fun validateData(){

        currentPassword = binding.currentPasswordEt.text.toString().trim()
        newPassword = binding.newPasswordEt.text.toString().trim()
        confirmNewPassword = binding.confirmNewPasswordEt.text.toString().trim()

        Log.d(TAG, "validateData: $currentPassword")
        Log.d(TAG, "newPassword: $newPassword")
        Log.d(TAG, "confirmNewPassword: $confirmNewPassword")

        if (currentPassword.isEmpty()){

            binding.currentPasswordEt.error = "Enter current password!"
            binding.currentPasswordEt.requestFocus()
        }else if (newPassword.isEmpty()){

            binding.newPasswordEt.error = "Enter new password!"
            binding.newPasswordEt.requestFocus()
        }else if (confirmNewPassword.isEmpty()){

            binding.confirmNewPasswordEt.error = "Enter confirm new password!"
            binding.confirmNewPasswordEt.requestFocus()
        }else if (newPassword != confirmNewPassword){

            binding.confirmNewPasswordEt.error = "Password doesn't match"
            binding.confirmNewPasswordEt.requestFocus()
        }else{

            authenticateUserForUpdatePassword()
        }

    }

    private fun authenticateUserForUpdatePassword(){

        progressDialog.setMessage("Authenticating User")
        progressDialog.show()

        val authCredential = EmailAuthProvider.getCredential(firebaseUser.email.toString(), currentPassword)
        firebaseUser.reauthenticate(authCredential)
            .addOnSuccessListener {
                //successfully authenticated, begin update
                Log.d(TAG, "authenticateUserForUpdatePassword: Auth success")
                updatePassword()
            }
            .addOnFailureListener {e ->
                //failed to authenticate user, maybe wrong current password entered
                Log.e(TAG, "authenticateUserForUpdatePassword: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to authenticate due to ${e.message}")
            }
    }

    private fun updatePassword(){

        progressDialog.setMessage("Changing Password")
        progressDialog.show()

        firebaseUser.updatePassword(newPassword)
            .addOnSuccessListener {

                Log.d(TAG, "updatePassword: Password updated...")
                progressDialog.dismiss()
                Utils.toast(this, "Password updated...!")
            }
            .addOnFailureListener {e ->

                Log.e(TAG, "updatePassword: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to update password due to ${e.message}")
            }
    }
}