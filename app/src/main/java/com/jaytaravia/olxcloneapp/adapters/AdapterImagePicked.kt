package com.jaytaravia.olxcloneapp.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.internal.Util
import com.jaytaravia.olxcloneapp.R
import com.jaytaravia.olxcloneapp.Utils
import com.jaytaravia.olxcloneapp.databinding.RowImagesPickedBinding
import com.jaytaravia.olxcloneapp.models.ModelImagePicked

class AdapterImagePicked(
    private val context: Context,
    private val imagesPickedArrayList: ArrayList<ModelImagePicked>,
    private val adId: String
    ) : Adapter<AdapterImagePicked.HolderImagePicked>() {

    private lateinit var binding: RowImagesPickedBinding

    private companion object{
        private const val TAG = "IMAGES_TAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagePicked {

        binding = RowImagesPickedBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderImagePicked(binding.root)
    }

    override fun onBindViewHolder(holder: HolderImagePicked, position: Int) {

        val model = imagesPickedArrayList[position]

        //check if image is from firebase storage or device storage
        if (model.fromInternet){

            try {

                val imageUrl = model.imageUrl
                Log.d(TAG, "onBindViewHolder: imageUrl: $imageUrl")

                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(holder.imageIv)
            } catch (e: Exception){
                Log.e(TAG, "onBindViewHolder: ", e)
            }

        } else {

            //Image is picked from Gallery/Camera. Get image Uri of the image to set in imageIv
            try {
                //get imageUri
                val imageUri = model.imageUri
                Log.d(TAG, "onBindViewHolder: imageUri $imageUri")
                //set to image view i.e. imageTv
                Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(holder.imageIv)
            }catch (e: Exception){
                Log.e(TAG, "onBindViewHolder: ", e)
            }
        }



        //handle closeBtn click, remove image from imagePickedArrayList
        holder.closeBtn.setOnClickListener {
            //check if image is from Device Storage or Firebase
            if (model.fromInternet){

                deleteImageFirebase(model, holder, position)
            } else {

                imagesPickedArrayList.remove(model)
                notifyDataSetChanged()
            }

        }

    }

    private fun deleteImageFirebase(model: ModelImagePicked, holder: AdapterImagePicked.HolderImagePicked, position: Int) {

        val imageId = model.id

        Log.d(TAG, "deleteImageFirebase: adId: $adId")
        Log.d(TAG,  "deleteImageFirebase: imageId: $imageId")

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId).child("Images").child(imageId)
            .removeValue()
            .addOnSuccessListener {
                //Delete Success
                Log.d(TAG, "deleteImageFirebase: Image $imageId deleted")

                Utils.toast(context, "Image deleted")
                //remove from imagePickedArrayList
                try {
                    imagesPickedArrayList.remove(model)
                    notifyItemRemoved(position)
                } catch (e: Exception){
                    Log.d(TAG, "deleteImageFirebase1: ", e)
                }
            }
            .addOnFailureListener {e->
                //Delete Failure
                Log.e(TAG, "deleteImageFirebase2: ", e)
                Utils.toast(context, "Failed to delete image due to ${e.message}")
            }
    }

    override fun getItemCount(): Int {

        return imagesPickedArrayList.size
    }

    inner class HolderImagePicked(itemView: View) : ViewHolder(itemView) {

        var imageIv = binding.imageIv
        var closeBtn = binding.closeBtn

    }

}