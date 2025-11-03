package com.travelfoodie.core.data.repository

import com.travelfoodie.core.data.local.dao.PoiDao
import com.travelfoodie.core.data.local.entity.PoiEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoiRepository @Inject constructor(
    private val poiDao: PoiDao
) {
    fun getPoisByRegion(regionId: String): Flow<List<PoiEntity>> {
        return poiDao.getPoisByRegion(regionId)
    }

    suspend fun generateMockAttractions(
        regionId: String,
        regionName: String
    ): List<PoiEntity> {
        android.util.Log.d("PoiRepository", "generateMockAttractions called - regionId: $regionId, regionName: $regionName")
        val mockPois = when {
            regionName.contains("서울") -> listOf(
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "경복궁",
                    category = "역사",
                    rating = 4.7f,
                    imageUrl = null,
                    description = "조선시대 대표 궁궐로 아름다운 전통 건축을 감상할 수 있습니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "N서울타워",
                    category = "전망대",
                    rating = 4.5f,
                    imageUrl = null,
                    description = "서울의 야경을 한눈에 볼 수 있는 랜드마크입니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "명동",
                    category = "쇼핑",
                    rating = 4.3f,
                    imageUrl = null,
                    description = "다양한 쇼핑과 먹거리를 즐길 수 있는 번화가입니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "북촌 한옥마을",
                    category = "문화",
                    rating = 4.6f,
                    imageUrl = null,
                    description = "전통 한옥이 보존된 아름다운 마을입니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "한강공원",
                    category = "자연",
                    rating = 4.4f,
                    imageUrl = null,
                    description = "산책과 자전거를 즐길 수 있는 도심 속 휴식처입니다."
                )
            )
            regionName.contains("부산") -> listOf(
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "해운대 해수욕장",
                    category = "해변",
                    rating = 4.8f,
                    imageUrl = null,
                    description = "부산의 대표 해수욕장으로 아름다운 해변을 자랑합니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "감천문화마을",
                    category = "문화",
                    rating = 4.6f,
                    imageUrl = null,
                    description = "알록달록한 집들이 모여있는 예술 마을입니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "자갈치시장",
                    category = "시장",
                    rating = 4.5f,
                    imageUrl = null,
                    description = "신선한 해산물을 맛볼 수 있는 전통 시장입니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "광안대교",
                    category = "랜드마크",
                    rating = 4.7f,
                    imageUrl = null,
                    description = "야경이 아름다운 부산의 상징적인 다리입니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "태종대",
                    category = "자연",
                    rating = 4.6f,
                    imageUrl = null,
                    description = "절벽과 바다가 어우러진 절경을 감상할 수 있습니다."
                )
            )
            regionName.contains("제주") -> listOf(
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "한라산",
                    category = "자연",
                    rating = 4.9f,
                    imageUrl = null,
                    description = "제주도의 상징인 아름다운 산입니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "성산일출봉",
                    category = "자연",
                    rating = 4.8f,
                    imageUrl = null,
                    description = "일출이 아름다운 화산 분화구입니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "섭지코지",
                    category = "해안",
                    rating = 4.7f,
                    imageUrl = null,
                    description = "드라마 촬영지로 유명한 아름다운 해안입니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "우도",
                    category = "섬",
                    rating = 4.6f,
                    imageUrl = null,
                    description = "제주 옆 작은 섬으로 한적한 여행지입니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "천지연 폭포",
                    category = "자연",
                    rating = 4.5f,
                    imageUrl = null,
                    description = "울창한 숲 속의 아름다운 폭포입니다."
                )
            )
            else -> listOf(
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "$regionName 중심가",
                    category = "쇼핑",
                    rating = 4.3f,
                    imageUrl = null,
                    description = "${regionName}의 번화가로 다양한 상점이 있습니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "$regionName 박물관",
                    category = "문화",
                    rating = 4.4f,
                    imageUrl = null,
                    description = "${regionName}의 역사와 문화를 배울 수 있습니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "$regionName 공원",
                    category = "자연",
                    rating = 4.5f,
                    imageUrl = null,
                    description = "산책과 휴식을 즐길 수 있는 공원입니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "$regionName 전통시장",
                    category = "시장",
                    rating = 4.2f,
                    imageUrl = null,
                    description = "지역 특산물과 먹거리를 즐길 수 있습니다."
                ),
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = "$regionName 전망대",
                    category = "전망",
                    rating = 4.6f,
                    imageUrl = null,
                    description = "${regionName}의 전경을 한눈에 볼 수 있습니다."
                )
            )
        }

        android.util.Log.d("PoiRepository", "Inserting ${mockPois.size} POIs into database")
        mockPois.forEach {
            android.util.Log.d("PoiRepository", "  - POI: ${it.name}, regionId: ${it.regionId}")
        }
        poiDao.insertPois(mockPois)
        android.util.Log.d("PoiRepository", "POIs inserted successfully")
        return mockPois
    }

    suspend fun insertPoi(poi: PoiEntity) {
        poiDao.insertPoi(poi)
    }

    suspend fun deletePoi(poi: PoiEntity) {
        poiDao.deletePoi(poi)
    }
}
