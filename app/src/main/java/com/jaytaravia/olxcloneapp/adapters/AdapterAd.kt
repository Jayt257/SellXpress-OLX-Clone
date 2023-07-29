package com.jaytaravia.olxcloneapp.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jaytaravia.olxcloneapp.FilterAd
import com.jaytaravia.olxcloneapp.R
import com.jaytaravia.olxcloneapp.Utils
import com.jaytaravia.olxcloneapp.activities.AdDetailsActivity
import com.jaytaravia.olxcloneapp.databinding.RowAdBinding
import com.jaytaravia.olxcloneapp.models.ModelAd

class AdapterAd : RecyclerView.Adapter<AdapterAd.HolderAd>, Filterable {

    private lateinit var binding: RowAdBinding

    private companion object{

        private const val TAG = "ADAPTER_AD_TAG"
    }

    private var context: Context
    var adArrayList: ArrayList<ModelAd>
    private var filterList: ArrayList<ModelAd>

    //private hatu pela
    private var filter: FilterAd? = null


    private var firebaseAuth: FirebaseAuth

    //context - The context of activity/fragment from where instance of AdapterAd class is created
    //adArrayList - The list of ads


    constructor(context: Context, adArrayList: ArrayList<ModelAd>) {
        this.context = context
        this.adArrayList = adArrayList
        this.filterList = adArrayList

        firebaseAuth = FirebaseAuth.getInstance()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAd {
        //inflate/bind the row_ad.xml
        binding = RowAdBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderAd(binding.root)
    }

    override fun onBindViewHolder(holder: HolderAd, position: Int) {
        //get data from particular position of list and set to the UI Views of row_ad.xml and Handle clicks
        val modelAd = adArrayList[position]

        val title = modelAd.title
        val description = modelAd.description
        val address = modelAd.address
        val condition = modelAd.condition
        val price = modelAd.price
        val timestamp = modelAd.timestamp
        val formattedDate = Utils.formatTimestampData(timestamp)

        loadAdFirstImage(modelAd, holder)

        if (firebaseAuth.currentUser != null){
            checkIsFavorite(modelAd, holder)
        }

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.addressTv.text = address
        holder.conditionTv.text = condition
        holder.priceTv.text = price
        holder.dateTv.text = formattedDate

        //handle itemView (i.e. Ad) click, open the AdDetailsActivity. also pass the id of the Ad to intent to load details
        holder.itemView.setOnClickListener {

            val intent = Intent(context, AdDetailsActivity::class.java)
            intent.putExtra("adId", modelAd.id)
            context.startActivity(intent)
        }


        //handle favBtn click, add/remove the ad to/from favorite of current user
        holder.favBtn.setOnClickListener {

            val favorite = modelAd.favorite
            if (favorite){

                Utils.removeFromFavorite(context, modelAd.id)
            } else{

                Utils.addToFavorite(context, modelAd.id)
            }
        }

    }

    private fun checkIsFavorite(modelAd: ModelAd, holder: HolderAd) {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(modelAd.id)
            .addValueEventListener(object: ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {

                    val favorite = snapshot.exists()

                    modelAd.favorite = favorite

                    if (favorite) {

                        holder.favBtn.setImageResource(R.drawable.ic_fav_yes)
                    } else{

                        holder.favBtn.setImageResource(R.drawable.ic_fav_no)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadAdFirstImage(modelAd: ModelAd, holder: HolderAd) {
        //load first image from available images of AD e.g. if there are 5 images of Ad, load first one
        //Ad id to get image of it
        val adId = modelAd.id

        Log.d(TAG, "loadAdFirstImage: adId: $adId")

        val reference = FirebaseDatabase.getInstance().getReference("Ads")
        reference.child(adId).child("Images").limitToFirst(1)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //this will return only 1 image as we have used query .limitToFirst(1)
                    for (ds in snapshot.children){
                        //get url of the image, make sure spellings are same as in firebase db
                        val imageUrl = "${ds.child("imageUrl").value}"
                        Log.d(TAG, "onDataChange: imageUrl: $imageUrl")

                        //set image to Image View i.e. imageIv
                        try {
                            Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_image_gray)
                                .into(holder.imageIv)
                        } catch (e: Exception){
                            Log.e(TAG, "onDataChange: ", e)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


    }

    override fun getItemCount(): Int {
        //return the size of list
        return adArrayList.size
    }

    override fun getFilter(): Filter {
        //init the filter obj only if it is null
        if (filter === null){
            filter = FilterAd(this, filterList)
        }

        return filter as FilterAd
    }


    inner class HolderAd(itemView: View) : RecyclerView.ViewHolder(itemView){

        var imageIv = binding.imageIv
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var favBtn = binding.favBtn
        var addressTv = binding.addressTv
        var conditionTv = binding.conditionTv
        var priceTv = binding.priceTv
        var dateTv = binding.dateTv

    }


}