package com.rcappstudio.campingapp.data.model

data class CampingModel(
    val emailId : String ?= null,
    val password : String ?= null,
    val state : String ?= null,
    val district : String ?= null,
    val campingName : String ?= null,
    val campId : String ?= null,
    val location : LatLng ?= null
)