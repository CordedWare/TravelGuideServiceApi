package com.travelguide.service

import cats.effect.IO
import com.travelguide.model.Algebra.*
import com.travelguide.model.Algebra.AttractionOrdering.*
import com.travelguide.model.Algebra.PopCultureSubject.*

trait WikiDataAccessInterface {
    def findAttractions(name: String, ordering: AttractionOrdering, limit: Int, lang: String): IO[List[Attraction]]
    def findArtistsFromLocation(locationId: LocationId, limit: Int, lang: String): IO[List[Artist]]
    def findMoviesAboutLocation(locationId: LocationId, limit: Int, lang: String): IO[List[Movie]]
}
