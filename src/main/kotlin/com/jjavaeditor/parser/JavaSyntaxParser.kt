package com.jjavaeditor.parser

class JavaSyntaxParser {

    companion object {

        fun generateDescription(line: String, bolContext: LineContext): LineDescription {
            val description = LineDescription(bolContext)
            description.eolContext = bolContext
            val segments = mutableListOf<LineSegment>()

            var offset = 0
            val length = line.length

            if (bolContext == LineContext.MultilineComment) {
                while (true) {
                    if (offset >= length - 1) {
                        return description
                    }
                    if (line[offset] == '*' && line[offset + 1] == '/') {
                        description.eolContext = LineContext.None
                        offset++
                        break
                    }
                    offset++
                }
                segments.add(LineSegment(SegmentKinds.COMMENT, 0, offset))
                offset++
            } else if (bolContext == LineContext.MultilineLiteral) {
                while (true) {
                    if (offset >= length - 2) {
                        return description
                    }
                    if (line[offset] == '"' && line[++offset] == '"' && line[++offset] == '"') {
                        description.eolContext = LineContext.None
                        break
                    }
                    offset++
                }
                segments.add(LineSegment(SegmentKinds.LITERAL, 0, offset))
                offset++
            }

            if (offset == length) {
                return description
            }

            while (offset < length) {
                val c = line[offset]
                when {
                    c.isWhitespace() -> {
                        offset++
                        continue
                    }

                    c.isLetter() || c == '_' -> {
                        val startOffset = offset
                        while (++offset < length) {
                            val currentChar = line[offset]
                            if (!currentChar.isLetterOrDigit() && currentChar != '_') break
                        }

                        var segmentKind = SegmentKinds.IDENTIFIER
                        if (line.substring(startOffset, offset) in JavaSyntax.KEYWORDS) {
                            segmentKind = SegmentKinds.KEYWORD
                        }
                        segments.add(LineSegment(segmentKind, startOffset, offset - 1))
                    }

                    c.isDigit() -> {
                        val startOffset = offset

                        if (c == '0' && offset + 2 < length && line[offset + 1].lowercaseChar() == 'x' && line[offset + 2].isDigit()) {
                            offset += 2
                        }

                        var isExpAllowed = true
                        while (++offset < length) {

                            val currentChar = line[offset]
                            if (currentChar.isDigit()) {
                                continue
                            }
                            if ((currentChar == '.' || currentChar == '_') && offset + 1 < length && line[offset + 1].isDigit()) {
                                offset++
                                continue
                            }

                            if (isExpAllowed && currentChar.lowercaseChar() == 'e' && offset + 1 < length && line[offset + 1].isDigit()) {
                                isExpAllowed = false
                                offset++
                                continue
                            }

                            if (currentChar == 'L' || currentChar == 'f' || currentChar == 'd') offset++
                            break
                        }
                        segments.add(LineSegment(SegmentKinds.NUMERIC, startOffset, offset - 1))
                    }


                    c == '/' -> {
                        if (offset + 1 < length) {
                            val secondChar = line[offset + 1]

                            if (secondChar == '/') {
                                segments.add(LineSegment(SegmentKinds.COMMENT, offset, length - 1))
                                offset = length
                                continue
                            }

                            if (secondChar == '*') {
                                var isMultiLine = true
                                val startIndex = offset
                                offset += 1
                                var previousChar = ' '
                                while (++offset < length) {
                                    val currentChar = line[offset]
                                    if (currentChar == '/' && previousChar == '*') {
                                        isMultiLine = false
                                        break
                                    }
                                    previousChar = currentChar
                                }
                                segments.add(LineSegment(SegmentKinds.COMMENT, startIndex, offset - 1))
                                if (isMultiLine) description.eolContext = LineContext.MultilineComment
                                continue
                            }
                        }
                        offset++
                    }

                    c == '"' -> {
                        if (offset + 2 < length && line[offset + 1] == '"' && line[offset + 2] == '"') {
                            segments.add(LineSegment(SegmentKinds.LITERAL, offset, length - 1))
                            description.eolContext = LineContext.MultilineLiteral
                            offset = length
                            continue
                        }
                        val startOffset = offset
                        while (++offset < length) {
                            val currentChar = line[offset]
                            if (currentChar == '"') {
                                offset++
                                break
                            }
                        }
                        segments.add(LineSegment(SegmentKinds.LITERAL, startOffset, offset - 1))
                    }

                    else -> {
                        val segmentKind = getBracketSegmentKind(c)
                        if (segmentKind != SegmentKinds.UNKNOWN) {
                            description.registerBracket(segmentKind)
                            segments.add(LineSegment(segmentKind, offset, offset))
                        }
                        offset++
                    }
                }
            }
            if (segments.any()) {
                val serializationResult = LineSegment.serialize(segments)
                description.arrayItemSize = serializationResult.second
                description.compressed = serializationResult.first
            }
            return description
        }

        private fun getBracketSegmentKind(char: Char): SegmentKind {
            return when (char) {
                '{' -> SegmentKinds.CURLY_OPEN_BRACKET
                '}' -> SegmentKinds.CURLY_CLOSE_BRACKET
                '(' -> SegmentKinds.ROUND_OPEN_BRACKET
                ')' -> SegmentKinds.ROUND_CLOSE_BRACKET
                '[' -> SegmentKinds.SQUARE_OPEN_BRACKET
                ']' -> SegmentKinds.SQUARE_CLOSE_BRACKET
                else -> SegmentKinds.UNKNOWN
            }
        }

    }
}

