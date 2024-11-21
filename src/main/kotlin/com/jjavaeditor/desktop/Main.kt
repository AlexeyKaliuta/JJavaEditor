package com.jjavaeditor.desktop

import java.awt.EventQueue

private fun createAndShowGUI() {

    val frame = MainFrame()
    frame.isVisible = true
}

fun main() {
        EventQueue.invokeLater(::createAndShowGUI)
}