package com.jaytaravia.olxcloneapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport.Session.Event.Application.Execution
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jaytaravia.olxcloneapp.R
import com.jaytaravia.olxcloneapp.Utils
import com.jaytaravia.olxcloneapp.adapters.AdapterAd
import com.jaytaravia.olxcloneapp.databinding.ActivityAdSellerProfileBinding
import com.jaytaravia.olxcloneapp.models.ModelAd

class AdSellerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdSellerProfileBinding


    private companion object{
        //TAB for logs in logcat
        private const val TAG = "SELLER_PROFILE_TAG"
    }

    private var sellerUid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdSellerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sellerUid = intent.getStringExtra("sellerUid").toString()
        Log.d(TAG, "onCreate: sellerUid: $sellerUid")

        loadSellerDetails()

        loadAds()


        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadSellerDetails(){
        Log.d(TAG, "loadSellerDetails: ")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(sellerUid)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val name = "${snapshot.child("name").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    val timestamp = snapshot.child("timestamp").value as Long

                    val formattedDate = Utils.formatTimestampData(timestamp)

                    binding.sellerNameTv.text = name
                    binding.sellerMemberSinceLableTv.text = formattedDate
                    try {
                        Glide.with(this@AdSellerProfileActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.sellerProfileIv)
                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadAds(){
        Log.d(TAG, "loadAds: ")

        val adArrayList: ArrayList<ModelAd> = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.orderByChild("uid").equalTo(sellerUid)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    adArrayList.clear()

                    for (ds in snapshot.children){
                        try {

                            val modelAd = ds.getValue(ModelAd::class.java)

                            adArrayList.add(modelAd!!)
                        } catch (e: Exception){
                            Log.e(TAG, "onDataChange: ", e)
                        }
                    }

                    val adapterAd = AdapterAd(this@AdSellerProfileActivity, adArrayList)
                    binding.adsRv.adapter = adapterAd

                    val adsCount = "${adArrayList.size}"
                    binding.publishedAdsCountTv.text= adsCount
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}