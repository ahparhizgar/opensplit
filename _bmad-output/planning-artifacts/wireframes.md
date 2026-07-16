---
project: bmad-test
date: 2026-05-03
source: _bmad-output/planning-artifacts/ux-design-specification.md
---

# OpenSplit Wireframes

## 1. Home / Household Balance

```text
--------------------------------------------------
 OpenSplit                         [Profile]
--------------------------------------------------
 Household: Maple House

 [You are owed]                [Total balance]
        $42.50                     +$18.25

--------------------------------------------------
 Recent activity
 - Groceries           Maya      -$24.00
 - Internet            Sam       +$12.00
 - Utilities           Group      -$8.25
--------------------------------------------------
 [Add expense]   [Settle up]   [View all]
--------------------------------------------------
```

Purpose: make the household status obvious at a glance and give fast access to the two core actions.

## 2. Add Expense

```text
--------------------------------------------------
 Add expense                                 [X]
--------------------------------------------------
 Amount          [$  0.00          ]
 Paid by         [Maya           v ]
 Split           [Equal split    v ]
 For             [Sam, Maya, Leo v ]
 Note            [Optional note       ]

 [Save expense]
--------------------------------------------------
```

Purpose: minimize friction with strong defaults and a single save action.

## 3. Balance Detail / Settle Up

```text
--------------------------------------------------
 Balance details                           [Back]
--------------------------------------------------
 Maya owes Sam                         $18.25
 Leo owes group                        $12.00

--------------------------------------------------
 Suggested settlement
 Pay Sam                               $18.25
 [Confirm settlement]
--------------------------------------------------
```

Purpose: show owed relationships clearly and make the next action unmissable.

## 4. Offline Sync State

```text
--------------------------------------------------
 OpenSplit                         [Offline icon]
--------------------------------------------------
 You are offline
 Expenses you add will save locally and sync later.

 [Add expense anyway]

 Pending changes
 - Groceries draft
 - Utilities edit
--------------------------------------------------
```

Purpose: reassure users that work is safe even when connectivity is lost.

## 5. Household Membership Desktop View

```text
------------------------------------------------------------------------------------------------
 OpenSplit                              Household membership                   [Refresh] [Profile]
                                        Active: Maple House
------------------------------------------------------------------------------------------------

  ---------------------------------------------------    --------------------------------------
  Maple House                                          |    Your households                     |
  Active household • 4 members • You are owner         |    Change which household you view    |
                                                        |                                      |
  Members                                               |    Maple House       4 members        |
  Everyone who can currently share expenses here        |    [Current]              [Leave]     |
                                                        |                                      |
  Maya Chen                         [You] [Owner]       |    Cedar Flat       3 members         |
  sam@example.com                                        |    [Switch]               [Leave]     |
  leo@example.com                                        |                                      |
  nina@example.com                                       |    River House      5 members         |
                                                        |    [Switch]               [Leave]     |
  Switching households changes what you are viewing,    |                                      |
  not which expenses belong to you.                     |                                      |
  ---------------------------------------------------    --------------------------------------
```

Purpose: use desktop width to show members and household actions together, while keeping the current household obvious and destructive actions deliberate.

## Wireframe Notes

- Mobile-first layouts keep one primary action visible at all times.
- Household context stays pinned to reduce confusion.
- Empty and offline states should explain the next step in plain language.
- Desktop can expand recent activity and balance detail into a two-column view.
- Desktop household management should use side-by-side member and household panels rather than a narrow stacked card layout.
