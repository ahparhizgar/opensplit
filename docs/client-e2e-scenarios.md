# Client E2E Test Scenarios

## Test Philosophy & Setup
- **Component E2E / Flow Tests**: Test Decompose tree, Repositories, Room DB, Sync, State without Compose UI rendering. Fast, robust, preferred when no UI logic under test.
- **UI E2E Tests**: Test full Compose hierarchy with [`App(root)`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/App.kt). Verify keyboard actions, navigation animations, clicks, text replacement, screen elements.
- **Mocks vs Fakes**: Use fake APIs ([`FakeAuthApi`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/fake/FakeAuthApi.kt), [`FakeGroupApi`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/fake/FakeGroupApi.kt), [`FakeExpenseApi`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/fake/FakeExpenseApi.kt), [`FakeSyncApi`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/fake/FakeSyncApi.kt)) and in-memory Room database.
- **Naming Rule**: Name tests after **user journeys and flows** (e.g. `CreateGroupFlowE2eTest`), not isolated classes (e.g. `GroupComponentTest`).

---

## Existing Test Coverage

### UI E2E (`app/shared/src/commonTest/kotlin/com/opensplit/e2e/ui/`)
- [x] [`AuthE2eUiTest.kt`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/e2e/ui/AuthE2eUiTest.kt) *(Candidate to rename to `AuthenticationFlowUiTest`)*
  - [x] `testLoginFlowHappyPath`: Welcome -> Login -> enter email/password via keyboard -> group list.
- [x] [`AddExpenseE2eUiTest.kt`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/e2e/ui/AddExpenseE2eUiTest.kt) *(Candidate to rename to `ExpenseCreationFlowUiTest`)*
  - [x] `testEqualSplit`: Add expense -> default equal split -> saved -> displayed.
  - [x] `testExactAmounts`: Add expense -> Unequally split -> exact amounts per user -> saved.
  - [x] `testPercentageSplit`: Add expense -> Percentage split -> percentages per user -> saved.
  - [x] `testMultiplePayers`: Add expense -> Paid by multiple people -> custom amounts -> saved.

### Component E2E (`app/shared/src/commonTest/kotlin/com/opensplit/e2e/component/`)
- [x] [`AuthE2eComponentTest.kt`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/e2e/component/AuthE2eComponentTest.kt) *(Candidate to rename to `LoginFlowE2eComponentTest`)*
  - [x] `E2EAuthTest`: Cold start -> Splash -> Welcome -> Login -> credentials -> [`MyGroupsListComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/my/MyGroupsListComponent.kt).
- [x] [`ExpenseSyncE2eComponentTest.kt`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/e2e/component/ExpenseSyncE2eComponentTest.kt) *(Candidate to rename to `ExpenseSyncFlowE2eTest`)*
  - [x] Client-to-server sync: local add -> optimistic UI -> outbox sync -> API verified; local edit -> optimistic UI -> outbox sync; local delete -> optimistic UI -> outbox delete call.
  - [x] Server-to-client sync: remote new expense -> sync pull -> UI updated & balances recalculated; remote edit -> sync pull -> UI & balances updated; remote delete -> sync pull -> UI empty & balances 0.0.
- [x] [`GroupBalanceE2eComponentTest.kt`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/e2e/component/GroupBalanceE2eComponentTest.kt) *(Candidate to rename to `GroupBalancePropagationFlowTest`)*
  - [x] Group balance updates across [`MyGroupsListComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/my/MyGroupsListComponent.kt) and [`GroupDetailsComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/details/GroupDetailsComponent.kt) for local & remote add, update, delete.

---

## Missing Scenarios by Layer (Journey & Flow Based)

### Layer 1: Component-Level Flow & E2E Tests (`app/shared/src/commonTest/kotlin/com/opensplit/e2e/component/`)
*Prefer component level when there is no UI logic (pure state transitions, navigation, database, and sync logic).*

- [x] **C1: Auto-Login on Startup with Stored Token**
  - **Test Class**: [`AutoLoginStartupFlowTest`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/e2e/component/AutoLoginStartupFlowTest.kt)
  - **Precondition**: [`TokenStorage`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/auth/TokenStorage.kt) contains valid token before root creation.
  - **Flow**: Init [`DefaultRootComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/root/RootComponent.kt). Advance coroutines.
  - **Assertion**: Active stack instance is [`MyGroupsListComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/my/MyGroupsListComponent.kt). [`AuthComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/auth/AuthComponent.kt) never created.

- [x] **C2: Sign Up Happy Path End-to-End**
  - **Test Class**: [`SignUpFlowE2eTest`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/e2e/component/SignUpFlowE2eTest.kt)
  - **Flow**: App opens -> [`AuthComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/auth/AuthComponent.kt) -> `onSignUpClicked()` -> [`SignUpComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/auth/SignUpComponent.kt) -> `onEmailChanged()`, `onPasswordChanged()` -> `onDoneClicked()`.
  - **Assertion**: [`FakeAuthApi.signUp()`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/fake/FakeAuthApi.kt#L11) called -> token saved in [`TokenStorage`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/auth/TokenStorage.kt) -> user saved in [`ProfileRepository`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/repository/ProfileRepository.kt) -> root stack navigates to [`MyGroupsListComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/my/MyGroupsListComponent.kt).

- [x] **C3: Logout Clears Tokens, Profile, and DB Tables**
  - **Test Class**: [`LogoutAndSessionClearFlowTest`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/e2e/component/LogoutAndSessionClearFlowTest.kt)
  - **Precondition**: Logged-in user with cached groups and expenses in [`AppDatabase`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/db/AppDatabase.kt).
  - **Flow**: In [`ProfileComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/profile/ProfileComponent.kt) -> call `onLogoutClicked().join()`.
  - **Assertion**: Token cleared from [`TokenStorage`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/auth/TokenStorage.kt) -> profile null in [`ProfileRepository`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/repository/ProfileRepository.kt) -> all Room DB tables empty -> root stack replaced with [`AuthComponent.Config`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/auth/AuthComponent.kt).

- [x] **C4: Create Group Flow Navigation & Persistence**
  - **Test Class**: [`CreateGroupFlowE2eTest`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/e2e/component/CreateGroupFlowE2eTest.kt)
  - **Flow**: [`MyGroupsListComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/my/MyGroupsListComponent.kt) -> `onAddGroupClick()` -> [`GroupSelectionComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/createjoin/GroupSelectionComponent.kt) -> `onCreateGroupClicked()` -> [`CreateGroupComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/createjoin/CreateGroupComponent.kt) -> `updateGroupName("New House")` -> `submit().join()`.
  - **Assertion**: [`FakeGroupApi.createGroup()`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/fake/FakeGroupApi.kt#L23) called -> group stored in local DB -> navigation transitions to [`GroupDetailsComponent.Config`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/details/GroupDetailsComponent.kt#L35).

- [x] **C5: Join Group by Invite Code Navigation & Persistence**
  - **Test Class**: [`JoinGroupFlowE2eTest`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/e2e/component/JoinGroupFlowE2eTest.kt)
  - **Flow**: [`MyGroupsListComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/my/MyGroupsListComponent.kt) -> `onAddGroupClick()` -> [`GroupSelectionComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/createjoin/GroupSelectionComponent.kt) -> `onJoinGroupClicked()` -> [`JoinGroupComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/createjoin/JoinGroupComponent.kt) -> `updateInviteCode("code123")` -> `submit().join()`.
  - **Assertion**: [`FakeGroupApi.joinGroup()`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/fake/FakeGroupApi.kt#L33) called -> joined group persisted in DB -> navigation transitions to [`GroupDetailsComponent.Config`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/details/GroupDetailsComponent.kt#L35).

- [x] **C6: Leave Group Flow from Settings**
  - **Test Class**: [`LeaveGroupFlowE2eTest`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/e2e/component/LeaveGroupFlowE2eTest.kt)
  - **Flow**: In [`GroupSettingsComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/settings/GroupSettingsComponent.kt) -> `onLeaveGroupClicked()`.
  - **Assertion**: [`FakeGroupApi.leaveGroup()`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/fake/FakeGroupApi.kt#L76) called -> group deleted from Room DB -> navigation pops -> [`MyGroupsListComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/my/MyGroupsListComponent.kt) list updates and excludes left group.

- [x] **C7: Offline Outbox Queue & Reconnect Sync**
  - **Test Class**: [`OfflineExpenseSyncFlowTest`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/e2e/component/OfflineExpenseSyncFlowTest.kt)
  - **Precondition**: Set [`fakeExpenseApi.disconnect()`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/util/FakeService.kt#L13).
  - **Flow**:
    1. Create expense via [`AddExpenseComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/expense/AddExpenseComponent.kt).
    2. Save expense -> stored locally with `SyncStatus.PENDING` -> outbox queued in [`SyncQueueDao`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/db/DAOs.kt).
    3. Daemon sync fails silently without crash.
    4. Set [`fakeExpenseApi.connect()`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/util/FakeService.kt#L17). Trigger sync daemon.
  - **Assertion**: Outbox queue empty -> expense status becomes `SyncStatus.SYNCED` -> [`FakeExpenseApi.createdExpenses`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/fake/FakeExpenseApi.kt#L13) contains item.

- [ ] **C8: Split Method Shares Math & Balance Propagation**
  - **Test Class**: `SharesSplitMethodFlowTest`
  - **Flow**: Create expense of 90.0 with [`SplitMethod.Shares`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/expense/AddExpenseFlowComponent.kt) (User1: 2 shares, User2: 1 share).
  - **Assertion**: Shares saved as 60.0 and 30.0 -> synced to server -> member balances reflect shares accurately in [`GroupDetailsComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/details/GroupDetailsComponent.kt).

- [ ] **C9: Split Method Adjustment Math & Balance Propagation**
  - **Test Class**: `AdjustmentSplitMethodFlowTest`
  - **Flow**: Create expense of 50.0 with [`SplitMethod.Adjustment`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/expense/AddExpenseFlowComponent.kt) (User1: +10.0 adjustment).
  - **Assertion**: User1 share 30.0, User2 share 20.0 -> persisted & synced -> balances updated in [`GroupDetailsComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/details/GroupDetailsComponent.kt).

- [ ] **C10: Change Payer to Other Member Flow**
  - **Test Class**: `ChangePayerExpenseFlowTest`
  - **Flow**: In [`WhoPaidComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/expense/WhoPaidComponent.kt) -> select `user-2` as payer -> equal 50/50 split -> save expense.
  - **Assertion**: `user-2` paid 100% -> `user-1` owes `user-2` -> [`GroupDetailsComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/details/GroupDetailsComponent.kt) and [`MyGroupsListComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/my/MyGroupsListComponent.kt) show negative balance for current user.

- [ ] **C11: Large Screen Adaptive Dual-Panel Navigation Flow**
  - **Test Class**: `AdaptiveDualPanelNavigationFlowTest`
  - **Flow**: Set window size class to Medium/Expanded (`ChildPanelsMode.DUAL`) in [`GroupFlowComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/details/GroupFlowComponent.kt) -> select expense -> select settings.
  - **Assertion**: Details panel activates [`ExpenseDetailsComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/expense/ExpenseDetailsComponent.kt) concurrently without hiding [`GroupDetailsComponent`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/details/GroupDetailsComponent.kt); switching to settings changes details panel while main panel persists.

---

### Layer 2: UI E2E Tests (`app/shared/src/commonTest/kotlin/com/opensplit/e2e/ui/`)
*Only for critical journeys testing Compose user interactions (clicks, keyboard focus/tab, text entry, screen transitions).*

- [ ] **U1: Complete Sign Up UI Flow**
  - **Test Class**: `SignUpFlowUiTest`
  - **Flow**: Welcome screen -> click "Sign up" -> enter email -> Tab -> enter password -> Enter/click submit.
  - **Assertion**: Group list screen (`group-list` / `group-active-shell`) is displayed.

- [ ] **U2: Logout via Account UI Flow**
  - **Test Class**: `LogoutFlowUiTest`
  - **Flow**: From group list -> click `header-more-options` -> click `menu-item-account` ("Account") -> Profile screen -> click "Log out".
  - **Assertion**: Returns to `welcome-screen`.

- [ ] **U3: Create Group UI Flow**
  - **Test Class**: `CreateGroupFlowUiTest`
  - **Flow**: From group list -> click `header-add-group` -> click `group-create-btn` ("Create a Group") -> type into `group-name` -> click `group-submit` ("Create").
  - **Assertion**: Navigates to group details screen displaying new group name in top bar.

- [ ] **U4: Join Group UI Flow**
  - **Test Class**: `JoinGroupFlowUiTest`
  - **Flow**: From group list -> click `header-add-group` -> click `group-join-btn` ("Join a Group") -> type invite code into `group-invite-code` -> click `group-submit` ("Join").
  - **Assertion**: Navigates to group details screen with joined group name.

- [ ] **U5: Edit Existing Expense UI Flow**
  - **Test Class**: `EditExpenseFlowUiTest`
  - **Flow**: Open group details with existing expense -> click expense card -> [`ExpenseDetailsScreen`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/expense/ExpenseDetailsScreen.kt) opens -> click Edit icon button -> modify title to "Updated Dinner" & amount -> click Save.
  - **Assertion**: Navigates back to group details -> "Updated Dinner" visible in list.

- [ ] **U6: Delete Existing Expense UI Flow**
  - **Test Class**: `DeleteExpenseFlowUiTest`
  - **Flow**: Open group details with existing expense -> click expense card -> click Delete icon button.
  - **Assertion**: Navigates back to group details -> expense removed from list -> group balance reflects deletion.

- [ ] **U7: Leave Group UI Flow**
  - **Test Class**: `LeaveGroupFlowUiTest`
  - **Flow**: Open group details -> click Settings icon button -> [`GroupSettingsScreen`](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/integration/group/settings/GroupSettingsScreen.kt) opens -> click "Leave group".
  - **Assertion**: Navigates back to group list -> left group card no longer present.

- [ ] **U8: Settled Groups Accordion UI Flow**
  - **Test Class**: `SettledGroupsAccordionUiTest`
  - **Flow**: Group list with settled group -> click `show-settled-btn` ("Show X settled-up groups") -> settled group visible -> click `hide-settled-btn` ("Re-hide").
  - **Assertion**: Settled groups show on expand, disappear on collapse.

---

## Recommended Execution Order

1. **Phase 1: Core Lifecycle & Session**
   - `C1: AutoLoginStartupFlowTest`
   - `C3: LogoutAndSessionClearFlowTest` / `U2: LogoutFlowUiTest`
   - `C2: SignUpFlowE2eTest` / `U1: SignUpFlowUiTest`
2. **Phase 2: Group Management**
   - `C4: CreateGroupFlowE2eTest` / `U3: CreateGroupFlowUiTest`
   - `C5: JoinGroupFlowE2eTest` / `U4: JoinGroupFlowUiTest`
   - `C6: LeaveGroupFlowE2eTest` / `U7: LeaveGroupFlowUiTest`
3. **Phase 3: Expense Mutations & Offline**
   - `U5: EditExpenseFlowUiTest` & `U6: DeleteExpenseFlowUiTest`
   - `C7: OfflineExpenseSyncFlowTest`
   - `C10: ChangePayerExpenseFlowTest`
4. **Phase 4: Split Edge Cases & Adaptive**
   - `C8: SharesSplitMethodFlowTest` & `C9: AdjustmentSplitMethodFlowTest`
   - `C11: AdaptiveDualPanelNavigationFlowTest`
   - `U8: SettledGroupsAccordionUiTest`
