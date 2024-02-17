package at.co.schwaerzler.maximilian.androidmailer

import kotlinx.serialization.Serializable
import java.nio.file.Path

@Serializable
data class AndroidMailerAttachment(
    val filePath: Path,
    val fileName: String
)