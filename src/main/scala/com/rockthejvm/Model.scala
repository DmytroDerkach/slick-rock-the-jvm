package com.rockthejvm

import java.time.LocalDate

case class Movie(id: Long, name: String, releaseDate: LocalDate, lengthInMin: Int)
case class  Actor(id: Long, name: String)
case class MovieActorMapping(id: Long, movieId: Long, actorId: Long)
case class StreamingProviderMapping(id: Long, movieId: Long, streamingProvider: StreamingProvider.Provider)

object StreamingProvider extends Enumeration {
  type Provider = Value
  val Netflix = Value("Netflix")
  val Disney = Value("Disney")
  val Prime = Value("Prime")
  val Hulu = Value("Hulu")
}

object SlickTables{
  import slick.jdbc.PostgresProfile.api._

  class MovieTable(tag: Tag) extends Table[Movie](tag, Some("movies"), "Movie"){
    def id: Rep[Long] = column[Long]("movie_id", O.PrimaryKey, O.AutoInc)
    def name: Rep[String] = column[String]("name")
    def releaseDate: Rep[LocalDate] = column[LocalDate]("release_date")
    def lengthInMin: Rep[Int] = column[Int]("length_in_min")

    //mapping function to the case class
    override def * = (id, name, releaseDate, lengthInMin) <> (Movie.tupled, Movie.unapply)
  }

  class ActorTable(tag: Tag) extends Table[Actor](tag, Some("movies"), "Actor"){
    def id: Rep[Long] = column[Long]("actor_id", O.PrimaryKey, O.AutoInc)
    def name: Rep[String] = column[String]("name")

    //mapping function to the case class
    override def * = (id, name) <> (Actor.tupled, Actor.unapply)
  }

  class MovieActorMappingTable(tag: Tag) extends Table[MovieActorMapping](tag, Some("movies"), "MovieActorMapping"){
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def movieId: Rep[Long] = column[Long]("movie_id")
    def actorId: Rep[Long] = column[Long]("actor_id")

    //mapping function to the case class
    override def * = (id, movieId, actorId) <> (MovieActorMapping.tupled, MovieActorMapping.unapply)
  }

  class StreamingProviderMappingTable(tag: Tag) extends Table[StreamingProviderMapping](tag, Some("movies"), "StreamingProviderMapping"){
    implicit val providerMapper = MappedColumnType.base[StreamingProvider.Provider, String](
      provider => provider.toString,
      string => StreamingProvider.withName(string)
    )
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def movieId: Rep[Long] = column[Long]("movie_id")
    def streamingProvider: Rep[StreamingProvider.Provider] = column[StreamingProvider.Provider]("streaming_provider")
    override def * = (id, movieId, streamingProvider) <> (StreamingProviderMapping.tupled, StreamingProviderMapping.unapply)
  }

  // "API entry point"
  lazy val movieTable = TableQuery[MovieTable]
  lazy val actorTable = TableQuery[ActorTable]
  lazy val movieActorMappingTable = TableQuery[MovieActorMappingTable]
  lazy val streamingProviderMappingTable = TableQuery[StreamingProviderMappingTable]



  // table generation script
  val tables = List(movieTable, actorTable, movieActorMappingTable, streamingProviderMappingTable)
  val ddl = tables.map(_.schema).reduce( _ ++ _)
}

object TableDefinitionGenerator {
  def main(args: Array[String]): Unit = {
    println(SlickTables.ddl.createIfNotExistsStatements.mkString("; \n"))
  }
}
