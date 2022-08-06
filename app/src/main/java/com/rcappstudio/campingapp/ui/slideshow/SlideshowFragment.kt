package com.rcappstudio.campingapp.ui.slideshow

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.rcappstudio.adip.notifications.NotificationData
import com.rcappstudio.adip.notifications.PushNotification
import com.rcappstudio.adip.notifications.RetrofitInstance
import com.rcappstudio.campingapp.databinding.FragmentSlideshowBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SlideshowFragment : Fragment() {

private var _binding: FragmentSlideshowBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

    _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
    val root: View = binding.root

    val textView: TextView = binding.textSlideshow
    slideshowViewModel.text.observe(viewLifecycleOwner) {
      textView.text = it
    }
    return root
  }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createNotification()
    }


    private fun createNotification(){
        PushNotification(
            NotificationData("New message", "Aids recived"),
"dTZ2O4X3TNakCSF2uKzuA5:APA91bGKL75TLl0VyGbM_7B2ZR-b872hBVB8d5bdeLsqDMXSsaDfSwHWldzjejEKsetAhWejA9JuDjPSIGmCVQMcJnlTS7Wm7fYRLzytsdkVuu292ZEElEO0W60ruqk_C7xVYn0zyGNc"
        ).also {
            sendNotification(it)
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d("notificationSent", "sendNotification: success!!")
            } else {
                Log.d("notificationSent", "sendNotification: failure!!")
            }
        } catch(e: Exception) {
            Log.e("TAG", e.toString())
        }
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}