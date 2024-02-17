package at.co.schwaerzler.maximilian.androidmailer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.nio.file.Files
import java.util.Properties
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.deleteRecursively
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists

@OptIn(ExperimentalSerializationApi::class)
class EmailSender(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    companion object {
        private const val NOTIFICATION_ID = 100
        private const val CHANNEL_ID = "AndroidMailerWorker"
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            "Android Mailer Email Worker",
            NotificationManager.IMPORTANCE_NONE
        )
        val notificationManager =
            applicationContext.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)

        return NotificationCompat.Builder(applicationContext, CHANNEL_ID).apply {
            setContentTitle("Sending Email...")
        }.build()
    }

    override suspend fun doWork(): Result {
        val mailDataPath = inputData.getString("mailData") ?: return Result.failure()

        val username = inputData.getString("username") ?: return Result.failure()
        val password = inputData.getString("password") ?: return Result.failure()

        val mailData = Json.decodeFromStream<AndroidMailer>(File(mailDataPath).inputStream())

        val mailProps = Properties()
        mailProps.setProperty("mail.smtp.host", mailData.smtpServer)
        mailProps.setProperty("mail.smtp.port", mailData.smtpPort.toString())
        mailProps.setProperty("mail.smtp.auth", true.toString())
        mailProps.setProperty("mail.smtp.starttls.enable", mailData.useStartTLS.toString())

        val authenticator = AndroidMailerAuthenticator(username, password)
        val session = Session.getDefaultInstance(mailProps, authenticator)
        val message = MimeMessage(session)
        message.setFrom(mailData.from)
        message.addRecipients(Message.RecipientType.TO, mailData.to)
        message.subject = mailData.subject

        val renamedAttachmentFiles = renameFiles(applicationContext, mailData.attachments)

        val messageBodyPart = MimeBodyPart()
        messageBodyPart.setText(mailData.body)

        var attachmentPart: MimeBodyPart? = null
        if (mailData.attachments.isNotEmpty()) {
            attachmentPart = MimeBodyPart()
            renamedAttachmentFiles.forEach { file ->
                attachmentPart.attachFile(file)
            }
        }

        val multipart = MimeMultipart()
        multipart.addBodyPart(messageBodyPart)
        attachmentPart?.let { bodyPart -> multipart.addBodyPart(bodyPart) }

        message.setContent(multipart)

        try {
            Transport.send(message)
        } catch (e: Exception) {
            Log.e("EmailSender", null, e)
            return Result.failure()
        }

        return Result.success()
    }

    @OptIn(ExperimentalPathApi::class)
    suspend fun renameFiles(
        context: Context,
        attachments: List<AndroidMailerAttachment>
    ): List<File> {
        val newFiles = mutableListOf<File>()
        withContext(Dispatchers.IO) {
            val newFileDirectory = Path(context.filesDir.path, "AndroidMailer")
            if (newFileDirectory.notExists()) {
                Files.createDirectory(newFileDirectory)
            }
            newFileDirectory.listDirectoryEntries().forEach { path ->
                path.deleteRecursively()
            }

            attachments.forEach { attachment ->
                if (attachment.filePath.name == attachment.fileName) {
                    return@forEach
                }
                val oldFile = attachment.filePath.toFile()
                val newFile = File(newFileDirectory.toFile(), attachment.fileName)
                oldFile.copyTo(newFile, true)
                newFiles.add(newFile)
            }
        }
        return newFiles
    }
}