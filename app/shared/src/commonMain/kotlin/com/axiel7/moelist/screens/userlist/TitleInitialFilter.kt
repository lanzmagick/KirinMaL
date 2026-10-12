package com.axiel7.moelist.screens.userlist

/** Matches the first visible character, including punctuation before a title. */
enum class TitleInitialFilter(val label: String) {
    ALL(""),
    A("A"), B("B"), C("C"), D("D"), E("E"), F("F"), G("G"),
    H("H"), I("I"), J("J"), K("K"), L("L"), M("M"), N("N"),
    O("O"), P("P"), Q("Q"), R("R"), S("S"), T("T"), U("U"),
    V("V"), W("W"), X("X"), Y("Y"), Z("Z"),
    ONE("1"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5"),
    SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"), ZERO("0"),
    SYMBOLS("#");

    val isActive get() = this != ALL

    fun matches(title: String): Boolean {
        if (this == ALL) return true
        val first = title.firstOrNull { !it.isWhitespace() } ?: return false
        val upper = first.uppercaseChar()
        return if (this == SYMBOLS) upper !in 'A'..'Z' && first !in '0'..'9'
        else upper == label[0]
    }

    companion object {
        val letters = entries.filter { it != ALL && it.label[0] in 'A'..'Z' }
        val numbers = entries.filter { it != ALL && it.label[0] in '0'..'9' }
    }
}
