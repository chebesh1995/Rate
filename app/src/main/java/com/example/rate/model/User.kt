package com.example.rate.model

data class User(
    var uid: String = "",
    var username: String = "",
    var profile: String = "",
    var cover: String = "",
    var status: String = "",
    var search: String = "",
    var facebook: String = "",
    var instagram: String = "",
    var website: String = "",
    var rate: Double = 0.1,
    var rateCount: Int = 1000
){
    fun addRating(newRate: Double): User {
        val averageRating = (rateCount * rate + newRate) / (rateCount + 1)
        rate = averageRating
        rateCount += 1
        return this
    }
}
