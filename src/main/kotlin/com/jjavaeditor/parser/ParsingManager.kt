package com.jjavaeditor.parser

import com.jjavaeditor.document.Document
import com.jjavaeditor.document.DocumentLine
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ParsingManager {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private var executorTask: Future<*>? = null
    private val tasksQueue = ConcurrentLinkedDeque<DocumentLine>()

    var notifyLineParsed: ((DocumentLine) -> Unit)? = null

    fun enqueue(documentLine: DocumentLine) {
        tasksQueue.offer(documentLine)
        if (executorTask?.isDone != false)
            executorTask = executor.submit { processQueue() }
    }

    fun enqueueWithAssumption(documentLine: DocumentLine, doc: Document) {
        var lineIndex = documentLine.lineIndex
        var nextLine = documentLine
        while (true) {
            lineIndex--
            val line = doc.getLine(lineIndex)
            if (line.bolContext != null)
                return
            val description = JavaSyntaxParser.generateDescription(line.content, LineContext.MultilineComment)
            if (description.eolContext == LineContext.None)
                break
            nextLine = line
        }
        nextLine.setBolContext(LineContext.None, null)
        return enqueue(nextLine)
    }

    private fun processQueue() {
        while (true) {
            val line = tasksQueue.poll() ?: return
            val bolContext = line.bolContext ?: continue
            line.description = JavaSyntaxParser.generateDescription(line.content, bolContext)
            notifyLineParsed?.invoke(line)
            val nextLine = line.nextLine

            if (nextLine != null) {
                @Suppress("ControlFlowWithEmptyBody")
                while (!nextLine.setBolContext(line.eolContext, nextLine.bolContext)) {
                }
            }
        }
    }

    fun dispose() {
        executor.shutdown()
    }
}