package com.jjavaeditor.document

import com.jjavaeditor.component.Caret
import kotlin.math.max
import kotlin.math.min

class DocumentNavigator(val document: Document) {

    enum class NavigateBy {
        Symbol, Page, Document
    }

    enum class NavigateTo {
        Left, Right, Up, Down,
    }

    fun changePosition(
        point: DocumentPoint, direction: NavigateTo, scope: NavigateBy, caret: Caret?
    ): DocumentPoint {
        return when (scope) {
            NavigateBy.Symbol -> changePositionBySymbol(point, direction, caret)
            NavigateBy.Page -> changePositionByPage(point, direction, caret)
            NavigateBy.Document -> changePositionByDocument(direction)
        }
    }

    private fun changePositionByDocument(direction: NavigateTo): DocumentPoint {
        return when (direction) {
            NavigateTo.Left -> return DocumentPoint.DOCUMENT_BEGIN
            NavigateTo.Right -> return document.getDocumentEnd()
            else -> DocumentPoint.DOCUMENT_BEGIN
        }
    }

    private fun changePositionByPage(point: DocumentPoint, direction: NavigateTo, caret: Caret?): DocumentPoint {
        var result = DocumentPoint.DOCUMENT_BEGIN
        when (direction) {
            NavigateTo.Left -> return point.toBol()

            NavigateTo.Right -> return point.toEol(document)

            NavigateTo.Up -> {
                var step = 30
                if (caret != null) {
                    step = caret.component.getPageHeightInDocumentPoints()
                }
                if (point.lineIndex == 0) return DocumentPoint.DOCUMENT_BEGIN
                result = DocumentPoint(max(point.lineIndex - step, 0), point.offset)
            }

            NavigateTo.Down -> {
                var step = 30
                if (caret != null) {
                    step = caret.component.getPageHeightInDocumentPoints()
                }
                val lastLineIndex = document.linesCount - 1
                if (point.lineIndex == lastLineIndex) return document.getDocumentEnd()
                result = DocumentPoint(min(point.lineIndex + step, lastLineIndex), point.offset)
            }
        }
        return adjustDocumentPoint(caret?.savedOffset, result)
    }

    fun changePositionBySymbol(point: DocumentPoint, direction: NavigateTo, caret: Caret?): DocumentPoint {
        var result = DocumentPoint.DOCUMENT_BEGIN
        when (direction) {
            NavigateTo.Left -> {
                if (point.offset == 0) {
                    if (point.lineIndex == 0) return point
                    return document.getEol(point.lineIndex - 1)
                }
                return point.toOffset(point.offset - 1)
            }

            NavigateTo.Right -> {
                val eol = point.toEol(document)
                if (point == eol) {
                    if (point.lineIndex == document.linesCount - 1) return point
                    return document.getBol(point.lineIndex + 1)
                }
                return point.toOffset(point.offset + 1)
            }

            NavigateTo.Up -> {
                if (point.lineIndex == 0) return DocumentPoint.DOCUMENT_BEGIN
                result = DocumentPoint(point.lineIndex - 1, point.offset)
            }

            NavigateTo.Down -> {
                if (point.lineIndex == document.linesCount - 1) return document.getDocumentEnd()
                result = DocumentPoint(point.lineIndex + 1, point.offset)
            }
        }
        return adjustDocumentPoint(caret?.savedOffset, result)
    }

    private fun adjustDocumentPoint(
        savedOffset: Int?, dirtyPoint: DocumentPoint
    ): DocumentPoint {
        var result = dirtyPoint
        if (savedOffset != null && result.offset < savedOffset) {
            result = result.toOffset(savedOffset)
        }

        val eol = result.toEol(document)
        if (eol < result) return eol
        return result
    }

}