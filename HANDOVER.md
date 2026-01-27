# Iterio 開発申し送り書

**更新日時:** 2026-01-28
**現在のステータス:** ビルド成功 ✅ / テスト全通過 ✅
**テスト結果:** 813 tests passed

---

## 最新セッションで完了したタスク

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
./gradlew jacocoTestCoverageVerification  # カバレッジ閾値検証（60%）
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
| **JaCoCoカバレッジ検証** | ✅ NEW | - |
| **Detekt静的解析** | ✅ NEW (CLI) | - |

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

1. **テストカバレッジ向上**: 現在60%閾値、目標80%へ段階的に引き上げ
2. **ウィジェット機能強化**: 基本実装あり、機能拡張可能
3. **E2Eテスト**: Compose UIテスト統合テスト
4. **パフォーマンス最適化**: プロファイリング・最適化

---

## 注意事項

- **暗号化キー**: Android KeyStoreに保存、アプリ再インストールでキー失効
- **後方互換性**: 平文JSONバックアップも読み込み可能
- **署名検証**: 公開鍵未設定時は購入拒否
- **Detekt**: Gradleプラグインとして追加不可（MockK競合）、CLI経由のみ

---

### Session End: 2026-01-28
- Branch: main
- Last Commit: d272be6 feat: implement Phase A-D (tests, docs, linting, forgetting curve UI)
- Status: Committed, not yet pushed
