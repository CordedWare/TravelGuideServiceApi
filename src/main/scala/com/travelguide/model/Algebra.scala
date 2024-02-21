package com.travelguide.model

object Algebra {
  
    opaque type LocationId = String
    object LocationId {
        def apply(value: String): LocationId = value
        extension (a: LocationId) def value: String = a
    }

    enum PopCultureSubject {
        case Artist(name: String, followers: Int)
        case Movie(name: String, boxOffice: Int)
    }

    enum AttractionOrdering {
        case ByName
        case ByLocationPopulation
    }

    case class Location(id: LocationId, name: String, population: Int)

    case class Attraction(name: String, description: Option[String], location: Location)

    case class TravelGuide(attraction: Attraction, subjects: List[PopCultureSubject])
}