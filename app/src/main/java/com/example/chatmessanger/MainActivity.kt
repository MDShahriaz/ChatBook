package com.example.chatmessanger

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
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

        val name = dialogBinding.myName.text.toString()
        dialogBinding.myName.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dialogBinding.btn.isEnabled = true
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        dialogBinding.btn.setOnClickListener {
            myDialog.dismiss()
            val name = dialogBinding.myName.text.toString()
            val intent = Intent(this,ConnectActivity::class.java).also {
                it.putExtra("NAME",name)
            }
            startActivity(intent)
            closeKeyboard(dialogBinding.myName)
            finish()
        }
    }

    private fun closeKeyboard(view: View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken,0)
    }
}