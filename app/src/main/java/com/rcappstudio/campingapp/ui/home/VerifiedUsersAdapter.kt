package com.rcappstudio.campingapp.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import com.rcappstudio.campingapp.R
import com.rcappstudio.campingapp.data.model.UserModel
import com.rcappstudio.campingapp.databinding.ItemUserBinding
import com.rcappstudio.campingapp.utils.getDateTime
import com.squareup.picasso.Picasso

class VerifiedUsersAdapter(
    private val context : Context,
    private var verifiedUserList: MutableList<UserModel>,
    val onClick : (UserModel, Int)->Unit
) : RecyclerView.Adapter<VerifiedUsersAdapter.ViewHolder>(){

    private lateinit var binding : ItemUserBinding


    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val userName = itemView.findViewById<TextView>(R.id.name)
        val dob = itemView.findViewById<TextView>(R.id.dateOfBirth)
        val udidNo = itemView.findViewById<TextView>(R.id.udidNo)
        val appliedOn = itemView.findViewById<TextView>(R.id.appliedOn)
        val imageView = itemView.findViewById<RoundedImageView>(R.id.profileImage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = verifiedUserList[position]

        binding.name.text = "Name: "+ user.name
        binding.appliedOn.text = "Applied on: "+ getDateTime(user.requestStatus?.appliedOnTimeStamp!!)

        binding.udidNo.text ="Udid no: " + user.udidNo

        if(user.requestStatus.aidsReceived!!){
            binding.dateOfBirth.text = "Status: Aids received"
            binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context,R.color.greenLight))
        } else{
            binding.dateOfBirth.text = "Status: Aids not received"
            binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context,R.color.redLight))
        }


        Picasso
            .get()
            .load(user.profileImageUrl)
            .fit()
            .centerCrop().into(binding.profileImage)

        binding.root.setOnClickListener {
            onClick.invoke(user,position)
        }

    }

    override fun getItemCount(): Int {
        return verifiedUserList.size
    }

    fun updateList(list : UserModel){
        this.verifiedUserList.add(list)
        notifyDataSetChanged()
    }


}