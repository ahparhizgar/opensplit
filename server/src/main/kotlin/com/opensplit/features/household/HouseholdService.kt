package com.opensplit.features.household

import com.opensplit.dto.household.HouseholdDto
import com.opensplit.dto.household.HouseholdMemberDto
import com.opensplit.dto.household.HouseholdSummaryDto
import com.opensplit.features.auth.UserPrincipal

class HouseholdService(private val householdRepository: HouseholdRepository) {
  fun loadHouseholds(user: UserPrincipal): List<HouseholdSummaryDto> =
      householdRepository.loadHouseholdSummaries(user.userId).map { summary ->
        HouseholdSummaryDto(
            id = summary.id,
            name = summary.name,
            memberCount = summary.memberCount,
            isOwner = summary.isOwner,
            inviteCode = summary.inviteCode,
        )
      }

  fun createHousehold(user: UserPrincipal, name: String): HouseholdDto {
    val household = householdRepository.createHousehold(name, user.userId)
    return HouseholdDto(
        id = household.id,
        name = household.name,
        members =
            listOf(
                HouseholdMemberDto(
                    userId = user.userId,
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

  fun joinHousehold(user: UserPrincipal, inviteCodeOrIdOrLink: String): JoinHouseholdResult {
    val inviteCode = inviteCodeOrIdOrLink.removePrefix("https://opensplit.com/join/")
    val householdByInvite = householdRepository.findHouseholdByInviteCode(inviteCode)
    val household = householdByInvite ?: householdRepository.findHouseholdById(inviteCodeOrIdOrLink)
    if (household == null) {
      return JoinHouseholdResult.InvalidInviteCode
    }

    if (
        householdByInvite == null &&
            household.ownerId != user.userId &&
            !householdRepository.hasMembership(household.id, user.userId)
    ) {
      return JoinHouseholdResult.MissingPermission
    }

    householdRepository.ensureMembership(household.id, user.userId)
    val detail =
        householdRepository.loadHouseholdDetail(household.id, user.userId)
            ?: return JoinHouseholdResult.InvalidInviteCode
    return JoinHouseholdResult.Success(detail.toDto(user.userId))
  }

  fun addMemberByEmail(
      user: UserPrincipal,
      householdId: String,
      email: String,
  ): AddMemberByEmailResult {
    val household =
        householdRepository.findHouseholdById(householdId)
            ?: return AddMemberByEmailResult.HouseholdNotFound
    if (household.ownerId != user.userId) {
      return AddMemberByEmailResult.Forbidden
    }

    val targetUser =
        householdRepository.findMemberByEmail(email)
            ?: return AddMemberByEmailResult.UserNotFound(email)

    householdRepository.ensureMembership(householdId, targetUser.userId)
    val detail =
        householdRepository.loadHouseholdDetail(householdId, user.userId)
            ?: return AddMemberByEmailResult.HouseholdNotFound
    return AddMemberByEmailResult.Success(detail.toDto(user.userId))
  }

  fun leaveHousehold(user: UserPrincipal, householdId: String): List<HouseholdSummaryDto> {
    householdRepository.leaveHousehold(householdId, user.userId)
    return loadHouseholds(user)
  }

  fun getHousehold(user: UserPrincipal, householdId: String): HouseholdDto? =
      householdRepository.loadHouseholdDetail(householdId, user.userId)?.toDto(user.userId)

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
