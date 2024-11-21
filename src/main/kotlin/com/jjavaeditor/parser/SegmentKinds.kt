package com.jjavaeditor.parser

typealias SegmentKind = UByte

class SegmentKinds {
    companion object {
        const val UNKNOWN: SegmentKind = 255u
        const val KEYWORD: SegmentKind = 254u
        const val IDENTIFIER: SegmentKind = 253u
        const val NUMERIC: SegmentKind = 252u
        const val LITERAL: SegmentKind = 251u
        const val COMMENT: SegmentKind = 250u

        const val ROUND_OPEN_BRACKET: SegmentKind = 0b000u
        const val ROUND_CLOSE_BRACKET: SegmentKind = 0b0001u
        const val SQUARE_OPEN_BRACKET: SegmentKind = 0b010u
        const val SQUARE_CLOSE_BRACKET: SegmentKind = 0b011u
        const val CURLY_OPEN_BRACKET: SegmentKind = 0b110u
        const val CURLY_CLOSE_BRACKET: SegmentKind = 0b111u

        fun SegmentKind.isOpenBracket(): Boolean {
            return (this and ROUND_CLOSE_BRACKET) == ROUND_OPEN_BRACKET
        }

        fun SegmentKind.isBracket(): Boolean {
            return this <= CURLY_CLOSE_BRACKET
        }

        fun SegmentKind.toBracketType(): BracketType {
            return when(this and CURLY_OPEN_BRACKET){
                ROUND_OPEN_BRACKET -> BracketType.Round
                SQUARE_OPEN_BRACKET -> BracketType.Square
                CURLY_OPEN_BRACKET -> BracketType.Curly
                else -> throw IllegalArgumentException("Unknown classification $this")
            }
        }
    }
}