package com.travelguide.service

import cats.effect.IO
import com.travelguide.model.Algebra._
import com.travelguide.model.Algebra.AttractionOrdering._
import com.travelguide.model.Algebra.PopCultureSubject._
import org.apache.jena.query.QuerySolution

object WikiDataAccessImplements  {

    def getSparqlDataAccess(execQuery: String => IO[List[QuerySolution]]): WikiDataAccessInterface = new WikiDataAccessInterface {

        val prefixes = """
              |  PREFIX wd:     <http://www.wikidata.org/entity/>
              |  PREFIX wdt:    <http://www.wikidata.org/prop/direct/>
              |  PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>
              |  PREFIX schema: <http://schema.org/>
              |  """.stripMargin

        def findAttractions(name: String, ordering: AttractionOrdering, limit: Int, lang: String): IO[List[Attraction]] = {
            val orderBy = ordering match {
                case ByName               => "?attractionLabel"
                case ByLocationPopulation => "DESC(?population)"
            }

            val query = s"""
                    |  $prefixes
                    |  SELECT DISTINCT
                    |  ?attraction
                    |  ?attractionLabel
                    |  ?description
                    |  ?location
                    |  ?locationLabel
                    |  ?population
                    |  WHERE {
                    |  ?attraction wdt:P31    wd:Q570116;
                    |              rdfs:label ?attractionLabel;
                    |              wdt:P131   ?location.
                    |  FILTER(LANG(?attractionLabel) = "en").
                    |
                    |  OPTIONAL {
                    |    ?attraction schema:description ?description.
                    |    FILTER(LANG(?description) = "$lang").
                    |  }
                    |
                    |  ?location wdt:P1082  ?population;
                    |            rdfs:label ?locationLabel;
                    |  FILTER(LANG(?locationLabel) = "$lang").
                    |
                    |  FILTER(CONTAINS(?attractionLabel, "$name")).
                    |  } ORDER BY $orderBy LIMIT $limit
                    |  """.stripMargin
            for {
                solutions   <- execQuery(query)
                attractions <- IO.delay(
                    solutions.map( s =>
                        Attraction( // вводим именованные параметры
                            name = s.getLiteral("attractionLabel").getString,
                            description =
                                if (s.contains("description")) Some(s.getLiteral("description").getString) else None,
                            location = Location(
                                LocationId(s.getResource("location").getLocalName),
                                s.getLiteral("locationLabel").getString,
                                s.getLiteral("population").getInt
                            )
                        )
                    )
                )
            } yield attractions
        }

        def findArtistsFromLocation(locationId: LocationId, limit: Int, lang: String): IO[List[Artist]] = {
            val query = s"""
                     |  $prefixes
                     |  SELECT DISTINCT
                     |  ?artist
                     |  ?artistLabel
                     |  ?followers
                     |  WHERE {
                     |  ?artist wdt:P136   ?genre;
                     |          wdt:P8687  ?followers;
                     |          rdfs:label ?artistLabel.
                     |  FILTER(LANG(?artistLabel) = "$lang").
                     |
                     |  ?artist wdt:P740 wd:${locationId.value}
                     |
                     |  } ORDER BY DESC(?followers) LIMIT $limit
                     |  """.stripMargin
            for {
                solutions <- execQuery(query)
                artists   <-
                    IO.delay(
                        solutions.map[Artist]( s =>
                            Artist(
                                name      = s.getLiteral("artistLabel").getString,
                                followers = s.getLiteral("followers").getInt
                            )
                        )
                    )
            } yield artists
        }

        def findMoviesAboutLocation(locationId: LocationId, limit: Int, lang: String): IO[List[Movie]] = {
            val query = s"""
                     |  $prefixes
                     |  SELECT DISTINCT
                     |  ?subject
                     |  ?subjectLabel
                     |  ?boxOffice
                     |  WHERE {
                     |  ?subject wdt:P31    wd:Q11424;
                     |           wdt:P2142  ?boxOffice;
                     |           rdfs:label ?subjectLabel.
                     |
                     |  ?subject wdt:P840 wd:${locationId.value}
                     |
                     |  FILTER(LANG(?subjectLabel) = "$lang").
                     |
                     |  } ORDER BY DESC(?boxOffice) LIMIT $limit
                     |  """.stripMargin
            for {
                solutions <- execQuery(query)
                movies    <- IO.delay(
                    solutions.map[Movie](s =>
                        Movie(
                            name      = s.getLiteral("subjectLabel").getString,
                            boxOffice = s.getLiteral("boxOffice").getInt
                        )
                    )
                )
            } yield movies
        }

    }

}
