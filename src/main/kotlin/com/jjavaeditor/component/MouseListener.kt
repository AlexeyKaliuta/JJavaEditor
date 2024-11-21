package com.jjavaeditor.component

import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.SwingUtilities

class MouseEventHandler(val component: JJavaTextArea) : MouseListener, MouseMotionListener {
    private var isDragging: Boolean = false
    private var shouldHandleRelease: Boolean = false

    init {
        component.addMouseListener(this)
        component.addMouseMotionListener(this)
    }

    private fun moveCaret(e: MouseEvent, keepSelection: Boolean, focus: Boolean = true) {
        component.setCaretPosition(e.point, keepSelection, focus)
    }

    override fun mousePressed(e: MouseEvent) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.isConsumed) {
                shouldHandleRelease = true
            } else {
                shouldHandleRelease = false
                adjustCaret(e)
            }
        }
    }

    private fun adjustCaret(e: MouseEvent) {
        if (e.isShiftDown) {
            moveCaret(e, true)
        } else if (!e.isPopupTrigger) {
            moveCaret(e, false)
        }
    }

    override fun mouseReleased(e: MouseEvent) {
        if (!e.isConsumed && shouldHandleRelease && SwingUtilities.isLeftMouseButton(e)) {
            if (isDragging) isDragging = false
            else adjustCaret(e)
        }
    }

    override fun mouseDragged(e: MouseEvent) {
        if ((!e.isConsumed) && SwingUtilities.isLeftMouseButton(e)) {
            moveCaret(e, true, focus = false)
            isDragging = true
        }
    }

    override fun mouseClicked(e: MouseEvent) {}

    override fun mouseEntered(e: MouseEvent?) {}

    override fun mouseExited(e: MouseEvent?) {}

    override fun mouseMoved(e: MouseEvent?) {}

}