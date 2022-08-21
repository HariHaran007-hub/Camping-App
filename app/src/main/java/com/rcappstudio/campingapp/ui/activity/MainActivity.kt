package com.rcappstudio.campingapp.ui.activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.FirebaseDatabase
import com.rcappstudio.campingapp.R
import com.rcappstudio.campingapp.data.model.CampingModel
import com.rcappstudio.campingapp.databinding.ActivityMainBinding
import com.rcappstudio.campingapp.utils.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     binding = ActivityMainBinding.inflate(layoutInflater)
     setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val header = navView.getHeaderView(0)
        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREF , MODE_PRIVATE)
        header.findViewById<TextView>(R.id.campName).text = sharedPreferences.getString(Constants.CAMPING_NAME, null)
        header.findViewById<TextView>(R.id.campGmail).text = sharedPreferences.getString(Constants.CAMP_EMAIL , null)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home, R.id.nav_search_udid, R.id.nav_slideshow
        ), drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

//        FirebaseDatabase.getInstance().getReference()
        //executeData()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
//
//    private fun executeData(){
//        val nearCampList = mutableListOf<CampingModel>()
//        FirebaseDatabase.getInstance().getReference(Constants.CAMPING).get()
//            .addOnSuccessListener { snapshot->
//                if(snapshot.exists()){
//                    for(campRawObj in snapshot.children){
//                        val camp = campRawObj.getValue(CampingModel::class.java)
//                        if(camp!!.state == "Tamil Nadu" && camp.district != "Dindigul"){
//                            nearCampList.add(camp)//This snippet will add all camps of Tamil Nadu
//                        }
//                    }
//                    manipulateData(nearCampList)
//                } else {
//                    Toast.makeText(applicationContext , "No data exist" , Toast.LENGTH_LONG).show()
//                }
//
//            }
//    }
//
//    private fun manipulateData(nearCampList : MutableList<CampingModel>){
//
//        //Below lat lng data is assumed to be our camp
//        val lat = 10.3712745
//        val long = 77.9828858
//
//        //This will store camp-id and the distance from current lat , lng
//        val hashMap = HashMap<String?, Double>()
//
//        for(camp in nearCampList){
//            hashMap.put(camp.campId ,
//                distance(lat1 = lat ,
//                    lon1 = long ,
//                    lat2 = camp.location!!.lat!!.toDouble() ,
//                    lon2 = camp.location.lng!!.toDouble()) )
//        }
//
//        hashMap.toList().sortedBy { (_, value) -> value }.toMap()//Sorting the map
//        campAllotment(hashMap)
//
//
//    }
//
//    private fun campAllotment(hashMap: HashMap<String? , Double>){
//        val userAidsList = mutableListOf<String>()
//        userAidsList.add("AssistiveAndAlarmDevices")
//        userAidsList.add("CommunicationEquipment")
//
//        for(campId in hashMap){
//            FirebaseDatabase.getInstance().getReference("${Constants.CAMPING}/$campId/aidsData").get()
//                .addOnSuccessListener { snapshot->
//                    userAidsList.con
//                }
//        }
//    }
//
//    fun nthNearestCamp(hashMap: HashMap<String?, Double>, K: Int): String {
//        return hashMap.toList()[K-1].first.toString()
//    }
//
//
//    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
//        // haversine great circle distance approximation, returns meters
//        val theta = lon1 - lon2
//        var dist = (Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
//                + (Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
//                * Math.cos(deg2rad(theta))))
//        dist = Math.acos(dist)
//        dist = rad2deg(dist)
//        dist = dist * 60 // 60 nautical miles per degree of seperation
//        dist = dist * 1852 // 1852 meters per nautical mile
//        return dist//Return in meters
//    }
//
//    private fun deg2rad(deg: Double): Double {
//        return deg * Math.PI / 180.0
//    }
//
//    private fun rad2deg(rad: Double): Double {
//        return rad * 180.0 / Math.PI
//    }
}