package com.rcappstudio.campingapp.data.model

data class RequestStatus(
    val aidsList: List<String> ?= null,
    val aidsReceived : Boolean ?= false,
    val appliedOnTimeStamp : Long ?= 0,
    var latLng : LatLng?= null,
    val message : String ?= null,
    val notAppropriate : Boolean ?= null

)