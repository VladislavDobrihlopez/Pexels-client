package com.voitov.pexels_app.data.mapper

import com.voitov.pexels_app.data.network.dto.featured_collection.CollectionDto
import com.voitov.pexels_app.data.network.dto.photo.PhotoDto
import com.voitov.pexels_app.domain.models.FeaturedCollection
import com.voitov.pexels_app.domain.models.Photo
import javax.inject.Inject

class PexelsMapper @Inject constructor() {
    fun mapDtoToDomainModel(dto: CollectionDto): FeaturedCollection {
        return FeaturedCollection(id = dto.id, title = dto.title)
    }

    fun mapDtoToDomainModel(dto: PhotoDto): Photo {
        return Photo(id = dto.id, url = dto.source.url)
    }
}