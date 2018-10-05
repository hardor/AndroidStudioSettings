package ru.profapp.RanobeReaderTest.Common

class ErrorConnectionException : Exception {

    constructor(message: String) : super(message)

    constructor(message: String, throwable: Throwable) : super(message, throwable)

    constructor() : super()

    constructor(cause: Throwable) : super(cause)
}