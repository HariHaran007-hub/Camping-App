package com.rcappstudio.campingapp.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.rcappstudio.campingapp.utils.Constants
import com.rcappstudio.campingapp.R
import com.rcappstudio.campingapp.data.model.*
import com.rcappstudio.campingapp.databinding.ActivityUserDetailsBinding
import com.rcappstudio.campingapp.utils.LoadingDialog
import com.rcappstudio.campingapp.utils.getDateTime
import com.rcappstudio.campingapp.utils.snakeToLowerCamelCase
import com.squareup.picasso.Picasso
import java.util.*


class UserDetailsActivity : AppCompatActivity() {

    private lateinit var userObject: UserModel

    private lateinit var loadingDialog: LoadingDialog

    private lateinit var binding: ActivityUserDetailsBinding

    private lateinit var requestStatusKeys : MutableList<String>

    private lateinit var campId : String

    private lateinit var userPath : String

    private lateinit var userId : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        loadingDialog = LoadingDialog(this, "Loading user details please wait!!")
        setContentView(binding.root)
        val sharedPref = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
        campId = sharedPref.getString(Constants.CAMP_ID ,null)!!
        requestStatusKeys = mutableListOf()
        userObject = Gson().fromJson(intent.getStringExtra("userObject"), UserModel::class.java)
        userPath = Constants.USERS + "/" + userObject.state + "/" + userObject.district

        initView()
        Log.d("dataSet", "updateDatabase: ${userObject.requestStatus!!.keys}")
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
        loadingDialog.startLoading()
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
                                    loadingDialog.isDismiss()
                                }
                                filteredCampData(requestStatusList)
                            }
                        }
                } else{
                    Toast.makeText(applicationContext, "No data present!!" , Toast.LENGTH_LONG).show()
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
        Log.d("tagData", "filteredCampData: $campId")


        for(requestStatus in requestStatusList){
            if(requestStatus.ngoList != null && requestStatus.documentVerified!!){
                generalCampAllocatedList.addAll(requestStatus.ngoList!!.values.toMutableList())
            }
        }

        for(camps in generalCampAllocatedList){
            if(camps.ngoId == campId){
                currentCampList.add(camps)
            }
        }
        Log.d("tagData", "filteredCampData: ${currentCampList.size}")
        populateData(currentCampList)
    }

    private fun populateData(campList : MutableList<NgoData>){
        //Checking weather all aids of particular ngo is received or not
        //campList is the filtered current camp list
        var flagReceived = 0
        var flagNotReceived = 0

        for(c in campList){
            if(!c.aidsReceived!!){
                //changeNotReceivedView()
                break
            } else{
//                changeReceivedView()
            }
        }

        var aidsNames = "\t\t\t\t\t\t"
        var count = 0
        var aidsToBeReceived = mutableListOf<String>()
        val aidsAlreadyReceived = mutableListOf<String>()
        val currentCamp = mutableListOf<NgoData>()
        for(c in campList){
            if(!c.aidsReceived!!){
                for(aid in c.aidsList!!){
                    count++
                    aidsToBeReceived.add(aid)
                    aidsNames += "$count)$aid\n\t\t\t\t\t\t"
                }
            } else{
                flagReceived++
                for (aid in c.aidsList!!){
                    aidsAlreadyReceived.add(aid)
                }
            }
        }

        if(flagReceived == campList.size){
            //TODO need to hide aids delevery
            var count2 = 0
            var aidsNames2 = ""
            Log.d("aidsData", "populateData: ${aidsAlreadyReceived.size}")

            Toast.makeText(applicationContext , "All aids are delivered to the user", Toast.LENGTH_LONG).show()
//            binding.tvAidsAllocated.visibility = View.INVISIBLE
            binding.deliveryButton.visibility = View.INVISIBLE
            binding.tvAidsList.visibility = View.VISIBLE
            binding.aidsDeliveryTextView.text = "Aids/Appliance received"
            binding.aidsDeliveryCardView.strokeColor = ContextCompat.getColor(applicationContext, R.color.green)
            binding.aidsDeliveryCardView.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.greenLight))
            binding.statusAidsList.strokeColor = ContextCompat.getColor(applicationContext, R.color.green)
            binding.statusAidsList.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.greenLight))

            if(aidsAlreadyReceived.isNotEmpty()){
                for(aid in aidsAlreadyReceived){
                    count2++
                    aidsNames2 += "\t\t\t\t\t\t$count2)$aid\n"

                }
                binding.tvAidsList.text = aidsNames2

            }else{
                binding.aidsDeliveryTextView.text = "No aids were allocated in this camp"
                binding.statusAidsList.visibility = View.INVISIBLE
                binding.tvAidsList.visibility = View.INVISIBLE
            }


        } else {
            binding.aidsDeliveryTextView.text = "Aids/Appliance not received"
            binding.statusAidsList.strokeColor = ContextCompat.getColor(applicationContext, R.color.red)
            binding.aidsDeliveryCardView.strokeColor = ContextCompat.getColor(applicationContext, R.color.red)
            binding.aidsDeliveryCardView.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.redLight))
            binding.statusAidsList.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.redLight))
            binding.tvAidsList.text = aidsNames
        }


//        Log.d("campList", "fetchRequiredData: ${aidsToBeReceived.size}")
        binding.deliveryButton.setOnClickListener {
            showAlertDialog(aidsToBeReceived)
        }

    }

    private fun updateDatabase(aidsToBeDelivered: MutableList<String>){

        val sharedPref = getSharedPreferences(Constants.SHARED_PREF , MODE_PRIVATE)
        val ngoId = sharedPref.getString(Constants.CAMP_ID, null)
        //TODO: Yet to do out of stock module and to hide the delivery button
        if(aidsToBeDelivered.isNotEmpty()){
            for (aidName in aidsToBeDelivered){
                FirebaseDatabase.getInstance().getReference("${Constants.CAMPING}/$campId/aidsData/${aidName}")
                    .get().addOnSuccessListener {
                        if(it.exists()){
                            var newValue = it.value.toString().toInt() - 1
                            FirebaseDatabase.getInstance().getReference("${Constants.CAMPING}/$campId/aidsData/${aidName}")
                                .setValue(newValue).addOnSuccessListener {
                                   binding.deliveryButton.visibility = View.GONE
                                }
                        }
                    }
            }
            FirebaseDatabase.getInstance().getReference("aidsReceived")
                .push().setValue(
                    AidsReceivedModel(
                        name= userObject.name,
                        udidNo = userObject.udidNo,
                        state = userObject.state,
                        district = userObject.district,
                        percentageOfDisability = userObject.percentageOfDisability,
                        category = userObject.category,
                        gender = userObject.gender,
                        dob = userObject.dateOfBirth,
                        aidsList = aidsToBeDelivered,
                        ngoId = ngoId,
                        deliveredOn = Calendar.getInstance().timeInMillis
                    )
                )
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




//            FirebaseDatabase.getInstance().getReference("aidsReceived")
//                .setValue()

        } else{
            Toast.makeText(applicationContext, "Allocated aids are deleveried", Toast.LENGTH_LONG).show()
        }
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
            binding.aidsDeliveryTextView.text = "Aids/Appliance received"
    }
}
