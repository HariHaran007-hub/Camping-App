package com.rcappstudio.campingapp.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase
import com.rcappstudio.campingapp.data.model.CampingModel
import com.rcappstudio.campingapp.databinding.ActivityLoginBinding
import com.rcappstudio.campingapp.utils.Constants

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var loginDetailsList : MutableList<CampingModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getLoginDetails()

    }

    private fun clickListener(){
        binding.login.setOnClickListener {
            if(binding.emailId.text.toString().isNotEmpty() && binding.password.text.toString().isNotEmpty()){
                login()
            }
        }
    }

    private fun getLoginDetails(){
        FirebaseDatabase.getInstance().getReference(Constants.CAMPING)
            .get().addOnSuccessListener { snapshot->

                if(snapshot.exists()){
                    for(c in snapshot.children){
                        val campingModel = c.getValue(CampingModel::class.java)
                        loginDetailsList.add(campingModel!!)
                    }
                    clickListener()
                }
            }
    }

    private fun login(){
        for(camp in loginDetailsList){
            if(camp.emailId == binding.emailId.text.toString() && camp.password == binding.password.text.toString()){
                saveToSharedPref(camp)
            }
        }
    }

    private fun saveToSharedPref(camp : CampingModel){
        val sharedPref = applicationContext.getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)

        sharedPref.edit().apply {
            putString(Constants.STATE , camp.state.toString())
            putString(Constants.CAMPING_NAME, camp.campingName.toString())
            putString(Constants.DISTRICT , camp.district)
            putString(Constants.CAMP_ID, camp.campId)
            putString(Constants.CAMP_EMAIL , camp.emailId)

        }.apply()
        startActivity(Intent(this , MainActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()
        val sharedPref = applicationContext.getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
        if(sharedPref.getString(Constants.STATE, null) != null){
            startActivity(Intent(this , MainActivity::class.java))
            finish()
        }

    }

}