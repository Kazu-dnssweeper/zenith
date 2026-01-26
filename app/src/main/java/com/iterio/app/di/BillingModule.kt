package com.iterio.app.di

import android.content.Context
import com.iterio.app.data.billing.BillingClientWrapper
import com.iterio.app.data.billing.LocalPurchaseVerifier
import com.iterio.app.data.billing.PurchaseVerifier
import com.iterio.app.data.billing.SignatureVerifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides
    @Singleton
    fun provideBillingClientWrapper(
        @ApplicationContext context: Context
    ): BillingClientWrapper {
        return BillingClientWrapper(context)
    }

    @Provides
    @Singleton
    fun provideSignatureVerifier(): SignatureVerifier {
        return SignatureVerifier()
    }

    @Provides
    @Singleton
    fun providePurchaseVerifier(
        signatureVerifier: SignatureVerifier
    ): PurchaseVerifier {
        return LocalPurchaseVerifier(signatureVerifier)
    }
}
