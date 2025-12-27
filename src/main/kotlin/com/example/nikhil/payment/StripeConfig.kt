package com.example.nikhil.payment

import com.stripe.Stripe
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

/**
 * Stripe Configuration
 * Initializes Stripe with API key
 */
@Configuration
class StripeConfig {

    @Value("\${stripe.api.key}")
    private lateinit var stripeApiKey: String

    @PostConstruct
    fun init() {
        Stripe.apiKey = stripeApiKey
    }
}