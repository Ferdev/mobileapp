package coredevices.ring.service.recordings.button

import coredevices.ring.service.recordings.RecordingProcessingQueue
import coredevices.util.SystemClipboard

/**
 * Decorator that puts the transcript of [transcribing] on the system clipboard.
 * A blank transcript leaves the clipboard as it was.
 */
internal class CopyToClipboardRecordingOperation(
    private val transcribing: TranscribingRecordingOperation,
    private val clipboard: SystemClipboard,
) : TranscribingRecordingOperation {

    override var onTranscriptionPersisted: (suspend (transcription: String) -> Unit)? = null

    override suspend fun run(handle: RecordingProcessingQueue.TaskHandle?) {
        transcribing.onTranscriptionPersisted = { transcription ->
            if (transcription.isNotBlank()) clipboard.setText(transcription)
            onTranscriptionPersisted?.invoke(transcription)
        }
        transcribing.run(handle)
    }
}
