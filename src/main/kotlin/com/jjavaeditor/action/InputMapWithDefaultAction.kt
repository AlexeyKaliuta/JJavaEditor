package com.jjavaeditor.action

import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.InputMap
import javax.swing.KeyStroke

class InputMapWithDefaultAction : InputMap() {
    override fun get(keyStroke: KeyStroke?): Any? {
        val actionName = super.get(keyStroke)
        if (actionName == null && keyStroke != null &&
            keyStroke.keyChar != KeyEvent.CHAR_UNDEFINED &&
            isPrintableCharacterModifiersMask(keyStroke.modifiers)
        ) {
            return ActionNames.UNMAPPED_KEY
        }
        return actionName
    }

    private fun isPrintableCharacterModifiersMask(mods: Int): Boolean {
        return mods and InputEvent.ALT_DOWN_MASK == 0 &&
                mods and InputEvent.CTRL_DOWN_MASK == 0 &&
                mods and InputEvent.ALT_GRAPH_DOWN_MASK == 0
    }
}