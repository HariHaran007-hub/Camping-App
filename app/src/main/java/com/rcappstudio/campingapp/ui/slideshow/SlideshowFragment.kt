package com.rcappstudio.campingapp.ui.slideshow

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.rcappstudio.adip.notifications.NotificationData
import com.rcappstudio.adip.notifications.PushNotification
import com.rcappstudio.adip.notifications.RetrofitInstance
import com.rcappstudio.campingapp.R
import com.rcappstudio.campingapp.data.model.AidsDataModel
import com.rcappstudio.campingapp.databinding.FragmentSlideshowBinding
import com.rcappstudio.campingapp.utils.Constants
import com.rcappstudio.campingapp.utils.snakeToLowerCamelCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SlideshowFragment : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private var campId: String? = null
    private var sharedPref: SharedPreferences? = null
    private lateinit var orthopedicDisabilityList: MutableList<KeyPairBoolData>
    private lateinit var visualDisabilityList: MutableList<KeyPairBoolData>
    private lateinit var hearingDisabilityList: MutableList<KeyPairBoolData>
    private lateinit var multipleDisabilityList: MutableList<KeyPairBoolData>

    private lateinit var selectedCategory: String
    private lateinit var disabilityAdapter: ArrayAdapter<CharSequence>

    private lateinit var listOfAidsSelectedToUploadToDatabase: MutableList<String>


    private var _binding: FragmentSlideshowBinding? = null

    private var aidsDataList: MutableList<AidsDataModel> = mutableListOf()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchList()
        clickListener()
        sharedPref = requireContext().getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
        campId = sharedPref!!.getString(Constants.CAMP_ID, null)
        databaseReference = FirebaseDatabase.getInstance()
            .getReference("${Constants.CAMPING}/$campId/${Constants.AIDS_DATA}")
        fetchAidsData()
    }

    private fun clickListener(){
        binding.addButton.setOnClickListener {
            if(binding.categorySpinner.visibility == View.VISIBLE && binding.multipleItemSelectionSpinner.visibility == View.VISIBLE ){
                binding.categorySpinner.visibility = View.GONE
                binding.multipleItemSelectionSpinner.visibility = View.GONE
                binding.addAidsToDatabase.visibility = View.GONE
                binding.addButton.text = "Add aids"
            }  else {
                binding.categorySpinner.visibility = View.VISIBLE
                binding.multipleItemSelectionSpinner.visibility = View.VISIBLE
                binding.addAidsToDatabase.visibility = View.VISIBLE
                binding.addButton.text = "Close"
            }
        }

        binding.addAidsToDatabase.setOnClickListener {
            if(listOfAidsSelectedToUploadToDatabase.isNotEmpty()){
                validateAidsData()
            } else {
                Toast.makeText(requireContext(), "Please select at least on aid ", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchList() {

        //TODO: Add network optimization in future
        listOfAidsSelectedToUploadToDatabase = mutableListOf()
        orthopedicDisabilityList = mutableListOf()
        orthopedicDisabilityList.add(KeyPairBoolData("Tricycle", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Wheel_chair(adult_and_child)", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Walking_stick", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Rollator", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Quadripod", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Tetrapod", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Auxiliary_crutches", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Elbow_crutches", false))
        orthopedicDisabilityList.add(KeyPairBoolData("CP_chair", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Corner_chair", false))

        visualDisabilityList = mutableListOf()
        visualDisabilityList.add(
            KeyPairBoolData(
                "Accessible_mobile_phones,_Laptop,_Braille_note_taker_,_Brallier_(school_going_students)",
                false
            )
        )
        visualDisabilityList.add(KeyPairBoolData("Learning_equipment", false))
        visualDisabilityList.add(KeyPairBoolData("Communication_equipment", false))
        visualDisabilityList.add(
            KeyPairBoolData(
                "Braille_attachment_for_telephone_for_deafblind_persons",
                false
            )
        )
        visualDisabilityList.add(KeyPairBoolData("Low_vision_Aids", false))
        visualDisabilityList.add(
            KeyPairBoolData(
                "Special_mobility_aids(for_muscular_dystrophy_and_cerebral_palsy_person)",
                false
            )
        )

        hearingDisabilityList = mutableListOf()
        hearingDisabilityList.add(KeyPairBoolData("Hearing_aids", false))
        hearingDisabilityList.add(KeyPairBoolData("Educational_kits", false))
        hearingDisabilityList.add(KeyPairBoolData("Assistive_and_alarm_devices", false))
        hearingDisabilityList.add(KeyPairBoolData("Cochlear_implant", false))

        multipleDisabilityList = mutableListOf()
        multipleDisabilityList.add(KeyPairBoolData("Teaching_learning_material_kit", false))
        initSpinner()
    }

    private fun initSpinner() {
        disabilityAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.array_disability_category, R.layout.spinner_layout2
        )

        disabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = disabilityAdapter

        binding.categorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {

                    selectedCategory = binding.categorySpinner.selectedItem.toString()

                    val parentId: Int = adapterView!!.id

                    if (parentId == R.id.categorySpinner) {
                        when (selectedCategory) {
                            "Select disability categor" -> {
                                //TODO: hide multiple selection spinner
                            }
                            "Orthopedic disability" -> {
                                setMultipleSelectionList(orthopedicDisabilityList)
                            }
                            "Visual disability" -> {
                                setMultipleSelectionList(visualDisabilityList)
                            }
                            "Hearing disability" -> {
                                setMultipleSelectionList(hearingDisabilityList)
                            }
                            "Mentally and multiple disability" -> {
                                setMultipleSelectionList(multipleDisabilityList)
                            }
                        }
                    }

                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }
    }

    private fun setMultipleSelectionList(list : MutableList<KeyPairBoolData>){
        if(list != null){
            binding.multipleItemSelectionSpinner.setItems(
                list
            ) { items ->
                for (i in 0 until items!!.size) {
                    if (items[i].isSelected) {
                        Log.i(
                            "multiData",
                            i.toString() + " : " + items[i].name + " : " + items[i]
                                .isSelected
                        )
                        listOfAidsSelectedToUploadToDatabase.add(items[i].name.toString().snakeToLowerCamelCase())
                    }
                }
            }
        }
    }

    private fun validateAidsData(){
        var flag = 0
        if(aidsDataList != null){
            for(aid in aidsDataList){
                if(listOfAidsSelectedToUploadToDatabase.contains(aid.aidsName)){
                    flag = 1
                }
            }
        }

        if(flag == 1){
            Snackbar.make(binding.root ,"Aids was already in account", Snackbar.LENGTH_LONG).show()
        } else{
            addAidsToDatabase(listOfAidsSelectedToUploadToDatabase)
        }
    }

    private fun addAidsToDatabase(list : MutableList<String>){

        for(aid in list){
            databaseReference.child(aid).setValue(0).addOnCompleteListener {
                if(it.isSuccessful)
                    Snackbar.make(binding.root ,"Aids uploaded successfully", Snackbar.LENGTH_LONG).show()
            }
        }
    }



    private fun fetchAidsData() {
           databaseReference.get().addOnSuccessListener {
                if (it.exists()) {
                    for (c in it.children) {
                        aidsDataList.add(AidsDataModel(c.key!!, c.value))
                    }
                    populateData()
                }
            }
    }

    private fun populateData() {
        binding.rvAids.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAids.adapter = AidsDataAdapter(aidsDataList, requireContext())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}