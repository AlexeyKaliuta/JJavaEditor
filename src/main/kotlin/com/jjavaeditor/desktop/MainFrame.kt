package com.jjavaeditor.desktop

import com.jjavaeditor.component.JJavaTextArea
import java.awt.BorderLayout
import java.io.File
import javax.swing.*
import javax.swing.event.CaretEvent
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.system.exitProcess


class MainFrame : JFrame() {
    private var statusLabel: JLabel? = null
    private lateinit var textControl: JJavaTextArea
    private var openedFilePath: String = ""

    init {
        createUI()
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(800, 600)
        setLocationRelativeTo(null)
    }

    private fun createUI() {
        createTextArea()
        createMenuBar()
        createStatusBar()
        setFilePath("")
    }



    private fun createTextArea() {
        textControl = JJavaTextArea()
        textControl.addCaretListener { e: CaretEvent? ->
            statusLabel?.text = "Caret line ${e!!.dot + 1} Selection line ${e.mark + 1}"
        }
        val scrollPane = JScrollPane(textControl)
        contentPane.add(scrollPane, BorderLayout.CENTER)

    }

    private fun createStatusBar() {
        statusLabel = JLabel()
        statusLabel!!.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            )
        )

        add(statusLabel!!, BorderLayout.SOUTH)
    }

    private fun createMenuBar() {
        val menubar = JMenuBar()

        val file = JMenu("File")
        menubar.add(file)

        createSubMenu(file, "New", ::newFile, KeyStroke.getKeyStroke("ctrl N"))
        createSubMenu(file, "Open", ::openFile, KeyStroke.getKeyStroke("ctrl O") )
        createSubMenu(file, "Save", ::saveFile, KeyStroke.getKeyStroke("ctrl S"))
        createSubMenu(file, "Save As", ::saveAsFile, KeyStroke.getKeyStroke("ctrl alt S") )
        file.addSeparator()
        createSubMenu(file, "Exit", { exitProcess(0) })

        val edit = JMenu("Edit")
        menubar.add(edit)

        createSubMenu(edit, "Undo", textControl::undo, KeyStroke.getKeyStroke("ctrl Z"))
        createSubMenu(edit, "Redo", textControl::redo, KeyStroke.getKeyStroke("ctrl Y"))
        edit.addSeparator()
        createSubMenu(edit, "Cut", textControl::cut, KeyStroke.getKeyStroke("ctrl X"))
        createSubMenu(edit, "Copy", textControl::copy, KeyStroke.getKeyStroke("ctrl C"))
        createSubMenu(edit, "Paste", textControl::paste, KeyStroke.getKeyStroke("ctrl V"))
        edit.addSeparator()
        createSubMenu(edit, "Select All", textControl::selectAll, KeyStroke.getKeyStroke("ctrl A"))
        createSubMenu(edit, "Go to Matched Brace", textControl::moveToMatchedBracket, KeyStroke.getKeyStroke("ctrl B"))

        jMenuBar = menubar
    }

    private fun createSubMenu(menu: JMenu, caption: String, action:() -> Unit, keyStroke: KeyStroke? = null) : JMenuItem {
        val menuItem = JMenuItem(caption)
        menuItem.addActionListener { action() }
        if (keyStroke != null) {
            menuItem.setAccelerator(keyStroke)
        }
        menu.add(menuItem)
        return menuItem
    }

    private fun newFile() {
        textControl.clear()
        setFilePath("")
    }

    private fun openFile()
    {
        val fileChooser = JFileChooser()
        fileChooser.fileFilter = FileNameExtensionFilter("JAVA files", "java")
        if (openedFilePath.isNotEmpty())
            fileChooser.currentDirectory = File(openedFilePath)
        val result = fileChooser.showOpenDialog(this)
        if (result != JFileChooser.APPROVE_OPTION)
            return
        setFilePath(fileChooser.selectedFile.absolutePath)
        textControl.read(fileChooser.selectedFile.bufferedReader())
    }

    private fun saveFile(){
        if (openedFilePath.isEmpty())
            return saveAsFile()

        File(openedFilePath).bufferedWriter().use { out ->
            textControl.write(out)
        }
    }
    private fun saveAsFile(){
        val fileChooser = JFileChooser()
        fileChooser.fileFilter = FileNameExtensionFilter("JAVA files", "java")
        if (openedFilePath.isNotEmpty())
            fileChooser.currentDirectory = File(openedFilePath)
        val result = fileChooser.showSaveDialog(this)
        if (result != JFileChooser.APPROVE_OPTION)
            return
        var selectedFile = fileChooser.selectedFile
        if (!selectedFile.name.endsWith(".java")) {
            selectedFile = File(selectedFile.absolutePath + ".java")
        }

        setFilePath(selectedFile.absolutePath)
        saveFile()
    }

    private fun setFilePath(newFilePath: String){
        openedFilePath = newFilePath
        this.title = "Java Editor" + if(newFilePath.isNotEmpty()) " ($openedFilePath)" else ""
    }

}