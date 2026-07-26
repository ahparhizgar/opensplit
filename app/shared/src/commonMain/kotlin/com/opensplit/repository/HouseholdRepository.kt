package com.opensplit.repository

import com.opensplit.db.HouseholdDao
import com.opensplit.db.toDto
import com.opensplit.db.toEntity
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.features.household.HouseholdApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HouseholdRepository(private val api: HouseholdApi, private val dao: HouseholdDao) {
  fun getHouseholds(): Flow<List<HouseholdDto>> {
    return dao.getHouseholds().map { entities -> entities.map { it.toDto() } }
  }

  suspend fun refreshHouseholds() {
    val result = api.loadOverview()
    dao.insertHouseholds(result.map { it.toEntity() })
  }

  suspend fun getHousehold(id: String): HouseholdDto? {
    return dao.getHousehold(id)?.toDto()
  }

  suspend fun refreshHousehold(id: String): HouseholdDto {
    val result = api.getHousehold(id)
    dao.insertHouseholds(listOf(result.toEntity()))
    return result
  }

  suspend fun createHousehold(name: String): HouseholdDto {
    val result = api.createHousehold(name)
    dao.insertHouseholds(listOf(result.toEntity()))
    return result
  }

  suspend fun leaveHousehold(householdId: String) {
    api.leaveHousehold(householdId)
    dao.deleteById(householdId)
  }
}
