package com.zenith.app.ui.screens.premium

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenith.app.domain.model.PremiumFeature
import com.zenith.app.domain.model.SubscriptionType
import com.zenith.app.domain.usecase.BillingUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val subscriptionStatus by viewModel.subscriptionStatus.collectAsState()
    val products by viewModel.products.collectAsState()
    val purchaseState by viewModel.purchaseState.collectAsState()

    var selectedPlan by remember { mutableStateOf<SubscriptionType?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // 購入状態のハンドリング
    LaunchedEffect(purchaseState) {
        when (val state = purchaseState) {
            is PremiumViewModel.PurchaseState.Success -> {
                snackbarHostState.showSnackbar("購入が完了しました")
                viewModel.resetPurchaseState()
            }
            is PremiumViewModel.PurchaseState.Restored -> {
                snackbarHostState.showSnackbar("購入を復元しました")
                viewModel.resetPurchaseState()
            }
            is PremiumViewModel.PurchaseState.NoPurchasesFound -> {
                snackbarHostState.showSnackbar("復元可能な購入が見つかりませんでした")
                viewModel.resetPurchaseState()
            }
            is PremiumViewModel.PurchaseState.AlreadyOwned -> {
                snackbarHostState.showSnackbar("すでに購入済みです。「購入を復元」をお試しください")
                viewModel.resetPurchaseState()
            }
            is PremiumViewModel.PurchaseState.Pending -> {
                snackbarHostState.showSnackbar("購入処理が保留中です")
                viewModel.resetPurchaseState()
            }
            is PremiumViewModel.PurchaseState.Error -> {
                snackbarHostState.showSnackbar("エラー: ${state.message}")
                viewModel.resetPurchaseState()
            }
            else -> {}
        }
    }

    // エラーハンドリング
    LaunchedEffect(Unit) {
        viewModel.billingError.collect { error ->
            val message = when (error) {
                is PremiumViewModel.BillingError.ProductLoadFailed ->
                    "商品情報の取得に失敗しました"
                is PremiumViewModel.BillingError.PurchaseFailed ->
                    "購入処理に失敗しました"
                is PremiumViewModel.BillingError.RestoreFailed ->
                    "購入の復元に失敗しました"
                is PremiumViewModel.BillingError.AlreadyOwned ->
                    "すでに購入済みです"
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    // 商品情報からプラン情報を取得
    fun getProductPrice(type: SubscriptionType): String {
        return products.find { it.type == type }?.price ?: when (type) {
            SubscriptionType.MONTHLY -> "¥480"
            SubscriptionType.QUARTERLY -> "¥1,200"
            SubscriptionType.HALF_YEARLY -> "¥2,000"
            SubscriptionType.YEARLY -> "¥3,000"
            SubscriptionType.LIFETIME -> "¥4,000"
            SubscriptionType.FREE -> ""
        }
    }

    val isProcessing = purchaseState is PremiumViewModel.PurchaseState.Processing ||
            purchaseState is PremiumViewModel.PurchaseState.Restoring ||
            purchaseState is PremiumViewModel.PurchaseState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ZENITH Premium",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "すべての機能をアンロック",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // トライアルボタン（利用可能な場合）
            if (subscriptionStatus.canStartTrial) {
                Button(
                    onClick = { viewModel.startTrial() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("7日間無料で試す")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Premium機能一覧
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Premium機能",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    PremiumFeature.entries.forEach { feature ->
                        FeatureRow(feature.titleJa)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // プラン選択
            Text(
                text = "プランを選択",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            PlanCard(
                title = "月額プラン",
                price = getProductPrice(SubscriptionType.MONTHLY),
                period = "/月",
                isSelected = selectedPlan == SubscriptionType.MONTHLY,
                onClick = { selectedPlan = SubscriptionType.MONTHLY }
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlanCard(
                title = "3ヶ月プラン",
                price = getProductPrice(SubscriptionType.QUARTERLY),
                period = "/3ヶ月",
                badge = "17%お得",
                monthlyEquivalent = "¥400/月",
                isSelected = selectedPlan == SubscriptionType.QUARTERLY,
                onClick = { selectedPlan = SubscriptionType.QUARTERLY }
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlanCard(
                title = "6ヶ月プラン",
                price = getProductPrice(SubscriptionType.HALF_YEARLY),
                period = "/6ヶ月",
                badge = "31%お得",
                monthlyEquivalent = "¥333/月",
                isSelected = selectedPlan == SubscriptionType.HALF_YEARLY,
                onClick = { selectedPlan = SubscriptionType.HALF_YEARLY }
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlanCard(
                title = "年額プラン",
                price = getProductPrice(SubscriptionType.YEARLY),
                period = "/年",
                badge = "おすすめ・48%お得",
                monthlyEquivalent = "¥250/月",
                isRecommended = true,
                isSelected = selectedPlan == SubscriptionType.YEARLY,
                onClick = { selectedPlan = SubscriptionType.YEARLY }
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlanCard(
                title = "買い切り",
                price = getProductPrice(SubscriptionType.LIFETIME),
                period = "",
                badge = "永久利用",
                isSelected = selectedPlan == SubscriptionType.LIFETIME,
                onClick = { selectedPlan = SubscriptionType.LIFETIME }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 購入ボタン
            Button(
                onClick = {
                    selectedPlan?.let { plan ->
                        activity?.let { viewModel.purchase(it, plan) }
                    }
                },
                enabled = selectedPlan != null && !isProcessing && activity != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("購入する")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 購入復元ボタン
            TextButton(
                onClick = { viewModel.restorePurchases() },
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("購入を復元")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "購入はGoogle Playを通じて処理されます",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    period: String,
    badge: String? = null,
    monthlyEquivalent: String? = null,
    isRecommended: Boolean = false,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardColors = if (isSelected) {
        CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    } else if (isRecommended) {
        CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
        )
    } else {
        CardDefaults.outlinedCardColors()
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isRecommended -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = cardColors,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor),
            width = if (isRecommended || isSelected) 2.dp else 1.dp
        )
    ) {
        Column {
            // おすすめバナー
            if (isRecommended) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            text = "おすすめ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (badge != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    // 月額換算表示
                    if (monthlyEquivalent != null) {
                        Text(
                            text = monthlyEquivalent,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = price,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (period.isNotEmpty()) {
                            Text(
                                text = period,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
