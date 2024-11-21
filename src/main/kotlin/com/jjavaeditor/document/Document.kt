package com.jjavaeditor.document

import com.jjavaeditor.parser.LineContext
import com.jjavaeditor.undo.UndoRedoActionResult
import java.awt.Dimension
import java.io.Reader
import java.io.Writer


class Document {
    private var lines: MutableList<DocumentLine> = mutableListOf()

    var notifyLineModified: ((DocumentLine) -> Unit)? = null
        set(value) {
            field = value
            for (line in lines) {
                line.notifyChange = value
            }
        }

    init {
        setText(listOf())
    }

    var maxOffset: Int = 0

    val linesCount: Int
        get() = lines.size

    fun getTextDimension() = Dimension(maxOffset, linesCount)

    fun getDocumentEnd(): DocumentPoint = getEol(lines.size - 1)

    fun getBol(index: Int): DocumentPoint = DocumentPoint(index, 0)

    fun getEol(index: Int): DocumentPoint {
        if (index in 0..<lines.size) {
            return DocumentPoint(index, lines[index].length)
        }
        return DocumentPoint(index, 0)
    }

    fun getLine(index: Int): DocumentLine = lines[index]

    fun getLine(point: DocumentPoint): DocumentLine = lines[point.lineIndex]

    fun getLineRange(lineIndex: Int): DocumentRange {
        return DocumentRange(getBol(lineIndex), getEol(lineIndex))
    }

    fun getString(range: DocumentRange): String {
        if (range.isPoint)
            return ""
        if (range.isSingleLine) {
            return lines[range.begin.lineIndex].getString(range)
        }
        val sb: StringBuilder = StringBuilder()
        for (lineIndex in range.begin.lineIndex..<range.end.lineIndex) {
            val line = lines[lineIndex]
            sb.appendLine(line.getString(line.getRange().intersect(range)!!))
        }
        val lastLine = lines[range.end.lineIndex]
        sb.append(lastLine.getString(lastLine.getRange().intersect(range)!!))
        return sb.toString()
    }

    fun setText(newLines: List<String>) {
        for (line in lines) {
            line.notifyChange = null
        }
        maxOffset = 0
        lines.clear()
        addLines(if (newLines.any()) newLines else listOf(""), 0)
    }

    private fun addLines(content: List<String>, lineIndex: Int) {
        require(lineIndex in 0..lines.size)
        if (content.isEmpty())
            return
        var nextNode = if (lineIndex == lines.size) null else lines[lineIndex]
        val prevNode = if (lineIndex == 0) null else lines[lineIndex - 1]

        val newList =
            List(content.size) { index ->
                DocumentLine(
                    content[index].replace("\t", "    "),
                    lineIndex + index,
                    null
                )
            }

        for (i in newList.size - 1 downTo 0) {

            val line = newList[i]
            if (line.length > maxOffset) {
                maxOffset = line.length
            }
            line.nextLine = nextNode

            line.notifyChange = notifyLineModified
            nextNode = line
        }
        lines.addAll(lineIndex, newList)

        prevNode?.nextLine = nextNode
        nextNode!!.setBolContext(prevNode?.eolContext ?: LineContext.None, null)
    }

    private fun removeLines(fromIndex: Int, toIndex: Int) {
        require(fromIndex in 0..toIndex)
        require(toIndex < lines.size)
        val nextNode = if (toIndex == lines.size - 1) null else lines[toIndex + 1]
        val prevNode = if (fromIndex == 0) null else lines[fromIndex - 1]

        prevNode?.nextLine = nextNode

        nextNode?.setBolContext(prevNode?.eolContext ?: LineContext.None, nextNode.bolContext)

        for (i: Int in fromIndex..toIndex) {
            lines[fromIndex].notifyChange = null
            lines.removeAt(fromIndex)
        }
    }

    private fun updateRowIndices(fromRow: Int) {
        var index = fromRow
        val count = linesCount
        while (index < count) {
            lines[index].lineIndex = index++
        }
    }

    fun replaceContent(newContent: String, range: DocumentRange): UndoRedoActionResult {
        val oldContent = getString(range)
        removeContent(range)

        val newRange: DocumentRange = insertContent(newContent, range.begin)

        if (newRange.end.lineIndex != range.end.lineIndex) {
            updateRowIndices(newRange.end.lineIndex)
        }
        return UndoRedoActionResult(newRange, oldContent)
    }

    private fun removeContent(range: DocumentRange) {
        if (range.isPoint)
            return
        val fromLine = range.begin.lineIndex
        val toLine = range.end.lineIndex
        val oldLines = lines.subList(fromLine, toLine + 1).toList()
        if (oldLines.size == 1) {
            oldLines.first().replaceContent(range, "")
        } else {
            oldLines.first().replaceContent(oldLines.first().getRange().intersect(range)!!, "")
            oldLines.last().replaceContent(oldLines.last().getRange().intersect(range)!!, "")

            if (oldLines.size > 2)
                removeLines(fromLine + 1, toLine - 1)

            if (fromLine < linesCount) {
                oldLines.first().merge(lines[fromLine + 1])
                removeLines(fromLine + 1, fromLine + 1)
            }
        }
    }

    private fun insertContent(
        newContent: String,
        insertPoint: DocumentPoint
    ): DocumentRange {
        val newLines = newContent.split('\n').toMutableList()
        if (newLines.size == 1) {
            val singleLine = lines[insertPoint.lineIndex]

            singleLine.replaceContent(insertPoint.toEmptyRange(), newContent)
            if (singleLine.length > maxOffset) {
                maxOffset = singleLine.length
            }
            return DocumentRange(
                insertPoint,
                insertPoint.toOffset(insertPoint.offset + newContent.length)
            )
        }

        val newContentRange = DocumentRange(
            insertPoint,
            DocumentPoint(insertPoint.lineIndex + newLines.size - 1, newLines.last().length)
        )

        val tailRange = DocumentRange(insertPoint, insertPoint.toEol(this))
        val tailText = lines[insertPoint.lineIndex].getString(tailRange)
        lines[insertPoint.lineIndex].replaceContent(tailRange, newLines.first())

        newLines[newLines.size - 1] += tailText
        addLines(newLines.subList(1, newLines.size).toList(), insertPoint.lineIndex + 1)

        return newContentRange
    }

    fun read(stream: Reader) {
        setText(stream.readLines())
    }

    fun write(writer: Writer) {
        for (line in lines) {
            writer.write(line.content)
            writer.write("\n")
        }
        writer.flush()
    }
}

fun DocumentPoint.toEol(document: Document): DocumentPoint = document.getEol(this.lineIndex)
