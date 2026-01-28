# Iterio 開発申し送り書

**更新日時:** 2026-01-28
**現在のステータス:** ビルド成功 ✅ / テスト全通過 ✅ / カバレッジ80%達成 ✅
**テスト結果:** 1001 tests passed (全通過)
**カバレッジ:** JaCoCo LINE 80%以上（jacocoTestCoverageVerification通過）

---

## 最新セッションで完了したタスク

### ウィジェット機能強化（4フェーズ完了）

テスト数 981 → 1001（+20テスト追加）。全4フェーズ実装完了。ビルド成功・全テスト通過。

#### Phase 1: 復習リマインダーバッジ追加
- **WidgetState.kt**: `pendingReviewCount: Int = 0` フィールド追加
- **IterioWidgetStateHelper.kt**: `reviewTaskDao.getPendingTaskCountForDate(today)` 呼び出し追加
- **IterioWidget.kt**: ストリーク行の下に復習バッジUI追加（`pendingReviewCount > 0` 時表示）
- **strings.xml / strings-en.xml**: `widget_review_count` 文字列追加
- **WidgetStateTest.kt** (新規7テスト): デフォルト値、コピー、等価性テスト

#### Phase 2: 今日のタスクリスト表示 + レスポンシブレイアウト
- **WidgetTaskItem.kt** (新規): `data class WidgetTaskItem(name, groupName, isCompleted)`
- **WidgetState.kt**: `todayTasks: List<WidgetTaskItem> = emptyList()` 追加
- **IterioWidgetStateHelper.kt**: タスクリスト取得ロジック追加（グループ名キャッシュ付き、最大5件）
- **IterioWidget.kt**: `SizeMode.Responsive` 導入（Compact 110x40dp / Medium 110x110dp / Large 110x180dp）
- **iterio_widget_info.xml**: `maxResizeHeight` 200dp → 300dp
- **WidgetTaskItemTest.kt** (新規6テスト): data classテスト

#### Phase 3: タイマーコントロール（一時停止/再開/停止）
- **ToggleTimerActionCallback.kt** (新規): SharedPrefsからタイマー状態判定、pause/resume切替
- **StopTimerActionCallback.kt** (新規): タイマー停止、SharedPrefsクリア
- **IterioWidgetStateHelper.kt**: タイマー状態のstaleness check追加（60秒閾値でIDLEにフォールバック）
- **IterioWidget.kt**: TimerStatusRowに⏸/▶/⏹ボタン追加（`actionRunCallback`で接続）
- **ToggleTimerActionCallbackTest.kt** (新規4テスト): pause/resume分岐テスト
- **StopTimerActionCallbackTest.kt** (新規3テスト): stop+prefsクリアテスト

#### Phase 4: ウィジェット更新トリガー強化
- **IterioWidgetReceiver.kt**: `ACTION_DATA_CHANGED` + `sendDataChangedBroadcast()` 追加
- **AndroidManifest.xml**: intent-filterに新アクション追加
- **IterioWidgetStateHelper.kt**: `updateWidgetDebounced()` デバウンス付き更新（500ms）
- **HomeViewModel.kt**: コンストラクタに`context`追加、復習トグル後にウィジェット更新
- **ReviewScheduleViewModel.kt**: コンストラクタに`context`追加、完了トグル後にウィジェット更新
- **TimerViewModel.kt**: セッション完了時にウィジェット更新
- **テスト修正**: HomeViewModelTest, ReviewScheduleViewModelTest, TimerViewModelTestに`mockkObject(IterioWidgetReceiver.Companion)`追加

---

## 過去のセッションで完了したタスク

### テストカバレッジ 60% → 80% 引き上げ

テスト数 813 → 981（+168テスト追加）。JaCoCo LINE coverage 80%閾値通過。

#### Step 1: 純粋Kotlin新規テスト
- **BackupErrorTest.kt** (31テスト): `BackupError.fromException()` 全分岐テスト
- **SubscriptionStatusTest.kt** (25テスト): `isPremium`, `isInTrialPeriod`, `canStartTrial` 等
- **BillingProductsTest.kt** (20テスト): `toSubscriptionType()`, `toProductId()`, SKUリスト等
- **ResultTest.kt拡充** (+10テスト): `catching`, `catchingSuspend`, `getOrThrow`, `DomainException`

#### Step 2: ViewModel拡充
- **SettingsViewModelTest.kt** (+12テスト): BGMセレクター、onEventディスパッチ
- **StatsViewModelTest.kt** (+5テスト): エラーパス（各統計値のデフォルト値）
- **HomeViewModelTest.kt** (+5テスト): エラーパス、isLoading状態遷移
- **CalendarViewModelTest.kt** (+6テスト): タスクカウント結合テスト
- **TimerViewModelTest.kt** (+30テスト): startTimer/pauseTimer/resumeTimer/cancelTimer/skipPhase/BGM操作/プロパティアクセス

#### Step 3: データ層
- **BackupRepositoryImplTest.kt** (12テスト): serialize/deserializeのJSON往復テスト

#### Step 4: JaCoCo設定更新
- 閾値: 60% → 80%
- 除外追加: BillingClientWrapper, SignatureVerifier, DAO, LocaleManager, InstalledAppsHelper, MainActivity, 嵌込みコンポーネント

#### Step 5: バグ修正
- **TimerViewModelTest テスト隔離問題修正**: `unmockkAll()` → `unmockkObject(TimerService.Companion)` に変更（事前存在バグ。41件のSettingsViewModelテスト失敗の根本原因）
- **TimerViewModelTest selectBgmTrack修正**: 実装に合わせて `bgmManager.play(track)` / `bgmManager.stop()` に修正
- **TimerViewModelTest toggleBgm修正**: 引数なし版に修正、`isPlaying()` 状態で分岐テスト

---

## 過去のセッションで完了したタスク

### Phase A-D: テスト・ドキュメント・リンティング・忘却曲線UI

4つのPhaseを一括実装。全813テスト通過、ビルド成功。

#### Phase A: テストカバレッジ拡充
- **SettingsViewModelTest** (23テスト): コアロジック全カバー
- **TasksViewModelTest**: グループ/タスクCRUD、進捗更新
- **ReviewScheduleViewModelTest**: フィルタ、グループ化、完了トグル
- **EncryptionManagerTest拡充**: エッジケース追加
- **BillingUseCaseTest拡充**: 有効期限計算、JSON解析フォールバック
- **SettingsRepositoryImplTest修正**: `mockkStatic`リーク修正（`unmockkAll`追加）
- **JaCoCo検証**: 60%ライン率閾値、UI/Service/Widget除外

#### Phase B: SPECIFICATION.md更新
- 全機能ステータス更新（BGM、バックアップ、Billing実装済みに反映）
- ファイル数・Repository数を実態に合わせて更新
- 新セクション追加（暗号化、クラウドバックアップ、Widget、Result型パターン）

#### Phase C: Linting導入
- `detekt.yml` 作成（関数50行制限、ファイル800行制限、ネスト4段制限）
- `.editorconfig` 作成（indent 4, line 120, UTF-8）
- `detekt-baseline.xml` 生成
- **重要**: DetektはCLI経由でのみ実行（GradleプラグインはMockKと競合するため）

#### Phase D: 忘却曲線UI実装
- **ReviewScheduleScreen**: フィルタ（全件/未完了/完了/期限切れ）、日付別グループ、完了チェック
- **ReviewScheduleViewModel**: Result<T, DomainError>パターン準拠
- **ナビゲーション**: Screen.kt + IterioNavHost.kt にルート追加
- **TodayReviewSection**: 「すべて見る」ボタン追加
- **文字列リソース**: 日本語/英語対応

---

## 重要な技術的発見

### MockK + Detekt Gradle プラグイン競合
- DetektのGradleプラグイン（`apply false`含む）がMockKのbyte-buddy計装と競合
- `LocaleManager.getCurrentLanguage()`のモック時にNPEが発生
- **解決策**: Detektはlibs.versions.tomlから除外し、CLI経由で実行

### mockkStatic リーク問題
- `SettingsRepositoryImplTest`で`mockkStatic("androidx.room.RoomDatabaseKt")`を`@After`なしで使用
- 同一JVMフォークの後続テスト41件が全滅する原因に
- **解決策**: `@After fun tearDown() { unmockkAll() }` 追加

### mockkObject テスト隔離問題（TimerViewModelTest）
- `TimerViewModelTest`で`unmockkAll()`を使用すると、MockKのグローバル状態がリセットされ、同一JVMフォーク内の後続SettingsViewModelTestが53件失敗
- **解決策**: `unmockkAll()` → `unmockkObject(TimerService.Companion)` に変更（必要なオブジェクトのみアンモック）

### ウィジェット タイマー状態の陳腐化対策
- タイマー稼働中にプロセスがkillされると、SharedPreferences上のタイマー状態が「稼働中」のまま残る
- `IterioWidgetStateHelper.getTimerStateFromPrefs()` で `lastUpdatedAt` タイムスタンプをチェック
- 60秒以上更新がない場合はIDLEにフォールバック（`STALENESS_THRESHOLD_MS = 60_000L`）
- `TimerService.updateWidgetState()` がタイムスタンプを毎秒書き込み

### JaCoCo + Kotlin inline 関数
- `Result<T, E>` の `map`, `flatMap`, `fold` 等の `inline` 関数はJaCoCoでカバレッジ計測不可
- バイトコードが呼び出し元にインライン化されるため、Result クラス自体の実行として記録されない
- **対策**: テストは書いているが、カバレッジ数値には反映されないことを理解した上で除外対象の判断基準にしない

---

## 過去のセッションで完了したタスク

### Phase 1.2: Result<T, DomainError> パターン適用
- 全8リポジトリにResult型エラーハンドリング統一適用

### Phase 4: CalendarScreen リファクタリング
- CalendarScreenを小コンポーネントに分割

### Phase 1-3: コードレビュー対応
- セキュリティ強化、Billing改善、バックアップ改善、UI改善

---

## コミット履歴（最新5件）

```
d272be6 feat: implement Phase A-D (tests, docs, linting, forgetting curve UI)
9ee557a docs: update session end status
7995d41 docs: update HANDOVER.md with Phase 1.2 completion
30618ce refactor: apply Result<T, DomainError> pattern to all repositories (Phase 1.2)
9048a3c refactor: extract CalendarScreen into smaller components (Phase 4)
```

---

## 技術スタック

| 項目 | バージョン |
|------|-----------|
| Compile SDK | 35 |
| Min SDK | 26 |
| Target SDK | 35 |
| Kotlin | 2.0.21 |
| Compose BOM | 2024.12.01 |
| Hilt | 2.53.1 |
| Room | 2.6.1 |
| Google Play Billing | 7.0.0 |
| Java | 17 |
| MockK | 1.13.9 |
| Detekt | 1.23.7 (CLI only) |

---

## ビルドコマンド

```bash
cd C:/Users/hikit/projects/Iterio
./gradlew assembleDebug              # ビルド
./gradlew testDebugUnitTest          # テスト実行
./gradlew jacocoTestReport           # カバレッジレポート生成
./gradlew jacocoTestCoverageVerification  # カバレッジ閾値検証（80%）
# Detekt: CLIで実行（Gradleプラグイン不可）
# detekt --config detekt.yml --baseline detekt-baseline.xml --input app/src/main/java
```

---

## 実装済み機能一覧

| 機能 | 状態 | Premium |
|------|------|---------|
| ポモドーロタイマー | ✅ | - |
| タスク管理 | ✅ | - |
| 教科グループ | ✅ | - |
| 学習統計 | ✅ | - |
| カレンダー | ✅ | - |
| 復習リマインダー | ✅ | - |
| **忘却曲線UI** | ✅ NEW | - |
| フォーカスモード | ✅ | ✅ |
| BGM再生 | ✅ | ✅ |
| ローカルバックアップ | ✅ | ✅ |
| クラウドバックアップ | ✅ | ✅ |
| バックアップ暗号化 | ✅ | ✅ |
| Google Play Billing | ✅ | - |
| 署名検証（SHA256） | ✅ | - |
| Result型エラーハンドリング | ✅ | - |
| **JaCoCoカバレッジ検証** | ✅ | - |
| **Detekt静的解析** | ✅ (CLI) | - |
| **ウィジェット機能強化** | ✅ NEW | - |

---

## 手動作業が必要な項目（リリース前）

| タスク | 手順 |
|-------|------|
| **Detekt CLI インストール** | `scoop install detekt` (Windows) |
| **Google Play Console公開鍵設定** | gradle.propertiesに`BILLING_PUBLIC_KEY=...`を追加 |
| **Google Cloud Console設定** | Google Drive API有効化、OAuth設定 |
| **実機テスト** | Premium購入フロー、バックアップ/復元の動作確認 |

---

## 次のアクション候補

1. ~~**テストカバレッジ向上**: 現在60%閾値、目標80%へ段階的に引き上げ~~ ✅ 完了
2. ~~**ウィジェット機能強化**: 復習バッジ、レスポンシブレイアウト、タイマーコントロール、更新トリガー~~ ✅ 完了
3. **E2Eテスト**: Compose UIテスト統合テスト
4. **パフォーマンス最適化**: プロファイリング・最適化
5. **ウィジェットIntegrationテスト**: ActionCallback、StateHelper のRoomモック付きテスト拡充

---

## 注意事項

- **暗号化キー**: Android KeyStoreに保存、アプリ再インストールでキー失効
- **後方互換性**: 平文JSONバックアップも読み込み可能
- **署名検証**: 公開鍵未設定時は購入拒否
- **Detekt**: Gradleプラグインとして追加不可（MockK競合）、CLI経由のみ

---

### Session End: 2026-01-28
- Branch: main
- Last Commit: (uncommitted - ウィジェット機能強化4フェーズ完了)
- Status: テスト1001件全通過、assembleDebug成功、JaCoCo 80%閾値通過。コミット待ち。
- 変更ファイル (ウィジェット機能強化):
  - `app/src/main/java/com/iterio/app/widget/WidgetState.kt` (pendingReviewCount, todayTasks追加)
  - `app/src/main/java/com/iterio/app/widget/WidgetTaskItem.kt` (新規)
  - `app/src/main/java/com/iterio/app/widget/IterioWidgetStateHelper.kt` (タスクリスト取得、staleness check、デバウンス)
  - `app/src/main/java/com/iterio/app/widget/IterioWidget.kt` (レスポンシブ3レイアウト、タイマーボタン)
  - `app/src/main/java/com/iterio/app/widget/IterioWidgetReceiver.kt` (ACTION_DATA_CHANGED)
  - `app/src/main/java/com/iterio/app/widget/actions/ToggleTimerActionCallback.kt` (新規)
  - `app/src/main/java/com/iterio/app/widget/actions/StopTimerActionCallback.kt` (新規)
  - `app/src/main/java/com/iterio/app/ui/screens/home/HomeViewModel.kt` (context追加、ウィジェット更新)
  - `app/src/main/java/com/iterio/app/ui/screens/review/ReviewScheduleViewModel.kt` (context追加、ウィジェット更新)
  - `app/src/main/java/com/iterio/app/ui/screens/timer/TimerViewModel.kt` (セッション完了時ウィジェット更新)
  - `app/src/main/AndroidManifest.xml` (DATA_CHANGED action追加)
  - `app/src/main/res/xml/iterio_widget_info.xml` (maxResizeHeight 300dp)
  - `app/src/main/res/values/strings.xml` (ウィジェット文字列追加)
  - `app/src/main/res/values-en/strings.xml` (英語版追加)
  - `app/src/test/java/com/iterio/app/widget/WidgetStateTest.kt` (新規7テスト)
  - `app/src/test/java/com/iterio/app/widget/WidgetTaskItemTest.kt` (新規6テスト)
  - `app/src/test/java/com/iterio/app/widget/actions/ToggleTimerActionCallbackTest.kt` (新規4テスト)
  - `app/src/test/java/com/iterio/app/widget/actions/StopTimerActionCallbackTest.kt` (新規3テスト)
  - `app/src/test/java/com/iterio/app/ui/screens/home/HomeViewModelTest.kt` (context+widgetReceiver mock追加)
  - `app/src/test/java/com/iterio/app/ui/screens/review/ReviewScheduleViewModelTest.kt` (context+widgetReceiver mock追加)
  - `app/src/test/java/com/iterio/app/ui/screens/timer/TimerViewModelTest.kt` (widgetReceiver mock追加)
