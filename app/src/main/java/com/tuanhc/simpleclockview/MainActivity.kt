package com.tuanhc.simpleclockview

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tuanhc.simpleclockview.databinding.ActivityMainBinding
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  @SuppressLint("SimpleDateFormat")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.clockView.setTimeUpdateListener { calendar ->
      binding.tvTime.text = SimpleDateFormat("HH:mm:ss a").format(calendar.time)
    }
  }
}