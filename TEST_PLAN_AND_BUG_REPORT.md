# Test Plan Coverage and Bug Report

## Overview

This document captures the planned test coverage for the user service API and maps the current implementation to actual TestNG tests in the repository. It also reports the current bugs observed in the latest Allure test run.

## Test Case Coverage

| ID | Category | Endpoint | Description | Expected Status | Actual Test(s) | Coverage Notes |
|---|---|---|---|---|---|---|
| TC-01 | Happy Path | POST /users | Create valid user (Name, Email, Age: 30). | 201 Created | `UserTests.shouldCreateUser` | Covered by positive create test with valid users. |
| TC-02 | Happy Path | GET /users/{email} | Retrieve the user created in TC-01. | 200 OK | `UserTests.shouldCreateAndGetUser` | Covered. |
| TC-03 | Happy Path | PUT /users/{email} | Update name/age for existing user. | 200 OK | `UserWriteTests.shouldUpdateUser` | Covered. |
| TC-04 | Happy Path | DELETE /users/{email} | Delete user with mysecrettoken. | 204 No Content | `UserWriteTests.shouldDeleteUserWithAuth` | Covered. |
| TC-05 | Boundary | POST /users | Age = 1 (Minimum boundary). | 201 Created | `UserTests.shouldCreateUserAndAppearInList` | Covered as boundary min with positive create. |
| TC-06 | Boundary | POST /users | Age = 150 (Maximum boundary). | 201 Created | `UserTests.shouldCreateUserAndAppearInList` | Covered as boundary max with positive create. |
| TC-07 | Boundary | POST /users | Age = 0 (Below minimum). | 400 Bad Request | `UserTests.shouldFailToCreateUser` | Covered by invalid user data provider. |
| TC-08 | Boundary | POST /users | Age = 151 (Above maximum). | 400 Bad Request | `UserTests.shouldFailToCreateUser` | Covered by invalid user data provider. |
| TC-09 | Invalid Format | POST /users | Email without @ or domain (e.g. jane.doe). | 400 Bad Request | `UserTests.shouldFailToCreateUser` | Covered by invalid email test case. |
| TC-10 | Invalid Format | POST /users | Send age as a string instead of integer. | 400 Bad Request | **No direct mapping found** | No explicit test currently exists for string age payload. |
| TC-11 | Conflict | POST /users | Create user with an email that already exists. | 409 Conflict | `UserTests.shouldNotAllowDuplicateUsers`, `UserWriteTests.shouldFailToCreateDuplicateUser` | Covered by duplicate creation tests. |
| TC-12 | Conflict | PUT /users/{email} | Change email to one that belongs to another user. | 409 Conflict | `UserWriteTests.shouldFailToUpdateWithDuplicateEmail` | Covered. |
| TC-13 | Not Found | GET /users/{email} | Retrieve a non-existent email. | 404 Not Found | `UserReadTests.shouldReturn404ForNonExistingUser` | Covered. |
| TC-14 | Not Found | DELETE /users/{email} | Delete a non-existent email (with valid token). | 404 Not Found | **No direct mapping found** | No explicit delete-nonexistent test is present. |
| TC-15 | Auth | DELETE /users/{email} | Delete without Authentication header. | 401 Unauthorized | `UserWriteTests.shouldFailDeleteWithoutAuth` | Covered. |
| TC-16 | State | GET /users | Verify count increases after POST. | 200 OK | `UserReadTests.shouldIncreaseListAfterMultipleUsers` | Covered. |
| TC-17 | Isolation | GET /prod/users | Check for user created only in /dev. | 404 Not Found | `UserTests.shouldNotLeakUsersAcrossEnvironments` | Covered. |

## Current Allure Run Status

The latest Allure test results show the following failing or broken tests in the current run.

### Failing / Broken Tests

| Test Class | Test Method | Scenario | Result | Notes |
|---|---|---|---|---|
| `UserWriteTests` | `shouldUpdateUser` | TC-03 | Failed | Expected 200 on update, received 400; update POST succeeded but PUT returned bad request. |
| `UserWriteTests` | `shouldDeleteUserWithAuth` | TC-04 | Failed | Delete returned 204, but subsequent GET returned 500 instead of 404. |
| `UserWriteTests` | `shouldFailToCreateDuplicateUser` | TC-11 | Failed | Expected 409 duplicate error, got 500 internal server error. |
| `UserTests` | `shouldNotAllowDuplicateUsers` | TC-11 | Failed | Expected 409 duplicate error, got 500 internal server error. |
| `UserReadTests` | `shouldReturn404ForNonExistingUser` | TC-13 | Failed | Expected 404 not found, got 500 internal server error. |
| `UserWriteTests` | `shouldFailDeleteWithoutAuth` | TC-15 | Broken | Framework bug: null auth header caused `Request.Builder.addHeader` NPE. |
| `UserReadTests` | `shouldIncreaseListAfterMultipleUsers` | TC-16 | Failed | Expected list size to increase by 1; actual list size was much larger, indicating shared or non-isolated environment state. |
| `UserTests` | `shouldNotLeakUsersAcrossEnvironments` | TC-17 | Failed | Expected 404 in opposite env, got 500 internal server error. |
| `UserTests` | `shouldFailToCreateUser` | TC-09 | Failed | Invalid email payload was accepted and created successfully (201 returned). |

## Bug Report

### Bug 1: PUT update returns 400 instead of 200
- Test: `UserWriteTests.shouldUpdateUser`
- Expected: `200 OK`
- Actual: `400 Bad Request`
- Evidence: Allure attachment shows `PUT http://localhost:3000/prod/users/update_... -> 400`
- Impact: Happy-path update flow fails, blocking TC-03.

### Bug 2: Deleted user retrieval returns 500 instead of 404
- Test: `UserWriteTests.shouldDeleteUserWithAuth`
- Expected: `404 Not Found` when reading deleted user
- Actual: `500 Internal Server Error`
- Evidence: GET after delete returned 500 in Allure attachment
- Impact: TC-04 passes delete but fails post-delete validation.

### Bug 3: Duplicate user create returns 500 instead of 409
- Tests: `UserWriteTests.shouldFailToCreateDuplicateUser`, `UserTests.shouldNotAllowDuplicateUsers`
- Expected: `409 Conflict`
- Actual: `500 Internal Server Error`
- Evidence: second `POST /prod/users` returned 500 in Allure attachments
- Impact: TC-11 fails due to incorrect duplicate handling.

### Bug 4: Non-existent user GET returns 500 instead of 404
- Test: `UserReadTests.shouldReturn404ForNonExistingUser`
- Expected: `404 Not Found`
- Actual: `500 Internal Server Error`
- Evidence: GET `http://localhost:3000/prod/users/notfound@mail.com` returned 500
- Impact: TC-13 fails and reveals unstable not-found handling.

### Bug 5: Missing auth header causes framework NPE
- Test: `UserWriteTests.shouldFailDeleteWithoutAuth`
- Expected: `401 Unauthorized`
- Actual: broken due to `NullPointerException` in `UserApi.deleteUser`
- Evidence: Allure shows exception in `okhttp3.Request$Builder.addHeader`
- Impact: TC-15 cannot reliably verify auth failure due framework bug.

### Bug 6: Environment isolation check returns 500 instead of 404
- Test: `UserTests.shouldNotLeakUsersAcrossEnvironments`
- Expected: `404 Not Found` in other environment
- Actual: `500 Internal Server Error`
- Evidence: GET request to opposite env returned 500
- Impact: TC-17 fails and indicates cross-env lookup returns server error.

### Bug 7: Invalid email format accepted by API
- Test: `UserTests.shouldFailToCreateUser` (invalid email case)
- Expected: `400 Bad Request`
- Actual: `201 Created` and user persisted
- Evidence: Allure attachments show POST success and GET returned 200
- Impact: TC-09 fails, showing API validation gap.

### Bug 8: User list state is not isolated across runs
- Test: `UserReadTests.shouldIncreaseListAfterMultipleUsers`
- Expected: list size increase by 1 after two creates
- Actual: list size was much larger than expected (79)
- Impact: TC-16 is unreliable due shared or pre-populated state in the test environment.

## Coverage Gaps and Recommendations

- **TC-10**: No explicit coverage exists for sending age as a string. Add a dedicated negative create test.
- **TC-14**: No explicit coverage exists for deleting a non-existent user with a valid auth token. Add a dedicated delete-not-found test.
- **Null auth header handling**: Fix `UserApi.deleteUser` to omit the Authorization header when auth is not provided, or use a dedicated helper for unauthenticated requests.
- **500 vs 404**: Investigate server-side not-found handling in GET and delete validation flows; many tests fail because 404 is returned as 500.
- **Duplicate email handling**: Ensure the API returns 409 Conflict and stable validation messages for duplicate user creation and update.
- **Test isolation**: Reset or isolate in-memory data between tests or use a clean test environment before each run.

## Notes

- The current test suite is implemented in:
  - `src/test/java/com/ben/sdet/tests/UserTests.java`
  - `src/test/java/com/ben/sdet/tests/UserReadTests.java`
  - `src/test/java/com/ben/sdet/tests/UserWriteTests.java`
- Test data is loaded from `src/test/resources/testdata/users.yaml`.
- Environment-specific base URLs are configured in `src/main/resources/config/dev.properties` and `prod.properties`.
