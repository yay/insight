package com.vitalyk.insight.main

data class TaxBracket(
    val rate: Double,
    val range: IntRange
)

data class EffectiveTax(
    val abs: Double,
    val pct: Double
)

val usaSingleFilerTaxBrackets2017: Array<TaxBracket> = arrayOf(
    TaxBracket(10.0, 0..9_325),
    TaxBracket(15.0, 9_325..37_950),
    TaxBracket(25.0, 37_950..91_900),
    TaxBracket(28.0, 91_900..191_650),
    TaxBracket(33.0, 191_650..416_700),
    TaxBracket(35.0, 416_700..418_400),
    TaxBracket(39.6, 418_400..Int.MAX_VALUE)
)

val usaSingleFilerTaxBrackets2018: Array<TaxBracket> = arrayOf(
    TaxBracket(10.0, 0..9_525),
    TaxBracket(12.0, 9_525..38_700),
    TaxBracket(22.0, 38_700..70_000),
    TaxBracket(24.0, 70_000..160_000),
    TaxBracket(32.0, 160_000..200_000),
    TaxBracket(35.0, 200_000..500_000),
    TaxBracket(38.5, 500_000..Int.MAX_VALUE)
)

fun intersect(a: IntRange, b: IntRange): IntRange =
    Math.max(a.first, b.first)..Math.min(a.last, b.last)

fun calculateTax(income: Int, brackets: Array<TaxBracket>): EffectiveTax {
    var taxAbs = 0.0
    val earnings = 0..income

    for (bracket in brackets) {
        val range = intersect(bracket.range, earnings)
        val delta = range.last - range.first
        if (delta > 0) {
            taxAbs += delta * bracket.rate / 100
        }
    }
    return EffectiveTax(taxAbs, taxAbs / income * 100)
}

fun trumpTaxBillDiff() {
    for (income in 30_000..600_000 step 30_000) {
        val oldTax = calculateTax(income, usaSingleFilerTaxBrackets2017)
        val newTax = calculateTax(income, usaSingleFilerTaxBrackets2018)
        println("$income -> $oldTax")
        println("$income -> $newTax")
        println("Delta: ${newTax.pct - oldTax.pct}\n")
    }
}

fun main(args: Array<String>) {
    trumpTaxBillDiff()
}