package com.jjavaeditor.document

import com.jjavaeditor.parser.BracketType
import com.jjavaeditor.parser.LineSegment
import com.jjavaeditor.parser.SegmentKinds.Companion.isBracket
import com.jjavaeditor.parser.SegmentKinds.Companion.isOpenBracket
import com.jjavaeditor.parser.SegmentKinds.Companion.toBracketType

class BracketMatcher {
    companion object {

        fun getBracketsPair(doc: Document, caretPoint: DocumentPoint, currentRange: DocumentRange?): DocumentRange? {
            val (bracketPoint, bracketSegmentIndex) = getNearBracketSegmentIndex(doc, caretPoint)
            if (bracketPoint == null) return null
            if (bracketPoint == currentRange?.begin || bracketPoint == currentRange?.end) return currentRange
            val pairedPoint = getPairedBracket(doc, bracketPoint, bracketSegmentIndex!!) ?: return null
            return DocumentRange.safeCreate(bracketPoint, pairedPoint)
        }

        private fun getNearBracketSegmentIndex(doc: Document, caretPoint: DocumentPoint): Pair<DocumentPoint?, Int?> {
            val line = doc.getLine(caretPoint)
            if (caretPoint.offset > line.content.length) return Pair(null, null)
            val description = line.description
            if (description?.hasSegments != true || !description.containsAnyBracket) return Pair(null, null)

            val caretOffset = caretPoint.offset
            val segments = description.segments!!

            val indexOfRightCandidate = segments.indexOfFirst { info -> info.startOffset >= caretOffset }
            val indexOfLeftCandidate = if (indexOfRightCandidate == -1) segments.size - 1 else indexOfRightCandidate - 1

            fun getPointIfValid(index: Int): DocumentPoint? {
                if (index < 0) return null
                val segment = segments[index]
                if (isNearBracketSegment(segment, caretOffset, line.content)) {
                    return caretPoint.toOffset(segment.startOffset)
                }
                return null
            }

            val rightCandidate = getPointIfValid(indexOfRightCandidate)
            val leftCandidate = getPointIfValid(indexOfLeftCandidate)

            if (isRightBracketCloserToCaret(leftCandidate, caretOffset, rightCandidate))
                return Pair(rightCandidate, indexOfRightCandidate)

            if (leftCandidate != null) return Pair(leftCandidate, indexOfLeftCandidate)
            if (rightCandidate != null) return Pair(rightCandidate, indexOfRightCandidate)
            return Pair(null, null)
        }

        private fun getPairedBracket(
            doc: Document, bracketPoint: DocumentPoint, bracketSegmentIndex: Int
        ): DocumentPoint? {
            val segments = doc.getLine(bracketPoint).description!!.segments!!
            val kind = segments[bracketSegmentIndex].kind
            val bracketType = kind.toBracketType()
            val isForward = kind.isOpenBracket()


            val increment = if (isForward) 1 else -1

            var (bracketLevel, matchingBracketOffset) = searchMatchingBracketInCurrentLine(
                increment, bracketType, bracketSegmentIndex + increment, segments
            )

            if (matchingBracketOffset != null) return bracketPoint.toOffset(matchingBracketOffset)

            val (newBracketLevel, matchedLineIndex) = searchMatchingBracketLine(
                bracketLevel, bracketType, bracketPoint.lineIndex + increment, doc
            )

            if (matchedLineIndex == null) return null

            val matchedLineSegments = doc.getLine(matchedLineIndex).description!!.segments!!
            matchingBracketOffset = searchMatchingBracketInCurrentLine(
                newBracketLevel, bracketType, if (isForward) 0 else matchedLineSegments.size - 1, matchedLineSegments
            ).second
            return DocumentPoint(matchedLineIndex, matchingBracketOffset!!)
        }

        private fun searchMatchingBracketInCurrentLine(
            currentBracketLevel: Int, bracketType: BracketType, startFromSegment: Int, segments: List<LineSegment>
        ): Pair<Int, Int?> {
            var bracketLevel = currentBracketLevel
            val increment = if (bracketLevel > 0) 1 else -1
            var index = startFromSegment
            while (true) {
                if (index < 0 || index == segments.size) return Pair(bracketLevel, null)
                val segment = segments[index]
                if (segment.kind.isBracket() && segment.kind.toBracketType() == bracketType) {
                    bracketLevel += if (segment.kind.isOpenBracket()) 1 else -1
                    if (bracketLevel == 0) {
                        return Pair(bracketLevel, segment.startOffset)
                    }
                }
                index += increment
            }
        }

        private fun searchMatchingBracketLine(
            currentBracketLevel: Int,
            bracketType: BracketType,
            startFromLine: Int,
            doc: Document,
        ): Pair<Int, Int?> {
            var bracketLevel = currentBracketLevel
            val increment = if (bracketLevel > 0) 1 else -1
            val lCount = doc.linesCount
            var lineIndex = startFromLine
            while (true) {
                if (lineIndex < 0 || lineIndex == lCount) return Pair(bracketLevel, null)

                val info = doc.getLine(lineIndex).description ?: return Pair(bracketLevel, null)

                if (info.containsMatchingBracket(bracketType, bracketLevel))
                    return Pair(bracketLevel, lineIndex)
                bracketLevel += info.getLevelCumulata(bracketType)

                lineIndex += increment
            }
        }


        private fun isRightBracketCloserToCaret(
            leftColumn: DocumentPoint?, caretColumn: Int, rightColumn: DocumentPoint?
        ): Boolean {
            if (leftColumn == null || rightColumn == null) return false
            return caretColumn - leftColumn.offset - 1 > rightColumn.offset - caretColumn
        }

        private fun isNearBracketSegment(segment: LineSegment, caretOffset: Int, string: String): Boolean {
            if (!segment.kind.isBracket()) return false
            if (segment.startOffset < caretOffset) {
                return string.substring(segment.startOffset + 1, caretOffset).isBlank()
            }
            return string.substring(caretOffset, segment.startOffset).isBlank()
        }

    }
}