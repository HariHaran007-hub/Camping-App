package com.rcappstudio.campingapp.data.model

data class RequestStatus(
    var verified : Boolean = false,
    var notAppropriate : Boolean = false,
    var message : String ?= null,
//    var latLng : LatLng ?= null,
    var aidsReceived : Boolean = false,
    var appliedOnTimeStamp  :Long ?= 0,
    val ngoList : HashMap<String , NgoData> ?= null,
    var aidsList : List<String> ?= null,
    var incomeCertificate : String ?= null
)