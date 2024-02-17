package at.co.schwaerzler.maximilian.androidmailer

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.nio.file.Path
import java.util.Date
import java.util.UUID
import kotlin.io.path.name

@Serializable
class AndroidMailer private constructor(
    val from: String,
    val to: String,
    val subject: String,
    val body: String,
    val smtpServer: String,
    val smtpPort: Int,
    val useStartTLS: Boolean,
    val attachments: List<AndroidMailerAttachment>
) {

    @OptIn(ExperimentalSerializationApi::class)
    fun send(context: Context, username: String, password: String) {
        val timestamp = Date().time
        val fileName = "mail_$timestamp"
        val mailFile = File(context.cacheDir, fileName)
        Json.encodeToStream(this, mailFile.outputStream())

        val workerData = Data.Builder()
        workerData.putString("mailData", mailFile.toString())
        workerData.putString("username", username)
        workerData.putString("password", password)

        val emailWorkerBuilder = OneTimeWorkRequestBuilder<EmailSender>()
        emailWorkerBuilder.setInputData(workerData.build())
        emailWorkerBuilder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        val emailWorker = emailWorkerBuilder.build()

        val emailWorkerId = emailWorker.id
        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(emailWorker)
        Log.d(LOG_TAG, "Enqueued worker with id $emailWorkerId")

//        workManager.getWorkInfoById(emailWorkerId)
    }

    companion object {
        private const val LOG_TAG = "AndroidMailer"
    }

    /**
     * The Builder for [AndroidMailer].
     * Use [build] to retrieve an instance of [AndroidMailer]
     */
    class Builder {
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

        val attachments: MutableList<AndroidMailerAttachment> = mutableListOf()

        fun attachment(filePath: Path) = apply {
            customAttachment(AndroidMailerAttachment(filePath, filePath.name))
        }

        fun customAttachment(attachment: AndroidMailerAttachment) = apply {
            if (!attachment.filePath.toFile().isFile) {
                throw IllegalArgumentException("Attachment has to be a file, not a directory")
            }
            this.attachments.add(attachment)
        }

        fun attachments(filePaths: List<Path>) = apply {
            customAttachments(filePaths.map { AndroidMailerAttachment(it, it.name) })
        }

        fun customAttachments(attachments: List<AndroidMailerAttachment>) = apply {
            if (attachments.any { !it.filePath.toFile().isFile }) {
                throw IllegalArgumentException("Attachment have to be files, not directories")
            }
            this.attachments.addAll(attachments)
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

            return AndroidMailer(
                from!!,
                to!!,
                subject,
                body,
                smtpServer!!,
                smtpPort!!,
                useStartTLS,
                attachments
            )
        }
    }
}