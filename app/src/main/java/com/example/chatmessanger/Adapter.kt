package com.example.chatmessanger

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatmessanger.databinding.MsgRecieveViewBinding
import com.example.chatmessanger.databinding.MsgSendViewBinding
import java.lang.IllegalArgumentException

const val SEND = 0
const val RECEIVE = 1
class Adapter(private val message:List<Data>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class SendViewHolder(private val msgSendBinding: MsgSendViewBinding):RecyclerView.ViewHolder(msgSendBinding.root){
        fun bind(item:Data) = msgSendBinding.apply {
            name.text = item.name
            msgDescription.text = item.message
        }
    }

    class ReceiverViewHolder(private val msgReceiveBinding: MsgRecieveViewBinding):RecyclerView.ViewHolder(msgReceiveBinding.root){
        fun bind(item: Data) = msgReceiveBinding.apply {
            name.text = item.name
            msgDescription.text = item.message
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            SEND -> SendViewHolder(MsgSendViewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            RECEIVE -> ReceiverViewHolder(MsgRecieveViewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            else -> throw IllegalArgumentException("Invalid ViewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is SendViewHolder -> holder.bind(message[position])
            is ReceiverViewHolder -> holder.bind(message[position])
        }
    }

    override fun getItemCount(): Int {
        return message.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(message[position].id){
            0 ->  SEND
            1 -> RECEIVE
            else -> throw IllegalArgumentException("Invalid Item")
        }
    }
}