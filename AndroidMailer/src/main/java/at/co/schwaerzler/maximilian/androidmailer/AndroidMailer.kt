package at.co.schwaerzler.maximilian.androidmailer

import android.util.Log
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

class AndroidMailer private constructor(
    val from: String,
    val to: String,
    val subject: String,
    val body: String,
    val smtpServer: String,
    val smtpPort: Int,
    val useStartTLS: Boolean
) {

    fun send(username: String, password: String) {
        MAIL_PROPERTIES.setProperty("mail.smtp.host", smtpServer)
        MAIL_PROPERTIES.setProperty("mail.smtp.port", smtpPort.toString())
        MAIL_PROPERTIES.setProperty("mail.smtp.starttls.enable", useStartTLS.toString())

        val message = MimeMessage(Session.getDefaultInstance(MAIL_PROPERTIES))
        message.setFrom(from)
        message.addRecipient(Message.RecipientType.TO, InternetAddress(to))
        message.subject = subject
        message.setText(body)
        Transport.send(message, username, password)
        Log.d(LOG_TAG, "Email sent from $from to $to")
    }

    companion object {
        private val MAIL_PROPERTIES = Properties()

        private const val LOG_TAG = "AndroidMailer"
    }

    /**
     * The Builder for [AndroidMailer].
     * Use [build] to retrieve an instance of [AndroidMailer]
     */
    class Builder {
        /**
         * The email address from which the email is sent from.
         * Either a plain email address (e.g. `john.doe@example.com`) or with a display name (e.g. `John Doe <john.doe@example.com>`).
         */
        var from: String? = null
            private set

        fun from(from: String) = apply {
            this.from = from
        }

        var to: String? = null
            private set

        fun to(to: String) = apply {
            this.to = to
        }

        var subject: String = ""
            private set

        fun subject(subject: String) = apply {
            this.subject = subject
        }

        var body: String = ""
            private set

        fun body(body: String) = apply {
            this.body = body
        }

        var smtpServer: String? = null
            private set

        fun smtpServer(server: String) = apply {
            this.smtpServer = server
        }

        var smtpPort: Int? = null
            private set

        fun smtpPort(port: Int) = apply {
            this.smtpPort = port
        }

        var useStartTLS: Boolean = false
            private set

        fun useStartTLS(value: Boolean) = apply {
            this.useStartTLS = value
        }

        fun build(): AndroidMailer {
            requireNotNull(from) { "from is undefined" }
            requireNotNull(to) { "to is undefined" }
            requireNotNull(smtpServer) { "SMTP server is undefined" }
            requireNotNull(smtpPort) { "SMTP port is undefined" }

            return AndroidMailer(from!!, to!!, subject, body, smtpServer!!, smtpPort!!, useStartTLS)
        }
    }
}