package services.tokenmeister

import java.util.concurrent.Executors

import cats.implicits._
import cats.data.{Xor, XorT}
import controllers.MainController.BodyNotFound
import models.AppError
import org.scalatest.FunSpec

import scala.concurrent.{ExecutionContext, Future}

class FlatMapSpec extends FunSpec {
  describe("Three ways of flatMapping over a monad?") {
    val executorService = Executors.newFixedThreadPool(3)
    implicit val executionContext = ExecutionContext.fromExecutorService(executorService)

    it("they should finish at the same time, sometimes 'y' doesn't. Hard to reproduce.") {

      val x: Future[Xor[AppError, String]] = Future { Xor.left(BodyNotFound) }
      val y: XorT[Future, AppError, String] = XorT(Future { Xor.left(BodyNotFound) })
      val z: XorT[Future, AppError, String] = XorT.left(Future.successful { BodyNotFound })

      val rx: Future[Xor[AppError, String]] = for {
        s <- x
      } yield s

      rx.map {
        case Xor.Right(str) => println(s"x: $str")
        case Xor.Left(_) => println("x left")
      }

      val ry: XorT[Future, AppError, String] = for {
        something <- y
      } yield something

      ry.map(s => println(s"y: $s")).recover {
        case BodyNotFound => println("y left")
      }

      val rz: XorT[Future, AppError, String] = for {
        something <- z
      } yield something

      rz.map(s => println(s"z: $s")).recover {
        case BodyNotFound => println("z left")
      }

      Thread.sleep(2000)
    }
  }
}

