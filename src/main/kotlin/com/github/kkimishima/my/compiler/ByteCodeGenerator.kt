package com.github.kkimishima.my.compiler

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import java.io.DataOutputStream
import java.io.FileOutputStream
import java.io.IOException

/*
Classfile ./Sample.class
  Last modified 2020/03/08; size 348 bytes
  MD5 checksum fb21669c1d62fbb65017d15e747638f6
  Compiled from "Sample.java"
public class com.github.kkimishima.ninecc.sample.Sample
  minor version: 0
  major version: 49
  flags: (0x0021) ACC_PUBLIC, ACC_SUPER
  this_class: #3                          // com/github/kkimishima/ninecc/sample/Sample
  super_class: #4                         // java/lang/Object
  interfaces: 0, fields: 0, methods: 2, attributes: 1
Constant pool:
   #1 = Methodref          #4.#13         // java/lang/Object."<init>":()V
   #2 = Methodref          #14.#15        // java/lang/System.exit:(I)V
   #3 = Class              #16            // com/github/kkimishima/ninecc/sample/Sample
   #4 = Class              #17            // java/lang/Object
   #5 = Utf8               <init>
   #6 = Utf8               ()V
   #7 = Utf8               Code
   #8 = Utf8               LineNumberTable
   #9 = Utf8               main
  #10 = Utf8               ([Ljava/lang/String;)V
  #11 = Utf8               SourceFile
  #12 = Utf8               Sample.java
  #13 = NameAndType        #5:#6          // "<init>":()V
  #14 = Class              #18            // java/lang/System
  #15 = NameAndType        #19:#20        // exit:(I)V
  #16 = Utf8               com/github/kkimishima/ninecc/sample/Sample
  #17 = Utf8               java/lang/Object
  #18 = Utf8               java/lang/System
  #19 = Utf8               exit
  #20 = Utf8               (I)V
{
  public com.github.kkimishima.ninecc.sample.Sample();
    descriptor: ()V
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 3: 0

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=1, locals=1, args_size=1
         0: bipush        -10
         2: invokestatic  #2                  // Method java/lang/System.exit:(I)V
         5: return
      LineNumberTable:
        line 5: 0
        line 6: 5
}
 */

object ByteCodeGenerator : Opcodes {
  private const val mainClassName = "Main"

  fun mainClassCreate(): ClassWriter {
    val cw = ClassWriter(0) // main class
    /*
      Main class
      public class com.github.kkimishima.ninecc.sample.Sample
        minor version: 0
        major version: 49
        flags: (0x0021) ACC_PUBLIC, ACC_SUPER
        this_class: #3                          // com/github/kkimishima/ninecc/sample/Sample
        super_class: #4                         // java/lang/Object
        interfaces: 0, fields: 0, methods: 2, attributes: 1
     */
    cw.visit(
      V1_5, // version
      ACC_PUBLIC + ACC_SUPER, // access
      mainClassName, // name
      null, // signature
      "java/lang/Object", // superName
      null // interfaces
    )
    return cw
  }

  fun mainClassConcentrator(cw: ClassWriter): ClassWriter {
    /*
      Main class Constructor
      public com.github.kkimishima.ninecc.sample.Sample();
        descriptor: ()V
        flags: (0x0001) ACC_PUBLIC
        Code:
          stack=1, locals=1, args_size=1
            0: aload_0
            1: invokespecial #1                  // Method java/lang/Object."<init>":()V
            4: return
        LineNumberTable:
          line 3: 0
    */
    cw.visitMethod(
      ACC_PUBLIC, // access
      "<init>", // name
      "()V", // descriptor
      null,   // signature
      null // exception
    ).apply {
      visitCode()
      visitVarInsn(ALOAD, 0) // 0番目の局所変数から参照値を取り出してスタックに積む
      visitMethodInsn(
        INVOKESPECIAL, // opcode
        "java/lang/Object", // owner
        "<init>", // name
        "()V", // descriptor
        false // isInterFace
      ) // invokespecial インスタンス初期化メソッド、プライベートメソッド、スーパークラスのインスタンスメソッドを呼び出し
      visitInsn(RETURN) // return スタックに値を残さずに、呼び出し元の戻り番地に制御を移す。
      visitMaxs(
        1, //maxStack
        1 // maxLocals
      ) // スタックサイズ
      visitEnd()
    }
    return cw
  }

  fun mainMethod(cw: ClassWriter, f: MethodVisitor.() -> Unit) {
    /*
      main Method
      public static void main(java.lang.String[]);
        descriptor: ([Ljava/lang/String;)V
        flags: (0x0009) ACC_PUBLIC, ACC_STATIC
  */
    cw.visitMethod(
      ACC_PUBLIC + ACC_STATIC,  // access
      "main", // main
      "([Ljava/lang/String;)V", // descriptor
      null, // signature
      null // exception
    ).apply {
      visitCode()
      f(this)
      visitEnd()
    }
  }


  fun mainMethodBuild(cw: ClassWriter, node: Node): ClassWriter {
    /*
      Code:
        stack=1, locals=1, args_size=1
          0: bipush        -10
          2: invokestatic  #2                  // Method java/lang/System.exit:(I)V
          5: return
         LineNumberTable:
          line 5: 0
          line 6: 5
    */
    mainMethod(cw) {
      visitCode()

      var stackSize = 0

      Node.forEach(
        node = node,
        numFn = {
          stackSize++
          visitIntInsn(SIPUSH, it)
        },
        reservedFn = {
          when (it) {
            NodeKind.ADD -> visitInsn(IADD)
            NodeKind.SUB -> visitInsn(ISUB)
          }
        }
      )

      visitMethodInsn(
        INVOKESTATIC, // opcode
        "java/lang/System", // owner
        "exit", // name
        "(I)V", // descriptor
        false // isInterFace
      ) // System.exit(int) クラスメソッドを呼び出す
      visitInsn(RETURN) // return スタックに値を残さずに、呼び出し元の戻り番地に制御を移す。
      visitMaxs(
        stackSize, //maxStack
        stackSize // maxLocals
      ) // スタックサイズ
    }
    return cw
  }

  fun writeClass(byteArray: ByteArray, classNme: String = "Main.class") {
    try {
      FileOutputStream(classNme).use { fo ->
        DataOutputStream(fo).use { out ->
          out.write(byteArray)
        }
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  operator fun invoke(node: Node) {
    return mainClassCreate()
      .let(::mainClassConcentrator)
      .let { mainMethodBuild(it, node) }
      .apply { visitEnd() }
      .toByteArray()
      .let { writeClass(it) }
  }
}

