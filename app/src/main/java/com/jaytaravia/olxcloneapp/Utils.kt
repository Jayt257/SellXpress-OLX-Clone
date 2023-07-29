package com.jaytaravia.olxcloneapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

import java.util.*
import kotlin.collections.HashMap

//A class that will contain static functions, constants, variables that we will be used in whole application
object Utils {

    const val AD_STATUS_AVAILABLE = "AVAILABLE"
    const val AD_STATUS_SOLD = "SOLD"

    //Categories array of the Ads
    val categories = arrayOf(
        "All",
        "Mobiles",
        "Computer/Laptop",
        "Electronics & Home Appliance",
        "Vehicles",
        "Furniture & Home Decor",
        "Fashion & Beauty",
        "Books",
        "Sports",
        "Animals",
        "Businesses",
        "Agriculture"
    )

    val categoryIcons = arrayOf(
        R.drawable.ic_category_all,
        R.drawable.ic_category_mobiles,
        R.drawable.ic_category_computer,
        R.drawable.ic_category_electronics,
        R.drawable.ic_category_vehicles,
        R.drawable.ic_category_furniture,
        R.drawable.ic_category_fashion,
        R.drawable.ic_category_books,
        R.drawable.ic_category_sports,
        R.drawable.ic_category_animals,
        R.drawable.ic_category_business,
        R.drawable.ic_category_agriculture
    )

    //Ad product conditions e.g. New, Used, Refurbished
    val conditions = arrayOf(
        "New",
        "Used",
        "Refurbished"
    )

//    A function to show Toast
//    @param context the context of activity/fragment from where this function will be called
//    @param message the message to be shown in the Toast

    fun toast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

//    A function to get current timestamp
//    @return Return the current timestamp as long datatype

    fun getTimestamp() : Long{
        return System.currentTimeMillis()
    }

    fun formatTimestampData(timestamp: Long) : String{

        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp

        return DateFormat.format("dd/MM/yyyy", calendar).toString()

    }

    fun addToFavorite(context: Context, adId: String){

        val firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null){

            Utils.toast(context, "You're not logged-in!")
        }else{

            val timestamp = Utils.getTimestamp()

            val hashMap = HashMap<String, Any>()
            hashMap["adId"] = adId
            hashMap["timestamp"] = timestamp

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(adId)
                .setValue(hashMap)
                .addOnSuccessListener {

                    Utils.toast(context, "Added to favorite..!")
                }
                .addOnFailureListener { e->

                    Utils.toast(context, "Failed to add to favorite due to ${e.message}")

                }

        }
    }

    //context - the context of activity/fragment from where this function will be called
    fun removeFromFavorite(context: Context, adId: String){

        val firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null){

            Utils.toast(context, "You're not logged-in!")
        } else {

             val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(adId)
                .removeValue()
                .addOnSuccessListener {

                    Utils.toast(context, "Removed from favorite!")
                }
                .addOnFailureListener {e->

                    Utils.toast(context, "Failed to remove from favorite due to ${e.message}")
                }
        }
    }

    /**
     *Launch Call Intent with phone number
     *
     * @param context the context of activity/fragment from where this function will be called
     * @param phone the phone number that will be opened in call intent
     */

    fun callIntent(context: Context, phone: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:+${Uri.encode(phone)}"))
        context.startActivity(intent)
    }

    /**
     * Launch Sms Intent with phone number
     *
     * @param context the context of activity/fragment from where this function will be called
     * @param phone the phone number that will be opened in sms intent
     */

    fun smsIntent(context: Context, phone: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${Uri.encode(phone)}"))
        context.startActivity(intent)
    }
}