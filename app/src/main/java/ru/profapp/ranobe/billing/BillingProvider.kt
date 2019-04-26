package ru.profapp.ranobe.billing


/**
 * An interface that provides an access to BillingLibrary methods
 */
interface BillingProvider {
    val billingManager: BillingManager
    val isPremiumPurchased: Boolean
    val isGoldMonthlySubscribed: Boolean
    val isTankFull: Boolean
    val isGoldYearlySubscribed: Boolean
}