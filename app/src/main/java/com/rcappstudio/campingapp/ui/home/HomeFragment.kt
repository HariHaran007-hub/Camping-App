package com.rcappstudio.campingapp.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.rcappstudio.adip.notifications.NotificationData
import com.rcappstudio.adip.notifications.PushNotification
import com.rcappstudio.adip.notifications.RetrofitInstance
import com.rcappstudio.campingapp.data.model.CampingModel
import com.rcappstudio.campingapp.data.model.UserModel
import com.rcappstudio.campingapp.data.model.VerifiedUserData
import com.rcappstudio.campingapp.databinding.FragmentHomeBinding
import com.rcappstudio.campingapp.ui.activity.HomeUserDetailsActivity
import com.rcappstudio.campingapp.utils.Constants
import com.rcappstudio.campingapp.utils.LoadingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.jar.Manifest


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var map: GoogleMap? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private val binding get() = _binding!!

    private lateinit var state: String
    private lateinit var district: String

    private lateinit var loadingDialog : LoadingDialog

    private var verifiedUserIdList: MutableList<VerifiedUserData> = mutableListOf()

    private var userList: MutableList<UserModel> = mutableListOf()

    private lateinit var verifiedUserAdapter: VerifiedUsersAdapter

    private var aidsNotReceivedLUserList: MutableList<UserModel> = mutableListOf()

    private lateinit var campId : String



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity(), "Loading Home page")
        init()
        binding.bottomSheet.mapView.onCreate(savedInstanceState)
        clickListener()
        fetchVerifiedUserId()
    }

    private fun init(){
        val sharedPref = requireContext().getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
        state = sharedPref.getString(Constants.STATE, null)!!
        district = sharedPref.getString(Constants.DISTRICT, null)!!
        binding.location.text = "Location: " + district
        campId = sharedPref.getString(Constants.CAMP_ID, null)!!
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.root)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED;
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

        })
    }

    private fun mapPermissionChecker() {
        Dexter.withContext(requireContext())
            .withPermissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    //TODO: Fetch gps location
                    fetchLocationDetails()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).check()

    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(requireContext()).setMessage("Please enable the required permissions")
            .setPositiveButton("GO TO SETTINGS")
            { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireContext().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel")
            { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationDetails(){

        //TODO: fetch data and display it
        val fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireContext())

        fusedLocationProvider.lastLocation.addOnSuccessListener {
            if(it != null){
                val geoCoder = Geocoder(requireContext())
                val currentLocation = geoCoder.getFromLocation(it.latitude,it.longitude,1)
                val latLng = com.rcappstudio.campingapp.data.model.LatLng((it.latitude).toString(), (it.longitude).toString(),
                    currentLocation.first().locality +", " + currentLocation.first().adminArea)

                FirebaseDatabase.getInstance().getReference("${Constants.CAMPING}/${campId}")
                    .child("location")
                    .setValue(latLng)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            Log.d("TAGlocation", "fetchLocationDetails: success")
                        }
                    }
                FirebaseDatabase.getInstance().getReference("${Constants.CAMPING_LOCATIONS}/$state/$district/$campId")
                    .setValue(latLng)
//            map!!.addMarker(MarkerOptions().position(latLng)
//                .icon(BitmapDescriptorFactory.defaultMarker(
//                BitmapDescriptorFactory.HUE_RED)))

        }}
    }

    private fun setUpMap() {
        binding.bottomSheet.mapView.getMapAsync {
            map = it
            fetchCampingLocations()
        }
    }

    private fun fetchCampingLocations(){
        FirebaseDatabase.getInstance().getReference(Constants.CAMPING)
            .get().addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    for(loc in snapshot.children){
                        val camp = loc.getValue(CampingModel::class.java)
                        if(camp!!.location != null){
                            val location = camp!!.location
                            val latLng = LatLng(location?.lat!!.toDouble(), location.lng!!.toDouble())
                            map!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                            map!!.addMarker(MarkerOptions().position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_RED)))
                        }
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomSheet.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.bottomSheet.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.bottomSheet.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.bottomSheet.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.bottomSheet.mapView.onLowMemory()
    }


    private fun fetchVerifiedUserId() {
        loadingDialog.startLoading()
        verifiedUserIdList.clear()
        FirebaseDatabase.getInstance()
            .getReference("${Constants.VERIFICATION_COMPLETED}/$state/$district")
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (c in snapshot.children) {
                        val data = c.getValue(VerifiedUserData::class.java)
                        verifiedUserIdList.add(data!!)
                    }
                    extractIndividualDetails()
                } else{
                    loadingDialog.isDismiss()
                    Toast.makeText(requireContext(), "No data!!", Toast.LENGTH_LONG).show()
                }
            }
    }


    private fun extractIndividualDetails() {
        userList.clear()
        var i = 0

        for (u in verifiedUserIdList) {
            i++
            Log.d("iLoop", "extractIndividualDetails: $i")
            FirebaseDatabase.getInstance()
                .getReference("${Constants.USERS}/${u.state}/${u.district}/${u.userId}")
                .get().addOnSuccessListener {
                    if (it.exists()) {
                        val user = it.getValue(UserModel::class.java)
                        userList.add(user!!)
                        if (i == verifiedUserIdList.size) {
                            initRecyclerView()
                            calculateStatisticalData()

                        }
                        Log.d(
                            "TAG",
                            "extractIndividualDetails: ${user!!.aidsVerificationDocs!!.addressProofUrl}"
                        )
                        loadingDialog.isDismiss()
                    }else{
                        //TODO: no data
                        Toast.makeText(requireContext(), "No data!!", Toast.LENGTH_LONG).show()
                        loadingDialog.isDismiss()
                    }
                }
        }
    }

    private fun calculateStatisticalData() {
        var received = 0
        var notReceived = 0
        for (c in userList) {
            if (c.requestStatus?.aidsReceived!!)
                received++
            else {
                notReceived++
                aidsNotReceivedLUserList.add(c)
            }
        }

        binding.received.text = "No of applicants received aids: $received"
        binding.notReceived.text = "No of applicants not received aids: $notReceived"
    }

    private fun clickListener() {
        binding.notifyAllUsers.setOnClickListener {
            if (aidsNotReceivedLUserList.isNotEmpty()) {
                for (c in aidsNotReceivedLUserList) {
                    createNotification(c.fcmToken.toString(), c.district!!)
                }
            } else {
                Toast.makeText(requireContext(), "No users left to get aids!!", Toast.LENGTH_LONG)
                    .show()
            }
        }

        binding.bottomSheet.updateLocation.setOnClickListener {
            Log.d("ClickTag", "clickListener: clicked")
            mapPermissionChecker()
        }

        binding.bottomSheet.llRoot.setOnClickListener {

            val state = if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                BottomSheetBehavior.STATE_COLLAPSED else{
                    setUpMap()
                    BottomSheetBehavior.STATE_EXPANDED
            }

            bottomSheetBehavior.state = state
        }


  }

    private fun initRecyclerView() {
        binding.rvVerifiedUsers.layoutManager = LinearLayoutManager(requireContext())
        verifiedUserAdapter = VerifiedUsersAdapter(requireContext(), userList) { item, pos ->
            val intent = Intent(requireContext(), HomeUserDetailsActivity::class.java)
            intent.putExtra("userObject", Gson().toJson(item))
            startActivity(intent)
        }
        binding.rvVerifiedUsers.adapter = verifiedUserAdapter
    }

    private fun createNotification(fcmToken: String, district: String) {
        PushNotification(
            NotificationData(
                "Aids arrived",
                "Your respective aids has been arrived at ${district} camp. For more information please see the status bar."
            ),
            fcmToken
        ).also {
            sendNotification(it)
        }
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.d("notificationSent", "sendNotification: success!!")
                } else {
                    Log.d("notificationSent", "sendNotification: failure!!")
                }
            } catch (e: Exception) {
                Log.e("TAG", e.toString())
            }
        }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}