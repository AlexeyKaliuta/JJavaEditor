package com.jjavaeditor.parser

import com.jjavaeditor.parser.SegmentKinds.Companion.isOpenBracket
import com.jjavaeditor.parser.SegmentKinds.Companion.toBracketType


class LineDescription(val bolContext: LineContext) {
    var segments: Array<LineSegment>? = null
    var eolContext: LineContext = LineContext.None

    private var roundBrackets: BracketsAggregation? = null
    private var squareBrackets: BracketsAggregation? = null
    private var curlyBrackets: BracketsAggregation? = null

    fun getBracketAggregation(bracketType: BracketType, createIfAbsent: Boolean = false): BracketsAggregation? {
        return when (bracketType) {
            BracketType.Round -> roundBrackets ?: if (createIfAbsent) {
                roundBrackets = BracketsAggregation(); roundBrackets
            } else null

            BracketType.Square -> squareBrackets ?: if (createIfAbsent) {
                squareBrackets = BracketsAggregation(); squareBrackets
            } else null

            BracketType.Curly -> curlyBrackets ?: if (createIfAbsent) {
                curlyBrackets = BracketsAggregation(); curlyBrackets
            } else null
        }
    }

    fun containsAnyBracket(): Boolean {
        return roundBrackets != null || curlyBrackets != null || squareBrackets != null
    }

    fun registerBracket(kind: SegmentKind){
        val aggregation = getBracketAggregation(kind.toBracketType(), true)!!
        aggregation.registerBracket(kind.isOpenBracket())
    }
}

data class LineSegment(val kind: SegmentKind, val startOffset: Int, val endOffset: Int)

enum class LineContext {
    None, MultilineComment, MultilineLiteral
}

enum class BracketType {
    Round, Square, Curly
}

