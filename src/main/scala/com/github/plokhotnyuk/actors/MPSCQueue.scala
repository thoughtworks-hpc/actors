package com.github.plokhotnyuk.actors

import java.util.concurrent.atomic.AtomicReference
import annotation.tailrec

/**
 * Version of multi producer/single consumer lock-free unbounded queue
 * based on non-intrusive MPSC node-based queue, described by Dmitriy Vyukov:
 * http://www.1024cores.net/home/lock-free-algorithms/queues/non-intrusive-mpsc-node-based-queue
 *
 * @tparam T type of data to queue/dequeue
 */
class MPSCQueue[T] {
  private[this] var anyData: T = _  // Don't know how to simplify this
  var t0, t1, t2, t3, t4, t5, t6: Long = _
  private[this] var tail = new Node[T](anyData)
  var h0, h1, h2, h3, h4, h5, h6: Long = _
  private[this] val head = new AtomicReference[Node[T]](tail)

  def enqueue(data: T) {
    val node = new Node[T](data)
    head.getAndSet(node).lazySet(node)
  }

  @tailrec
  final def dequeue(): T = {
    val next = tail.get
    if (next ne null) {
      tail = next
      next.data
    } else {
      dequeue()
    }
  }
}

private[actors] class Node[T](val data: T) extends AtomicReference[Node[T]]