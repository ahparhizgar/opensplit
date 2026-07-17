package com.opensplit.features.household

import com.opensplit.dto.household.HouseholdDto
import com.opensplit.dto.household.HouseholdMemberDto
import com.opensplit.dto.household.HouseholdOverviewDto
import com.opensplit.dto.household.HouseholdSummaryDto
import com.opensplit.features.auth.AuthUser

class HouseholdService(private val householdRepository: HouseholdRepository) {
  fun loadOverview(user: AuthUser): HouseholdOverviewDto =
      HouseholdOverviewDto(
          households =
              householdRepository.loadHouseholdSummaries(user.id).map { summary ->
                HouseholdSummaryDto(
                    id = summary.id,
                    name = summary.name,
                    memberCount = summary.memberCount,
                    isOwner = summary.isOwner,
                    inviteCode = summary.inviteCode,
                )
              },
          members = emptyList(),
      )

  fun createHousehold(user: AuthUser, name: String): HouseholdDto {
    val household = householdRepository.createHousehold(name, user.id)
    return HouseholdDto(
        id = household.id,
        name = household.name,
        members =
            listOf(
                HouseholdMemberDto(
                    userId = user.id,
                    name = user.name,
                    email = user.email,
                    isOwner = true,
                    isCurrentUser = true,
                    balance = 0.0,
                    balanceCurrency = "IRR",
                )
            ),
        inviteLink = household.inviteLink(),
    )
  }

  fun joinHousehold(user: AuthUser, inviteCodeOrIdOrLink: String): JoinHouseholdResult {
    val inviteCode = inviteCodeOrIdOrLink.removePrefix("https://opensplit.com/join/")
    val householdByInvite = householdRepository.findHouseholdByInviteCode(inviteCode)
    val household = householdByInvite ?: householdRepository.findHouseholdById(inviteCodeOrIdOrLink)
    if (household == null) {
      return JoinHouseholdResult.InvalidInviteCode
    }

    if (
        householdByInvite == null &&
            household.ownerId != user.id &&
            !householdRepository.hasMembership(household.id, user.id)
    ) {
      return JoinHouseholdResult.MissingPermission
    }

    householdRepository.ensureMembership(household.id, user.id)
    val detail =
        householdRepository.loadHouseholdDetail(household.id, user.id)
            ?: return JoinHouseholdResult.InvalidInviteCode
    return JoinHouseholdResult.Success(detail.toDto(user.id))
  }

  fun addMemberByEmail(user: AuthUser, householdId: String, email: String): AddMemberByEmailResult {
    val household =
        householdRepository.findHouseholdById(householdId)
            ?: return AddMemberByEmailResult.HouseholdNotFound
    if (household.ownerId != user.id) {
      return AddMemberByEmailResult.Forbidden
    }

    val targetUser =
        householdRepository.findMemberByEmail(email)
            ?: return AddMemberByEmailResult.UserNotFound(email)

    householdRepository.ensureMembership(householdId, targetUser.userId)
    val detail =
        householdRepository.loadHouseholdDetail(householdId, user.id)
            ?: return AddMemberByEmailResult.HouseholdNotFound
    return AddMemberByEmailResult.Success(detail.toDto(user.id))
  }

  fun leaveHousehold(user: AuthUser, householdId: String): HouseholdOverviewDto {
    householdRepository.leaveHousehold(householdId, user.id)
    return loadOverview(user)
  }

  fun getHousehold(user: AuthUser, householdId: String): HouseholdDto? =
      householdRepository.loadHouseholdDetail(householdId, user.id)?.toDto(user.id)

  private fun HouseholdDetailRecord.toDto(currentUserId: String): HouseholdDto =
      HouseholdDto(
          id = household.id,
          name = household.name,
          members =
              members.map { member ->
                HouseholdMemberDto(
                    userId = member.userId,
                    name = member.name,
                    email = member.email,
                    isOwner = member.userId == household.ownerId,
                    isCurrentUser = member.userId == currentUserId,
                    balance = if (member.userId == currentUserId) 10.15 else -10.15,
                    balanceCurrency = "IRR",
                )
              },
          inviteLink = household.inviteLink(),
      )

  private fun HouseholdRecord.inviteLink(): String =
      "https://opensplit.com/join/${inviteCode.orEmpty()}"
}
