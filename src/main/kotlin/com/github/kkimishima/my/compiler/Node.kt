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

    fun addNumNode(tokenList: TokenList, node: Node): Node =
      when (node) {
        is Num -> of(tokenList.headNumInt())
        is Reserved -> of(node.nodeKind, of(tokenList.headNumInt()), node)
        else -> throw RuntimeException("Nodeが初期化されていません")
      }

    fun addReserved(nodeKind: NodeKind, tokenList: TokenList, node: Node): Node =
      when (node) {
        is Num -> {
          val a = createAst(tokenList.tail(), node)
          val b = of(nodeKind, node, a)
          createAst(tokenList.tail().tail(), b)
        }
        is Reserved -> {
          val a = createAst(tokenList.tail(), node.left)
          val c = of(nodeKind, a, node)
          createAst(tokenList.tail().tail(), c)
        }
        else -> throw RuntimeException("2")
      }

    fun initNilNode(tokenList: TokenList, node: Node, f: (TokenList, Node) -> Node): Node =
      when {
        node.isNil() && tokenList.isFirstNum() -> {
          createAst(tokenList.tail(), of(tokenList.headNumInt()))
        }
        else -> {
          f(tokenList, node)
        }
      }

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

    fun nextInitNilNode(tokenList: TokenList, node: Node, f: (TokenList, Node) -> Node): Node =
      tokenList.next(node) { tokenList1 ->
        initNilNode(tokenList1, node, f)
      }


    fun createAst(tokenList: TokenList, node: Node = emptyOf()): Node {
      return nextInitNilNode(tokenList, node) { t, n ->
        when {
          t.isFirstNum() -> addNumNode(t, n)
          t.isHeadReservedValue() == "+" -> addReserved(NodeKind.ADD, t, n)
          t.isHeadReservedValue() == "-" -> addReserved(NodeKind.SUB, t, n)
          else -> throw RuntimeException("定義外のトークン")
        }
      }
    }
  }
}


