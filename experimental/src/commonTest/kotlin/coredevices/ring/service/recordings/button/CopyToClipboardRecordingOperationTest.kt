package coredevices.ring.service.recordings.button

import coredevices.ring.service.recordings.RecordingProcessingQueue
import coredevices.util.SystemClipboard
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CopyToClipboardRecordingOperationTest {

    private val copied = mutableListOf<String>()
    private val clipboard = SystemClipboard { copied += it }

    @Test
    fun transcriptIsCopiedToTheClipboard() = runTest {
        CopyToClipboardRecordingOperation(FakeTranscribing("buy milk"), clipboard).run(null)

        assertEquals(listOf("buy milk"), copied)
    }

    @Test
    fun blankTranscriptLeavesTheClipboardAlone() = runTest {
        CopyToClipboardRecordingOperation(FakeTranscribing("  "), clipboard).run(null)

        assertTrue(copied.isEmpty())
    }

    /** The webhook decorator hooks the same transcript, so it has to be passed on. */
    @Test
    fun transcriptIsPassedOnToTheOuterHook() = runTest {
        val passedOn = mutableListOf<String>()
        val operation = CopyToClipboardRecordingOperation(FakeTranscribing("buy milk"), clipboard)
        operation.onTranscriptionPersisted = { passedOn += it }

        operation.run(null)

        assertEquals(listOf("buy milk"), copied)
        assertEquals(listOf("buy milk"), passedOn)
    }

    @Test
    fun failedTranscriptionCopiesNothing() = runTest {
        val operation = CopyToClipboardRecordingOperation(FakeTranscribing(null), clipboard)

        assertFailsWith<IllegalStateException> { operation.run(null) }
        assertTrue(copied.isEmpty())
    }
}

private class FakeTranscribing(private val transcript: String?) : TranscribingRecordingOperation {
    override var onTranscriptionPersisted: (suspend (transcription: String) -> Unit)? = null

    override suspend fun run(handle: RecordingProcessingQueue.TaskHandle?) {
        onTranscriptionPersisted?.invoke(transcript ?: throw IllegalStateException("transcription failed"))
    }
}
