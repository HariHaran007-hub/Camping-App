package com.rcappstudio.campingapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.rcappstudio.adip.notifications.NotificationData
import com.rcappstudio.adip.notifications.PushNotification
import com.rcappstudio.adip.notifications.RetrofitInstance
import com.rcappstudio.campingapp.R
import com.rcappstudio.campingapp.data.model.UserModel
import com.rcappstudio.campingapp.databinding.ActivityHomeUserDetailsBinding
import com.rcappstudio.campingapp.utils.getDateTime
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeUserDetailsActivity : AppCompatActivity() {


    private lateinit var userObject : UserModel

    private lateinit var binding : ActivityHomeUserDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userObject = Gson().fromJson(intent.getStringExtra("userObject"), UserModel::class.java)
        initView()
    }

    private fun initView(){
//        binding.appliedOn.text = "Applied on: "+ getDateTime(userObject?.requestStatus!!.appliedOnTimeStamp!!)
//        binding.name.text = "Name: "+userObject.name
//        binding.dob.text = "DOB: " +userObject.dateOfBirth
//        binding.mobileNo.text = "Mobile no: "+ userObject.mobileNo
//
//        if(userObject.requestStatus!!.aidsReceived!!){
//            binding.aidsDeliveryCardView.setCardBackgroundColor(ContextCompat.getColor(applicationContext,
//                R.color.greenLight
//            ))
//            binding.aidsDeliveryCardView.strokeColor = ContextCompat.getColor(applicationContext,
//                R.color.green
//            )
//            binding.aidsDeliveryTextView.text = "Aids/Appliance"
//        } else{
//            binding.aidsDeliveryCardView.setCardBackgroundColor(ContextCompat.getColor(applicationContext,
//                R.color.redLight
//            ))
//            binding.aidsDeliveryCardView.strokeColor = ContextCompat.getColor(applicationContext,
//                R.color.red
//            )
//        }

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

        Picasso
            .get()
            .load(userObject.profileImageUrl)
            .fit().centerCrop()
            .into(binding.profileIage)

//        Picasso.get()
//            .load(userObject.aidsVerificationDocs?.disabilityCertificateURL)
//            .fit().centerCrop()
//            .into(binding.disabilityCertificateImage)
//
//        Picasso.get()
//            .load(userObject.aidsVerificationDocs?.passportSizePhotoURL)
//            .fit().centerCrop()
//            .into(binding.passportSizeImage)
//
//        Picasso.get()
//            .load(userObject.aidsVerificationDocs?.incomeTaxCertificateUrl)
//            .fit().centerCrop()
//            .into(binding.incomeCertificate)
//
//        Picasso.get()
//            .load(userObject.aidsVerificationDocs?.identityProofUrl)
//            .fit().centerCrop()
//            .into(binding.identityProof)
//
//        Picasso.get()
//            .load(userObject.aidsVerificationDocs?.addressProofUrl)
//            .fit().centerCrop()
//            .into(binding.addressProof)
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

}