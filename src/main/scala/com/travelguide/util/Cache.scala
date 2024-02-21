package com.travelguide.util

import cats.effect.{IO, Ref}
import com.travelguide.ServerApp.execQuery
import org.apache.jena.query.QuerySolution
import org.apache.jena.rdfconnection.RDFConnection

object Cache {
    /** нам не нужно выполнять запросы, мы можем кэшировать их локально
     */
    def cachedExecQuery(connection: RDFConnection, cache: Ref[IO, Map[String, List[QuerySolution]]])(query: String):
    IO[List[QuerySolution]] = {
        for {
            cachedQueries <- cache.get
            solutions     <- cachedQueries.get(query) match {
                case Some(cachedSolutions) => IO.pure(cachedSolutions)
                case None                  => for {
                    realSolutions <- execQuery(connection)(query)
                    _             <- cache.update(_.updated(query, realSolutions))
                } yield realSolutions
            }
        } yield solutions
    }
}
