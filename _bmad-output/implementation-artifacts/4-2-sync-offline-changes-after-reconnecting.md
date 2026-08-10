# 4-2 Sync offline changes after reconnecting

Status: done

Summary:
Sync locally-created or edited expenses and settlements after the device reconnects; resolve conflicts in a user-understandable way.

Acceptance Criteria:
- Offline changes are queued and synced automatically on reconnect.
- Conflicts are surfaced with clear resolution options.
- Sync preserves original expense currency and user intent.

Implementation notes:
- Implemented outside BMAD; please add commit reference if desired.
- Consider end-to-end tests covering conflict resolution.
