package com.rcappstudio.campingapp.data.model

data class AidsReceivedModel(
    val name : String ?= null,
    val udidNo : String ?= null,
    val state : String ?= null,
    val district : String ?= null,
    val gender : String ?= null,
    val category : String ?= null,
    val dob : String ?= null,
    val aidsList : List<String> ?= null,
    val ngoId : String ?= null,
    val percentageOfDisability  :String ?= null,
    val deliveredOn : Long ?= null
)