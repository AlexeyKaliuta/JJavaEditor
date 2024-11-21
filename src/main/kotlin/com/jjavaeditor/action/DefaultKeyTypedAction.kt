package com.jjavaeditor.action

import com.jjavaeditor.component.JJavaTextArea
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

class DefaultKeyTypedAction : AbstractAction(ActionNames.UNMAPPED_KEY) {
    override fun actionPerformed(e: ActionEvent) {
        val target = e.source as? JJavaTextArea
        if (target != null) {
            val content = e.actionCommand
            if ((content != null) && (content.isNotEmpty())) {
                val c = content[0]
                if ((c.code >= 0x20) && (c.code != 0x7F)) {
                    target.replaceContent(content)
                }
            }
        }
    }

}

