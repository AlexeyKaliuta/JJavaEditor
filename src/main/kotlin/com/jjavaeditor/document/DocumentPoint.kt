package com.jjavaeditor.document

data class DocumentPoint(val lineIndex: Int, val offset: Int){
    operator fun compareTo(other: DocumentPoint): Int {
        val result  = lineIndex.compareTo(other.lineIndex)
        if (result == 0)
            return offset.compareTo(other.offset)
        return result
    }

    fun toBol(): DocumentPoint = DocumentPoint(lineIndex, 0)

    fun sameLineTo(other: DocumentPoint): Boolean {
        return lineIndex == other.lineIndex
    }

    fun toSingleSymbolRange(): DocumentRange {
        return DocumentRange(this, toOffset(offset + 1))
    }

    fun toOffset(newOffset: Int):DocumentPoint = DocumentPoint(lineIndex, newOffset)

    fun toEmptyRange(): DocumentRange {
        return DocumentRange(this, this)
    }
    companion object {
        val DOCUMENT_BEGIN = DocumentPoint(0,0)
    }
}

