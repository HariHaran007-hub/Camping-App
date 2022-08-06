package com.rcappstudio.campingapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.rcappstudio.campingapp.utils.Constants
import com.rcappstudio.campingapp.R
import com.rcappstudio.campingapp.data.model.UdidReferenceModel
import com.rcappstudio.campingapp.data.model.UserModel
import com.rcappstudio.campingapp.databinding.ActivityUserDetailsBinding
import com.rcappstudio.campingapp.utils.getDateTime
import com.squareup.picasso.Picasso


class UserDetailsActivity : AppCompatActivity() {

    private lateinit var userObject : UserModel

    private lateinit var binding: ActivityUserDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userObject = Gson().fromJson(intent.getStringExtra("userObject"), UserModel::class.java)
        initView()
    }

    private fun initView(){
        binding.appliedOn.text = "Applied on: "+ getDateTime(userObject?.requestStatus!!.appliedOnTimeStamp!!)
        binding.name.text = "Name: "+userObject.name
        binding.dob.text = "DOB: " +userObject.dateOfBirth
        binding.mobileNo.text = "Mobile no: "+ userObject.mobileNo

        if(userObject.requestStatus!!.aidsReceived!!){
            binding.aidsDeliveryCardView.setCardBackgroundColor(ContextCompat.getColor(applicationContext,R.color.greenLight))
            binding.aidsDeliveryCardView.strokeColor = ContextCompat.getColor(applicationContext, R.color.green)
            binding.aidsDeliveryTextView.text = "Aids/Appliance"
        } else{
            binding.aidsDeliveryCardView.setCardBackgroundColor(ContextCompat.getColor(applicationContext,R.color.redLight))
            binding.aidsDeliveryCardView.strokeColor = ContextCompat.getColor(applicationContext,R.color.red)
        }

        Picasso
            .get()
            .load(userObject.profileImageUrl)
            .fit().centerCrop()
            .into(binding.profileIage)

        Picasso.get()
            .load(userObject.aidsVerificationDocs?.disabilityCertificateURL)
            .fit().centerCrop()
            .into(binding.disabilityCertificateImage)

        Picasso.get()
            .load(userObject.aidsVerificationDocs?.passportSizePhotoURL)
            .fit().centerCrop()
            .into(binding.passportSizeImage)

        Picasso.get()
            .load(userObject.aidsVerificationDocs?.incomeTaxCertificateUrl)
            .fit().centerCrop()
            .into(binding.incomeCertificate)

        Picasso.get()
            .load(userObject.aidsVerificationDocs?.identityProofUrl)
            .fit().centerCrop()
            .into(binding.identityProof)

        Picasso.get()
            .load(userObject.aidsVerificationDocs?.addressProofUrl)
            .fit().centerCrop()
            .into(binding.addressProof)

        binding.deliveryButton.setOnClickListener {
            showAlertDialog()
        }
    }

    private fun showAlertDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure?")

        builder.setPositiveButton("Yes") { dialog, which ->
            FirebaseDatabase.getInstance().getReference("${Constants.UDID_NO_LIST}/${userObject.udidNo}")
                .get().addOnSuccessListener {
                    val c = it.getValue(UdidReferenceModel::class.java)
                    FirebaseDatabase.getInstance()
                        .getReference("${Constants.USERS}/${userObject.state}/${userObject.district}/${c!!.userId}/requestStatus")
                        .child("aidsReceived")
                        .setValue(true).addOnSuccessListener {
                            finish()
                        }
                }

        }

        builder.setNegativeButton("No") { dialog, which ->
            Toast.makeText(applicationContext,
                "Noooo", Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }
}