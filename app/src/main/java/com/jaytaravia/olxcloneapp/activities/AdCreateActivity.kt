package com.jaytaravia.olxcloneapp.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.jaytaravia.olxcloneapp.fragments.HomeFragment
import com.jaytaravia.olxcloneapp.R
import com.jaytaravia.olxcloneapp.Utils
import com.jaytaravia.olxcloneapp.adapters.AdapterImagePicked
import com.jaytaravia.olxcloneapp.databinding.ActivityAdCreateBinding
import com.jaytaravia.olxcloneapp.models.ModelImagePicked

class AdCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdCreateBinding

    private companion object{

        private const val TAG = "ADD_CREATE_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

//  Image Uri to hold uri of the image (picked/captured using Gallery/Camera) to add in AD Images List
    private var imageUri: Uri? = null

    //list of images (picked/captured using Gallery/Camera or form internet)
    private lateinit var imagePickedArrayList: ArrayList<ModelImagePicked>

    //adapter to be set in RecyclerView that will load list of images (picked/capturedusing Gallery/Camera or from internet)
    private lateinit var adapterImagePicked: AdapterImagePicked

    private var isEditMode = false
    private var adIdForEditing = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //Firevase Auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance()

        //Setup and set the categories adapter to the Category Input Filed i.e. categoryAct
        val adapterCategories = ArrayAdapter(this, R.layout.row_category_act, Utils.categories)
        binding.categoryAct.setAdapter(adapterCategories)

        val adapterConditions = ArrayAdapter(this, R.layout.row_condition_act, Utils.conditions)
        binding.conditionAct.setAdapter(adapterConditions)

        isEditMode = intent.getBooleanExtra("isEditMode", false)
        Log.d(TAG, "onCreate: isEditMode: $isEditMode")

        if (isEditMode){

            adIdForEditing = intent.getStringExtra("adId") ?: ""

            //function call to load Ad details by using Ad Id
            loadAdDetails()

            //change toolbar title and submit button text
            binding.toolbarTitleTv.text = "Update Ad"
            binding.postAdBtn.text = "Update Ad"
        } else{
            //New Ad Mode: Change toolbar title and submit button text
            binding.toolbarTitleTv.text = "Create Ad"
            binding.postAdBtn.text = "Post Ad"
        }

        //init imagePickedArrayList
        imagePickedArrayList = ArrayList()
        //loadImages()
        loadImages()

        //handle toolbarBackBtn click, go-back
        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.toolbarAdImageBtn.setOnClickListener {
            showImagePickOptions()
        }

        binding.postAdBtn.setOnClickListener {
            validateData()
        }

    }

    private fun loadImages(){
        Log.d(TAG, "loadImages: ")

        adapterImagePicked = AdapterImagePicked(this, imagePickedArrayList, adIdForEditing)

        binding.imagesRv.adapter = adapterImagePicked

    }

    private fun showImagePickOptions(){
        Log.d(TAG, "showImagePickOptions: ")
        val popupMenu = PopupMenu(this, binding.toolbarAdImageBtn)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            //get the id of the item clciked in popup menu
            val itemId = item.itemId

            //check which item id is clicked from popup menu. 1=Camera, 2=Gallery as we defined
            if (itemId == 1){
                //Camera is clciked we need to check if we have permissions of Camera, Storage before launching Camera to Capture image
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

                    val cameraPermissions = arrayOf(android.Manifest.permission.CAMERA)
                    requestCameraPermission.launch(cameraPermissions)

                } else {

                    val cameraPermissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestCameraPermission.launch(cameraPermissions)

                }

            } else if (itemId == 2){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

                    pickImageGallery()
                } else {
                     val storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    requestStoragePermission.launch(storagePermission)
                }
            }

            true
        }
    }

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ isGranted ->
        Log.d(TAG, "requestStoragePermission: isGranted: $isGranted")

        if (isGranted){

            pickImageGallery()

        }else{

            Utils.toast(this, "Storage permission denied...")
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ result ->
        Log.d(TAG, "requestCameraPermission: result: $result")

        var areAllGranted = true
        for (isGranted in result.values){
            areAllGranted = areAllGranted && isGranted
        }

        if (areAllGranted){

            pickImageCamera()

        } else {

            Utils.toast(this, "Camera or Storage or both permissions denied...")
        }
    }

    private fun pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ")

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ")
        //Setup Content values, MediaStore to capture high quality image using camera intent
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_IMAGE_TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_IMAGE_DESCRIPTION")
        //Uri of the imageto be captured from camera
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        //Intent to launch camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)

    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->
        Log.d(TAG, "galleryActivityResultLauncher: ")
        //Check if image is picked or not
        if (result.resultCode == Activity.RESULT_OK){

            val data = result.data

            imageUri = data!!.data
            Log.d(TAG, "galleryActivityResultLauncher: imageUri: $imageUri")

            val timestamp = "${Utils.getTimestamp()}"

            val modelImagePicked = ModelImagePicked(timestamp, imageUri, null, false)

            imagePickedArrayList.add(modelImagePicked)

            loadImages()
        } else{

            Utils.toast(this, "Cancelled...!")
        }
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->
        Log.d(TAG, "cameraActivityResultLauncher: ")
        //Check if image is picked or not
        if (result.resultCode == Activity.RESULT_OK){

            Log.d(TAG, "cameraActivityResultLauncher: imageUri $imageUri")

            val timestamp = "${Utils.getTimestamp()}"

            val modelImagePicked = ModelImagePicked(timestamp, imageUri, null, false)

            imagePickedArrayList.add(modelImagePicked)

            loadImages()
        } else{

            Utils.toast(this, "Cancelled...!")
        }
    }

    private var brand = ""
    private var category = ""
    private var condition = ""
    private var address = ""
    private var price = ""
    private var title = ""
    private var description = ""
    private var latitude = 0.0
    private var longitude = 0.0



    private fun validateData(){

        var size = imagePickedArrayList.size

        Log.d(TAG, "validateData: ")
        //input data
        brand = binding.brandEt.text.toString().trim()
        category = binding.categoryAct.text.toString().trim()
        condition = binding.conditionAct.text.toString().trim()
        address = binding.locationAct.text.toString().trim()
        price = binding.priceEt.text.toString().trim()
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        //validate data
        if (size==0){

            Utils.toast(this@AdCreateActivity, "Please select atleat One Image")

        } else if (brand.isEmpty()){
            //no brand entered in brandEt, show error in brandEt and focus
            binding.brandEt.error = "Enter Brand"
            binding.brandEt.requestFocus()
        } else if (category.isEmpty()){

            binding.categoryAct.error = "Choose Category"
            binding.categoryAct.requestFocus()
        } else if (condition.isEmpty()){

            binding.conditionAct.error = "Choose Condition"
            binding.conditionAct.requestFocus()
        }else if (address.isEmpty()){

            binding.conditionAct.error = "Please Enter Your Location"
            binding.conditionAct.requestFocus()
        }else if (title.isEmpty()){

            binding.titleEt.error = "Enter Title"
            binding.titleEt.requestFocus()
        }else if (description.isEmpty()){

            binding.descriptionEt.error = "Enter Description"
            binding.descriptionEt.requestFocus()
        }else {
            //All data is validated, we can proceed further now

            if (isEditMode){

                updateAd()
            } else{

                postAd()
            }

        }
    }

    private fun postAd(){

        Log.d(TAG, "postAd: ")

        progressDialog.setMessage("Publishing Ad")
        progressDialog.show()

        val timestamp = Utils.getTimestamp()

        val refAds = FirebaseDatabase.getInstance().getReference("Ads")

        val keyId = refAds.push().key

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$keyId"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["brand"] = "$brand"
        hashMap["category"] = "$category"
        hashMap["condition"] = "$condition"
        hashMap["address"] = "$address"
        hashMap["price"] = "$price"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["status"] = "${Utils.AD_STATUS_AVAILABLE}"
        hashMap["timestamp"] = timestamp
        hashMap["latitude"] = latitude
        hashMap["longitude"] = longitude

        //set data to firebase database. Ads -> AdId -> AdDataJSON
        refAds.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "postAd: Ad Published")
                uploadImageStorage(keyId)

            }
            .addOnFailureListener {e ->

                Log.e(TAG, "postAd: ", e)
                progressDialog.dismiss()
                Utils.toast(this, " Failed due to ${e.message}")
            }
    }

    private fun updateAd(){
        Log.d(TAG, "updateAd:")

        progressDialog.setMessage("Updating Ad...")
        progressDialog.show()


        val hashMap = HashMap<String, Any>()
        hashMap["brand"] = "$brand"
        hashMap["category"] = "$category"
        hashMap["condition"] = "$condition"
        hashMap["address"] = "$address"
        hashMap["price"] = "$price"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["latitude"] = latitude
        hashMap["longitude"] = longitude

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adIdForEditing)
            .updateChildren(hashMap)
            .addOnSuccessListener {

                Log.d(TAG, "updateAd: Ad Updated...")
                progressDialog.dismiss()
                uploadImageStorage(adIdForEditing)
            }
            .addOnFailureListener {e->

                Log.e(TAG, "updateAd: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to update the Ad due to ${e.message}")
            }

    }

    private fun uploadImageStorage(adId: String){

        for (i in imagePickedArrayList.indices){

            val modelImagePicked = imagePickedArrayList[i]

            //Upload image only if picked from gallery/camera
            if (!modelImagePicked.fromInternet){

                //for name of the image inn firebase storage
                val imageName = modelImagePicked.id
                //path and  name of the image in firebase storage
                val filePathAndName = "Ads/$imageName"
                val imageIndexForProgress = i + 1

                val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
                storageReference.putFile(modelImagePicked.imageUri!!)
                    .addOnProgressListener {snapshot ->
                        //calculalate the current progress of the image being uploaded
                        val progress = 100.0 *snapshot.bytesTransferred / snapshot.totalByteCount
                        Log.d(TAG, "uploadImageStorage: progress: $progress")
                        val message = "Uploading $imageIndexForProgress of ${imagePickedArrayList.size} images... Progress ${progress.toInt()}%"
                        Log.d(TAG, "uploadImageStorage: message: $message")

                        progressDialog.setMessage(message)
                        progressDialog.show()
                    }
                    .addOnSuccessListener {taskSnapshot ->
                        //image uploaded get url of uploaded image
                        Log.d(TAG, "uploadImageStorage: onSuccess")

                        val uriTask = taskSnapshot.storage.downloadUrl
                        while (!uriTask.isSuccessful);
                        val uploadedImageUrl = uriTask.result

                        if (uriTask.isSuccessful){

                            val hashMap = HashMap<String, Any>()
                            hashMap["id"] = "${modelImagePicked.id}"
                            hashMap["imageUrl"] = "$uploadedImageUrl"

                            val ref = FirebaseDatabase.getInstance().getReference("Ads")
                            ref.child(adId).child("Images")
                                .child(imageName)
                                .updateChildren(hashMap)
                        }

                        progressDialog.dismiss()
                        Log.d(TAG, "Going to Home Fragment")
                        startActivity(Intent(this@AdCreateActivity, HomeFragment::class.java))
                        Log.d(TAG, "Gone")
                    }
                    .addOnFailureListener{e ->
                        //failed to upload image
                        Log.e(TAG, "uploadImagesStorage: ", e)
                        progressDialog.dismiss()
                    }
            }


        }
    }

    private fun loadAdDetails(){
        Log.d(TAG, "loadAdDetails: ")

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adIdForEditing)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val brand = "${snapshot.child("brand").value}"
                    val category = "${snapshot.child("category").value}"
                    val condition = "${snapshot.child("condition").value}"
                    val address = "${snapshot.child("address").value}"
                    val price = "${snapshot.child("price").value}"
                    val title = "${snapshot.child("title").value}"
                    val description = "${snapshot.child("description").value}"

                    //set data to UI Views (Form)
                    binding.brandEt.setText(brand)
                    binding.categoryAct.setText(category)
                    binding.conditionAct.setText(condition)
                    binding.locationAct.setText(address)
                    binding.priceEt.setText(price)
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    //Load the Ad images.Ads > ADId > Images
                    val refImages = snapshot.child("Images").ref
                    refImages.addListenerForSingleValueEvent(object: ValueEventListener{

                        override fun onDataChange(snapshot: DataSnapshot) {
                            //might be multiple images so loop to get all
                            for (ds in snapshot.children){
                                //get image data i.e. id, imageUrl. Note: Spellings should be same as in firebase db
                                val id = "${ds.child("id").value}"
                                val imageUrl = "${ds.child("imageUrl").value}"
                                //setup modelImagePicked with data we got and add to our images list i.e. imagePickedArrayList
                                val modelImagePicked = ModelImagePicked(id, null, imageUrl, true)
                                imagePickedArrayList.add(modelImagePicked)
                            }
                            //reload images (all images picked from device storage and got from firebase storage)
                            loadImages()
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}