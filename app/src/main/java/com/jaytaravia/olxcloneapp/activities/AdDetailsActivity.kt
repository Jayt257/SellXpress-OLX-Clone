package com.jaytaravia.olxcloneapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jaytaravia.olxcloneapp.R
import com.jaytaravia.olxcloneapp.Utils
import com.jaytaravia.olxcloneapp.databinding.ActivityAdDetailsBinding
import com.jaytaravia.olxcloneapp.models.AdapterImageSlider
import com.jaytaravia.olxcloneapp.models.ModelAd
import com.jaytaravia.olxcloneapp.models.ModelImageSlider

class AdDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdDetailsBinding

    private companion object{

        private const val TAG = "AD_DETAILS_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private var adId = ""

    private var sellerUid = ""
    private var sellerPhone = ""

    //hold the Ad's favorite state by current user
    private var favorite = false

    //list of Ad's images to show in slider
    private lateinit var imageSliderArrayList: ArrayList<ModelImageSlider>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //hide same UI views in start. We will show the Edit, Delete optionif the user is Ad owner. We will show Call, Chat, SMS option if user isn't Ad owner
        binding.toolbarEditBtn.visibility = View.GONE
        binding.toolbarDeleteBtn.visibility = View.GONE
        binding.chatBtn.visibility = View.GONE
        binding.callBtn.visibility = View.GONE
        binding.smsBtn.visibility = View.GONE


        //Firebase Auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance()

        adId = intent.getStringExtra("adId").toString()
        Log.d(TAG, "onCreate: adId: $adId")

        if (firebaseAuth.currentUser!= null){
            checkIsFavorite()
        }

        loadAdDetails()
        loadAdImages()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.toolbarDeleteBtn.setOnClickListener {

            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            materialAlertDialogBuilder.setTitle("Delete Ad")
                .setMessage("Are you sure you want to delete this Ad?")
                .setPositiveButton("DELETE"){ dialog, which ->
                    Log.d(TAG, "onCreate: DELETE clicked...")
                    deleteAd()
                }
                .setNegativeButton("CANCLE"){dialog, which ->
                    Log.d(TAG, "onCreate: CANCLE clicked...")
                    dialog.dismiss()
                }
                .show()
        }

        //handle toolbarEditBtn click, start AdCreateActivity to edit ths Ad
        binding.toolbarEditBtn.setOnClickListener {
            editOptionsDialog()
        }

        //handle toolbarFavBtn click,
        binding.toolbarFavBtn.setOnClickListener {
            if (favorite){
                Utils.removeFromFavorite(this, adId)
            }else{
                //this Ad is not in favorite of current user, add to favorite
                Utils.addToFavorite(this, adId)
            }
        }

        binding.sellerProfileCv.setOnClickListener {
            Log.d(TAG, "arrived 1")
            val intent  = Intent(this, AdSellerProfileActivity::class.java)
            intent.putExtra("sellerUid", sellerUid)
            Log.d(TAG, "arrived 2")
            startActivity(intent)
        }

        binding.chatBtn.setOnClickListener {

        }

        binding.callBtn.setOnClickListener {
            Utils.callIntent(this, sellerPhone)
        }

        binding.smsBtn.setOnClickListener {
            Utils.smsIntent(this, sellerPhone)
        }

    }

    private fun editOptionsDialog(){
        Log.d(TAG, "editOptionDialog: ")

        val popupMenu = PopupMenu(this, binding.toolbarEditBtn)

        popupMenu.menu.add(Menu.NONE, 0, 0, "Edit")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Mark As Sold")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->

            val itemId = menuItem.itemId

            if (itemId == 0){

                val intent = Intent(this, AdCreateActivity::class.java)
                intent.putExtra("isEditMode", true)
                intent.putExtra("adId", adId)
                startActivity(intent)
            } else if (itemId == 1){
                //Mark As Sold
                showMarkAsSoldDialog()
            }

            return@setOnMenuItemClickListener true
        }
    }

    private fun showMarkAsSoldDialog(){
        Log.d(TAG, "showMarkAsDialog: ")
        //Material Alert Dialog - Setup and show
        val alertDialogBuilder = MaterialAlertDialogBuilder(this)
        alertDialogBuilder.setTitle("Mark as sold?")
            .setMessage("Are you sure you want to mark this Ad as sold?")
            .setPositiveButton("SOLD"){ dialog, which ->
                Log.d(TAG, "showMarkAsSoldDialog: SOLD clicked")

                val hashMap = HashMap<String, Any>()
                hashMap["status"] = "${Utils.AD_STATUS_SOLD}"

                val ref = FirebaseDatabase.getInstance().getReference("Ads")
                ref.child(adId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        //Success
                        Log.d(TAG, "showMarkAsSoldDialog: Marked as sold")
                    }
                    .addOnFailureListener {e ->
                        //Failure
                        Log.e(TAG, "showMarkAsSoldDialog: ", e)
                        Utils.toast(this, "Failed to mark as sold due to ${e.message}")
                    }
            }
            .setNegativeButton("CANCEL"){ dialog, which ->

                Log.d(TAG, "showMarkAsSoldDialog: CANCEL clicked")
                dialog.dismiss()
            }
            .show()
    }

    private fun loadAdDetails(){
        Log.d(TAG, "loadAdDetails: ")

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    try {

                        val modelAd = snapshot.getValue(ModelAd::class.java)

                        sellerUid = "${modelAd!!.uid}"
                        val title = modelAd.title
                        val description = modelAd.description
                        val address = modelAd.address
                        val condition = modelAd.condition
                        val category = modelAd.category
                        val price = modelAd.price
                        val timestamp = modelAd.timestamp

                        val formattedDate = Utils.formatTimestampData(timestamp)

                        //check if the Ad is by currently signed-in user
                        if (sellerUid == firebaseAuth.uid){
                            //Ad is created by currently signed-in user so
                            //1) Should be able to edit and delete Ad
                            binding.toolbarEditBtn.visibility = View.VISIBLE
                            binding.toolbarDeleteBtn.visibility = View.VISIBLE
                            //2) Shouldn't able to chat, call, sms (to himself)
                            binding.chatBtn.visibility = View.GONE
                            binding.callBtn.visibility = View.GONE
                            binding.smsBtn.visibility = View.GONE
                            binding.sellerProfileLabelTv.visibility = View.GONE
                            binding.sellerProfileCv.visibility = View.GONE
                        }else{
                            //Ad is not created by currently signed in user so
                            //1) Shouldn't be able to edit and delete Ad
                            binding.toolbarEditBtn.visibility = View.GONE
                            binding.toolbarDeleteBtn.visibility = View.GONE
                            //2) Shouldbe able to chat, call, sms (to Ad creator), view seller profile
                            binding.chatBtn.visibility = View.VISIBLE
                            binding.callBtn.visibility = View.VISIBLE
                            binding.smsBtn.visibility = View.VISIBLE
                            binding.sellerProfileLabelTv.visibility = View.VISIBLE
                            binding.sellerProfileCv.visibility = View.VISIBLE
                        }

                        //set data to UI Views
                        binding.titleTv.text = title
                        binding.descriptionTv.text = description
                        binding.addressTv.text = address
                        binding.conditionTv.text = condition
                        binding.categoryTv.text = category
                        binding.priceTv.text = price
                        binding.dateTv.text = formattedDate

                        //function call, load seller info e.g. profile image, name, member since
                        loadSellerDetails()


                    } catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadSellerDetails(){
        Log.d(TAG, "loadSellerDetails: ")
        //Db path to load seller info. User > sellerUid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(sellerUid)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    val timestamp = snapshot.child("timestamp").value as Long

                    //format date time e.g. timestamp to dd/MM/yyyy
                    val formattedDate = Utils.formatTimestampData(timestamp)
                    //phone number of seller
                    sellerPhone = "$phoneCode$phoneNumber"

                    //set data to UI Views
                    binding.sellerNameTv.text = name
                    binding.memberSinceTv.text = formattedDate
                    try {
                        Glide.with(this@AdDetailsActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.sellerProfileIv)
                    } catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun checkIsFavorite(){
        Log.d(TAG, "checkIsFavorite: ")
        //DB path to check id Ad is in Favorite of current user. User > uid > Favorites > adId
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}").child("Favorites").child(adId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    favorite = snapshot.exists()
                    Log.d(TAG, "onDataChange: favorite: $favorite")

                    if (favorite){

                        binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_yes)
                    } else {

                        binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_no)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadAdImages(){
        Log.d(TAG, "loadAdImages: ")

        //init list before starting adding data into it
        imageSliderArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId).child("Images")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    imageSliderArrayList.clear()

                    for (ds in snapshot.children){

                        try {

                            val modelImageSlider = ds.getValue(ModelImageSlider::class.java)

                            imageSliderArrayList.add(modelImageSlider!!)

                        }catch (e: Exception){
                            Log.e(TAG, "onDataChange: ", e)
                        }
                    }
                    //setup adapter and set to viewpager i.e. imageSliderVp
                    val adapterImageSlider = AdapterImageSlider(this@AdDetailsActivity, imageSliderArrayList)
                    binding.imageSliderVp.adapter = adapterImageSlider
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }

    private fun deleteAd(){
        Log.d(TAG, "deleteAd: ")

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId)
            .removeValue()
            .addOnSuccessListener {

                Log.e(TAG, "deleteAd: Deleted")
                Utils.toast(this, "Deleted...!")

                finish()
            }
            .addOnFailureListener {e->

                Log.e(TAG, "deleteAd: ", e)
                Utils.toast(this, "Failed to delete due to ${e.message}")

            }
    }

}