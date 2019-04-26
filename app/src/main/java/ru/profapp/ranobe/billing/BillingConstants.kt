package ru.profapp.ranobe.billing


import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.SkuType
import java.util.*

/**
 * Static fields and methods useful for billing
 */
object BillingConstants {
    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    val SKU_PREMIUM = "ru.profapp.ranobe.premium"


    // SKU for our subscription (infinite gas)
    val SKU_GOLD_MONTHLY = "gold_monthly"
    val SKU_GOLD_YEARLY = "gold_yearly"

    private val IN_APP_SKUS = arrayOf(SKU_PREMIUM)
    private val SUBSCRIPTIONS_SKUS = arrayOf(SKU_GOLD_MONTHLY, SKU_GOLD_YEARLY)

    /**
     * Returns the list of all SKUs for the billing type specified
     */
    fun getSkuList(@BillingClient.SkuType billingType: String): List<String> {
        return if (billingType === SkuType.INAPP) Arrays.asList(*IN_APP_SKUS)
        else Arrays.asList(*SUBSCRIPTIONS_SKUS)
    }
}