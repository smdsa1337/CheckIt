package com.smdsa.checkit.adapters

data class TasksDataClass(
    var createdBy : String? = "",
    var dateOfCreation : String? = "",
    var dateOfUpdater : String? = "",
    var description : String? = "",
    var expirationDate : String? = "",
    var forWho: String? = "",
    var header: String? = "",
    var owner : String? = "",
    var priority: String? = "",
    var responsible : String? = "",
    var status: String? = "",
)
