package com.rcappstudio.campingapp.data.model

data class UserModel(
    val aidsVerificationDocs : AidsDoc?= null,
    var name : String ?= null,
    var mobileNo : String ?= null,
    var udidNo : String ?= null,
    var dateOfBirth : String ?= null,
    var state : String ?= null,
    var district : String ?= null,
    var alreadyApplied : Boolean = false,
    var fcmToken : String ?= null,
    val profileImageUrl : String ?= null,
    val requestStatus: RequestStatus?= null

)