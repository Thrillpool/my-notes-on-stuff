import cats.effect.{IO, IOApp}
import cats.effect.std.Semaphore

import java.lang.management.ManagementFactory
import java.util.concurrent.atomic.AtomicInteger
import cats.implicits.*

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object MyApp extends IOApp.Simple {

  val tbean = ManagementFactory.getThreadMXBean
  tbean.setThreadCpuTimeEnabled(true)

  val startTime = System.currentTimeMillis()
  val runTimeMillis = 10000

  def busyWorkForTime(time: Double, counter: AtomicInteger): IO[Unit] = IO {
    val nowTime = System.currentTimeMillis()

    if (nowTime - startTime > runTimeMillis) {
      throw new RuntimeException("nah too late for you")
    }

    val tid = Thread.currentThread().getId
    val startCpuNs = tbean.getThreadCpuTime(tid)
    var accumulatedCpuNs = 0L
    var x = 0.0

    while (accumulatedCpuNs < time * 1_000_000_000L) {
      x += Math.sqrt(x + 1)
      accumulatedCpuNs = tbean.getThreadCpuTime(tid) - startCpuNs
    }
    counter.incrementAndGet()
  }

  val fastTaskCount = new AtomicInteger(0)
  val slowTaskCount = new AtomicInteger(0)

  val numCpus = Runtime.getRuntime.availableProcessors()
  val slowTaskPoolService = Executors.newFixedThreadPool(numCpus / 2)
  val slowTaskExContext = ExecutionContext.fromExecutor(slowTaskPoolService)

  override def run: IO[Unit] = for {
    slowTaskSemaphore <- Semaphore[IO](numCpus / 2)
    lotsOfWorkToDo = (0 to 50000).map { i =>
      if (i % 2 == 0) {
        busyWorkForTime(0.01, fastTaskCount).flatTap(_ => IO(fastTaskCount.incrementAndGet()))
      } else {
        slowTaskSemaphore.permit
          .use(_ => busyWorkForTime(0.4, slowTaskCount).evalOn(slowTaskExContext).flatTap(_ => IO(slowTaskCount.incrementAndGet())))
      }
    }
    _ <- lotsOfWorkToDo.toList.map(task => task.handleErrorWith(_ => IO.unit)).traverse(_.start)
    _ <- IO.sleep((runTimeMillis + 1000).millis)
    _ <- IO.println(fastTaskCount.get())
    _ <- IO.println(slowTaskCount.get())
  } yield ()
}

object MyApp2 extends IOApp.Simple {

  val tbean = ManagementFactory.getThreadMXBean
  tbean.setThreadCpuTimeEnabled(true)

  def busyWorkForTime(time: Double, startTime: Long): IO[Unit] = IO {
    val nowTime = System.currentTimeMillis()

    var x = 0.0

    val tid = Thread.currentThread().getId
    val startCpuNs = tbean.getThreadCpuTime(tid)
    var accumulatedCpuNs = 0L

    while (System.currentTimeMillis() - startTime < time) {
      x += Math.sqrt(x + 1)
      accumulatedCpuNs = tbean.getThreadCpuTime(tid) - startCpuNs
    }
    println(accumulatedCpuNs)
  }

  val startTime = System.currentTimeMillis()
  val numCpus = Runtime.getRuntime.availableProcessors()

  val largePool = Executors.newFixedThreadPool(numCpus * 2)
  val largeExContext = ExecutionContext.fromExecutor(largePool)

  override def run: IO[Unit] = for {
    _ <- (0 until numCpus * 2).map(_ => busyWorkForTime(10000, startTime).evalOn(largeExContext)).toList.parSequence
    _ <- IO(largePool.shutdown())
  } yield ()
}
