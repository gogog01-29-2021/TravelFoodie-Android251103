package com.travelfoodie.core.data.repository

import com.travelfoodie.core.data.local.dao.*
import com.travelfoodie.core.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val tripDao: TripDao,
    private val memberDao: MemberDao,
    private val regionDao: RegionDao,
    private val notifScheduleDao: NotifScheduleDao
) {
    fun getTripsByUser(userId: String): Flow<List<TripEntity>> {
        return tripDao.getTripsByUser(userId)
    }

    suspend fun getTripById(tripId: String): TripEntity? {
        return tripDao.getTripById(tripId)
    }

    suspend fun insertTrip(trip: TripEntity) {
        tripDao.insertTrip(trip)
        scheduleNotifications(trip)
    }

    suspend fun updateTrip(trip: TripEntity) {
        tripDao.updateTrip(trip)
        // Reschedule notifications
        notifScheduleDao.deleteSchedulesByTrip(trip.tripId)
        scheduleNotifications(trip)
    }

    suspend fun deleteTrip(trip: TripEntity) {
        tripDao.deleteTrip(trip)
    }

    suspend fun getNextTrip(currentTime: Long): TripEntity? {
        return tripDao.getNextTrip(currentTime)
    }

    private suspend fun scheduleNotifications(trip: TripEntity) {
        val schedules = mutableListOf<NotifScheduleEntity>()
        
        // D-7
        val d7 = trip.startDate - (7 * 24 * 60 * 60 * 1000)
        if (d7 > System.currentTimeMillis()) {
            schedules.add(NotifScheduleEntity(tripId = trip.tripId, fireAt = d7, type = "D-7"))
        }
        
        // D-3
        val d3 = trip.startDate - (3 * 24 * 60 * 60 * 1000)
        if (d3 > System.currentTimeMillis()) {
            schedules.add(NotifScheduleEntity(tripId = trip.tripId, fireAt = d3, type = "D-3"))
        }
        
        // D-0
        if (trip.startDate > System.currentTimeMillis()) {
            schedules.add(NotifScheduleEntity(tripId = trip.tripId, fireAt = trip.startDate, type = "D-0"))
        }
        
        if (schedules.isNotEmpty()) {
            notifScheduleDao.insertSchedules(schedules)
        }
    }

    fun getMembersByTrip(tripId: String): Flow<List<MemberEntity>> {
        return memberDao.getMembersByTrip(tripId)
    }

    suspend fun insertMember(member: MemberEntity) {
        memberDao.insertMember(member)
    }

    suspend fun deleteMember(member: MemberEntity) {
        memberDao.deleteMember(member)
    }

    fun getRegionsByTrip(tripId: String): Flow<List<RegionEntity>> {
        return regionDao.getRegionsByTrip(tripId)
    }

    suspend fun getRegionById(regionId: String): RegionEntity? {
        return regionDao.getRegionById(regionId)
    }

    suspend fun insertRegion(region: RegionEntity) {
        regionDao.insertRegion(region)
    }

    suspend fun insertRegions(regions: List<RegionEntity>) {
        regionDao.insertRegions(regions)
    }

    suspend fun deleteRegion(region: RegionEntity) {
        regionDao.deleteRegion(region)
    }
}
