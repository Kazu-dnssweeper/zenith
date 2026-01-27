# Iterio - アプリ仕様書

## 1. アプリ概要

### アプリ名
**Iterio**

### コンセプト
ポモドーロテクニックとエビングハウス忘却曲線を組み合わせた、集中学習支援アプリ。フォーカスモード（アプリブロック機能）により、スマートフォン依存を防ぎながら効率的な学習を実現する。

### ターゲットユーザー
- 受験生（中学・高校・大学受験）
- 資格試験受験者
- 集中力を高めたい学習者
- スマートフォン依存から脱却したい人

---

## 2. 技術スタック

### 言語・SDK
| 項目 | 値 |
|------|-----|
| 言語 | Kotlin |
| Minimum SDK | 26 (Android 8.0) |
| Target SDK | 35 |
| Compile SDK | 35 |
| Java Version | 17 |

### アーキテクチャ
**MVVM + Repository Pattern + Clean Architecture + Result<T, DomainError>**

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer                          │
│  (Composable Screens + ViewModels)                  │
├─────────────────────────────────────────────────────┤
│                  Domain Layer                        │
│  (Use Cases + Repository Interfaces + Models)       │
│  (Result<T, DomainError> パターン)                   │
├─────────────────────────────────────────────────────┤
│                   Data Layer                         │
│  (Repository Impl + DAO + Room Database)            │
│  (Encryption + Cloud Backup + Billing)              │
├─────────────────────────────────────────────────────┤
│                  Service Layer                       │
│  (TimerService + FocusModeService + LockOverlay     │
│   + BgmService + ReviewReminderWorker)              │
└─────────────────────────────────────────────────────┘
```

### Result<T, DomainError> パターン

全リポジトリ・ユースケースで統一的なエラーハンドリングを実施:

```kotlin
sealed class Result<out T, out E> {
    data class Success<T>(val value: T) : Result<T, Nothing>()
    data class Failure<E>(val error: E) : Result<Nothing, E>()
}

sealed class DomainError {
    data class DatabaseError(val message: String) : DomainError()
    data class NetworkError(val message: String) : DomainError()
    data class ValidationError(val message: String) : DomainError()
    data class UnknownError(val message: String) : DomainError()
}
```

### 使用ライブラリ

| カテゴリ | ライブラリ |
|---------|-----------|
| UI | Jetpack Compose, Material Design 3 |
| Navigation | Navigation Compose |
| Database | Room |
| DI | Hilt |
| 非同期処理 | Kotlin Coroutines, Flow |
| バックグラウンド | WorkManager |
| データ保存 | DataStore Preferences |
| シリアライズ | Gson, Kotlinx Serialization |
| Widget | Glance |
| 課金 | Google Play Billing |
| クラウド | Google Drive API, Google Play Services Auth |
| 暗号化 | Android KeyStore (AES-256-GCM) |
| ログ | Timber |

---

## 3. プロジェクト構成

### ディレクトリ構造

```
app/src/main/java/com/iterio/app/
├── config/                 # アプリ設定定数
├── data/
│   ├── billing/            # Google Play Billing
│   ├── cloud/              # クラウドバックアップ
│   ├── encryption/         # AES-256-GCM暗号化
│   ├── local/
│   │   ├── entity/         # Roomエンティティ (8)
│   │   ├── dao/            # Data Access Objects (7)
│   │   ├── converter/      # 型コンバーター
│   │   ├── migration/      # DBマイグレーション (3→4→5→6)
│   │   └── IterioDatabase.kt
│   ├── mapper/             # Entity ↔ Domain マッパー
│   └── repository/         # リポジトリ実装 (9)
├── domain/
│   ├── common/             # Result, DomainError
│   ├── model/              # ドメインモデル
│   ├── repository/         # リポジトリインターフェース (9)
│   └── usecase/            # ユースケース (8)
├── di/                     # Hilt DIモジュール
├── ui/
│   ├── bgm/                # BGM管理
│   ├── components/         # 共通UIコンポーネント
│   ├── navigation/         # ナビゲーション設定
│   ├── premium/            # プレミアム管理
│   ├── screens/            # 各画面 (8ディレクトリ)
│   │   ├── home/
│   │   ├── tasks/
│   │   ├── timer/
│   │   ├── stats/
│   │   ├── calendar/
│   │   ├── settings/
│   │   ├── backup/
│   │   └── premium/
│   ├── theme/              # テーマ・カラー定義
│   └── MainActivity.kt
├── service/                # サービス群 (5)
├── util/                   # ユーティリティ
├── widget/                 # ホームウィジェット
├── worker/                 # WorkManager (2)
└── IterioApplication.kt
```

### 主要ファイルの役割

| ファイル | 役割 |
|---------|------|
| `IterioDatabase.kt` | Room Database定義（version 6） |
| `TimerService.kt` | ポモドーロタイマーのForeground Service |
| `FocusModeService.kt` | アプリブロックのAccessibility Service |
| `LockOverlayService.kt` | 完全ロックモードのオーバーレイ表示 |
| `BgmService.kt` | BGMバックグラウンド再生 |
| `ReviewReminderWorker.kt` | 復習リマインダーのWorkManager |
| `EncryptionManager.kt` | AES-256-GCMバックアップ暗号化 |
| `BillingUseCase.kt` | Google Play Billing統合 |
| `CloudBackupUseCase.kt` | Google Driveバックアップ |

---

## 4. データベース設計

### Roomバージョン
**Version 6** (マイグレーション: 3→4→5→6)

| マイグレーション | 内容 |
|----------------|------|
| 3→4 | スケジュール機能追加（ScheduleType, repeatDays, deadlineDate, specificDate） |
| 4→5 | 復習設定・最終学習日追加（reviewCount, reviewEnabled, lastStudiedAt） |
| 5→6 | 科目テーブル追加（subjects: 単独科目管理） |

### エンティティ一覧

#### SubjectGroupEntity（科目グループ）
```kotlin
@Entity(tableName = "subject_groups")
data class SubjectGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#00838F",
    val displayOrder: Int,
    val createdAt: LocalDateTime
)
```

#### TaskEntity（タスク）
```kotlin
@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = SubjectGroupEntity::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val name: String,
    val progressNote: String? = null,
    val progressPercent: Int? = null,
    val nextGoal: String? = null,
    val workDurationMinutes: Int? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val scheduleType: String = "NONE",
    val repeatDaysJson: String = "[]",
    val deadlineDate: LocalDate? = null,
    val specificDate: LocalDate? = null,
    val lastStudiedAt: LocalDateTime? = null,
    val reviewCount: Int? = null,
    val reviewEnabled: Boolean = true
)
```

#### StudySessionEntity（学習セッション）
```kotlin
@Entity(
    tableName = "study_sessions",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime? = null,
    val workDurationMinutes: Int = 0,
    val plannedDurationMinutes: Int = 25,
    val cyclesCompleted: Int = 0,
    val wasInterrupted: Boolean = false,
    val notes: String? = null
)
```

#### ReviewTaskEntity（復習タスク）
```kotlin
@Entity(tableName = "review_tasks")
data class ReviewTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studySessionId: Long,
    val taskId: Long,
    val scheduledDate: LocalDate,
    val reviewNumber: Int,  // 1-6
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime
)
```

#### SettingsEntity（設定）
```kotlin
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)
```

#### DailyStatsEntity（日別統計）
```kotlin
@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val date: LocalDate,
    val totalStudyMinutes: Int,
    val sessionCount: Int,
    val subjectBreakdownJson: String
)
```

#### SubjectEntity（科目）
```kotlin
@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val groupId: Long,
    val colorHex: String = "#00838F",
    val createdAt: LocalDateTime
)
```

#### ReviewTaskWithDetails（結合エンティティ）
```kotlin
// review_tasks + tasks + subject_groups のJOINビュー
data class ReviewTaskWithDetails(
    // ReviewTask fields + taskName, groupName
)
```

### テーブル関連図

```
subject_groups (1) ──< tasks (N)
                          │
                          │ (1)
                          ▼
                    study_sessions (N)
                          │
                          │ (1)
                          ▼
                    review_tasks (N)

subjects (N) >── subject_groups (1)
```

---

## 5. 機能一覧

### ポモドーロタイマー
| 項目 | 状況 | 詳細 |
|------|------|------|
| 基本タイマー | ✅ 完了 | 作業・短休憩・長休憩のサイクル |
| カスタム作業時間 | ✅ 完了 | 1-180分（タスク単位・設定単位） |
| カスタム休憩時間 | ✅ 完了 | 短休憩3-15分、長休憩10-30分 |
| サイクル数設定 | ✅ 完了 | 2-10サイクル（セッション開始時に変更可） |
| 通知表示 | ✅ 完了 | Foreground Service通知 |
| 一時停止/再開 | ✅ 完了 | |
| スキップ | ✅ 完了 | フォーカスモード時は非表示 |
| 自動ループ | ✅ 完了 | Premium機能: サイクル自動繰り返し |

### フォーカスモード（アプリブロック）
| 項目 | 状況 | 詳細 |
|------|------|------|
| Accessibility Service | ✅ 完了 | アプリ切り替え検出 |
| アプリへの強制復帰 | ✅ 完了 | 他アプリ起動時にIterioに戻る |
| システムアプリ許可 | ✅ 完了 | 電話・設定・ランチャーは許可 |
| 通常モード | ✅ 完了 | 緊急時は解除可能 |
| 許可アプリ管理 | ✅ 完了 | ユーザーが許可アプリを選択可能 |

### 完全ロックモード
| 項目 | 状況 | 詳細 |
|------|------|------|
| オーバーレイ表示 | ✅ 完了 | 半透明のフルスクリーンオーバーレイ |
| タイマー表示 | ✅ 完了 | オーバーレイ上に残り時間表示 |
| 停止ボタン無効化 | ✅ 完了 | タイマー終了まで解除不可 |
| 権限リクエスト | ✅ 完了 | SYSTEM_ALERT_WINDOW権限 |
| セッション単位切替 | ✅ 完了 | 開始時にON/OFF選択可能 |

### 学習記録・タスク管理
| 項目 | 状況 | 詳細 |
|------|------|------|
| 科目グループ作成 | ✅ 完了 | カラー選択（8色）可能 |
| タスク作成 | ✅ 完了 | タスク固有の作業時間設定可 |
| 進捗管理 | ✅ 完了 | パーセンテージ・メモ・次の目標 |
| セッション履歴 | ✅ 完了 | 学習時間・サイクル数記録 |
| スケジュール管理 | ✅ 完了 | 繰り返し・期限・特定日 |
| タスク別復習設定 | ✅ 完了 | 復習回数・有効/無効切替 |

### 忘却曲線（復習スケジュール）
| 項目 | 状況 | 詳細 |
|------|------|------|
| 自動スケジュール生成 | ✅ 完了 | セッション完了時に生成 |
| 復習間隔 | ✅ 完了 | 1, 3, 7, 14, 30, 60日後 |
| 完了マーク | ✅ 完了 | |
| リスケジュール | ✅ 完了 | |
| リマインダー通知 | ✅ 完了 | WorkManagerで毎朝9時 |
| 復習タスク管理 | ✅ 完了 | 一覧表示・一括削除 |
| Premium復習オプション | ✅ 完了 | 復習回数カスタマイズ (2-6回) |

### カレンダー・統計
| 項目 | 状況 | 詳細 |
|------|------|------|
| 月間カレンダー | ✅ 完了 | ヒートマップ表示（5段階） |
| 連続学習日数 | ✅ 完了 | ストリーク表示 |
| 週間統計 | ✅ 完了 | 棒グラフ表示 |
| 月間合計 | ✅ 完了 | |
| 日別セッション詳細 | ✅ 完了 | タスク別の復習予定表示 |

### ウィジェット
| 項目 | 状況 | 詳細 |
|------|------|------|
| Glanceウィジェット | ✅ 完了 | 2x2サイズ |
| 学習時間表示 | ✅ 完了 | 本日の学習時間 |
| ストリーク表示 | ✅ 完了 | 連続日数 |
| タイマー状態表示 | ✅ 完了 | 実行中のタイマー表示 |

### BGM機能
| 項目 | 状況 | 詳細 |
|------|------|------|
| バックグラウンド再生 | ✅ 完了 | BgmService (MediaPlayer) |
| 曲選択 | ✅ 完了 | プリセットBGMトラック選択UI |
| 音量調整 | ✅ 完了 | スライダーで0-100%調整 |
| 自動再生 | ✅ 完了 | タイマー開始時に自動再生 |
| BgmManager | ✅ 完了 | 選択トラック・音量・自動再生をFlow管理 |

### バックアップ
| 項目 | 状況 | 詳細 |
|------|------|------|
| ローカルバックアップ | ✅ 完了 | JSONエクスポート/インポート |
| クラウドバックアップ | ✅ 完了 | Google Drive連携 |
| バックアップ暗号化 | ✅ 完了 | AES-256-GCM (Android KeyStore) |
| 自動バックアップ | ✅ 完了 | BackupUseCase統合 |
| バックアップ画面 | ✅ 完了 | 専用UI (BackupScreen) |

### 暗号化
| 項目 | 状況 | 詳細 |
|------|------|------|
| AES-256-GCM | ✅ 完了 | Android KeyStore管理 |
| NONCE (96bit) | ✅ 完了 | 毎回ランダム生成 |
| 暗号化判定 | ✅ 完了 | JSON/バイナリ自動判定 |
| EncryptionException | ✅ 完了 | カスタム例外 |

### Premium課金
| 項目 | 状況 | 詳細 |
|------|------|------|
| Google Play Billing | ✅ 完了 | BillingClientWrapper |
| サブスクリプション | ✅ 完了 | 月額・四半期・半年・年額 |
| 買い切り | ✅ 完了 | Lifetime購入 |
| 購入検証 | ✅ 完了 | SignatureVerifier + LocalPurchaseVerifier |
| 購入復元 | ✅ 完了 | restorePurchases |
| Premium画面 | ✅ 完了 | PremiumScreen + PremiumViewModel |
| トライアル | ✅ 完了 | 無料試用期間 |
| Premium機能管理 | ✅ 完了 | PremiumManager + PremiumFeature enum |

### 多言語対応
| 項目 | 状況 | 詳細 |
|------|------|------|
| 日本語 | ✅ 完了 | デフォルト言語 |
| 英語 | ✅ 完了 | values-en/strings.xml |
| 言語切替 | ✅ 完了 | LocaleManager + AppCompatDelegate |

---

## 6. 画面構成

### 画面一覧

| 画面 | ルート | 役割 |
|------|--------|------|
| ホーム | `home` | 今日の学習状況・クイックスタート |
| タスク | `tasks` | 科目グループ・タスク管理 |
| タイマー | `timer/{taskId}` | ポモドーロタイマー |
| 統計 | `stats` | 学習統計・グラフ |
| カレンダー | `calendar` | 月間学習記録 |
| 設定 | `settings` | アプリ設定 |
| Premium | `premium` | Premium購入画面 |
| バックアップ | `backup` | バックアップ管理 |
| 許可アプリ | `allowed_apps` | フォーカスモード許可アプリ管理 |

### ナビゲーション

```
BottomNavigationBar
├── ホーム (Home)
├── タスク (Tasks)
├── カレンダー (Calendar)
├── 統計 (Stats)
└── 設定 (Settings)

ホーム画面 → タイマー画面（taskIdで遷移）
タスク画面 → タイマー画面（taskIdで遷移）
カレンダー画面 → タイマー画面（taskIdで遷移）
設定画面 → Premium画面
設定画面 → バックアップ画面
設定画面 → 許可アプリ画面
バックアップ画面 → Premium画面
```

### 主要UIコンポーネント

| コンポーネント | 用途 |
|---------------|------|
| `IterioTopBar` | カスタムトップバー |
| `IterioCard` | テーマ適用カード |
| `CircularTimer` | 円形タイマー表示 |
| `PhaseIndicator` | フェーズ・サイクル表示 |
| `TimerControls` | タイマー操作ボタン |
| `LoadingIndicator` | ローディング表示 |
| `ReviewTaskManagementComponents` | 復習タスク管理UI |

---

## 7. 権限・サービス

### Android権限一覧

| 権限 | 用途 | 状況 |
|------|------|------|
| `FOREGROUND_SERVICE` | タイマーサービス | ✅ |
| `FOREGROUND_SERVICE_SPECIAL_USE` | 特殊用途サービス | ✅ |
| `POST_NOTIFICATIONS` | 通知表示 | ✅ |
| `VIBRATE` | バイブレーション | ✅ |
| `WAKE_LOCK` | 画面ON維持 | ✅ |
| `SYSTEM_ALERT_WINDOW` | オーバーレイ表示 | ✅ |
| `BIND_ACCESSIBILITY_SERVICE` | アプリブロック | ✅ |
| `INTERNET` | クラウドバックアップ・課金 | ✅ |
| `BILLING` | Google Play Billing | ✅ |

### サービス実装状況

| サービス | 種別 | 状況 |
|---------|------|------|
| `TimerService` | Foreground Service | ✅ 完了 |
| `FocusModeService` | Accessibility Service | ✅ 完了 |
| `LockOverlayService` | 通常Service | ✅ 完了 |
| `BgmService` | Foreground Service | ✅ 完了 |
| `ReviewReminderWorker` | WorkManager | ✅ 完了 |

---

## 8. デザイン

### カラーテーマ

| 用途 | カラーコード | 説明 |
|------|-------------|------|
| Primary | `#00838F` | Teal 700 |
| Accent | `#4DD0E1` | Cyan 300 |
| Background | `#121212` | ダークグレー |
| Surface | `#1E1E1E` | カード背景 |
| TextPrimary | `#DEFFFFFF` | 白87% |
| TextSecondary | `#99FFFFFF` | 白60% |
| Success | `#4CAF50` | 緑 |
| Warning | `#FF9800` | オレンジ |
| Error | `#E53935` | 赤 |

### ヒートマップカラー（5段階）

| レベル | 条件 | カラー |
|--------|------|--------|
| 0 | 0分 | `#2D2D2D` |
| 1 | 1-29分 | `#004D40` |
| 2 | 30-59分 | `#00695C` |
| 3 | 60-119分 | `#00838F` |
| 4 | 120分以上 | `#4DD0E1` |

### UIの方針
- **ダークテーマ**: 目に優しい暗色基調
- **Material Design 3**: 最新のデザインガイドライン
- **多言語UI**: 日本語・英語対応
- **シンプル**: 機能を直感的に使えるUI

---

## 9. マネタイズ設計

### 現在の状況
**✅ 実装済み** - Google Play Billing統合完了

### サブスクリプションプラン

| プラン | Product ID | 期間 |
|--------|-----------|------|
| 月額 | `iterio_premium_monthly` | 1ヶ月 |
| 四半期 | `iterio_premium_quarterly` | 3ヶ月 |
| 半年 | `iterio_premium_half_yearly` | 6ヶ月 |
| 年額 | `iterio_premium_yearly` | 1年 |
| 買い切り | `iterio_premium_lifetime` | 無期限 |

### Premium機能一覧

| 機能 | 無料版 | Premium版 |
|------|--------|-----------|
| 基本タイマー | ✅ | ✅ |
| フォーカスモード | ✅ | ✅ |
| 完全ロックモード | ✅ (通常) | ✅ (厳格モード) |
| BGM | 基本 | 全トラック |
| クラウドバックアップ | - | ✅ |
| 自動ループ | - | ✅ |
| 復習回数 | 2回固定 | 2-6回カスタマイズ |
| 統計詳細 | 基本 | 詳細 |

### 購入フロー
1. PremiumScreen表示 → プラン選択
2. BillingUseCase.startPurchase → Google Play購入フロー
3. PurchaseVerifier署名検証 → acknowledgePurchase
4. PremiumRepository.updateSubscription → ローカルDB更新
5. PremiumManager → Feature制御

---

## 10. 当初の計画から変更した点

### 変更1: 厳格モード → 完全ロックモード
- **変更内容**: 名称を「厳格モード」から「完全ロックモード」に変更
- **理由**: より直感的でわかりやすい名前にするため
- **追加実装**: フルスクリーンオーバーレイによる物理的なアプリブロック

### 変更2: タスク固有の作業時間
- **変更内容**: グローバル設定のみ → タスクごとに作業時間設定可能
- **理由**: 科目や内容によって最適な学習時間が異なるため

### 変更3: セッション開始時のカスタマイズ
- **変更内容**: 設定画面のみ → セッション開始ダイアログでも変更可能
- **理由**: 状況に応じて柔軟に変更したいニーズ
- **対象**: サイクル数、完全ロックモードのON/OFF

### 変更4: 作業時間スライダーの範囲
- **変更内容**: 15-60分 → 1-180分
- **理由**: 短時間の集中や長時間の作業にも対応するため
- **追加**: +/−ボタンによる微調整機能

### 変更5: Phase 2/3の前倒し実装
- **変更内容**: BGM、バックアップ、課金、多言語対応をPhase 1内で実装
- **理由**: コア機能との統合が必要で、段階的実装よりも一括実装の方が効率的だったため

---

## 11. 今後の開発フェーズ

### Phase 2（次期）

| タスク | 優先度 | 状況 |
|--------|--------|------|
| テストカバレッジ80%+ | 高 | 進行中 |
| 忘却曲線UI画面 | 高 | 計画中 |
| Detekt静的解析導入 | 中 | 計画中 |
| レビュー画面の改善 | 中 | 計画中 |

### Phase 3（将来）

| タスク | 優先度 |
|--------|--------|
| 学習グループ機能 | 低 |
| 通知のカスタマイズ | 低 |
| パフォーマンス最適化 | 低 |
| Wear OS対応 | 低 |

### 完了済み（当初Phase 2/3に計画）

| タスク | 状況 |
|--------|------|
| ~~BGM機能実装~~ | ✅ 完了 |
| ~~ローカルバックアップ~~ | ✅ 完了 |
| ~~クラウド同期（Google Drive）~~ | ✅ 完了 |
| ~~Premium課金機能~~ | ✅ 完了 |
| ~~多言語対応（英語）~~ | ✅ 完了 |
| ~~バックアップ暗号化~~ | ✅ 完了 |

### 現在のブロッカー・課題

| 課題 | 詳細 | 対応案 |
|------|------|--------|
| Accessibility Service有効化 | ユーザーが手動で有効化する必要あり | 初回起動時のガイド表示 |
| オーバーレイ権限 | 完全ロックモード使用時に権限必要 | ダイアログで案内実装済み |
| Google Play Console設定 | 公開鍵・OAuth設定が手動作業 | リリース前に設定 |

---

## 12. ファイル総数

| カテゴリ | 数 |
|---------|-----|
| Kotlinファイル (main) | 115 |
| テストファイル (unit) | 59 |
| XMLリソース | 多数 |
| 画面数 | 9 (8ディレクトリ + AllowedApps) |
| ViewModel | 9 |
| UseCase | 8 |
| サービス | 5 |
| Worker | 2 |
| Entity | 8 |
| DAO | 7 |
| Repository Interface | 9 |
| Repository Impl | 9 |
| Mapper | 5 |
| Migration | 3 |

---

## 13. テスト戦略

### テストカバレッジ目標: 80%+

| テスト種別 | ツール | 対象 |
|-----------|-------|------|
| Unit Test | JUnit + MockK | ViewModel, UseCase, Repository, Mapper |
| Flow Test | Turbine | StateFlow/SharedFlow検証 |
| Coroutine Test | kotlinx-coroutines-test | 非同期処理 |
| UI Test | Compose Test + Espresso | 画面操作検証 |
| Coverage | JaCoCo 0.8.12 | 80%ライン率閾値 |

### テスト用Fake実装

| Fake | 対象 |
|------|------|
| FakeSettingsRepository | 設定データ |
| FakeSubjectGroupRepository | 科目グループ |
| FakeTaskRepository | タスク |
| FakeReviewTaskRepository | 復習タスク |
| FakeStudySessionRepository | 学習セッション |
| FakeDailyStatsRepository | 日別統計 |
| FakePremiumRepository | Premium状態 |
| FakeBackupRepository | バックアップ |

---

*最終更新: 2026年1月27日*
*バージョン: 1.0.0 (Phase 1 完了 + Phase 2 進行中)*
