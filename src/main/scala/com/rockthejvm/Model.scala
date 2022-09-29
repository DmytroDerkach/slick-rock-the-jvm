package com.rockthejvm

import com.rockthejvm.SlickTables.MovieTable
import play.api.libs.json.JsValue

import java.time.LocalDate

case class Movie(id: Long, name: String, releaseDate: LocalDate, lengthInMin: Int)
case class  Actor(id: Long, name: String)
case class MovieActorMapping(id: Long, movieId: Long, actorId: Long)
case class StreamingProviderMapping(id: Long, movieId: Long, streamingProvider: StreamingProvider.Provider)

// part 4
case class MovieLocations(id: Long, movieId: Long, locations: List[String])
case class MovieProperties(id: Long, movieId: Long, properties: Map[String, String])
case class ActorDetails(id: Long, actorId: Long, personalDetails: JsValue)

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

object SpecialTables{
  /*
  need to define in buildSb
   "com.github.tminglei" %% "slick-pg" % "0.20.3",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.20.3"
   */
  import com.rockthejvm.CustomPostgresProfile.api._

  class MovieLocationTable(tag: Tag) extends Table[MovieLocations](tag, Some("movies"), "MovieLocations"){
    def id: Rep[Long] = column[Long]("movie_location_id", O.PrimaryKey, O.AutoInc)
    def movieId: Rep[Long] = column[Long]("movie_id")
    def locations: Rep[List[String]] = column[List[String]]("locations")
    //mapping function to the case class
    override def * = (id, movieId, locations) <> (MovieLocations.tupled, MovieLocations.unapply)
  }

  class MoviePropertiesTable(tag: Tag) extends Table[MovieProperties](tag, Some("movies"), "MovieProperties"){
    def id: Rep[Long] = column[Long]("movie_location_id", O.PrimaryKey, O.AutoInc)
    def movieId: Rep[Long] = column[Long]("movie_id")
    def properties: Rep[Map[String, String]] = column[Map[String, String]]("properties")
    //mapping function to the case class
    override def * = (id, movieId, properties) <> (MovieProperties.tupled, MovieProperties.unapply)
  }

  class ActorDetailsTable(tag: Tag) extends Table[ActorDetails](tag, Some("movies"), "ActorDetails"){
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def actorId: Rep[Long] = column[Long]("actor_id")
    def personalInfo: Rep[JsValue] = column[JsValue]("personal_info")
    //mapping function to the case class
    override def * = (id, actorId, personalInfo) <> (ActorDetails.tupled, ActorDetails.unapply)
  }

  // "API entry point"
  lazy val movieLocationTable = TableQuery[MovieLocationTable]
  lazy val moviePropertiesTable = TableQuery[MoviePropertiesTable]
  lazy val actorDetailsTable = TableQuery[ActorDetailsTable]
}

object TableDefinitionGenerator {
  def main(args: Array[String]): Unit = {
    /*
    will create needed sql scripts for creating tables

    create table if not exists "movies"."Movie" ("movie_id" BIGSERIAL NOT NULL PRIMARY KEY,"name" VARCHAR NOT NULL,"release_date" DATE NOT NULL,"length_in_min" INTEGER NOT NULL);
create table if not exists "movies"."Actor" ("actor_id" BIGSERIAL NOT NULL PRIMARY KEY,"name" VARCHAR NOT NULL);
create table if not exists "movies"."MovieActorMapping" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"movie_id" BIGINT NOT NULL,"actor_id" BIGINT NOT NULL);
create table if not exists "movies"."StreamingProviderMapping" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"movie_id" BIGINT NOT NULL,"streaming_provider" VARCHAR NOT NULL)
     */
    println(SlickTables.ddl.createIfNotExistsStatements.mkString("; \n"))
  }
}
