package com.example.chatmessanger

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatmessanger.databinding.MsgRecieveViewBinding
import com.example.chatmessanger.databinding.MsgSendViewBinding
import java.lang.IllegalArgumentException

const val SEND = 0
const val RECIEVE = 1
class Adapter(val message:List<Data>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class SendViewHolder(val msgSendBinding: MsgSendViewBinding):RecyclerView.ViewHolder(msgSendBinding.root){
        fun bind(item:Data) = msgSendBinding.apply {
            name.text = item.name
            msgDescription.text = item.message
        }
    }

    class RecieverViewHolder(val msgRecieveBinding: MsgRecieveViewBinding):RecyclerView.ViewHolder(msgRecieveBinding.root){
        fun bind(item: Data) = msgRecieveBinding.apply {
            name.text = item.name
            msgDescription.text = item.message
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            SEND -> SendViewHolder(MsgSendViewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            RECIEVE -> RecieverViewHolder(MsgRecieveViewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            else -> throw IllegalArgumentException("Invalid ViewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is SendViewHolder -> holder.bind(message[position])
            is RecieverViewHolder -> holder.bind(message[position])
        }
    }

    override fun getItemCount(): Int {
        return message.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(message[position].id){
            0 ->  SEND
            1 -> RECIEVE
            else -> throw IllegalArgumentException("Invalid Item")
        }
    }
}