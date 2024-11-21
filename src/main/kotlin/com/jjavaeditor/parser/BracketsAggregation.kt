package com.jjavaeditor.parser

class BracketsAggregation(
    var levelCumulata: Short = 0,
    private var maxDrawdown: Short = 0
) {

    fun registerBracket(isOpenBracket: Boolean) {
        if (isOpenBracket) {
            levelCumulata++
        } else {
            levelCumulata--
            if (levelCumulata < maxDrawdown)
                maxDrawdown = levelCumulata
        }
    }

    fun containsMatchingBracket(bracketLevel: Int): Boolean {
        if (bracketLevel > 0) return bracketLevel <= -maxDrawdown
         return bracketLevel >= maxDrawdown - levelCumulata
    }
}