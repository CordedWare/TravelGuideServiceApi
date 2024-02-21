package com.travelguide

import cats.effect.{IO, Ref, Resource}
import cats.implicits.*
import cats.effect.unsafe.implicits.global
import org.apache.jena.query.{QueryExecution, QueryFactory, QuerySolution}
import org.apache.jena.rdfconnection.{RDFConnection, RDFConnectionRemote}

import scala.concurrent.duration.*
import scala.jdk.javaapi.CollectionConverters.asScala
import com.travelguide.model.Algebra.*
import com.travelguide.model.Algebra.AttractionOrdering.*
import com.travelguide.model.Algebra.PopCultureSubject.*
import com.travelguide.service.WikiDataAccessImplements.getSparqlDataAccess
import com.travelguide.service.WikiDataAccessInterface
import com.travelguide.util.UnsafeRunTimedIOApp.*

object ServerApp {

    def createExecution(connection: RDFConnection, query: String): IO[QueryExecution] = 
        IO.blocking(connection.query(QueryFactory.create(query)))

    def closeExecution(execution: QueryExecution): IO[Unit] = 
        IO.blocking(execution.close())

    def execQuery(connection: RDFConnection)(query: String): IO[List[QuerySolution]] = {
        val executionResource: Resource[IO, QueryExecution] = Resource.make(createExecution(connection, query))(closeExecution)
        executionResource.use(execution => IO.blocking(asScala(execution.execSelect()).toList))
    }

    val connectionResource: Resource[IO, RDFConnection] = Resource.make(
        IO.blocking(
            RDFConnectionRemote.create
                .destination("https://query.wikidata.org/")
                .queryEndpoint("sparql")
                .build
        ))(connection => IO.blocking(connection.close()))

    // // // // //
//    val dataAccessResource: Resource[IO, WikiDataAccessInterface] =
//        connectionResource.map(connection => getSparqlDataAccess(execQuery(connection)))
    // // // // //
}
