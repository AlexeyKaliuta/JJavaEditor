package com.jjavaeditor.document

import com.jjavaeditor.parser.LineContext
import com.jjavaeditor.parser.LineDescription

class DocumentLine(var content: String, var lineIndex: Int, var nextLine: DocumentLine?) {
    var notifyChange: ((DocumentLine) -> Unit)? = null
    var isDrawn: Boolean = false

    var description: LineDescription? = null
        get() = if (bolContext != null) field else null
        set(value) {
            field = if (bolContext == value?.bolContext) value else null
        }

    var bolContext: LineContext? = null
        set(value) {
            if (field == value) {
                return
            }

            field = value

            if (value != null) {
                notifyChange?.invoke(this)
            }
        }

    val eolContext: LineContext?
        get() = description?.eolContext

    val length: Int
        get() = content.length

    fun getBol(): DocumentPoint = DocumentPoint(lineIndex, 0)
    fun getEol(): DocumentPoint = DocumentPoint(lineIndex, content.length)
    fun getRange(): DocumentRange = DocumentRange(getBol(), getEol())

    fun replaceContent(range: DocumentRange, newContent: String) {
        content = content.replaceRange(range.begin.offset, range.end.offset, newContent)
        description = null
        notifyChange?.invoke(this)
    }

    fun merge(line: DocumentLine) {
        content += line.content
        description = null
        notifyChange?.invoke(this)
    }

    fun getString(range: DocumentRange): String {
        require(range.begin.lineIndex == range.end.lineIndex)
        return content.substring(range.begin.offset, range.end.offset)
    }
}