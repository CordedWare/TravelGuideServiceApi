package com.travelguide

import cats.effect.{IO, Ref, Resource}
import cats.implicits.*
import cats.effect.unsafe.implicits.global
import com.travelguide.ServerApp.*
import org.apache.jena.query.{QueryExecution, QueryFactory, QuerySolution}
import org.apache.jena.rdfconnection.{RDFConnection, RDFConnectionRemote}

import scala.concurrent.duration.*
import scala.jdk.javaapi.CollectionConverters.asScala
import com.travelguide.model.Algebra.*
import com.travelguide.model.Algebra.AttractionOrdering.*
import com.travelguide.model.Algebra.PopCultureSubject.*
import com.travelguide.service.WikiDataAccessImplements.getSparqlDataAccess
import com.travelguide.service.WikiDataAccessInterface
import com.travelguide.util.Cache.cachedExecQuery
import com.travelguide.util.UnsafeRunTimedIOApp.*


object TravelGuideService extends App {

    /** FUNCTIONAL: делаем его согласованным (concurrent)
     */
    def travelGuide(data: WikiDataAccessInterface, attractionName: String, lang: String): IO[Option[TravelGuide]] = {
        for {
            attractions <- data.findAttractions(attractionName, ByLocationPopulation, 3, lang)
            guides      <- attractions
                .map { attraction =>
                    List(
                        data.findArtistsFromLocation(attraction.location.id, 2, lang),
                        data.findMoviesAboutLocation(attraction.location.id, 2, lang)
                    ).parSequence.map(_.flatten).map { popCultureSubjects =>
                        TravelGuide(attraction, popCultureSubjects)
                    }
                }
                .parSequence
        } yield guides
            .sortBy(guideScore)
            .reverse
            .headOption
    }

    def populationScore(guide: TravelGuide): Int = guide.attraction.location.population

    /** FUNCTIONAL: поиск лучшего путеводителя
     * требования:
     * - 30 баллов за описание
     * - 10 баллов за каждого исполнителя и фильм (максимум 40 баллов)
     * - 1 балл за каждые 100_000 подписчиков (все артисты вместе взятые, максимум 15 баллов)
     * - 1 балл за каждые 10_000_000 долларов общих кассовых сборов (все фильмы вместе, максимум 15 баллов)
     */
    def guideScore(guide: TravelGuide): Int = {
        val descriptionScore = guide.attraction.description.map(_ => 30).getOrElse(0)
        val quantityScore    = Math.min(40, guide.subjects.size * 10)

        val totalFollowers = guide.subjects
            .map {
                case Artist(_, followers) => followers
                case _                    => 0
            }.sum

        val totalBoxOffice = guide.subjects
            .map {
                case Movie(_, boxOffice) => boxOffice
                case _                   => 0
            }.sum

        val followersScore = Math.min(15, totalFollowers / 100_000)
        val boxOfficeScore = Math.min(15, totalBoxOffice / 10_000_000)
        descriptionScore + quantityScore + followersScore + boxOfficeScore
    }

    /** ШАГ 7: сделайте это быстрее
     * нам не нужно выполнять запросы, мы можем кэшировать их локально
     */
    unsafeRunTimedIO(
        connectionResource.use(connection =>
            for {
                cache        <- Ref.of[IO, Map[String, List[QuerySolution]]](Map.empty)
                cachedSparql = getSparqlDataAccess(
                    cachedExecQuery(connection, cache).map(_.timeout(15.seconds))  // обратите внимание, что мы отображаем результат функции.
                ) // каждый запрос завершится неудачей через 30 секунд, освобождая все ресурсы
                result1 <- travelGuide(cachedSparql, "Moscow", "ru")
                result2 <- travelGuide(cachedSparql, "Yekaterinburg", "ru")
                result3 <- travelGuide(cachedSparql, "Kazan", "ru")
                result4 <- travelGuide(cachedSparql, "Krasnodar", "ru")
                result5 <- travelGuide(cachedSparql, "Samara", "ru")
            } yield result1
                .toList
                .appendedAll(result2)
                .appendedAll(result3)
                .appendedAll(result4)
                .appendedAll(result5)
                .sortBy(populationScore)
                .reverse
        )
    ) // второе и третье выполнение займут гораздо меньше времени, поскольку все запросы кэшируются


//    unsafeRunTimedIO(dataAccessResource.use(dataAccess => travelGuide(dataAccess, "Yekaterinburg", "ru")))

}

