package com.example.rate.model

class Chat {
    private var sender = ""
    private var message = ""
    private var receiver = ""
    private var isSeen = false
    private var url = ""
    private var messageId = ""

    constructor()

    constructor(
        sender: String,
        message: String,
        receiver: String,
        isSeen: Boolean,
        url: String,
        messageId: String
    ) {
        this.sender = sender
        this.message = message
        this.receiver = receiver
        this.isSeen = isSeen
        this.url = url
        this.messageId = messageId
    }

    fun getSender(): String?{
        return sender
    }

    fun setSender(sender: String?){
        this.sender = sender!!
    }

    fun getMessage(): String?{
        return message
    }

    fun setSMessage(message: String?){
        this.message = message!!
    }

    fun getReceiver(): String?{
        return receiver
    }

    fun setReceiver(receiver: String?){
        this.receiver = receiver!!
    }

    fun isIsSeen(): Boolean{
        return isSeen
    }

    fun setIsSeen(isSeen: Boolean?){
        this.isSeen = isSeen!!
    }

    fun getUrl(): String?{
        return url
    }

    fun setUrl(url: String?){
        this.url = url!!
    }

    fun getMessageId(): String?{
        return messageId
    }

    fun setMessageId(messageId: String?){
        this.messageId = messageId!!
    }
}