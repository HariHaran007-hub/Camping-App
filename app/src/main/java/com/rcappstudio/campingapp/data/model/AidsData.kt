package com.rcappstudio.campingapp.data.model

data class AidsData(

    //Ortho
    var tricycle : Int ?= 0,
    var wheelChair : Int ?= 0,
    var walkingStick  : Int ?=0 ,
    var walker : Int ?=0,
    var rollator : Int ?= 0,
    var standingFrame : Int ?= 0,
    var quadripod : Int ?= 0,
    var tetrapod : Int ?= 0,
    var auxiliaryCrutches : Int ?= 0,
    var elobowCrutches : Int ?= 0,
    var cpChair : Int ?= 0,
    var cornerChair : Int ?= 0,

    //Visual
    var brailleNoteTaker : Int ?= 0,
    var learningEquipment : Int ?= 0,
    var communicationEquipment : Int ?= 0,
    var brailleAttachmentForTelephone : Int ?= 0,
    var lowVisionAids : Int ?= 0,
    var specialMobilityAids : Int ?= 0,

    //Hearing
    var hearingAid : Int ?= 0,
    var educationalKits : Int ?= 0,
    var assistiveAndAlarmDevices : Int ?= 0,
    var cochlearImplant : Int ?= 0,

    //Mental
    var teachingLearningMaterialKit : Int ?= 0

)