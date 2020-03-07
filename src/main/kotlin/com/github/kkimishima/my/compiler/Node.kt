package com.github.kkimishima.my.compiler

enum class NodeKind {
  ADD,
  SUB,
//  MUL,
//  DIV
}

sealed class Node {
  data class Reserved(val nodeKind: NodeKind, val left: Node, val right: Node) : Node()

  data class Num(val num: Int) : Node()

  object Nil : Node()

  fun isNil(): Boolean =
    when (this) {
      is Nil -> true
      else -> false
    }

  companion object {
    fun emptyOf(): Node = Nil
    fun of(int: Int): Node = Num(int)
    fun of(nodeKind: NodeKind, left: Node, right: Node): Node =
      Reserved(nodeKind, left, right)

    fun forEach(node: Node) {
      when (node) {
        is Num -> println(node.num)
        is Reserved -> {
          forEach(node.right)
          forEach(node.left)
          println(node.nodeKind)
        }
        else -> throw RuntimeException("")
      }
    }

    tailrec fun createAst(tokenList: TokenList, node: Node = emptyOf()): Node {
      return when (tokenList) {
        is TokenList.Nil -> node
        is TokenList.Cons -> {
          when {
            node.isNil() && tokenList.isFirstNum() -> createAst(tokenList.tail(), of(tokenList.headNumInt()))
            else -> {
              when {
                tokenList.isFirstNum() -> {
                  when (val n = node) {
                    is Num -> of(tokenList.headNumInt())
                    is Reserved -> of(n.nodeKind, of(tokenList.headNumInt()), n)
                    else -> throw RuntimeException("1")
                  }
                }
                tokenList.isHeadReservedValue() == "+" -> {
                  when (val n = node) {
                    is Num -> {
                      val c = of(tokenList.tail().headNumInt())
                      val a = createAst(tokenList.tail(), n)
                      val b = of(NodeKind.ADD, n, a)
                      createAst(tokenList.tail().tail(), b)
                    }
                    is Reserved -> {
                      val a = createAst(tokenList.tail(), n)
                      createAst(tokenList.tail().tail(), a)
                    }
                    else -> throw RuntimeException("2")
                  }
                }
                tokenList.isHeadReservedValue() == "-" -> {
                  when (val n = node) {
                    is Num -> {
                      val a = createAst(tokenList.tail(), n)
                      val b = of(NodeKind.SUB, n, a)
                      createAst(tokenList.tail().tail(), b)
                    }
                    is Reserved -> {
                      val a = createAst(tokenList.tail(), n)
                      createAst(tokenList.tail().tail(), a)
                    }
                    else -> throw RuntimeException("3")
                  }
                }
                else -> throw RuntimeException("4")
              }
            }
          }
        }
      }
    }
  }
}
