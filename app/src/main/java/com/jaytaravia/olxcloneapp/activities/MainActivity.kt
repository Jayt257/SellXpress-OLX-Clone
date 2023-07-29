package com.jaytaravia.olxcloneapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.jaytaravia.olxcloneapp.fragments.HomeFragment
import com.jaytaravia.olxcloneapp.R
import com.jaytaravia.olxcloneapp.Utils
import com.jaytaravia.olxcloneapp.databinding.ActivityMainBinding
import com.jaytaravia.olxcloneapp.fragments.AccountFragment
import com.jaytaravia.olxcloneapp.fragments.ChatsFragment
import com.jaytaravia.olxcloneapp.fragments.MyAdsFragment

class MainActivity : AppCompatActivity() {

    //view binding or respective layout i.e. activity_main.xml
    private lateinit var binding: ActivityMainBinding

    //Firebase Auth for auth related tasks
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //activity_main.xml = ActivityMainBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get instance of firebase auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance()

        //check if user is logged in or not
        if(firebaseAuth.currentUser == null){
            //user is not logged in, move to LoginOptionsActivity
            startLoginOptions()
        }

        //By default (when app open) show HomeFragment


        //handle bottomNv item clicks to navigate between fragments
        binding.bottomNv.setOnItemSelectedListener {item->

            when(item.itemId){
                R.id.menu_home -> {
                    //Home item clicked, show HomeFragment
                    showHomeFragment()

                    true
                }
                R.id.menu_chats -> {
                    //Chats item clicked, show ChatsFragment
                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOptions()

                        false
                    }
                    else{
                        showChatsFragment()

                        true
                    }

                }
                R.id.menu_my_ads -> {
                    //My Ads item clicked, show MuAdsFragment

                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOptions()

                        false
                    }
                    else{

                        showMyAdsFragment()

                        true
                    }

                }
                R.id.menu_account -> {
                    //Account item clicked, show AccountFragment

                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOptions()

                        false
                    }
                    else{

                        showAccountFragment()

                        true
                    }

                }
                else -> {
                    false
                }
            }
        }

        binding.sellFab.setOnClickListener {
            val intent = Intent(this, AdCreateActivity::class.java)
            intent.putExtra("isEditMode", false)
            startActivity(intent)
        }
    }

    private fun showHomeFragment(){
        //change toolbar textview text/title to Home
        binding.toolbarTitleTv.text = "Home"

        //show HomeFragment
        val fragment = HomeFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "HomeFragment")
        fragmentTransaction.commit()

    }

    private fun showChatsFragment(){
        //change toolbar textview text/title to Chats
        binding.toolbarTitleTv.text = "Chats"

        //Show ChatsFragment
        val fragment = ChatsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "ChatsFragment")
        fragmentTransaction.commit()

    }

    private fun showMyAdsFragment(){
        //change toolbar textview text/title to My Ads
        binding.toolbarTitleTv.text = "My Ads"

        //Show MyAdsFragment
        val fragment = MyAdsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "MyAdsFragment")
        fragmentTransaction.commit()

    }

    private fun showAccountFragment(){
        //change toolbar textview text/title to Account
        binding.toolbarTitleTv.text = "Account"

        //Show AccountFragment
        val fragment = AccountFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "AccountFragment")
        fragmentTransaction.commit()

    }

    private fun startLoginOptions(){

        startActivity(Intent(this, LoginOptionActivity::class.java))
    }
}