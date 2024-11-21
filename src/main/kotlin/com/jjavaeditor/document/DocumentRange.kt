package com.jjavaeditor.document

data class DocumentRange(val begin: DocumentPoint, val end: DocumentPoint){

    init {
        require(begin <= end) { "begin must be less than or equal to end ($begin) ($end)" }
    }

    val isPoint : Boolean
        get() = begin == end

    val isSingleLine : Boolean
        get() = end.lineIndex == begin.lineIndex

    val isSingleSymbol : Boolean
        get() = isSingleLine && end.offset - begin.offset == 1

    companion object {
        fun safeCreate(point1 : DocumentPoint, point2 : DocumentPoint): DocumentRange {
            if (point1 < point2)
                return DocumentRange(point1, point2)
            return DocumentRange(point2, point1)


        }
    }
}

fun DocumentRange.merge(pointToInclude: DocumentPoint): DocumentRange {
    if (pointToInclude > this.end)
        return DocumentRange(this.begin, pointToInclude)
    if (pointToInclude < this.begin)
        return DocumentRange(pointToInclude, this.end)
    return this
}

fun DocumentRange.intersect(other: DocumentRange): DocumentRange? {
    val begin = if (begin > other.begin) begin else other.begin
    val end = if (end < other.end) end else other.end
    if (begin > end)
        return null
    return DocumentRange(begin, end)
}



