package com.jjavaeditor.undo

import com.jjavaeditor.document.merge
import java.time.Instant

class UndoRedoManager {
    private val undoStack = ArrayDeque<UndoRedoOperation>()
    private val redoStack = ArrayDeque<UndoRedoOperation>()

    fun push(op: UndoRedoManagerOperation, item: UndoRedoActionResult) {
        when (op) {
            UndoRedoManagerOperation.None -> {
                redoStack.clear()

                if (undoStack.lastOrNull()?.tryJoin(item) == true)
                    return
                undoStack.addLast(UndoRedoOperation(item))
            }

            UndoRedoManagerOperation.Undo -> redoStack.addLast(UndoRedoOperation(item))
            UndoRedoManagerOperation.Redo -> undoStack.addLast(UndoRedoOperation(item))
        }
    }

    fun pop(op: UndoRedoManagerOperation): UndoRedoActionResult? {
        return when (op) {
            UndoRedoManagerOperation.None -> null
            UndoRedoManagerOperation.Undo -> undoStack.removeLastOrNull()?.joinedHistoryItem
            UndoRedoManagerOperation.Redo -> redoStack.removeLastOrNull()?.joinedHistoryItem
        }
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    private class UndoRedoOperation(var joinedHistoryItem: UndoRedoActionResult) {
        private var lastHistoryItem: UndoRedoActionResult = joinedHistoryItem
        private var modificationTime: Instant = Instant.now()

        fun tryJoin(newItem: UndoRedoActionResult): Boolean {
            val now = Instant.now()
            if (now > modificationTime.plusMillis(300)) return false
            return tryJoinTyping(newItem) || tryJoinDeletion(newItem)
        }

        private fun tryJoinDeletion(newItem: UndoRedoActionResult): Boolean {
            if (!lastHistoryItem.isSingleSymbolDeletion() || !newItem.isSingleSymbolDeletion()) {
                return false
            }
            val lastRange = lastHistoryItem.insertRange
            val newRange = newItem.insertRange
            if (lastRange == newRange) {
                joinedHistoryItem =
                    UndoRedoActionResult(lastRange, joinedHistoryItem.removedContent + newItem.removedContent)
            } else {
                if (!lastRange.begin.sameLineTo(newRange.begin) || lastRange.begin.offset - newRange.begin.offset != 1) {
                    return false
                }
                joinedHistoryItem =
                    UndoRedoActionResult(newRange, newItem.removedContent + joinedHistoryItem.removedContent)
            }
            lastHistoryItem = newItem
            modificationTime = Instant.now()
            return true
        }

        private fun tryJoinTyping(newItem: UndoRedoActionResult): Boolean {
            if (!lastHistoryItem.isSingleSymbolInsertion() || !newItem.isSingleSymbolInsertion()) return false

            val lastRange = lastHistoryItem.insertRange
            val newRange = newItem.insertRange

            if (lastRange.end != newRange.begin) return false
            val combinedRange = joinedHistoryItem.insertRange.merge(newRange.end)
            joinedHistoryItem = UndoRedoActionResult(combinedRange, "")
            lastHistoryItem = newItem
            modificationTime = Instant.now()
            return true
        }

        private fun UndoRedoActionResult.isSingleSymbolDeletion(): Boolean {
            return this.removedContent.length == 1 && this.insertRange.isPoint
        }

        private fun UndoRedoActionResult.isSingleSymbolInsertion(): Boolean {
            return this.removedContent.isEmpty() && this.insertRange.isSingleSymbol
        }
    }
}

enum class UndoRedoManagerOperation {
    None, Undo, Redo
}


