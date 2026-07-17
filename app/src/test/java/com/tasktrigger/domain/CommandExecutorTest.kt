package com.tasktrigger.domain

import com.tasktrigger.data.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CommandExecutorTest {
    @Test
    fun `executor drains large output while the process is running`() {
        val output = "line\n".repeat(30_000)
        val process = DrainSensitiveProcess(output)
        val executor = CommandExecutor(processStarter = { process }, timeoutSeconds = 1)

        val result = executor.execute(task())

        assertEquals(CommandFailure.NONE, result.failure)
        assertEquals(output, result.output)
    }

    @Test
    fun `timeout keeps output already produced`() {
        val process = TimeoutProcess("partial output")
        val executor = CommandExecutor(processStarter = { process }, timeoutSeconds = 1)

        val result = executor.execute(task())

        assertEquals(CommandFailure.TIMEOUT, result.failure)
        assertTrue(result.output.contains("partial output"))
    }

    private fun task() = TaskEntity(name = "command", command = "ignored", triggerAt = 1)
}

private class DrainSensitiveProcess(output: String) : Process() {
    private val drained = CountDownLatch(1)
    private val input = DrainingInputStream(output.toByteArray(), drained)

    override fun waitFor(timeout: Long, unit: TimeUnit): Boolean = drained.await(timeout, unit)
    override fun waitFor(): Int {
        drained.await()
        return 0
    }

    override fun exitValue(): Int = if (drained.count == 0L) 0 else throw IllegalThreadStateException()
    override fun getInputStream(): InputStream = input
    override fun getErrorStream(): InputStream = ByteArrayInputStream(byteArrayOf())
    override fun getOutputStream(): OutputStream = ByteArrayOutputStream()
    override fun destroy() = Unit
}

private class DrainingInputStream(
    bytes: ByteArray,
    private val drained: CountDownLatch,
) : ByteArrayInputStream(bytes) {
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int =
        super.read(buffer, offset, length).also { if (it == -1) drained.countDown() }

    override fun read(): Int = super.read().also { if (it == -1) drained.countDown() }
}

private class TimeoutProcess(output: String) : Process() {
    private val input = ByteArrayInputStream(output.toByteArray())

    override fun waitFor(timeout: Long, unit: TimeUnit): Boolean = false
    override fun waitFor(): Int = 137
    override fun exitValue(): Int = throw IllegalThreadStateException()
    override fun getInputStream(): InputStream = input
    override fun getErrorStream(): InputStream = ByteArrayInputStream(byteArrayOf())
    override fun getOutputStream(): OutputStream = ByteArrayOutputStream()
    override fun destroy() = Unit
    override fun destroyForcibly(): Process = this
}
