package com.jjavaeditor.component

import com.jjavaeditor.document.DocumentPoint
import com.jjavaeditor.document.DocumentRange
import javax.swing.Timer
import javax.swing.event.CaretEvent


class Caret(val component: JJavaTextArea) {
    var position: DocumentPoint = DocumentPoint.DOCUMENT_BEGIN
        private set
    private var selectionPosition: DocumentPoint = DocumentPoint.DOCUMENT_BEGIN
    var savedOffset: Int? = null
    var visible: Boolean = true
        private set

    private val flasher: Timer = Timer(500) {
        visible = !visible
        component.damageCaretViewArea(position)
    }

    init {
        flasher.start()
    }

    fun setPosition(newPosition: DocumentPoint, keepSelection: Boolean = false) =
        setPosition(newPosition, if (keepSelection) selectionPosition else newPosition)

    fun setPosition(caretPoint: DocumentPoint, selectionPoint: DocumentPoint) {

        val isCaretMoved = caretPoint != position
        val isSelectionChanged = selectionPoint != selectionPosition
        if (isCaretMoved)
            changeCaretPosition(caretPoint)
        selectionPosition = selectionPoint
        if (isCaretMoved || isSelectionChanged) {
            component.setSelectedRange(getSelectedDocumentRange())
        }
        component.fireCaretUpdate(DocumentCaretEvent(position.lineIndex, selectionPosition.lineIndex, this))
    }

    private fun changeCaretPosition(newPosition: DocumentPoint) {
        if (visible)
            component.damageCaretViewArea(position)

        flasher.restart()
        visible = true

        position = newPosition
        savedOffset = newPosition.offset

        component.damageCaretViewArea(position)
        component.scrollToDocumentPoint(position)
    }

    fun getSelectedDocumentRange(): DocumentRange {
        return DocumentRange.safeCreate(position, selectionPosition)
    }
}

class DocumentCaretEvent(private val position: Int, private val selectionPosition: Int, source: Any?) :
    CaretEvent(source) {
    override fun getDot(): Int {
        return position
    }

    override fun getMark(): Int {
        return selectionPosition
    }
}