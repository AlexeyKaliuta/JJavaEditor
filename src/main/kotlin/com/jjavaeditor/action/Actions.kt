package com.jjavaeditor.action

import com.jjavaeditor.component.JJavaTextArea
import com.jjavaeditor.document.DocumentNavigator
import java.awt.AWTKeyStroke
import java.awt.KeyboardFocusManager
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.JComponent.WHEN_FOCUSED

class Actions {
    companion object {
        private fun getDefaultActions(): List<AbstractAction> = listOf(
            DefaultKeyTypedAction(),

            SimpleAction(ActionNames.ENTER_KEY) { component -> component.replaceContent("\n") },
            SimpleAction(ActionNames.TAB_KEY) { component -> component.replaceContent("    ") },
            SimpleAction(ActionNames.COPY) { component -> component.copy() },
            SimpleAction(ActionNames.PASTE) { component -> component.paste() },
            SimpleAction(ActionNames.CUT) { component -> component.cut() },
            SimpleAction(ActionNames.DELETE_KEY) { component -> component.deleteContent(false) },
            SimpleAction(ActionNames.BACKSPACE_KEY) { component -> component.deleteContent(true) },
            SimpleAction(ActionNames.UNDO) { component -> component.undo() },
            SimpleAction(ActionNames.REDO) { component -> component.redo() },
            SimpleAction(ActionNames.SELECT_ALL) { component -> component.selectAll() },
            SimpleAction(ActionNames.MATCHED_BRACKET) { component -> component.moveToMatchedBracket() },

            ChangePositionAction(
                ActionNames.UP,
                false,
                DocumentNavigator.NavigateTo.Up,
                DocumentNavigator.NavigateBy.Symbol
            ),
            ChangePositionAction(
                ActionNames.DOWN,
                false,
                DocumentNavigator.NavigateTo.Down,
                DocumentNavigator.NavigateBy.Symbol
            ),
            ChangePositionAction(
                ActionNames.LEFT,
                false,
                DocumentNavigator.NavigateTo.Left,
                DocumentNavigator.NavigateBy.Symbol
            ),
            ChangePositionAction(
                ActionNames.RIGHT,
                false,
                DocumentNavigator.NavigateTo.Right,
                DocumentNavigator.NavigateBy.Symbol
            ),

            ChangePositionAction(
                ActionNames.SELECT_UP,
                true,
                DocumentNavigator.NavigateTo.Up,
                DocumentNavigator.NavigateBy.Symbol
            ),
            ChangePositionAction(
                ActionNames.SELECT_DOWN,
                true,
                DocumentNavigator.NavigateTo.Down,
                DocumentNavigator.NavigateBy.Symbol
            ),
            ChangePositionAction(
                ActionNames.SELECT_LEFT,
                true,
                DocumentNavigator.NavigateTo.Left,
                DocumentNavigator.NavigateBy.Symbol
            ),
            ChangePositionAction(
                ActionNames.SELECT_RIGHT,
                true,
                DocumentNavigator.NavigateTo.Right,
                DocumentNavigator.NavigateBy.Symbol
            ),

            ChangePositionAction(
                ActionNames.PAGE_UP,
                false,
                DocumentNavigator.NavigateTo.Up,
                DocumentNavigator.NavigateBy.Page
            ),
            ChangePositionAction(
                ActionNames.PAGE_DOWN,
                false,
                DocumentNavigator.NavigateTo.Down,
                DocumentNavigator.NavigateBy.Page
            ),
            ChangePositionAction(
                ActionNames.BEGIN_LINE,
                false,
                DocumentNavigator.NavigateTo.Left,
                DocumentNavigator.NavigateBy.Page
            ),
            ChangePositionAction(
                ActionNames.END_LINE,
                false,
                DocumentNavigator.NavigateTo.Right,
                DocumentNavigator.NavigateBy.Page
            ),

            ChangePositionAction(
                ActionNames.SELECT_PAGE_UP,
                true,
                DocumentNavigator.NavigateTo.Up,
                DocumentNavigator.NavigateBy.Page
            ),
            ChangePositionAction(
                ActionNames.SELECT_PAGE_DOWN,
                true,
                DocumentNavigator.NavigateTo.Down,
                DocumentNavigator.NavigateBy.Page
            ),
            ChangePositionAction(
                ActionNames.SELECT_BEGIN_LINE,
                true,
                DocumentNavigator.NavigateTo.Left,
                DocumentNavigator.NavigateBy.Page
            ),
            ChangePositionAction(
                ActionNames.SELECT_END_LINE,
                true,
                DocumentNavigator.NavigateTo.Right,
                DocumentNavigator.NavigateBy.Page
            ),

            ChangePositionAction(
                ActionNames.BEGIN_DOCUMENT,
                false,
                DocumentNavigator.NavigateTo.Left,
                DocumentNavigator.NavigateBy.Document
            ),
            ChangePositionAction(
                ActionNames.END_DOCUMENT,
                false,
                DocumentNavigator.NavigateTo.Right,
                DocumentNavigator.NavigateBy.Document
            ),

            ChangePositionAction(
                ActionNames.SELECT_BEGIN_DOCUMENT,
                true,
                DocumentNavigator.NavigateTo.Left,
                DocumentNavigator.NavigateBy.Document
            ),
            ChangePositionAction(
                ActionNames.SELECT_END_DOCUMENT,
                true,
                DocumentNavigator.NavigateTo.Right,
                DocumentNavigator.NavigateBy.Document
            ),

            )

        fun JJavaTextArea.initializeDefaultActions() {
            val km = this.getInputMap(WHEN_FOCUSED)
            val newKM = InputMapWithDefaultAction()
            newKM.parent = km.parent
            km.parent = newKM
            val inputMap = this.getInputMap(WHEN_FOCUSED)

            for (keybinding in KeyBindings.defaultInputMap)
                inputMap.put(KeyStroke.getKeyStroke(keybinding.key), keybinding.value)
            for (action in getDefaultActions())
                this.actionMap.put(action.getValue(Action.NAME), action)

            updateFocusTraversalKeys(this)
        }


        private fun updateFocusTraversalKeys(component: JComponent) {
            val storedForwardTraversalKeys: Set<AWTKeyStroke> = component.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS)
            val storedBackwardTraversalKeys: Set<AWTKeyStroke> = component.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS)
            val forwardTraversalKeys: MutableSet<AWTKeyStroke> = HashSet(storedForwardTraversalKeys)
            val backwardTraversalKeys: MutableSet<AWTKeyStroke> = HashSet(storedBackwardTraversalKeys)
            forwardTraversalKeys.remove(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0))
            backwardTraversalKeys.remove(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK))
            LookAndFeel.installProperty(component, "focusTraversalKeysForward", forwardTraversalKeys)
            LookAndFeel.installProperty(component, "focusTraversalKeysBackward", backwardTraversalKeys)
        }
    }
}