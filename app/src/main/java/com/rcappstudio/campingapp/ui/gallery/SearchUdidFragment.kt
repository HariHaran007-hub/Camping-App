package com.rcappstudio.campingapp.ui.gallery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.rcappstudio.campingapp.utils.Constants
import com.rcappstudio.campingapp.R
import com.rcappstudio.campingapp.databinding.FragmentSearchUdidBinding
import com.rcappstudio.campingapp.data.model.UdidReferenceModel
import com.rcappstudio.campingapp.data.model.UserModel
import com.rcappstudio.campingapp.utils.getDateTime
import com.rcappstudio.campingapp.ui.UserDetailsActivity
import com.rcappstudio.campingapp.utils.LoadingDialog
import com.squareup.picasso.Picasso

class SearchUdidFragment : Fragment() {

private var _binding: FragmentSearchUdidBinding? = null
    private lateinit var loadingDialog: LoadingDialog
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {


    _binding = FragmentSearchUdidBinding.inflate(inflater, container, false)
    val root: View = binding.root
    return root
  }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity(), "Fetching user details")
        initSearchView()
    }


    private fun initSearchView(){
        binding.searchView.setOnQueryTextListener(object  :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query != null){
                    fetchDataFromDatabase(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }


        })
    }



    private fun fetchDataFromDatabase(udidNumber : String){
        //TODO: Making it and input id
        loadingDialog.startLoading()
        FirebaseDatabase.getInstance().getReference("udidNoList/${udidNumber}")
            .get()
            .addOnSuccessListener { snapshot->

                if(snapshot.exists()){
                    val user = snapshot.getValue(UdidReferenceModel::class.java)
                    checkIsVerified(user)
                } else {

                    //TODO : Show error message that user does not exist
                    Toast.makeText(requireContext(), "User does not exist", Toast.LENGTH_LONG).show()
                    loadingDialog.isDismiss()
                }
            }
    }

    private fun checkIsVerified(user : UdidReferenceModel?){
        FirebaseDatabase.getInstance().getReference("verificationList/${user!!.userId}")
            .get()
            .addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    getSpecificUserDetails(user)
                } else{
                    //TODO: User is not verified and update the UI accordingly
                    Toast.makeText(requireContext(), "User was not verified by the admin", Toast.LENGTH_LONG).show()
                    Log.d("userData", "no data exist ")
                    loadingDialog.isDismiss()

                }

            }
    }

    private fun getSpecificUserDetails(user : UdidReferenceModel?){
        FirebaseDatabase.getInstance()
            .getReference("${Constants.USERS}/${user!!.state}/${user.district}/${user.userId}")
            .get().addOnSuccessListener { snapshot->

                if(snapshot.exists()){
                    val c = snapshot.getValue(UserModel::class.java)
                    loadingDialog.isDismiss()
                    initDetailsView(c!!)
                } else{
                    //TODO: show error dialog
                    Toast.makeText(requireContext(), "User was not verified by the admin", Toast.LENGTH_LONG).show()
                    loadingDialog.isDismiss()
                }

            }
    }

    private fun initDetailsView(userData : UserModel){

        binding.userCardView.visibility = View.VISIBLE

//        if(userData.requestStatus!!.aidsReceived!!){
//            binding.userCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.greenLight))
//        }

        Picasso.get()
            .load(userData.profileImageUrl)
            .fit().centerCrop()
            .into(binding.profileImage)

        binding.userName.text = "Name: "+ userData.name
        binding.mobileNumber.text = "Numbmer: "+ userData.mobileNo
        binding.dateOfBirth.text = "Date of birth: "+ userData.dateOfBirth
       // binding.appliedOn.text = "Applied on: "+ getDateTime(userData.requestStatus?.appliedOnTimeStamp!!)

        binding.userCardView.setOnClickListener {
            val str = Gson().toJson(userData)
            Log.d("TAG", "initDetailsView: ${str}")
            val intent =  Intent(requireContext(), UserDetailsActivity::class.java)
            intent.putExtra("userObject", str)
            startActivity(intent)
        }
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}