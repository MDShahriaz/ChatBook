package com.example.chatmessanger

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatmessanger.Base.ProgressBarFragment
import com.example.chatmessanger.databinding.ActivityConnectBinding
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

class ConnectActivity : AppCompatActivity() {
    val msgList = mutableListOf<Data>()
    private val STRATEGY = Strategy.P2P_STAR
    private lateinit var  connectionsClient: ConnectionsClient
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1

    //opponent Info
    private var opponentName:String? = null
    private var opponentEndpointId:String? = null
    private var opponentMessage:String? = null

    //my Info
    private var myName:String? = null
    private var myMessage:String?=null
    val messageAdapter = MessageAdapter(msgList)

    private lateinit var binding : ActivityConnectBinding
    val progressBarFragment = ProgressBarFragment()

    private val connectionLifecycleCallback = object :ConnectionLifecycleCallback(){
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId,payloadCallback)
            opponentName = info.endpointName
        }

        @SuppressLint("SetTextI18n")
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if(result.status.isSuccess){
                connectionsClient.stopAdvertising()
                connectionsClient. stopDiscovery()
                opponentEndpointId = endpointId
                Toast.makeText(applicationContext,"Connected",Toast.LENGTH_SHORT)
                // here we dismiss progress bar
                progressBarFragment.dismiss()
                binding.group.visibility = View.VISIBLE
            }
        }

        override fun onDisconnected(endpointId: String) {
            resetInfo()
        }
    }


    // this object is use to discover and detect device.
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback(){
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection(myName.toString(),endpointId,connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpiontId: String) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressBarFragment.show(supportFragmentManager,"customDialog")
        connectionsClient = Nearby.getConnectionsClient(this)
        myName = intent.getStringExtra("NAME")
        binding.group.visibility = View.INVISIBLE
        Log.d("Shuvo","$myName in connect activity class")
        startAdvertising()
        startDiscovery()
        binding.sendButton.setOnClickListener{
            sendData(myName.toString(),binding.sendMsgText.text.toString())
            closeKeyboard(binding.sendMsgText)
            binding.sendMsgText.text?.clear()
        }
        // bind with recycler view
        binding.chatList.adapter = messageAdapter
        binding.chatList.layoutManager = LinearLayoutManager(this)
        binding.chatList.setHasFixedSize(true)
    }

    private fun closeKeyboard(view:View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken,0)
    }

    private fun startAdvertising(){
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(myName.toString(),packageName,connectionLifecycleCallback,options)
    }

    private val payloadCallback: PayloadCallback = object: PayloadCallback(){
        @SuppressLint("NotifyDataSetChanged")
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let{
                opponentMessage = it.decodeToString()
            }
            ///binding opponentName set hobe
            val obj = Data(opponentName.toString(),opponentMessage.toString())
            msgList.add(obj)
            messageAdapter.notifyDataSetChanged()
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {

        }
    }
    /// verify required permission for access::
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_REQUIRED_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val errMsg = "Cannot start without required permissions"
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            grantResults.forEach {
                if (it == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
                    finish()
                    return
                }
            }
            recreate()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sendData(myN:String, message:String){
        val obj = Data(myN,message)
        connectionsClient.sendPayload(
            opponentEndpointId!!,
            Payload.fromBytes(message.toByteArray())
        )
        val m = binding.sendMsgText.text.toString()
        msgList.add(Data(myN,m))
        messageAdapter.notifyDataSetChanged()
    }

    private fun resetInfo() {
        opponentEndpointId = null
        opponentName = null
    }

    private fun startDiscovery(){
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(packageName,endpointDiscoveryCallback,options)
    }
}