package com.example.chatmessanger

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
    lateinit var connectionsClient: ConnectionsClient
    lateinit var messageAdapter: Adapter

    private val STRATEGY = Strategy.P2P_CLUSTER
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1

    var isImHost = false
    private val divider = "[#?@%]"

    //opponent Info
    var opponentMessage: String = divider

    //my Info
    var myName: String? = null

    val msgList = mutableListOf<Data>()

    var devicesInfo: MutableList<DeviceInformation> = mutableListOf()
    var deviceName = hashMapOf<String, String>()
    val endpointList = mutableListOf<String>()

    private val deviceListAdapter = DeviceAdapter(this)

    private lateinit var deviceListBinding: SelectDeviceBinding
    val progressBarFragment = ProgressBarFragment()

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            endpointList.add(endpointId)
            deviceName[endpointId] = info.endpointName
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                Toast.makeText(applicationContext, "Connected", Toast.LENGTH_SHORT).show()

                inflateNewLayout()
            }
        }

        override fun onDisconnected(endpointId: String) {
            Toast.makeText(
                applicationContext,
                "${deviceName[endpointId]} Disconnected",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun inflateNewLayout() {
        setContentView(binding.root)
        setupUI(findViewById(R.id.container))
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            deviceListBinding.tvTitle.text = "Nearby Devices"
            val deviceInfo = DeviceInformation(endpointId, info.endpointName)
            removeEndpointFromList(info.endpointName)
            devicesInfo.add(deviceInfo)
            deviceListAdapter.notifyItemInserted(devicesInfo.size - 1)
        }

        override fun onEndpointLost(endpiontId: String) {
        }
    }

    private fun removeEndpointFromList(name: String) {
        if (devicesInfo.isEmpty()) return
        var iterator = 0

        while (iterator < devicesInfo.size) {
            if (devicesInfo[iterator].deviceName == name) {
                devicesInfo.removeAt(iterator)
                deviceListAdapter.notifyItemRemoved(iterator)
                iterator--
            }
            iterator++
        }

    }

    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {

                opponentMessage = it.decodeToString().toString()
            }
            val (senderName, message) = extractSenderNameAndMessage(opponentMessage!!)

            if (isImHost) {
                sendData(senderName, message)
            }

            if (senderName != myName) {
                val obj = Data(1, senderName, message)


                msgList.add(obj)
                messageAdapter.notifyDataSetChanged()
                binding.chatList.scrollToPosition(msgList.size - 1)
            }
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {

        }
    }

    private fun extractSenderNameAndMessage(opponentMessage: String): Pair<String, String> {
        var right = opponentMessage.length - 1
        var left = right - 5

        var actualMessage: String = ""
        var senderName: String = ""
        while (left > 0) {
            if (opponentMessage.subSequence(left, right + 1) == divider) {
                actualMessage = opponentMessage.subSequence(0, left).toString()
                senderName =
                    opponentMessage.subSequence(right + 1, opponentMessage.length).toString()
                break
            }
            right--
            left--
        }
        return Pair(senderName, actualMessage)
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
    private fun sendData(messageSenderName: String, message: String) {
        val messageWithName = addNameOnMessage(messageSenderName, message)
        connectionsClient.sendPayload(
            endpointList, Payload.fromBytes(messageWithName.toByteArray())
        )
        val m = binding.sendMsgText.text.toString()
        if (myName == messageSenderName) {
            msgList.add(Data(0, messageSenderName, m))
            messageAdapter.notifyDataSetChanged()
            binding.chatList.scrollToPosition(msgList.size - 1)
        }

    }

    private fun addNameOnMessage(name: String, message: String): String {
        return "$message$divider$name"
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
        deviceName.clear()
        for ((key, _) in devicesInfo) {
            connectionsClient.disconnectFromEndpoint(key)
            resetInfo()
        }
        super.onDestroy()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
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
        devicesInfo.clear()
        endpointList.clear()
    }

    private fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            myName.toString(), packageName, connectionLifecycleCallback, options
        )
    }

    private fun startDiscovery() {
        deviceListAdapter.submitList(devicesInfo)
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