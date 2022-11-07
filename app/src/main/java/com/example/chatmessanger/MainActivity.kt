package com.example.chatmessanger

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatmessanger.databinding.ActivityMainBinding
import com.example.chatmessanger.databinding.CustomDialogBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dialogBinding = CustomDialogBinding.inflate(layoutInflater)

        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding.root)


        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()

        dialogBinding.btn.setOnClickListener {
            myDialog.dismiss()
            val name = dialogBinding.myName.text.toString()
            val intent = Intent(this,ConnectActivity::class.java).also {
                it.putExtra("NAME",name)
            }
            startActivity(intent)
        }
    }
}