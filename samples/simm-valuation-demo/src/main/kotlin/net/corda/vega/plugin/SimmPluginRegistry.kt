package net.corda.vega.plugin

import com.google.common.collect.Ordering
import com.opengamma.strata.basics.currency.Currency
import com.opengamma.strata.basics.currency.CurrencyAmount
import com.opengamma.strata.basics.currency.MultiCurrencyAmount
import com.opengamma.strata.basics.date.Tenor
import com.opengamma.strata.collect.array.DoubleArray
import com.opengamma.strata.market.curve.CurveName
import com.opengamma.strata.market.param.CurrencyParameterSensitivities
import com.opengamma.strata.market.param.CurrencyParameterSensitivity
import com.opengamma.strata.market.param.TenorDateParameterMetadata
import net.corda.core.node.CordaPluginRegistry
import net.corda.core.serialization.SerializationCustomization
import net.corda.vega.analytics.CordaMarketData
import net.corda.vega.analytics.InitialMarginTriple
import net.corda.webserver.services.WebServerPluginRegistry

/**
 * [SimmService] is the object that makes available the flows and services for the Simm agreement / evaluation flow.
 * It is loaded via discovery - see [CordaPluginRegistry].
 * It is also the object that enables a human usable web service for demo purpose
 * It is loaded via discovery see [WebServerPluginRegistry].
 */
class SimmPluginRegistry : CordaPluginRegistry() {
    override fun customizeSerialization(custom: SerializationCustomization): Boolean {
        custom.apply {
            // OpenGamma classes.
            addToWhitelist(MultiCurrencyAmount::class.java)
            addToWhitelist(Ordering.natural<Comparable<Any>>().javaClass)
            addToWhitelist(CurrencyAmount::class.java)
            addToWhitelist(Currency::class.java)
            addToWhitelist(InitialMarginTriple::class.java)
            addToWhitelist(CordaMarketData::class.java)
            addToWhitelist(CurrencyParameterSensitivities::class.java)
            addToWhitelist(CurrencyParameterSensitivity::class.java)
            addToWhitelist(DoubleArray::class.java)
            addToWhitelist(CurveName::class.java)
            addToWhitelist(TenorDateParameterMetadata::class.java)
            addToWhitelist(Tenor::class.java)
        }
        return true
    }
}
