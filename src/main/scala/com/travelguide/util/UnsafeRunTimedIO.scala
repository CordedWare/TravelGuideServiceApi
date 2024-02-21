package com.travelguide.util

import cats.effect.IO
import cats.implicits.*
import cats.effect.unsafe.implicits.global

object UnsafeRunTimedIOApp {

    /** Вспомогательная функция, которая запускает заданный ввод IO[A], определяет время его выполнения, печатает его и возвращает.
     */
    def unsafeRunTimedIO[A](io: IO[A]): A = {
        val start  = System.currentTimeMillis()
        val result = io.unsafeRunSync()
        val end    = System.currentTimeMillis()
        println(s"$result (took ${end - start}ms)")
        result
    }
}
