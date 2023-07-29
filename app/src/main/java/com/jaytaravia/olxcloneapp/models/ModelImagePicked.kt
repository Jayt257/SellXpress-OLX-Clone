package com.jaytaravia.olxcloneapp.models

import android.net.Uri

class ModelImagePicked {

//    Variables
    var id = ""
    var imageUri: Uri? = null
    var imageUrl: String? = null
    var fromInternet = false //this model class will be used to show images (picked/taken from Gallery/Camera - false or from firebase - true) in AdCreateActivity.

//    Empty constructor require for firebase db
    constructor()

//    Constructor with all params
    constructor(id: String, imageUri: Uri?, imageUrl: String?, fromInternet: Boolean) {
        this.id = id
        this.imageUri = imageUri
        this.imageUrl = imageUrl
        this.fromInternet = fromInternet
    }


}