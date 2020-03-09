package com.github.kkimishima.my.compiler

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.nio.file.Files
import java.nio.file.Paths

internal class ApplicationKtTest {
  private val targetClassFile = "Main.class"
  private val targetClass = "Main"

  @BeforeEach
  fun setUp() {
    Files.deleteIfExists(Paths.get(targetClassFile))
  }

  @AfterEach
  fun cleanUp() {
    Files.deleteIfExists(Paths.get(targetClassFile))
  }

  private fun exitCodeAssert(exp: Int, act: Array<String>) {
    main(act)
    val ps = ProcessBuilder("java", targetClass).start().waitFor()
    assertEquals(exp, ps)
  }

  @Nested
  inner class Test_足し算と引き算テスト {
    @Test
    fun test_二項の演算() {
      assertAll(
        { exitCodeAssert(2, arrayOf("1+1")) },
        { exitCodeAssert(2, arrayOf("1 + 1")) },
        { exitCodeAssert(20, arrayOf("10 + 10")) },
        { exitCodeAssert(0, arrayOf("1 - 1")) },
        { exitCodeAssert(1, arrayOf("10 - 9")) },
        { exitCodeAssert(90, arrayOf("100 - 10")) }
      )
    }

    @Test
    fun test_三項の演算() {
      assertAll(
        { exitCodeAssert(3, arrayOf("1+1+1")) },
        { exitCodeAssert(3, arrayOf("1 + 1 + 1")) },
        { exitCodeAssert(3, arrayOf("2 + 2 - 1")) },
        { exitCodeAssert(2, arrayOf("2 - 1 + 1")) },
        { exitCodeAssert(1, arrayOf("3 - 1 - 1")) }
      )
    }
  }
}