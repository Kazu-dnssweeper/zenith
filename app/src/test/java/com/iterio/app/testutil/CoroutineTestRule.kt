package com.iterio.app.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit4テストルール: コルーチンのDispatcher標準化
 *
 * 使用例:
 * ```
 * class MyViewModelTest {
 *     @get:Rule
 *     val coroutineTestRule = CoroutineTestRule()
 *
 *     @Test
 *     fun `test something`() = runTest {
 *         // テストコード
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTestRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}
