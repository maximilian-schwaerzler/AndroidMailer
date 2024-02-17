package at.co.schwaerzler.maximilian.androidmailer

import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication

class AndroidMailerAuthenticator(private val username: String, private val password: String) :
    Authenticator() {
    override fun getPasswordAuthentication(): PasswordAuthentication {
        return PasswordAuthentication(username, password)
    }
}