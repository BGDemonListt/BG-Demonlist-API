# BGDL Manual Testing Procedure

Use this checklist when validating new backend changes locally. It is written around `docs/postman/BGDL-API.postman_collection.json` and the current API behavior in the codebase.

## Setup

1. Start the API locally.
2. Import `docs/postman/BGDL-API.postman_collection.json` into Postman.
3. Set `baseUrl` to your running API, usually `http://localhost:8080`.
4. Prepare at least:
   - one `ADMIN` account for moderation and demon/tag management
   - one normal `USER` account for profile and record submission testing
5. Keep these collection variables updated as you go:
   - `accessToken`
   - `refreshToken`
   - `userId`
   - `playerId`
   - `adminUserId`
   - `demonId`
   - `recordId`
   - `skillsetTagId`
   - `filePath`
6. If you want to test Google or Discord flows, make sure the frontend redirect URLs and OAuth credentials are configured first.

## Smoke Check

1. Run `Get All Demons`, `Get Players Page`, `Get Regions`, `Get GD Level By Id`, and `Get File`.
2. Expect `200 OK` from the public endpoints.
3. Set `filePath` to `regions/flags/targovishte.png`.
4. Confirm `Get File` returns image bytes with a cacheable image content type.
5. Confirm `Get Regions` returns objects with `code`, `name`, and `flagPath`.
6. If sample data already exists, confirm:
   - `Get Players Page` returns at most `15` items
   - `Get All Demons` returns at most `20` items
   - page numbers in responses are 1-based

## Authentication Flow

1. Run `Register` with a fresh email.
2. Confirm the account through `Confirm Registration` using the token from email or the database.
3. Run `Authenticate`.
4. Save the returned `accessToken`, `refreshToken`, `user.id`, and `user.playerId`.
5. Run `Get Me` with the bearer token.
6. Expect the response to include the same user, player id, role, and any linked Discord profile if present.
7. Run `Refresh Token` and confirm a fresh auth response is returned.
8. Optionally run `Forgot Password` and `Reset Password`.

## Google OAuth Sign-In

1. Run `Get Google OAuth URL`.
2. Open the returned URL in a browser and complete the Google login flow.
3. Capture the `code` from the frontend redirect URL.
4. Run `Authenticate Google` with that code.
5. Verify:
   - the response includes `accessToken` and `refreshToken`
   - the returned user is enabled
   - a player is created for the user if it did not already exist

## Discord Account Linking

1. Authenticate as a normal user and keep the bearer token active.
2. Run `Get Discord Link URL`.
3. Open the returned URL in a browser and complete the Discord authorization flow.
4. Capture `code` and `state` from the frontend redirect URL.
5. Run `Link Discord Account`.
6. Verify the returned user includes:
   - `discord.id`
   - `discord.username`
   - `discord.avatarUrl`
   - `discord.linkedAt`
7. Run `Unlink Discord Account`.
8. Verify the returned user has `discord = null`.

## User Profile And Region Update

1. Authenticate as a normal user.
2. Run `Update Profile` for the authenticated user id.
3. Send a new `name` and a valid `region`, for example `SOFIA`.
4. Run `Get Me`, `Get Player Details`, and `Get Players Page`.
5. Verify:
   - the updated name is visible in the user response
   - the linked player name changes too
   - the player details response includes the selected region metadata
   - filtering players by that region returns the player

## Skillset Tag Management

1. Authenticate as an admin.
2. Run `Create Skillset Tag` twice with distinct names.
3. Save one created id to `skillsetTagId`.
4. Run `Get All Skillset Tags`.
5. Verify both tags appear once and names are trimmed/unique as expected.
6. Run `Update Skillset Tag` on one tag.
7. Run `Delete Skillset Tag` on the other tag.
8. Re-run `Get All Skillset Tags` and verify the deleted tag is gone from active results.

## Demon Management, Tags, And Recalculation

1. Authenticate as an admin.
2. Create one or more skillset tags if you have not already.
3. Run `Create Demon` twice with different `levelId` values and positions `1` and `2`.
4. Attach one or more `skillsetTagIds` to at least one demon.
5. Run `Get All Demons`.
6. Verify:
   - demons are sorted by `position`
   - every demon has `points`
   - the top demon has more points than lower demons
   - `youtubeUrl` is returned when provided
   - `skillsetTags` are returned in the summary response
7. Run `Get All Demons` again with:
   - a partial `name`
   - one or more `tagIds`
8. Verify search and tag filtering both work.
9. Move a demon with `Update Demon` to another position.
10. Run `Get All Demons` again.
11. Verify all affected demons have updated `position` and recalculated `points`.
12. Optionally attempt to assign more than four tags and confirm the request is rejected.

## Record Submission Lifecycle

1. Register and confirm a normal user if you do not already have one.
2. Set `playerId` and `demonId` in the Postman variables.
3. Run `Create Record Submission`.
4. Verify the create response stores `status = PENDING` even if another status was sent.
5. Save the returned `id` to `recordId`.
6. Authenticate as an admin.
7. Run `Get All Records`.
8. Verify the new submission appears with the correct holder and demon info.
9. Run `Moderate Record Submission` with `status = ACCEPTED`.
10. Run `Get Player Details`.
11. Verify:
   - `points` increased by the accepted demon’s points
   - `completedDemons` contains the demon
   - `hardestDemon` matches the best-ranked accepted demon
   - `position` is populated when the player enters the leaderboard

## Rejection, Re-Acceptance, And Delete Rollback

1. Update the same record to `REJECTED`.
2. Run `Get Player Details`.
3. Verify:
   - the demon is removed from `completedDemons`
   - `points` decreased accordingly
   - `position` changed or became `null` if no accepted records remain
   - `hardestDemon` moved to the next best completed demon or became `null`
4. Re-accept the record and verify the values are restored.
5. Delete the accepted record.
6. Re-run `Get Player Details` and `Get Players Page`.
7. Verify leaderboard state is recalculated again.

## Multiple Accepted Records And Hardest Demon

1. Accept records for two different demons for the same player.
2. Confirm the hardest demon is the one with the smaller list `position`.
3. Reject or delete the hardest accepted record.
4. Confirm the next hardest accepted demon becomes `hardestDemon`.

## Demon Delete Rebuild

1. Delete a demon that already has accepted records tied to it.
2. Re-run `Get All Demons`, `Get Player Details`, and `Get Players Page`.
3. Verify:
   - remaining demons are re-numbered
   - demon points are recalculated
   - player points are recalculated
   - completed demon lists stay consistent
   - leaderboard positions update consistently

## Authorization Checks

1. Without admin auth cookies, call:
   - `Create Demon`
   - `Update Demon`
   - `Delete Demon`
   - `Get All Records`
   - `Moderate Record Submission`
   - `Delete Record Submission`
   - `Create Skillset Tag`
2. Expect access denial for admin-only operations.
3. Confirm public endpoints still work without authentication.
4. Confirm authenticated user profile actions still work for the logged-in user.

## Regression Notes

- Player leaderboard state should reflect accepted submissions only.
- Changing demon positions must recalculate both demon points and affected player standings.
- Region metadata should stay aligned with the file-serving endpoint under `/files`.
- Discord linking should reject invalid or replayed state values.
- Duplicate record submissions for the same player and demon should be rejected.
