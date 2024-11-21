package com.jjavaeditor.action

class KeyBindings {

    companion object {
        val defaultInputMap = mapOf(
            "ctrl B" to ActionNames.MATCHED_BRACKET,

            "ctrl Z" to ActionNames.UNDO,
            "ctrl Y" to ActionNames.REDO,
            "ctrl C" to ActionNames.COPY,
            "ctrl V" to ActionNames.PASTE,
            "ctrl X" to ActionNames.CUT,
            "COPY" to ActionNames.COPY,
            "PASTE" to ActionNames.PASTE,
            "CUT" to ActionNames.CUT,
            "control INSERT" to ActionNames.COPY,
            "shift INSERT" to ActionNames.PASTE,
            "shift DELETE" to ActionNames.CUT,
            "shift LEFT" to ActionNames.SELECT_LEFT,
            "shift KP_LEFT" to ActionNames.SELECT_LEFT,
            "shift RIGHT" to ActionNames.SELECT_RIGHT,
            "shift KP_RIGHT" to ActionNames.SELECT_RIGHT,
            "ctrl A" to ActionNames.SELECT_ALL,
            "HOME" to ActionNames.BEGIN_LINE,
            "END" to ActionNames.END_LINE,
            "shift HOME" to ActionNames.SELECT_BEGIN_LINE,
            "shift END" to ActionNames.SELECT_END_LINE,
            "UP" to ActionNames.UP,
            "KP_UP" to ActionNames.UP,
            "DOWN" to ActionNames.DOWN,
            "KP_DOWN" to ActionNames.DOWN,
            "PAGE_UP" to ActionNames.PAGE_UP,
            "PAGE_DOWN" to ActionNames.PAGE_DOWN,
            "shift PAGE_UP" to ActionNames.SELECT_PAGE_UP,
            "shift PAGE_DOWN" to ActionNames.SELECT_PAGE_DOWN,
            "shift UP" to ActionNames.SELECT_UP,
            "shift KP_UP" to ActionNames.SELECT_UP,
            "shift DOWN" to ActionNames.SELECT_DOWN,
            "shift KP_DOWN" to ActionNames.SELECT_DOWN,
            "ENTER" to ActionNames.ENTER_KEY,
            "BACK_SPACE" to ActionNames.BACKSPACE_KEY,
            "shift BACK_SPACE" to ActionNames.BACKSPACE_KEY,
            "DELETE" to ActionNames.DELETE_KEY,
            "RIGHT" to ActionNames.RIGHT,
            "LEFT" to ActionNames.LEFT,
            "KP_RIGHT" to ActionNames.RIGHT,
            "KP_LEFT" to ActionNames.LEFT,
            "TAB" to ActionNames.TAB_KEY,
            "ctrl HOME" to ActionNames.BEGIN_DOCUMENT,
            "ctrl END" to ActionNames.END_DOCUMENT,
            "ctrl shift HOME" to ActionNames.SELECT_BEGIN_DOCUMENT,
            "ctrl shift END" to ActionNames.SELECT_END_DOCUMENT,

            )
    }
}