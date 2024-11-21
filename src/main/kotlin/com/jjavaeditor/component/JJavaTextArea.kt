package com.jjavaeditor.component

import com.jjavaeditor.action.Actions.Companion.initializeDefaultActions
import com.jjavaeditor.document.*
import com.jjavaeditor.undo.UndoRedoManager
import com.jjavaeditor.undo.UndoRedoManagerOperation
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.io.Reader
import java.io.Writer
import javax.swing.*
import kotlin.math.max


class JJavaTextArea : JComponent(), Scrollable {
    var highlighter: Highlighter = Highlighter(this)
    var bracketsHighlighter: BracketsHighlighter = BracketsHighlighter(this)
    val doc: Document = Document()
    val navigator: DocumentNavigator = DocumentNavigator(doc)
    val caret: Caret = Caret(this)
    val mouseEventHandler: MouseEventHandler = MouseEventHandler(this)
    private val undoRedoManager: UndoRedoManager = UndoRedoManager()

    private var sizeDimension = Dimension(0, 0)

    init {
        enableEvents(AWTEvent.KEY_EVENT_MASK)

        font = Font("Courier New", Font.PLAIN, 12)
        cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)
        ui = JJavaTextAreaUI(this)

        initializeDefaultActions()

        doc.notifyLineModified = { documentLine ->
            if (documentLine.isDrawn) {
                documentLine.isDrawn = false
                damageViewArea(documentLine.getRange())
                if (documentLine.lineIndex == caret.position.lineIndex)
                {
                    bracketsHighlighter.searchMatchedBracket(caret.position)
                }
            }
        }
    }

    override fun getUI(): JJavaTextAreaUI = ui as JJavaTextAreaUI

    override fun getPreferredSize(): Dimension {
        val i: Insets = getInsets()
        val ui = getUI()
        sizeDimension = Dimension(doc.maxOffset, doc.linesCount)
        return Dimension(
            (sizeDimension.width + 1) * ui.colWidth + i.left + i.right,
            sizeDimension.height * ui.rowHeight + i.top + i.bottom
        )
    }

    override fun getPreferredScrollableViewportSize(): Dimension {
        return getPreferredSize()
    }

    override fun getScrollableUnitIncrement(visibleRect: Rectangle?, orientation: Int, direction: Int): Int {
        return when (orientation) {
            SwingConstants.VERTICAL -> getUI().rowHeight
            SwingConstants.HORIZONTAL -> getUI().colWidth
            else -> throw IllegalArgumentException("Invalid orientation: $orientation")
        }
    }

    override fun getScrollableBlockIncrement(visibleRect: Rectangle?, orientation: Int, direction: Int): Int {
        return when (orientation) {
            SwingConstants.VERTICAL -> visibleRect!!.height
            SwingConstants.HORIZONTAL -> visibleRect!!.width
            else -> throw java.lang.IllegalArgumentException("Invalid orientation: $orientation")
        }
    }

    fun getPageHeightInDocumentPoints(): Int {
        val rect = visibleRect
        if (rect == null || rect.height == 0) return 30
        return max(rect.height / getUI().rowHeight, 1)
    }

    override fun getScrollableTracksViewportWidth(): Boolean {
        val parent = SwingUtilities.getUnwrappedParent(this)
        if (parent is JViewport) {
            return parent.getWidth() > preferredSize.width
        }
        return false
    }

    override fun getScrollableTracksViewportHeight(): Boolean {
        val parent = SwingUtilities.getUnwrappedParent(this)
        if (parent is JViewport) {
            return parent.getHeight() > preferredSize.height
        }
        return false
    }

    fun replaceContent(content: String) {
        val deleteRange = caret.getSelectedDocumentRange()
        replaceContent(deleteRange, content, UndoRedoManagerOperation.None)
    }

    private fun replaceContent(deleteRange: DocumentRange, content: String, operation: UndoRedoManagerOperation) {
        var refreshRange: DocumentRange? = null
        if (deleteRange.isSingleLine) refreshRange = deleteRange.merge(deleteRange.end.toEol(doc))

        val historyItem = doc.replaceContent(content, deleteRange)
        undoRedoManager.push(operation, historyItem)

        val newRange = historyItem.insertRange
        bracketsHighlighter.setHighlighter(null)
        caret.setPosition(newRange.end)
        bracketsHighlighter.searchMatchedBracket(caret.position)
        if (refreshRange != null && newRange.isSingleLine && doc.getTextDimension() == sizeDimension) {
            this.damageViewArea(refreshRange)
        } else {
            revalidate()
            scrollToDocumentPoint(caret.position)
        }
    }

    fun deleteContent(backward: Boolean) {
        val selectedRange = caret.getSelectedDocumentRange()
        if (!selectedRange.isPoint) {
            replaceContent(selectedRange, "", UndoRedoManagerOperation.None)
            return
        }
        val newPosition = navigator.changePositionBySymbol(
            selectedRange.begin,
            if (backward) DocumentNavigator.NavigateTo.Left else DocumentNavigator.NavigateTo.Right,
            caret
        )
        val deleteRange = DocumentRange.safeCreate(selectedRange.begin, newPosition)

        replaceContent(deleteRange, "", UndoRedoManagerOperation.None)
    }

    fun copy() {
        val selectedRange = caret.getSelectedDocumentRange()
        val text: String = if (selectedRange.isPoint) {
            doc.getString(doc.getLineRange(selectedRange.begin.lineIndex)) + "\n"
        } else {
            doc.getString(selectedRange)
        }
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }

    fun undo() {
        val historyItem = undoRedoManager.pop(UndoRedoManagerOperation.Undo)
        if (historyItem != null) {
            replaceContent(historyItem.insertRange, historyItem.removedContent, UndoRedoManagerOperation.Undo)
        }
    }

    fun redo() {
        val historyItem = undoRedoManager.pop(UndoRedoManagerOperation.Redo)
        if (historyItem != null) {
            replaceContent(historyItem.insertRange, historyItem.removedContent, UndoRedoManagerOperation.Redo)
        }
    }


    fun paste() {
        var pasteText = ""
        val contents = Toolkit.getDefaultToolkit().systemClipboard.getContents(null)
        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            pasteText = Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor).toString()
        }

        replaceContent(pasteText)
    }

    fun cut() {
        copy()
        var selectedRange = caret.getSelectedDocumentRange()
        if (!selectedRange.isPoint) {
            return replaceContent("")
        }
        selectedRange = doc.getLineRange(selectedRange.begin.lineIndex)
        val newEndSymbol = navigator.changePositionBySymbol(selectedRange.end, DocumentNavigator.NavigateTo.Right, null)
        selectedRange = DocumentRange(selectedRange.begin, newEndSymbol)
        caret.setPosition(selectedRange.begin, selectedRange.end)
        replaceContent("")
    }


    fun read(reader: Reader) {
        undoRedoManager.clear()
        bracketsHighlighter.setHighlighter(null)
        caret.setPosition(DocumentPoint.DOCUMENT_BEGIN)
        doc.read(reader)
        revalidate()
    }

    fun clear() {
        undoRedoManager.clear()
        bracketsHighlighter.setHighlighter(null)
        caret.setPosition(DocumentPoint.DOCUMENT_BEGIN)
        doc.setText(listOf())
        revalidate()
    }


    fun write(writer: Writer) {
        doc.write(writer)
    }

    fun scrollToDocumentPoint(point: DocumentPoint) {
        val viewArea = getUI().getCaretViewArea(point)
        if (SwingUtilities.isEventDispatchThread()) {
            scrollRectToVisible(viewArea)
        } else {
            SwingUtilities.invokeLater { scrollRectToVisible(viewArea) }
        }
    }

    internal fun damageCaretViewArea(point: DocumentPoint) {
        repaint(getUI().getCaretViewArea(point))
    }

    internal fun setSelectedRange(range: DocumentRange) {
        highlighter.set(range)
        bracketsHighlighter.setHighlighter(null)
    }

    fun selectAll() {
        caret.setPosition(doc.getDocumentEnd(), DocumentPoint.DOCUMENT_BEGIN)
    }

    fun moveToMatchedBracket() {
        val bracketsHighlightInfo = bracketsHighlighter.getMatchedBrackets() ?: return
        val dot = caret.position
        var newPosition = bracketsHighlightInfo.close.begin
        if (bracketsHighlightInfo.close.begin == dot || bracketsHighlightInfo.close.end == dot) {
            newPosition = bracketsHighlightInfo.open.begin
        }
        caret.setPosition(newPosition)
    }

    private fun viewPointToPoint(point: Point): DocumentPoint {
        val pos: DocumentPoint = getUI().coordinatesToPoint(point, getControlViewArea())
        if (pos.lineIndex < 0) return DocumentPoint.DOCUMENT_BEGIN
        if (pos.lineIndex >= doc.linesCount) return doc.getDocumentEnd()

        val eos = pos.toEol(doc)
        return if (pos < eos) {
            pos
        } else {
            eos
        }
    }

    fun setCaretPosition(point: Point, keepSelection: Boolean, focus: Boolean) {
        caret.setPosition(viewPointToPoint(point), keepSelection)
        if (!keepSelection) {
            bracketsHighlighter.searchMatchedBracket(caret.position)
        }
        if (focus) requestFocus()
    }

    internal fun damageViewArea(range: DocumentRange) {
        repaint(getUI().rangeToDamageArea(range, getControlViewArea()))
    }

    internal fun getControlViewArea(): Rectangle {
        val alloc: Rectangle = bounds
        alloc.y = 0
        alloc.x = 0
        val insets: Insets = insets
        alloc.x += insets.left
        alloc.y += insets.top
        alloc.width -= insets.left + insets.right
        alloc.height -= insets.top + insets.bottom
        return alloc
    }

    fun dispose() {
        doc.dispose()
    }
}