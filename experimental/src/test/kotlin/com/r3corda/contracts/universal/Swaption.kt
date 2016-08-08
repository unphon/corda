package com.r3corda.contracts.universal

import com.r3corda.core.contracts.Frequency
import java.math.BigDecimal

/**
 * Created by sofusmortensen on 28/06/16.
 */

// Swaption



class Swaption {

    val notional = 10.M * USD
    val coupon = 1.5

    val dreary_contract =
            (wileECoyote or roadRunner).may {
                "proceed".givenThat(after("01/07/2015")) {
                    wileECoyote.gives(roadRunner, libor( notional, "01/04/2015", "01/07/2015" ) )
                    roadRunner.gives(wileECoyote, interest( notional, "act/365", coupon, "01/04/2015", "01/07/2015" ) )
                    (wileECoyote or roadRunner).may {
                        "proceed".givenThat(after("01/10/2015")) {
                            wileECoyote.gives(roadRunner, libor( notional, "01/07/2015", "01/10/2015" ) )
                            roadRunner.gives(wileECoyote, interest( notional, "act/365", coupon,  "01/07/2015", "01/10/2015" ) )

                            (wileECoyote or roadRunner).may {
                                // etc ...
                            }
                        }
                    } or roadRunner.may {
                        "cancel".anytime {
                            roadRunner.gives( wileECoyote, 10.K * USD )
                        }
                    }
                }
            } or roadRunner.may {
                "cancel".anytime {
                    roadRunner.gives( wileECoyote, 10.K * USD )
                }
            }


    val elegant_contract = rollout( "01/04/2015", "01/04/2025", Frequency.Quarterly ) {
        (wileECoyote or roadRunner).may {
            "proceed".givenThat(after(start)) {
                wileECoyote.gives(roadRunner, libor( notional, start, end ) )
                roadRunner.gives(wileECoyote, interest( notional, "act/365", coupon,  start, end ) )
                next()
            }
        } or roadRunner.may {
            "cancel".anytime {
                roadRunner.gives( wileECoyote, 10.K * USD )
            }
        }
    }


    val strike = 1.2
    val tarf = rollout( "01/04/2015", "01/04/2016", Frequency.Quarterly, object {
        val cap = Foo.CurrencyAmount
    }) {
        roadRunner.may {
            "exercise".givenThat(before(start)) {
                val payout = (EUR / USD - strike) * notional

                wileECoyote.gives(roadRunner, payout)

                next(vars.cap to get(vars.cap) - payout)
            }
        } or (roadRunner or wileECoyote).may {
            "proceed".givenThat(after(start)) {
                next()
            }
        }
    }

    val tarf2 = rollout( "01/04/2015", "01/04/2016", Frequency.Quarterly, object { val uses = Foo.Counter } ) {
        val z = get(vars.uses)

        next( vars.uses to z - 1)
    }

}