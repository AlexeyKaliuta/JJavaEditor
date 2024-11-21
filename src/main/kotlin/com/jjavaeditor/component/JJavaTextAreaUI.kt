package com.jjavaeditor.component

import com.jjavaeditor.document.DocumentLine
import com.jjavaeditor.document.DocumentPoint
import com.jjavaeditor.document.DocumentRange
import com.jjavaeditor.document.intersect
import com.jjavaeditor.parser.LineContext
import com.jjavaeditor.parser.SegmentKind
import com.jjavaeditor.parser.SegmentKinds
import com.jjavaeditor.parser.SegmentKinds.Companion.isBracket
import java.awt.*
import javax.swing.JComponent
import javax.swing.plaf.ComponentUI
import kotlin.math.max

class JJavaTextAreaUI(private val component: JJavaTextArea) : ComponentUI() {
    internal val rowHeight: Int
    internal val colWidth: Int
    private val ascent: Int

    init {
        installUI(component)
        val metrics = component.getFontMetrics(component.font)
        rowHeight = metrics.height
        colWidth = metrics.charWidth('m')
        ascent = metrics.ascent
    }

    fun coordinatesToPoint(coordinates: Point, viewArea: Rectangle): DocumentPoint {
        return DocumentPoint(
            max((coordinates.y - viewArea.y) / rowHeight, 0),
            max((coordinates.x - viewArea.x) / colWidth, 0)
        )
    }

    private fun pointToCoordinates(point: DocumentPoint, viewArea: Rectangle): Point {
        return Point(
            point.offset * colWidth + viewArea.x,
            point.lineIndex * rowHeight + viewArea.y
        )
    }

    private fun singleLineRangeToViewArea(range: DocumentRange, viewArea: Rectangle): Rectangle {
        return Rectangle(
            pointToCoordinates(range.begin, viewArea),
            Dimension(
                (range.end.offset - range.begin.offset) * colWidth,
                rowHeight
            )
        )
    }

    fun rangeToDamageArea(range: DocumentRange, viewArea: Rectangle): Rectangle {
        if (range.isSingleLine) return singleLineRangeToViewArea(range, viewArea)
        return Rectangle(
            viewArea.x,
            viewArea.y + range.begin.lineIndex * rowHeight,
            viewArea.width,
            (range.end.lineIndex - range.begin.lineIndex + 1) * rowHeight,
        )
    }

    fun getCaretViewArea(point: DocumentPoint): Rectangle {
        val r = pointToCoordinates(point, component.getControlViewArea())
        return Rectangle(
            max(r.x - CARET_WIDTH / 2, 0),
            r.y, CARET_WIDTH, rowHeight
        )
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        if (g == null) return
        val viewArea = component.getControlViewArea()

        val clip = g.clipBounds
        val heightAbove = clip.y - viewArea.y
        val linesAbove: Int = max(0, heightAbove / rowHeight)

        val heightBelow = (viewArea.y + viewArea.height) - (clip.y + clip.height)
        val linesBelow: Int = max(0, heightBelow / rowHeight)
        val linesTotal: Int = viewArea.height / rowHeight

        for (i: Int in linesAbove..<linesTotal - linesBelow) {
            paintLine(g, i, viewArea)
        }

        paintCaret(g, clip)
    }

    private fun paintLine(g: Graphics, lineIndex: Int, viewArea: Rectangle) {
        if (lineIndex >= component.doc.linesCount) return
        val line = component.doc.getLine(lineIndex)
        if (line.length == 0) return
        line.isDrawn = true
        paintHighlights(line, g, viewArea)

        val lineCoordinates = pointToCoordinates(line.getBol(), viewArea)

        if (line.description?.hasSegments == true) {
            paintParsedLine(line, g, lineCoordinates)
        } else {
            g.color = getColor(line.bolContext)
            g.drawString(line.content, lineCoordinates.x, lineCoordinates.y + ascent)
            if (line.bolContext == null )
                component.requestLineParsing(line)
        }

    }

    private fun paintHighlights(documentLine: DocumentLine, g: Graphics, viewArea: Rectangle) {
        val highlighter = component.highlighter
        if (highlighter.isSet()) {
            val highlightedRange = highlighter.getHighlightedRange(documentLine.getRange())
            paintHighlight(highlightedRange, g, viewArea)
            return
        }
        val bracketsInfo = component.bracketsHighlighter.getMatchedBrackets() ?: return
        val lineRange = documentLine.getRange()
        paintHighlight(bracketsInfo.open.intersect(lineRange), g, viewArea)
        paintHighlight(bracketsInfo.close.intersect(lineRange), g, viewArea)
    }

    private fun paintHighlight(
        highlightedRange: DocumentRange?, g: Graphics, viewArea: Rectangle
    ) {
        if (highlightedRange != null) {
            val visibleArea = singleLineRangeToViewArea(highlightedRange, viewArea)
            g.color = SELECTION_COLOR
            g.fillRect(visibleArea.x, visibleArea.y, visibleArea.width, visibleArea.height)

        }
    }

    private fun paintParsedLine(
        line: DocumentLine,
        g: Graphics,
        coordinates: Point,
    ) {
        val text = line.content
        var index = 0
        for (region in line.description!!.segments!!) {
            if (index < region.startOffset) {
                paintSegment(index, region.startOffset, DEFAULT_COLOR, text, g, coordinates)
            }
            paintSegment(region.startOffset, region.endOffset + 1, getColor(region.kind), text, g, coordinates)
            index = region.endOffset + 1
        }
        if (index < line.length) {
            paintSegment(index, text.length, DEFAULT_COLOR, text, g, coordinates)
        }
    }

    private fun paintSegment(
        startIndex: Int,
        endIndex: Int,
        color: Color,
        text: String,
        g: Graphics,
        coordinates: Point,
    ) {
        g.color = color
        g.drawString(text.substring(startIndex, endIndex),
            coordinates.x + startIndex * colWidth, coordinates.y + ascent
        )
    }

    private fun paintCaret(g: Graphics, clip: Rectangle) {
        val caret = component.caret
        if (!caret.visible) {
            return
        }
        val caretViewArea = getCaretViewArea(caret.position)
        if (clip.contains(caretViewArea)) {
            g.color = CARET_COLOR
            g.fillRect(caretViewArea.x, caretViewArea.y, caretViewArea.width, caretViewArea.height)
        }
    }

    companion object {
        private const val CARET_WIDTH = 2

        private val CARET_COLOR = Color.DARK_GRAY
        private val DEFAULT_COLOR = Color.DARK_GRAY
        private val BRACKET_COLOR = Color.BLUE
        private val SELECTION_COLOR = Color.lightGray

        private val COLOR_MAP = mapOf(
            SegmentKinds.COMMENT to Color(0x8C8C8C),
            SegmentKinds.NUMERIC to Color(0x0092D1),
            SegmentKinds.IDENTIFIER to Color(0xA812D1),
            SegmentKinds.KEYWORD to Color(0x0033B3),
            SegmentKinds.LITERAL to Color(0x067D17),
        )

        private fun getColor(kind: SegmentKind): Color {
            if (kind.isBracket()) return BRACKET_COLOR
            return COLOR_MAP[kind] ?: DEFAULT_COLOR

        }

        private fun getColor(lineContext: LineContext?): Color {
            if (lineContext == null) return DEFAULT_COLOR
            return when (lineContext) {
                LineContext.None -> DEFAULT_COLOR
                LineContext.MultilineComment -> COLOR_MAP[SegmentKinds.COMMENT]!!
                LineContext.MultilineLiteral -> COLOR_MAP[SegmentKinds.LITERAL]!!
            }

        }

    }


}
