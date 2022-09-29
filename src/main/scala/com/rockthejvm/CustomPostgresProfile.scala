package com.rockthejvm

import com.github.tminglei.slickpg.utils.SimpleArrayUtils
import com.github.tminglei.slickpg.{ExPostgresProfile, PgArraySupport, PgHStoreSupport, PgJsonSupport, PgPlayJsonSupport, PgSearchSupport}
import play.api.libs.json.{JsValue, Json}

trait CustomPostgresProfile extends ExPostgresProfile
    with PgArraySupport
    with PgHStoreSupport with PgJsonSupport with PgPlayJsonSupport
    with PgSearchSupport{

  override def pgjson = "jsonb"
  override val api = CustomPostgresAPI
  override protected def computeCapabilities: Set[slick.basic.Capability] =
    super.computeCapabilities + slick.jdbc.JdbcCapabilities.insertOrUpdate

  object CustomPostgresAPI extends API with ArrayImplicits with HStoreImplicits with JsonImplicits
  with SearchImplicits with SearchAssistants {

    implicit val stringListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val playJsonArrayTypeMapper = new AdvancedArrayJdbcType[JsValue](
        pgjson,
        string => SimpleArrayUtils.fromString(Json.parse)(string).orNull,
        value => SimpleArrayUtils.mkString[JsValue](_.toString())(value)
      ).to(_.toList)
  }



}

object CustomPostgresProfile extends CustomPostgresProfile
