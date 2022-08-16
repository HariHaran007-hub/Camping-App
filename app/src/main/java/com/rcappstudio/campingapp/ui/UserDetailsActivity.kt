package com.rcappstudio.campingapp.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.rcappstudio.campingapp.utils.Constants
import com.rcappstudio.campingapp.R
import com.rcappstudio.campingapp.data.model.NgoData
import com.rcappstudio.campingapp.data.model.RequestStatus
import com.rcappstudio.campingapp.data.model.UdidReferenceModel
import com.rcappstudio.campingapp.data.model.UserModel
import com.rcappstudio.campingapp.databinding.ActivityUserDetailsBinding
import com.rcappstudio.campingapp.utils.getDateTime
import com.rcappstudio.campingapp.utils.snakeToLowerCamelCase
import com.squareup.picasso.Picasso


class UserDetailsActivity : AppCompatActivity() {

    private lateinit var userObject: UserModel

    private lateinit var binding: ActivityUserDetailsBinding

    private lateinit var requestStatusKeys : MutableList<String>

    private lateinit var campId : String

    private lateinit var userPath : String

    private lateinit var userId : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPref = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
        campId = sharedPref.getString(Constants.CAMP_ID ,null)!!
        requestStatusKeys = mutableListOf()
        userObject = Gson().fromJson(intent.getStringExtra("userObject"), UserModel::class.java)
        userPath = Constants.USERS + "/" + userObject.state + "/" + userObject.district

        initView()
        fetchRequiredData()
        Log.d("campID", "onCreate: $campId")
    }

    private fun initView() {

        binding.name.text = "Name: " + userObject.name
        binding.dob.text = "DOB: " + userObject.dateOfBirth
        binding.mobileNo.text = "Mobile no: " + userObject.mobileNo

        Picasso
            .get()
            .load(userObject.profileImageUrl)
            .fit().centerCrop()
            .into(binding.profileIage)



    }

    private fun showAlertDialog(aidsToBeDelivered : MutableList<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure?")

        builder.setPositiveButton("Yes") { dialog, which ->
            updateDatabase(aidsToBeDelivered)
        }

        builder.setNegativeButton("No") { dialog, which ->
            Toast.makeText(
                applicationContext,
                "Noooo", Toast.LENGTH_SHORT
            ).show()
        }

        builder.show()
    }

    private fun fetchRequiredData() {

        var requestStatusList = mutableListOf<RequestStatus>()
        FirebaseDatabase.getInstance()
           // .getReference("${Constants.UDID_NO_LIST}/${userObject.udidNo}")
            .getReference("${Constants.UDID_NO_LIST}/${userObject.udidNo}")
            .get().addOnSuccessListener { it ->
                if (it.exists()) {
                    val c = it.getValue(UdidReferenceModel::class.java)
                    userId = c?.userId!!
                    FirebaseDatabase.getInstance()
                       .getReference("${Constants.USERS}/${userObject.state}/${userObject.district}/${c.userId}/requestStatus")
                        //.getReference("${Constants.USERS}/Telangana/Adilabad/Hd4Mbv0xJnMETkdXAbJzLxaerGI2/requestStatus")
                        .get().addOnSuccessListener { snapshot ->

                            if(snapshot.exists()){
                                for(snap in snapshot.children){
                                    requestStatusKeys.add(snap.key!!)
                                    val requestStatus = snap.getValue(RequestStatus::class.java)
                                    requestStatusList.add(requestStatus!!)
                                }
                                filteredCampData(requestStatusList)
                            }
                        }
                } else{
//                    Log.d("campID", "fetchRequiredData: no data!!")
                }
            }
    }
//        FirebaseDatabase.getInstance().getReference("${Constants.UDID_NO_LIST}/${userObject.udidNo}")
//            .get().addOnSuccessListener {
//                val c = it.getValue(UdidReferenceModel::class.java)
//                FirebaseDatabase.getInstance()
//                    .getReference("${Constants.USERS}/${userObject.state}/${userObject.district}/${c!!.userId}/requestStatus")
//                    .child("aidsReceived")
//                    .setValue(true).addOnSuccessListener {
//                        finish()
//                    }
//            }


    private fun filteredCampData(requestStatusList: MutableList<RequestStatus>){

        val generalCampAllocatedList = mutableListOf<NgoData>()
        val currentCampList = mutableListOf<NgoData>()

        for(requestStatus in requestStatusList){
            if(requestStatus.ngoList != null){
                generalCampAllocatedList.addAll(requestStatus.ngoList!!.values.toMutableList())
            }
        }

        for(camps in generalCampAllocatedList){
            if(camps.ngoId == campId){
                currentCampList.add(camps)
            }
        }
        populateData(currentCampList)
    }

    private fun populateData(campList : MutableList<NgoData>){
        //Checking weather all aids of particular ngo is received or not

        for(c in campList){
            if(!c.aidsReceived!!){
                changeNotReceivedView()
                break
            } else{
                changeReceivedView()
                break
            }
        }

        var aidsNames = "\t\t\t\t\t\t"
        var count = 0
        var aidsToBeReceived = mutableListOf<String>()
        for(c in campList){
            if(!c.aidsReceived!!){
                for(aid in c.aidsList!!){
                    count++
                    aidsToBeReceived.add(aid)
                    aidsNames += "$count)$aid\n\t\t\t\t\t\t"
                }
            } else{
                //TODO: Yet to add just display data
            }
        }
        binding.tvAidsList.text = aidsNames

//        Log.d("campList", "fetchRequiredData: ${aidsToBeReceived.size}")
        binding.deliveryButton.setOnClickListener {
            showAlertDialog(aidsToBeReceived)
        }

    }

    private fun updateDatabase(aidsToBeDelivered: MutableList<String>){

        //TODO: Yet to do out of stock module and to hide the delivery button
        if(aidsToBeDelivered.isNotEmpty()){
            for (aidName in aidsToBeDelivered){
                FirebaseDatabase.getInstance().getReference("${Constants.CAMPING}/$campId/aidsData/${aidName.snakeToLowerCamelCase()}")
                    .get().addOnSuccessListener {
                        if(it.exists()){
                            var newValue = it.value.toString().toInt() - 1
                            FirebaseDatabase.getInstance().getReference("${Constants.CAMPING}/$campId/aidsData/${aidName.snakeToLowerCamelCase()}")
                                .setValue(newValue)
                        }
                    }
            }

            for(aidName in aidsToBeDelivered){
                FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${userObject.state}/${userObject.district}/$userId/aidsApplied/${aidName.snakeToLowerCamelCase()}")
                    .setValue(true)
            }


            FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${userObject.state}/${userObject.district}/$userId/requestStatus")
                .get().addOnSuccessListener {
                    if(it.exists()){
                        for(i in it.children){
                            FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${userObject.state}/${userObject.district}/$userId/requestStatus/${i.key}/ngoList")
                                .get().addOnSuccessListener { snap->
                                    if(snap.exists()){
                                        for(n in snap.children){
                                            val ngo = n.getValue(NgoData::class.java)
                                            if(ngo!!.ngoId == campId ){
                                                FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${userObject.state}/${userObject.district}/$userId/requestStatus/${i.key}/ngoList/${n.key}/aidsReceived")
                                                    .setValue(true)
                                            }
                                        }
                                    }
                                }
                        }
                    }


                }

        } else{
            Toast.makeText(applicationContext, "Allocated aids are deleveried", Toast.LENGTH_LONG).show()
        }


//        FirebaseDatabase.getInstance().getReference("${Constants.USERS}/Telangana/Adilabad/$userId/requestStatus")
//            .get().addOnSuccessListener { snapshot->
//                if(snapshot.exists()){
//                    for(s in snapshot.children){
//                        if()
//                    }
//                }
//
//            }

    }

    private fun changeNotReceivedView(){
        binding.aidsDeliveryCardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.redLight
                )
            )
            binding.aidsDeliveryCardView.strokeColor =
                ContextCompat.getColor(applicationContext, R.color.red)
    }

    private fun changeReceivedView(){
        binding.aidsDeliveryCardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.greenLight
                )
            )
            binding.aidsDeliveryCardView.strokeColor =
                ContextCompat.getColor(applicationContext, R.color.green)
            binding.aidsDeliveryTextView.text = "Aids/Appliance"
    }
}
