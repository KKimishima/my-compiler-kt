package com.github.kkimishima.my.compiler

fun main(args: Array<String>) {
  if (args.size != 1) {
    throw RuntimeException("引数が不正です")
  }
  val tokeList = tokenize(charArray = args[0].toCharArray())
  val node = Node.createAst(tokeList)
  Node.forEach(node)
}
