package com.rcappstudio.campingapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.rcappstudio.adip.notifications.NotificationData
import com.rcappstudio.adip.notifications.PushNotification
import com.rcappstudio.adip.notifications.RetrofitInstance
import com.rcappstudio.campingapp.R
import com.rcappstudio.campingapp.data.model.NgoData
import com.rcappstudio.campingapp.data.model.RequestStatus
import com.rcappstudio.campingapp.data.model.UdidReferenceModel
import com.rcappstudio.campingapp.data.model.UserModel
import com.rcappstudio.campingapp.databinding.ActivityHomeUserDetailsBinding
import com.rcappstudio.campingapp.utils.Constants
import com.rcappstudio.campingapp.utils.getDateTime
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeUserDetailsActivity : AppCompatActivity() {


    private lateinit var userObject : UserModel

    private lateinit var binding : ActivityHomeUserDetailsBinding

    private lateinit var requestStatusKeys : MutableList<String>

    private lateinit var campId : String

    private lateinit var userPath : String

    private lateinit var userId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userObject = Gson().fromJson(intent.getStringExtra("userObject"), UserModel::class.java)
        val sharedPref = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
        campId = sharedPref.getString(Constants.CAMP_ID ,null)!!
        requestStatusKeys = mutableListOf()
        userObject = Gson().fromJson(intent.getStringExtra("userObject"), UserModel::class.java)
        userPath = Constants.USERS + "/" + userObject.state + "/" + userObject.district

        initView()
        fetchRequiredData()
    }

    private fun initView(){
        binding.name.text = "Name: " + userObject.name
        binding.dob.text = "DOB: " + userObject.dateOfBirth
        binding.mobileNo.text = "Mobile no: " + userObject.mobileNo

        Picasso
            .get()
            .load(userObject.profileImageUrl)
            .fit().centerCrop()
            .into(binding.profileIage)

        binding.notifyUser.setOnClickListener {
            PushNotification(
                NotificationData("Aids arrived", "Your respective aids has been arrived at ${userObject.district} camp. For more information please see status bar. "),
                userObject.fcmToken!!.toString()
            ).also {
                sendNotification(it)
            }

            Toast.makeText(applicationContext,"Notification has been sent successfully",Toast.LENGTH_LONG).show()
            binding.notifyUser.visibility = View.GONE
        }
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
                    Toast.makeText(applicationContext, "No data present!!" , Toast.LENGTH_LONG).show()
//                    Log.d("campID", "fetchRequiredData: no data!!")
                }
            }
    }

    private fun filteredCampData(requestStatusList: MutableList<RequestStatus>){

        val generalCampAllocatedList = mutableListOf<NgoData>()
        val currentCampList = mutableListOf<NgoData>()

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
                //changeReceivedView()
            }
        }

        var aidsNames = "Aids allocated: \n\t\t\t\t\t\t"
        var count = 0
        var aidsToBeReceived = mutableListOf<String>()
        val aidsAlreadyReceived = mutableListOf<String>()
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

            var count2 = 0
            var aidsNames2 = ""
            Log.d("aidsData", "populateData: ${aidsAlreadyReceived.size}")

            Toast.makeText(applicationContext , "All aids are delivered to the user", Toast.LENGTH_LONG).show()
//            binding.tvAidsAllocated.visibility = View.INVISIBLE
            binding.tvAidsList.visibility = View.VISIBLE
            binding.aidsDeliveryTextView.text = "Aids/Appliance received"
            binding.aidsDeliveryCardView.strokeColor = ContextCompat.getColor(applicationContext, R.color.green)
            binding.aidsDeliveryCardView.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.greenLight))
            binding.aidsTextCardView.strokeColor = ContextCompat.getColor(applicationContext, R.color.green)
            binding.aidsTextCardView.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.greenLight))

            if(aidsAlreadyReceived.isNotEmpty()){
                for(aid in aidsAlreadyReceived){
                    count2++
                    aidsNames2 += "\t\t\t\t\t\t$count2)$aid\n"

                }
                binding.tvAidsList.text = aidsNames2

            }else{
                binding.aidsDeliveryTextView.text = "No aids were allocated in this camp"
                binding.aidsTextCardView.visibility = View.INVISIBLE
                binding.tvAidsList.visibility = View.INVISIBLE
                binding.notifyUser.visibility = View.INVISIBLE
            }

        } else {
//
            binding.aidsDeliveryTextView.text = "Aids/Appliance not received"
            binding.aidsTextCardView.strokeColor = ContextCompat.getColor(applicationContext, R.color.red)
            binding.aidsDeliveryCardView.strokeColor = ContextCompat.getColor(applicationContext, R.color.red)
            binding.aidsDeliveryCardView.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.redLight))
            binding.aidsTextCardView.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.redLight))
            binding.tvAidsList.text = aidsNames
        }
    }


    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Toast.makeText(applicationContext,"Notification has been sent successfully",Toast.LENGTH_LONG).show()
                binding.notifyUser.visibility = View.GONE
            } else {
//                Log.e("TAG", response.errorBody().toString())
                Toast.makeText(applicationContext,"Error occured",Toast.LENGTH_LONG).show()

            }
        } catch(e: Exception) {
            Log.e("TAG", e.toString())
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
        binding.aidsDeliveryTextView.text = "Aids/Appliance"
    }



}