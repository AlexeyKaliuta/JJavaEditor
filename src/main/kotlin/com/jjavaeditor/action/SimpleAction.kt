package com.jjavaeditor.action

import com.jjavaeditor.component.JJavaTextArea
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

class SimpleAction(actionName: String,val action : (JJavaTextArea) -> Unit) : AbstractAction(actionName) {
    override fun actionPerformed(e: ActionEvent) {
        if (e.source is JJavaTextArea)
            action(e.source as JJavaTextArea)
    }
}