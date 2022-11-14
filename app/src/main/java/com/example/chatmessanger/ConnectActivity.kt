package com.example.chatmessanger

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatmessanger.Base.ProgressBarFragment
import com.example.chatmessanger.databinding.ActivityMessageBinding
import com.example.chatmessanger.databinding.SelectDeviceBinding
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*


class ConnectActivity : AppCompatActivity(), DeviceAdapter.Callback {
    private lateinit var binding: ActivityMessageBinding
    private val STRATEGY = Strategy.P2P_CLUSTER
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
    lateinit var messageAdapter: Adapter

    var isImHost = false

    lateinit var connectionsClient: ConnectionsClient

    //opponent Info
    var opponentName: String? = null
    var opponentEndpointId: String? = null
    var opponentMessage: String? = null

    //my Info
    var myName: String? = null
    private var myMessage: String? = null

    val msgList = mutableListOf<Data>()
    var devicesInfo: MutableList<DeviceInformation> = mutableListOf()

    val otherDevices = hashMapOf<String, String>()

    private val deviceListAdapter = DeviceAdapter(this)

    private lateinit var deviceListBinding: SelectDeviceBinding
    val progressBarFragment = ProgressBarFragment()

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            opponentName = info.endpointName
            otherDevices[endpointId] = info.endpointName
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                if(isImHost) {
                    onNameClicked()
                    isImHost = true
                }
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                opponentEndpointId = endpointId
                Toast.makeText(applicationContext, "Connected", Toast.LENGTH_SHORT).show()

                inflateNewLayout()
            }
        }

        override fun onDisconnected(endpointId: String) {
            removeEndpointFromList(endpointId)
            resetInfo()
        }
    }

    private fun inflateNewLayout() {
        setContentView(binding.root)
        setupUI(findViewById(R.id.container))
    }

    val endpointList = mutableListOf<String>()
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            deviceListBinding.tvTitle.text = "Nearby Devices"
            val deviceInfo = DeviceInformation(endpointId, info.endpointName)
            if (!devicesInfo.contains(deviceInfo)) {
                devicesInfo.add(deviceInfo)
                endpointList.add(endpointId)
                deviceListAdapter.notifyItemInserted(devicesInfo.size - 1)
            }
            deviceListAdapter.submitList(devicesInfo)
        }

        override fun onEndpointLost(endpiontId: String) {
            removeEndpointFromList(endpiontId)
        }
    }

    private fun removeEndpointFromList(endpiontId: String) {
        for (i in 0 until devicesInfo.size) {
            if (devicesInfo[i].deviceId == endpiontId) {
                devicesInfo.removeAt(i)
                deviceListAdapter.notifyDataSetChanged()
            }
        }
    }

    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {
                opponentMessage = it.decodeToString()
            }
            val obj =
                Data(1, otherDevices[endpointId]?:"Default_Name", opponentMessage.toString())
            msgList.add(obj)
            messageAdapter.notifyDataSetChanged()
            binding.chatList.scrollToPosition(msgList.size - 1)
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {

        }
    }

    override fun onNameClicked() {
        isImHost = true
        for (i in 0 until devicesInfo.size) {
            connectionsClient.requestConnection(
                myName.toString(), devicesInfo[i].deviceId, connectionLifecycleCallback
            )

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceListBinding = SelectDeviceBinding.inflate(layoutInflater)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(deviceListBinding.root)
        connectionsClient = Nearby.getConnectionsClient(this)
        myName = intent.getStringExtra("NAME")

        initConnection()

        deviceListBinding.rvDevices.adapter = deviceListAdapter
        deviceListBinding.rvDevices.layoutManager = LinearLayoutManager(this)


        messageAdapter = Adapter(msgList)

        binding.sendBtn.setOnClickListener {
            sendMessage()
        }
        binding.sendMsgText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
            }
            false

        })
        binding.chatList.adapter = messageAdapter
        binding.chatList.layoutManager = LinearLayoutManager(this)
        binding.chatList.setHasFixedSize(true)
    }

    private fun sendMessage() {
        sendData(myName.toString(), binding.sendMsgText.text.toString())
//                closeKeyboard(binding.sendMsgText)
        binding.sendMsgText.text?.clear()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sendData(myN: String, message: String) {
        if (opponentEndpointId == null) {
            initConnection()
        } else {
            connectionsClient.sendPayload(
                endpointList,
                Payload.fromBytes(message.toByteArray())
            )
            val m = binding.sendMsgText.text.toString()
            msgList.add(Data(0, myN, m))
            messageAdapter.notifyDataSetChanged()
            binding.chatList.scrollToPosition(msgList.size - 1)
        }

    }

    private fun initConnection() {
        startAdvertising()
        startDiscovery()
    }

    override fun onStop() {
        connectionsClient.apply {
            stopAdvertising()
            stopDiscovery()
            stopAllEndpoints()
        }
        resetInfo()
        super.onStop()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDestroy() {
        opponentEndpointId?.let {
            connectionsClient.disconnectFromEndpoint(it)
            resetInfo()
        }
        super.onDestroy()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(
                    Manifest.permission.FOREGROUND_SERVICE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.FOREGROUND_SERVICE
                    ), REQUEST_CODE_REQUIRED_PERMISSIONS
                )
            }
        } else {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_REQUIRED_PERMISSIONS
                )
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
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

    private fun resetInfo() {
        opponentEndpointId = null
        opponentName = null
    }

    private fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            myName.toString(), packageName, connectionLifecycleCallback, options
        )
    }

    private fun startDiscovery() {
        deviceListBinding.tvTitle.text = "No device found"
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(packageName, endpointDiscoveryCallback, options)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupUI(view: View) {

        if (view !is EditText && view !is ImageButton) {
            view.setOnTouchListener { v, event ->
                hideSoftKeyboard(this)
                false
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView)
            }
        }
    }

    private fun Activity.hideSoftKeyboard(context: Context) {
        currentFocus?.let {
            val inputMethodManager =
                ContextCompat.getSystemService(context, InputMethodManager::class.java)!!
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}