package com.example.chatmessanger

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatmessanger.databinding.DevicesListBinding

class DeviceAdapter(private val mCallback: Callback? = null) : ListAdapter<DeviceInformation,
        DeviceAdapter.DeviceListViewHolder>(DiffCallback) {
    class DeviceListViewHolder(var binding: DevicesListBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(deviceId: DeviceInformation) {
            binding.tvDeviceName.text = deviceId.deviceName
            binding.tvDeviceId.text = deviceId.deviceId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceListViewHolder {
        return DeviceListViewHolder(DevicesListBinding.inflate(
            LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: DeviceListViewHolder, position: Int) {
        val deviceInfo = getItem(position)
        holder.bind(deviceInfo)
        holder.binding.layoutDeviceInfo.setOnClickListener {
            mCallback?.onNameClicked()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DeviceInformation>() {
        override fun areItemsTheSame(oldItem: DeviceInformation, newItem: DeviceInformation): Boolean {
            return oldItem.deviceId == newItem.deviceId
        }

        override fun areContentsTheSame(oldItem: DeviceInformation, newItem: DeviceInformation): Boolean {
            return oldItem.deviceName == newItem.deviceName
        }
    }
    interface Callback {
        fun onNameClicked()
    }

}