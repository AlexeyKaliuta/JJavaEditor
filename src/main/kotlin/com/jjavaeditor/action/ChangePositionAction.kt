package com.jjavaeditor.action

import com.jjavaeditor.component.JJavaTextArea
import com.jjavaeditor.document.DocumentNavigator
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

class ChangePositionAction
internal constructor(
    nm: String,
    private val withSelection: Boolean,
    private val direction: DocumentNavigator.NavigateTo,
    private val scope: DocumentNavigator.NavigateBy
) : AbstractAction(nm) {
    override fun actionPerformed(e: ActionEvent) {
        val component = e.source as? JJavaTextArea
        if (component != null) {
            val caret = component.caret
            var dot = caret.position
            var savedOffset: Int? = null
            if (direction == DocumentNavigator.NavigateTo.Up || direction == DocumentNavigator.NavigateTo.Down) {
                savedOffset = caret.savedOffset
            }
            dot = component.navigator.changePosition(dot, direction, scope, caret)

            caret.setPosition(dot, withSelection)
            component.bracketsHighlighter.searchMatchedBracket(caret.position)

            if (savedOffset != null) {
                caret.savedOffset = savedOffset
            }
        }
    }
}

