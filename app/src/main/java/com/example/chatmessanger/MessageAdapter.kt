package com.example.chatmessanger

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatmessanger.databinding.MsgViewBinding

class MessageAdapter(val message:List<Data>): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    class MessageViewHolder(val binding: MsgViewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(msg:Data) = binding.apply {
            name.text = msg.name
            msgDescription.text = msg.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = MsgViewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(message[position])
    }

    override fun getItemCount(): Int {
        return message.size
    }
}