package com.github.kkimishima.my.compiler

import com.github.kkimishima.my.compiler.TokenList.Companion.emptyOf

sealed class Token {
  data class Reserved(val value: String) : Token()
  data class Num(val value: Int) : Token()
  companion object {
    fun of(str: String): Token =
      if (str.toIntOrNull() == null) {
        Reserved(str)
      } else {
        Num(str.toInt())
      }
  }
}

fun <T> Array<out T>.head(): T = this.first()
inline fun <reified T> Array<out T>.tail(): Array<T> = this.drop(1).toTypedArray()

sealed class TokenList {
  data class Cons(val token: Token, val tokenList: TokenList) : TokenList()
  object Nil : TokenList()

  fun headNumInt(): Int =
    when (this) {
      is Nil -> throw RuntimeException("")
      is Cons -> {
        when (val d = token) {
          is Token.Num -> d.value
          is Token.Reserved -> throw RuntimeException("")
        }
      }
    }

  fun isHeadReservedValue(): String =
    when (val h = this.head()) {
      is Token.Reserved -> h.value
      is Token.Num -> throw RuntimeException("")
    }

  fun isHeadNum(): Boolean =
    when (this.head()) {
      is Token.Num -> true
      is Token.Reserved -> true
    }

  fun head(): Token = when (this) {
    is Nil -> throw RuntimeException("対象がからです")
    is Cons -> this.token
  }

  fun tail(): TokenList = when (this) {
    is Nil -> Nil
    is Cons -> this.tokenList
  }

  fun isFirstNum(): Boolean = when (this) {
    is Nil -> false
    is Cons -> when (this.token) {
      is Token.Num -> true
      is Token.Reserved -> false
    }
  }

  fun isNotFirstNum(): Boolean = !isFirstNum()


  fun add(token: Token): TokenList =
    when (this) {
      is Nil -> Cons(token, Nil)
      is Cons -> Cons(token, Cons(this.token, this.tokenList))
    }

  companion object {
    fun of(vararg token: Token): TokenList =
      if (token.isEmpty()) {
        Nil
      } else {
        Cons(token.head(), of(*token.tail()))
      }

    fun tailAdd(tokenList: TokenList, token: Token): TokenList {
      return when (tokenList) {
        is Nil -> Cons(token, Nil)
        is Cons -> Cons(tokenList.token, tailAdd(tokenList.tokenList, token))
      }
    }

    fun emptyOf(): TokenList = Nil

    fun forEach(tokenList: TokenList, f: (Token) -> Unit): Unit {
      when (tokenList) {
        is Nil -> Unit
        is Cons -> {
          f(tokenList.token)
          forEach(tokenList.tokenList, f)
        }
      }
    }

    fun forNode(tokenList: TokenList, node: Node, f: (node: Node, token: Token) -> Node): Node {
      return when (tokenList) {
        is Nil -> node
        is Cons -> {
          forNode(tokenList.tokenList, f(node, tokenList.token), f)
        }
      }
    }
  }
}

tailrec fun tokenize(
  charArray: CharArray,
  index: Int = 0,
  tmpTokenStr: String = "",
  tokeList: TokenList = emptyOf()
): TokenList {
  return when {
    index == charArray.size -> {
      if (tmpTokenStr.isBlank()) {
        tokeList
      } else {
        TokenList.tailAdd(tokeList, Token.of(tmpTokenStr))
      }
    }
    charArray[index].isDigit() -> {
      val tt = tmpTokenStr + charArray[index].toString()
      tokenize(charArray, index + 1, tt, tokeList)
    }
    !charArray[index].isDigit() && tmpTokenStr.isNotBlank() -> {
      tokenize(charArray, index, "", TokenList.tailAdd(tokeList, Token.of(tmpTokenStr)))
    }
    charArray[index] == '+' -> {
      tokenize(charArray, index + 1, "", TokenList.tailAdd(tokeList, Token.of("+")))
    }
    charArray[index] == '-' -> {
      tokenize(charArray, index + 1, "", TokenList.tailAdd(tokeList, Token.of("-")))
    }
    charArray[index] == '(' -> {
      tokenize(charArray, index + 1, "", TokenList.tailAdd(tokeList, Token.of("(")))
    }
    charArray[index] == ')' -> {
      tokenize(charArray, index + 1, "", TokenList.tailAdd(tokeList, Token.of(")")))
    }
    charArray[index] == ' ' -> {
      tokenize(charArray, index + 1, "", tokeList)
    }
    else -> {
      val source = charArray.joinToString("")
      val errorPoint = (1..index).joinToString(separator = "", postfix = "^") { " " }
      val ms = "$source\n$errorPoint"
      throw RuntimeException("構文エラー\n$ms")
    }
  }
}
