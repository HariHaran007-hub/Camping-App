package com.rcappstudio.campingapp.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
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
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.rcappstudio.adip.notifications.NotificationData
import com.rcappstudio.adip.notifications.PushNotification
import com.rcappstudio.adip.notifications.RetrofitInstance
import com.rcappstudio.campingapp.data.model.CampingModel
import com.rcappstudio.campingapp.data.model.UdidReferenceModel
import com.rcappstudio.campingapp.data.model.UserModel
import com.rcappstudio.campingapp.data.model.VerifiedUserData
import com.rcappstudio.campingapp.databinding.FragmentHomeBinding
import com.rcappstudio.campingapp.ui.UserDetailsActivity
import com.rcappstudio.campingapp.ui.activity.HomeUserDetailsActivity
import com.rcappstudio.campingapp.utils.Constants
import com.rcappstudio.campingapp.utils.LoadingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileDescriptor
import java.io.IOException


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var map: GoogleMap? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private var udidDataList = mutableListOf<String>()

    private val binding get() = _binding!!

    private lateinit var state: String
    private lateinit var district: String

    private lateinit var loadingDialog: LoadingDialog

    private var verifiedUserIdList: MutableList<VerifiedUserData> = mutableListOf()

    private var userList: MutableList<UserModel> = mutableListOf()

    private lateinit var verifiedUserAdapter: VerifiedUsersAdapter

    private var aidsNotReceivedLUserList: MutableList<UserModel> = mutableListOf()

    private lateinit var campId: String




    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent: Uri = result.uriContent!!
            imageProcess(uriContent)
        } else {
            // an error occurred
            val exception = result.error
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity(), "Loading Home page")
        init()
        binding.bottomSheet.mapView.onCreate(savedInstanceState)
        clickListener()
        fetchVerifiedUserId()
    }

    private fun init() {
        val sharedPref = requireContext().getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
        state = sharedPref.getString(Constants.STATE, null)!!
        district = sharedPref.getString(Constants.DISTRICT, null)!!
        binding.location.text = "Location: $district"
        campId = sharedPref.getString(Constants.CAMP_ID, null)!!
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.root)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
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
    private fun fetchLocationDetails() {

        //TODO: fetch data and display it
        val fusedLocationProvider =
            LocationServices.getFusedLocationProviderClient(requireContext())

        fusedLocationProvider.lastLocation.addOnSuccessListener {
            if (it != null) {
                val geoCoder = Geocoder(requireContext())
                val currentLocation = geoCoder.getFromLocation(it.latitude, it.longitude, 1)
                val latLng = com.rcappstudio.campingapp.data.model.LatLng(
                    (it.latitude).toString(), (it.longitude).toString(),
                    currentLocation.first().locality + ", " + currentLocation.first().adminArea
                )

                FirebaseDatabase.getInstance().getReference("${Constants.CAMPING}/${campId}")
                    .child("location")
                    .setValue(latLng)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Snackbar.make(
                                binding.root,
                                "Camp location has been updated successfully",
                                Snackbar.LENGTH_LONG
                            ).show()
                            fetchCampingLocations()
                        } else {
                            Snackbar.make(
                                binding.root,
                                "Please enable GPS to update camp location",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }.addOnFailureListener {
                        Snackbar.make(
                            binding.root,
                            "Camp location has been updated successfully",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                FirebaseDatabase.getInstance()
                    .getReference("${Constants.CAMPING_LOCATIONS}/$state/$district/$campId")
                    .setValue(latLng)

            }
        }
    }

    private fun setUpMap() {
        binding.bottomSheet.mapView.getMapAsync {
            map = it
            fetchCampingLocations()
        }
    }

    private fun fetchCampingLocations() {
        FirebaseDatabase.getInstance().getReference(Constants.CAMPING)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (loc in snapshot.children) {
                        val camp = loc.getValue(CampingModel::class.java)
                        if (camp!!.location != null) {
                            val location = camp.location
                            val latLng =
                                LatLng(location?.lat!!.toDouble(), location.lng!!.toDouble())
                            map!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                            map!!.addMarker(
                                MarkerOptions().position(latLng)
                                    .icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_RED
                                        )
                                    )
                            )
                        }
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        loadingDialog.isDismiss()
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
                } else {
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
                            generateNotVerifiedUserList()
                            calculateStatisticalData()
                        }

                        loadingDialog.isDismiss()
                    } else {
                        //TODO: no data
                        Toast.makeText(requireContext(), "No data!!", Toast.LENGTH_LONG).show()
                        loadingDialog.isDismiss()
                    }
                }
        }
    }

    private fun calculateStatisticalData() {
//        var received = 0
//        var notReceived = 0
//        for (c in userList) {
//            Log.d("userData", "calculateStatisticalData: ${c.udidNo}")
//            var posFlag = 0
//            var negFlag = 0
//            val reqSize= c.requestStatus!!.values.toMutableList().size
//            for(requestStatus in c.requestStatus!!.values.toMutableList()){
//                if(requestStatus.verified){
//                    for(ngoData in requestStatus.ngoList!!.values.toMutableList()){
//                        if(ngoData.ngoId == campId && ngoData.aidsReceived!! ) {
//                            posFlag++
//                        } else {
//                            negFlag++
//                        }
//
//                    }
//                }
//            }
//            if(posFlag == reqSize ){
//                received++
//            } else {
//                notReceived++
//            }
//
//        }
//
//        binding.received.text = "No of applicants received aids: $received"
//        binding.notReceived.text = "No of applicants not received aids: $notReceived"
    }

    private fun generateNotVerifiedUserList() {

        aidsNotReceivedLUserList = mutableListOf()
        for (user in userList) {
            var flagNotReceived = 0

            for (singleRequest in user.requestStatus!!.values.toMutableList()) {
                if (singleRequest.verified) {
                    for (l in singleRequest.ngoList!!.values.toMutableList()) {
                        if (!l.aidsReceived!! && l.ngoId == campId) {
                            flagNotReceived++
                        }
                    }
                }
            }
            if (flagNotReceived > 0)
                aidsNotReceivedLUserList.add(user)
        }

        if (aidsNotReceivedLUserList.isNotEmpty()) {
            Log.d("generateData", "generateNotVerifiedUserList: ${aidsNotReceivedLUserList.size}")
//                for(a in aidsNotReceivedLUserList)
        } else {

        }
        binding.notifyAllUsers.setOnClickListener {
            if (aidsNotReceivedLUserList.isNotEmpty()) {
                for (c in aidsNotReceivedLUserList) {
                    createNotification(c.fcmToken.toString(), c.district!!)
                }
                Snackbar.make(binding.root, "Notification sent successfully", Snackbar.LENGTH_LONG)
                    .show()
            } else {
                Snackbar.make(binding.root, "No users left to get aids!!", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun clickListener() {

        binding.bottomSheet.updateLocation.setOnClickListener {
            Log.d("ClickTag", "clickListener: clicked")
            showAlertDialog()
        }

        binding.scanUdidCard.setOnClickListener {
            permissionChecker()
        }
        binding.bottomSheet.llRoot.setOnClickListener {

            val state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                binding.rvVerifiedUsers.visibility = View.VISIBLE
                binding.scanUdidCard.visibility = View.VISIBLE
                BottomSheetBehavior.STATE_COLLAPSED
            } else {
                setUpMap()
                binding.rvVerifiedUsers.visibility = View.INVISIBLE
                binding.scanUdidCard.visibility = View.INVISIBLE
                BottomSheetBehavior.STATE_EXPANDED
            }
            bottomSheetBehavior.state = state
        }
    }

    private fun permissionChecker() {
        Dexter.withContext(requireContext())
            .withPermission(android.Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    cropImage.launch(options { setGuidelines(CropImageView.Guidelines.ON)
                        setCropShape(CropImageView.CropShape.RECTANGLE_HORIZONTAL_ONLY)
                        setAspectRatio(4,3)
                        setAutoZoomEnabled(true)
                        setFixAspectRatio(true)
                        setFixAspectRatio(true)
                        setScaleType(CropImageView.ScaleType.CENTER_CROP)
                         })
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(
                        requireContext(), "You have denied!! camera permissions",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
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

    private fun showAlertDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure to update camp location?")

        builder.setPositiveButton("Yes") { dialog, which ->
            mapPermissionChecker()
        }

        builder.setNegativeButton("No") { dialog, which ->
            Toast.makeText(
                requireContext(),
                "Noooo", Toast.LENGTH_SHORT
            ).show()
        }

        builder.show()
    }

    private fun imageProcess(uri: Uri?) {
        udidDataList.clear()
        var bitmap: Bitmap? = null
        try {

            val parcelFileDescriptor =
                requireContext().contentResolver.openFileDescriptor(uri!!, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.d("bitmap", "imageProcess: $bitmap")
        val textRecognizer = TextRecognizer.Builder(requireContext()).build()

        if (!textRecognizer.isOperational) {
            Toast.makeText(requireContext(), "Some error occured", Toast.LENGTH_LONG).show()
        } else {
            val frame = Frame.Builder().setBitmap(bitmap).build()

            val textBlockSparseArray = textRecognizer.detect(frame)
            var s = "s"
            for (i in 0 until textBlockSparseArray.size()) {
                val textBlock = textBlockSparseArray.valueAt(i)
//                Log.d("imageProcessing", "imageProcess: ${textBlock.value}")

                s = s + " " + textBlock.value.trim().replace("\n", " ")
            }
            Log.d("imageProcessing", "imageProcess: ${s}")
            udidDataList.addAll(s.lowercase().trim().split(" "))

            Log.d("imageProcessing", "imageProcess: $udidDataList")
            for (str in udidDataList) {
                val charArray = str.contains("%")
                val isUdidNo = isLetters(str.chunked(2)[0])
                if (charArray) {
                    val disabilityPercentage = str.chunked(3)[0]
                    Log.d("percent", "imageProcess: $disabilityPercentage")
                }

                if (str.length >= 17 && isUdidNo) {
                    Log.d("dataProcessing", "imageProcess: ${str.uppercase().trim()}")
                    fetchDataFromDatabase(str.uppercase().trim())
                }
            }
        }
    }

    private fun fetchDataFromDatabase(udidNumber: String) {
        //TODO: Making it and input id
        loadingDialog.startLoading()
        FirebaseDatabase.getInstance().getReference("udidNoList/${udidNumber}")
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {
                    val user = snapshot.getValue(UdidReferenceModel::class.java)
                    checkIsVerified(user)
                } else {

                    //TODO : Show error message that user does not exist
                    Toast.makeText(requireContext(), "User does not exist", Toast.LENGTH_LONG)
                        .show()
                    loadingDialog.isDismiss()
                }
            }
    }

    private fun checkIsVerified(user: UdidReferenceModel?) {
        FirebaseDatabase.getInstance().getReference("verificationList/${user!!.userId}")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    getSpecificUserDetails(user)
                } else {
                    //TODO: User is not verified and update the UI accordingly
                    Toast.makeText(
                        requireContext(),
                        "User was not verified by the admin",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("userData", "no data exist ")
                    loadingDialog.isDismiss()

                }

            }
    }

    private fun getSpecificUserDetails(user: UdidReferenceModel?) {
        FirebaseDatabase.getInstance()
            .getReference("${Constants.USERS}/${user!!.state}/${user.district}/${user.userId}")
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val c = snapshot.getValue(UserModel::class.java)
                    loadingDialog.isDismiss()
                    val str = Gson().toJson(c)
                    Log.d("TAG", "initDetailsView: ${str}")
                    val intent = Intent(requireContext(), UserDetailsActivity::class.java)
                    intent.putExtra("userObject", str)
                    startActivity(intent)
                } else {
                    //TODO: show error dialog
                    Toast.makeText(
                        requireContext(),
                        "User was not verified by the admin",
                        Toast.LENGTH_LONG
                    ).show()
                    loadingDialog.isDismiss()
                }

            }
    }

    private fun isLetters(string: String): Boolean {
        return string.none { it !in 'A'..'Z' && it !in 'a'..'z' }
    }


}