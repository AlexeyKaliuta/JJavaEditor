package com.jjavaeditor.undo

import com.jjavaeditor.document.DocumentRange

class UndoRedoActionResult(val insertRange: DocumentRange, val removedContent: String)