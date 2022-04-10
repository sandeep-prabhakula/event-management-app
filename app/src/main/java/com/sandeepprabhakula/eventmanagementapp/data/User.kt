package com.sandeepprabhakula.eventmanagementapp.data

data class User(
    var uid: String = "",
    var userName: String = "",
    var userEmail: String = "",
    var userImageURL: String = "",
    var userRollNumber: String = "",
    var userBranch: String = "",
    var userYear: String = "",
    var userMobileNumber: String = "",
    var profileIsComplete: Boolean = false,
    var userCollegeName :String = ""
)