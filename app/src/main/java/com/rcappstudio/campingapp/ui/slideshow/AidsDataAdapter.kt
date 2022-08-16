package com.rcappstudio.campingapp.ui.slideshow

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.rcappstudio.campingapp.R
import com.rcappstudio.campingapp.data.model.AidsDataModel
import com.rcappstudio.campingapp.utils.Constants

class AidsDataAdapter (
    private var aidsList: MutableList<AidsDataModel>,
    val context : Context) : RecyclerView.Adapter<AidsDataAdapter.ViewHolder>() {


    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val tvAidsName = view.findViewById<TextView>(R.id.aidsName)!!
        val ivAddAids = view.findViewById<ImageView>(R.id.addAids)!!
        val ivSubAids = view.findViewById<ImageView>(R.id.subAids)!!
        val tvAidsCount = view.findViewById<TextView>(R.id.aidsCount)!!
        val updateButton = view.findViewById<Button>(R.id.updateAidsDatabase)!!
    }



    override fun getItemCount(): Int {
        return aidsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val aidsDataModel = aidsList[position]
        holder.tvAidsName.text = aidsDataModel.aidsName!!.toUpperCase()
        holder.tvAidsCount.text = aidsDataModel.aidsCount.toString()

        holder.ivAddAids.setOnClickListener {
            //TODO: Yet to write maximum logic
           holder.tvAidsCount.text = (holder.tvAidsCount.text.toString().toInt() + 1).toString()
        }

        holder.ivSubAids.setOnClickListener {
            if(holder.tvAidsCount.text.toString().toInt() >= 0){
                holder.tvAidsCount.text = (holder.tvAidsCount.text.toString().toInt() + 1).toString()
            }

        }

        holder.updateButton.setOnClickListener {
            //TODO : Firebase logic
            val sharedPref = context.getSharedPreferences(
                Constants.SHARED_PREF,
                Context.MODE_PRIVATE
            )
            val campId  = sharedPref.getString(Constants.CAMP_ID, null)
            FirebaseDatabase.getInstance()
                .getReference("${Constants.CAMPING}/$campId/${Constants.AIDS_DATA}/${aidsDataModel.aidsName}")
                .setValue( holder.tvAidsCount.text.toString().toInt())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_aids,parent, false)
        return ViewHolder(view)
    }

     fun updateList(updatedList : MutableList<AidsDataModel>){
        this.aidsList = updatedList
    }

}