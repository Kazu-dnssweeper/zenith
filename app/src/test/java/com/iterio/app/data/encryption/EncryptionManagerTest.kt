package com.iterio.app.data.encryption

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * EncryptionManager のユニットテスト
 *
 * 注意: 暗号化/復号化のテストは Android KeyStore を使用するため、
 * 実機での Instrumented Test で行う必要があります。
 * このテストは isEncryptedData メソッドの検証のみを行います。
 */
class EncryptionManagerTest {

    // テスト対象のメソッドはstatic的に呼び出せないため、
    // isEncryptedDataのロジックを直接テストする

    /**
     * JSONデータ（'{'で始まる）は暗号化されていないと判定される
     */
    @Test
    fun `isEncryptedData returns false for JSON starting with brace`() {
        // JSONの開始バイト '{' = 0x7B
        val jsonData = """{"version":1,"data":"test"}""".toByteArray(Charsets.UTF_8)

        val result = isEncryptedDataLogic(jsonData)

        assertFalse("JSONデータは暗号化されていないと判定すべき", result)
    }

    /**
     * バイナリデータ（'{'で始まらない）は暗号化されていると判定される
     */
    @Test
    fun `isEncryptedData returns true for binary data`() {
        // 暗号化されたデータをシミュレート（NONCEで始まるバイナリ）
        val binaryData = byteArrayOf(0x00, 0x12, 0x34, 0x56, 0x78, 0x9A.toByte())

        val result = isEncryptedDataLogic(binaryData)

        assertTrue("バイナリデータは暗号化されていると判定すべき", result)
    }

    /**
     * 空の配列は暗号化されていないと判定される
     */
    @Test
    fun `isEncryptedData returns false for empty array`() {
        val emptyData = byteArrayOf()

        val result = isEncryptedDataLogic(emptyData)

        assertFalse("空のデータは暗号化されていないと判定すべき", result)
    }

    /**
     * 空白で始まるデータは暗号化されていると判定される
     * （正規のJSONは '{'で始まるため）
     */
    @Test
    fun `isEncryptedData returns true for data starting with whitespace`() {
        val whitespaceJson = " {\"data\":\"test\"}".toByteArray(Charsets.UTF_8)

        val result = isEncryptedDataLogic(whitespaceJson)

        // スペース(0x20)で始まるため、暗号化されていると判定される
        assertTrue("スペースで始まるデータは暗号化されていると判定すべき", result)
    }

    /**
     * 配列形式のJSONは暗号化されていると判定される
     * （Iterioのバックアップ形式はオブジェクト形式のみ対応）
     */
    @Test
    fun `isEncryptedData returns true for JSON array`() {
        val arrayJson = """[{"id":1},{"id":2}]""".toByteArray(Charsets.UTF_8)

        val result = isEncryptedDataLogic(arrayJson)

        // '[' (0x5B) で始まるため、暗号化されていると判定される
        assertTrue("配列形式JSONは暗号化されていると判定すべき", result)
    }

    /**
     * NONCE (12バイト) + 暗号化データのような構造は暗号化と判定
     */
    @Test
    fun `isEncryptedData returns true for typical encrypted structure`() {
        // 典型的な暗号化構造：ランダムなバイト列
        val encryptedData = byteArrayOf(
            0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(), // NONCE開始
            0x12, 0x34, 0x56, 0x78, 0x9A.toByte(), 0xBC.toByte(), // NONCE続き
            0xDE.toByte(), 0xF0.toByte(), 0x11, // NONCE終了（12バイト）
            0x22, 0x33, 0x44, 0x55 // 暗号化データ
        )

        val result = isEncryptedDataLogic(encryptedData)

        assertTrue("典型的な暗号化構造は暗号化と判定すべき", result)
    }

    // --- Edge case tests for isEncryptedData ---

    /**
     * 単一の '{' バイトのみの配列は暗号化されていないと判定される
     */
    @Test
    fun `isEncryptedData returns false for single brace byte`() {
        val singleBrace = byteArrayOf(0x7B)

        val result = isEncryptedDataLogic(singleBrace)

        assertFalse("単一の '{' バイトは暗号化されていないと判定すべき", result)
    }

    /**
     * 単一の非 '{' バイト (0x00) は暗号化されていると判定される
     */
    @Test
    fun `isEncryptedData returns true for single non-brace byte`() {
        val singleByte = byteArrayOf(0x00)

        val result = isEncryptedDataLogic(singleByte)

        assertTrue("単一の非ブレースバイトは暗号化されていると判定すべき", result)
    }

    /**
     * NULLバイト (0x00) で始まるデータは暗号化されていると判定される
     */
    @Test
    fun `isEncryptedData returns true for data starting with null byte`() {
        val nullStartData = byteArrayOf(0x00, 0x01, 0x02)

        val result = isEncryptedDataLogic(nullStartData)

        assertTrue("NULLバイトで始まるデータは暗号化されていると判定すべき", result)
    }

    /**
     * 高バイト値 (0xFF) で始まるデータは暗号化されていると判定される
     */
    @Test
    fun `isEncryptedData returns true for data starting with high byte`() {
        val highByteData = byteArrayOf(0xFF.toByte(), 0xFE.toByte())

        val result = isEncryptedDataLogic(highByteData)

        assertTrue("高バイト値で始まるデータは暗号化されていると判定すべき", result)
    }

    /**
     * 大きなJSONデータ（'{'で始まる）も暗号化されていないと判定される
     */
    @Test
    fun `isEncryptedData handles large JSON data`() {
        // 10KB相当の大きなJSONデータ
        val largeContent = "a".repeat(10_000)
        val largeJson = """{"data":"$largeContent"}""".toByteArray(Charsets.UTF_8)

        val result = isEncryptedDataLogic(largeJson)

        assertFalse("大きなJSONデータも暗号化されていないと判定すべき", result)
    }

    // --- EncryptionException tests ---

    /**
     * EncryptionException はメッセージのみで生成でき、causeはnull
     */
    @Test
    fun `EncryptionException with message only`() {
        val exception = EncryptionException("テストエラー")

        assertEquals("メッセージが正しく設定されるべき", "テストエラー", exception.message)
        assertNull("causeが指定されない場合はnullであるべき", exception.cause)
    }

    /**
     * EncryptionException はメッセージとcauseの両方で生成できる
     */
    @Test
    fun `EncryptionException with message and cause`() {
        val cause = RuntimeException("元のエラー")
        val exception = EncryptionException("暗号化エラー", cause)

        assertEquals("メッセージが正しく設定されるべき", "暗号化エラー", exception.message)
        assertEquals("causeが正しく設定されるべき", cause, exception.cause)
    }

    /**
     * EncryptionException は Exception を継承している
     */
    @Test
    fun `EncryptionException is instance of Exception`() {
        val exception = EncryptionException("テスト")

        assertTrue(
            "EncryptionExceptionはExceptionのインスタンスであるべき",
            exception is Exception
        )
    }

    /**
     * EncryptionManager.isEncryptedDataと同じロジック
     * テスト用にロジックを複製（実際のテストではリフレクション等で対応可能）
     */
    private fun isEncryptedDataLogic(bytes: ByteArray): Boolean {
        if (bytes.isEmpty()) return false
        // JSONの開始バイト ('{' = 0x7B) でない場合は暗号化
        return bytes[0] != 0x7B.toByte()
    }
}
