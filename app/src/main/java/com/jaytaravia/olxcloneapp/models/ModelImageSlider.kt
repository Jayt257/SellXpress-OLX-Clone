package com.jaytaravia.olxcloneapp.models

class ModelImageSlider {

//    Variables, spellings and case should be same as in firebase db [Ads > AdId > Images > ...]
    var id: String = ""
    var imageUrl: String = ""

    constructor()

    constructor(id: String, imageUrl: String) {
        this.id = id
        this.imageUrl = imageUrl
    }


}