package com.jjavaeditor.component

import com.jjavaeditor.document.BracketMatcher
import com.jjavaeditor.document.DocumentPoint
import com.jjavaeditor.document.DocumentRange

class BracketsHighlighter(private val component: JJavaTextArea) {

    private var bracketsHighlightInfo : BracketsHighlightInfo? = null

    data class BracketsHighlightInfo(val open: DocumentRange, val close: DocumentRange){
        fun getRange() : DocumentRange = DocumentRange(open.begin, close.begin)
    }

    fun searchMatchedBracket(point : DocumentPoint){
        setHighlighter(BracketMatcher.getBracketsPair(component.doc, point, bracketsHighlightInfo?.getRange()))
    }

    fun setHighlighter(newRange: DocumentRange?) {
        var info : BracketsHighlightInfo? = null
        if (newRange != null)
            info= BracketsHighlightInfo(newRange.begin.toSingleSymbolRange(), newRange.end.toSingleSymbolRange())
        val oldInfo = bracketsHighlightInfo
        bracketsHighlightInfo = info
        if (oldInfo != null){
            component.damageViewArea(oldInfo.open)
            component.damageViewArea(oldInfo.close)
        }
        if (info != null){
            component.damageViewArea(info.open)
            component.damageViewArea(info.close)
        }
    }

    fun getMatchedBrackets(): BracketsHighlightInfo? {
        return bracketsHighlightInfo
    }
}