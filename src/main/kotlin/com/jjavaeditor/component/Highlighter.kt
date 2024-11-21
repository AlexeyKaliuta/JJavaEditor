package com.jjavaeditor.component

import com.jjavaeditor.document.DocumentRange
import com.jjavaeditor.document.intersect


class Highlighter(private val component: JJavaTextArea) {

    private var highlightedRange: DocumentRange? = null

    fun isSet(): Boolean = highlightedRange != null

    fun set(range: DocumentRange?) {
        var newRange = range
        if (newRange?.isPoint == true){
            newRange = null
        }
        if (highlightedRange == newRange) return
        if (newRange == null)
        {
            val oldRange = highlightedRange!!
            highlightedRange = null
            component.damageViewArea(oldRange)
            return
        }
        if (highlightedRange == null)
        {
            highlightedRange = newRange
            component.damageViewArea(newRange)
            return
        }
        val oldRange = highlightedRange!!
        highlightedRange = newRange
        if (newRange.begin == oldRange.begin) {
            component.damageViewArea(DocumentRange.safeCreate(oldRange.end, newRange.end))
        } else if (newRange.end == oldRange.end) {
            component.damageViewArea(DocumentRange.safeCreate(oldRange.begin, newRange.begin))
        } else {
            component.damageViewArea(oldRange)
            component.damageViewArea(newRange)
        }
    }

    fun getHighlightedRange(range: DocumentRange): DocumentRange? {
        val intersectedRange = highlightedRange?.intersect(range)
        if (intersectedRange?.isPoint == false)
            return intersectedRange
        return null
    }

}