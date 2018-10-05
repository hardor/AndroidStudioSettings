package ru.profapp.RanobeReaderTest.JsonApi.Rulate

data class LoginGson(
        var status: String,
        var msg: String,
        var response: Login
)

data class Login(
        var id: Int,
        var token: String,
        var login: String,
        var avatar: String,
        var balance: Int
)
