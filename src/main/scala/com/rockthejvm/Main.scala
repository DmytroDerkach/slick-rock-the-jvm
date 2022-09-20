package com.rockthejvm

import slick.jdbc.GetResult

import java.time.LocalDate
import slick.jdbc.PostgresProfile.api._

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object PrivateExecutionContext{
  val executor: ExecutorService = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(executor)
}

object Main {
  import PrivateExecutionContext._
  val somemovie: Movie = Movie(1, "Name", LocalDate.of(1994, 9, 23), 162)
  val somemovie2: Movie = Movie(1, "The Shoshend Redemption", LocalDate.of(1960, 9, 23), 155)
  val somemovie3: Movie = Movie(3, "NFS", LocalDate.of(2010, 5, 10), 198)
  val phantomMenace: Movie = Movie(3, "Start Wars: a phantom Menace", LocalDate.of(1999, 5, 16), 133)
  val leanNeeson: Actor = Actor(10L, "Lean Neeson")

  val providers = List(
    StreamingProviderMapping(1L, 5L, StreamingProvider.Netflix),
    StreamingProviderMapping(1L, 6L, StreamingProvider.Prime),
    StreamingProviderMapping(1L, 7L, StreamingProvider.Disney)
  )

  def demoInsertMovie(): Unit ={
    val queryDescription = SlickTables.movieTable += somemovie3
    val futureId: Future[Int] = Connection.db.run(queryDescription)
    futureId.onComplete {
      case Failure(exception) => println(s"QueryFailed, $exception")
      case Success(value) => println(s"Query ok $value")
    }
    Thread.sleep(10000)
  }

  def demoReadAllMovies() = {
    val futureId: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.result)
    futureId.onComplete {
      case Failure(exception) => println(s"QueryFailed, $exception")
      case Success(value) => println(s"fetched ${value.mkString(",\n")}")
    }
    Thread.sleep(10000)
  }

  def demoReadSomeMovies() = {
    val futureId: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.filter(_.name.like("Name2")).result)
    futureId.onComplete {
      case Failure(exception) => println(s"QueryFailed, $exception")
      case Success(value) => println(s"fetched ${value.mkString(",\n")}")
    }
    Thread.sleep(10000)
  }

  def demoUpdate() = {
    val quesryDescriptior = SlickTables.movieTable.filter(_.id === 1L)
      .map(v => v.name)
      .update("The Matrix")
    val futureId: Future[Int] = Connection.db.run(quesryDescriptior)
    futureId.onComplete {
      case Failure(exception) => println(s"QueryFailed, $exception")
      case Success(value) => println(s"Query ok $value")
    }
    Thread.sleep(10000)
  }

  def demoDelete() = {
    Connection.db.run(SlickTables.movieTable.filter(_.name.like("Name2")).delete)
    Thread.sleep(5000)
  }

  def readMoviesByPlainQuery(): Future[Vector[Movie]] = {
    implicit val getResultMovie: GetResult[Movie] =
      GetResult(positionedResult =>
        Movie(positionedResult.<<,
          positionedResult.<<,
          LocalDate.parse(positionedResult.nextString()),
          positionedResult.<<)
      )
    val query = sql"""select * from movies."Movie"""".as[Movie]
    Connection.db.run(query)
  }

  def demoInsertActors(): Unit ={
    val queryDescription = SlickTables.actorTable ++= Seq(Actor(1L, "Dima"),Actor(2L, "Nati"),Actor(3L, "Ihor"))
    val futureId = Connection.db.run(queryDescription)
    futureId.onComplete {
      case Failure(exception) => println(s"QueryFailed, $exception")
      case Success(_) => println(s"Actor Query ok ")
    }
  }

  def multipleQueriesSingleTransaction() = {
    val insertMovie = SlickTables.movieTable += phantomMenace
    val insertActor = SlickTables.actorTable += leanNeeson
    val finalQuery = DBIO.seq(insertMovie,insertActor)
    Connection.db.run(finalQuery.transactionally)
  }

  def findAllActorsByMovie(movidId: Long): Future[Seq[Actor]] = {
    val joinQuery = SlickTables.movieActorMappingTable
      .filter(_.movieId === movidId )
      .join(SlickTables.actorTable)
      .on(_.actorId === _.id)
      .map(_._2)
    Connection.db.run(joinQuery.result)
  }

  def addStreamingProvider() = {
    val streamingQuery = SlickTables.streamingProviderMappingTable ++= providers
    Connection.db.run(streamingQuery)
  }

  def findProviderForMovidId(movieId: Long): Future[Seq[StreamingProviderMapping]] = {
    val findQuery = SlickTables.streamingProviderMappingTable.filter(_.movieId === movieId)
    Connection.db.run(findQuery.result)
  }

  def main(args: Array[String]) : Unit = {

    findProviderForMovidId(6).onComplete {
      case Failure(exception) => ???
      case Success(value) => println(s"result: ${value.map(_.streamingProvider)}")
    }
    Thread.sleep(5000)
    PrivateExecutionContext.executor.shutdown()
  }

}
