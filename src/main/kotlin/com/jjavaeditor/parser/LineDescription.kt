package com.jjavaeditor.parser


class LineDescription(val bolContext: LineContext) {

    var compressed: ByteArray? = null
    var arrayItemSize: UByte = 0u
    val segments: List<LineSegment>?
        get() {
            return LineSegment.deserialize(compressed, arrayItemSize)
        }

    val hasSegments: Boolean
        get() {
            return compressed != null
        }

    var eolContext: LineContext = LineContext.None

    var containsAnyBracket: Boolean = false
    private var roundLevelCumulata: Byte = 0
    private var roundMaxDrawdown: Byte = 0

    private var squareLevelCumulata: Byte = 0
    private var squareMaxDrawdown: Byte = 0

    private var curlyLevelCumulata: Byte = 0
    private var curlyMaxDrawdown: Byte = 0

    fun registerBracket(kind: SegmentKind) {
        containsAnyBracket = true
        when (kind) {
            SegmentKinds.CURLY_OPEN_BRACKET ->
                curlyLevelCumulata++

            SegmentKinds.ROUND_OPEN_BRACKET ->
                roundLevelCumulata++

            SegmentKinds.SQUARE_OPEN_BRACKET ->
                squareLevelCumulata++

            SegmentKinds.CURLY_CLOSE_BRACKET -> {
                curlyLevelCumulata--
                if (curlyLevelCumulata < curlyMaxDrawdown) {
                    curlyMaxDrawdown = curlyLevelCumulata
                }
            }

            SegmentKinds.ROUND_CLOSE_BRACKET -> {
                roundLevelCumulata--
                if (roundLevelCumulata < roundMaxDrawdown) {
                    roundMaxDrawdown = roundLevelCumulata
                }
            }

            SegmentKinds.SQUARE_CLOSE_BRACKET -> {
                squareLevelCumulata--
                if (squareLevelCumulata < squareMaxDrawdown) {
                    squareMaxDrawdown = squareLevelCumulata
                }
            }
        }
    }

    fun containsMatchingBracket(bracketType: BracketType, bracketLevel: Int): Boolean {
        return when (bracketType) {
            BracketType.Round ->
                if (bracketLevel > 0) bracketLevel <= -roundMaxDrawdown
                else bracketLevel >= roundMaxDrawdown - roundLevelCumulata

            BracketType.Square ->
                if (bracketLevel > 0) bracketLevel <= -squareMaxDrawdown
                else bracketLevel >= squareMaxDrawdown - squareLevelCumulata

            BracketType.Curly ->
                if (bracketLevel > 0) bracketLevel <= -curlyMaxDrawdown
                else bracketLevel >= curlyMaxDrawdown - curlyLevelCumulata
        }
    }

    fun getLevelCumulata(bracketType: BracketType): Byte {
        return when (bracketType) {
            BracketType.Round -> roundLevelCumulata
            BracketType.Square -> squareLevelCumulata
            BracketType.Curly -> curlyLevelCumulata
        }
    }
}

enum class LineContext {
    None, MultilineComment, MultilineLiteral
}

enum class BracketType {
    Round, Square, Curly
}

