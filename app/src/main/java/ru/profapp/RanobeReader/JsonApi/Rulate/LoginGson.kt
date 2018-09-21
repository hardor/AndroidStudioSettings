package ru.profapp.RanobeReader.JsonApi.Rulate

data class LoginGson(
        var status: String? = null,
        var msg: String? = null,
        var response: Login? = null
)

data class Login(
        var id: Int? = null,
        var token: String? = null,
        var login: String? = null,
        var avatar: String? = null,
        var balance: Int? = null
)
