package com.zenobank.u7.planet.entity

import io.quarkus.mongodb.panache.PanacheMongoEntity
import io.quarkus.mongodb.panache.common.MongoEntity
import java.time.LocalDateTime
import java.util.UUID

@MongoEntity(collection = "planets")
class Planet(
    var name: String,
    var galaxy: String? = null,
    var population: Long = 0,
    var status: String = "ACTIVE",
    var planetId: UUID = UUID.randomUUID(),
    var createdAt: LocalDateTime = LocalDateTime.now()
) : PanacheMongoEntity()