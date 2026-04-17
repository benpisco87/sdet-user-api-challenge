# Test Plan Coverage and Bug Report: Full Cross-Environment Audit (TC-01 to TC-17)

## Overview
This document provides a comprehensive audit of the User Service API. It maps the complete test plan to actual TestNG execution results and provides a comparative analysis of the behavioral discrepancies between the **Development (DEV)** and **Production (PROD)** environments.

## 1. Full Test Case Coverage & Results Comparison
This table includes the full original test plan. Cases marked as "Not Executed" were not present in the provided report files.

| ID | Category | Endpoint | Description | Expected | Actual Test(s) | DEV Result | PROD Result |
|---|---|---|---|---|---|---|---|
| TC-01 | Happy Path | POST /users | Create valid user | 201 | `UserTests.shouldCreateUser` | **PASS** | **PASS** |
| TC-02 | Happy Path | GET /users/{e} | Retrieve created user | 200 | `UserTests.shouldCreateAndGetUser` | **PASS** | **PASS** |
| TC-03 | Happy Path | PUT /users/{e} | Update existing user | 200 | `UserWriteTests.shouldUpdateUser` | **PASS** | **PASS** |
| TC-04 | Happy Path | DELETE /users/{e} | Delete with valid token | 204 | `UserWriteTests.shouldDeleteUserWithAuth` | **FAIL (500)** | **PASS** |
| TC-05 | Boundary | POST /users | Age = 1 (Min) | 201 | `UserTests.shouldCreateAndGetUser` | **PASS** | **PASS** |
| TC-06 | Boundary | POST /users | Age = 150 (Max) | 201 | `UserTests.shouldCreateAndGetUser` | **PASS** | **PASS** |
| TC-07 | Boundary | POST /users | Age = 0 (Below min) | 400 | `UserTests.shouldFailToCreateUser` | **PASS** | **PASS** |
| TC-08 | Boundary | POST /users | Age = 151 (Above max) | 400 | `UserTests.shouldFailToCreateUser` | **PASS** | **PASS** |
| TC-09 | Invalid Format | POST /users | Invalid email format | 400 | `UserTests.shouldFailToCreateUser` | **FAIL (201)** | **PASS** |
| TC-10 | Invalid Format | POST /users | Age as string | 400 | N/A | **Not Executed** | **Not Executed** |
| TC-11 | Conflict | POST /users | Create duplicate email | 409 | `UserWriteTests.shouldFailToCreateDuplicateUser` | **FAIL (500)** | **FAIL (500)** |
| TC-12 | Conflict | PUT /users/{e} | Update to existing email | 409 | `UserWriteTests.shouldFailToUpdateWithDuplicateEmail` | **FAIL (Msg)** | **FAIL (Msg)** |
| TC-13 | Not Found | GET /users/{e} | Retrieve non-existent email | 404 | `UserReadTests.shouldReturn404ForNonExistingUser` | **FAIL (500)** | **PASS** |
| TC-14 | Not Found | DELETE /users/{e} | Delete non-existent email | 404 | N/A | **Not Executed** | **Not Executed** |
| TC-15 | Auth | DELETE /users/{e} | Delete without valid Auth | 401 | `UserWriteTests.shouldFailDeleteWithoutAuth` | **FAIL (204)** | **PASS** |
| TC-16 | State | GET /users | Verify count increase | 200 | `UserReadTests.shouldIncreaseListAfterMultipleUsers` | **PASS** | **PASS** |
| TC-17 | Isolation | GET /prod/users | Check cross-env leakage | 404 | `UserTests.shouldNotLeakUsersAcrossEnvironments` | **FAIL (500)** | **PASS** |

---

## 2. DEV Environment Bug Report
The DEV environment exhibits critical failures in security enforcement and error-handling stability.

* **Bug DEV-01: 404-to-500 Mapping Error (TC-04, TC-13, TC-17)**
  - **Actual:** Returns `500 Internal Server Error` when a user is not found.
  - **Expected:** `404 Not Found`.
* **Bug DEV-02: Broken Authentication (TC-15)**
  - **Actual:** Accepts invalid tokens and deletes resources (204).
  - **Expected:** `401 Unauthorized`.
* **Bug DEV-03: Missing Input Validation (TC-09)**
  - **Actual:** Accepts malformed email strings and returns `201 Created`.
  - **Expected:** `400 Bad Request`.
* **Bug DEV-04: Duplicate Creation Handle (TC-11)**
  - **Actual:** Returns `500` instead of `409 Conflict`.
* **Bug DEV-05: Contract Message Mismatch (TC-12)**
  - **Actual:** Returns "Email already exists" instead of "Duplicate email".

---

## 3. PROD Environment Bug Report
PROD is significantly more stable, with active security and validation, but shares core logic bugs with DEV.

* **Bug PROD-01: Duplicate Creation Handle (TC-11)**
  - **Actual:** Returns `500` instead of `409 Conflict`.
* **Bug PROD-02: Contract Message Mismatch (TC-12)**
  - **Actual:** Returns "Email already exists" instead of "Duplicate email".

---

## 4. Final Comparative Analysis & Bug Matrix

### Environmental Discrepancy Note
**CRITICAL:** There is a major logic divergence between environments. PROD correctly enforces security (Auth) and data integrity (Email Validation) that are bypassed in DEV. Furthermore, DEV lacks proper exception mapping for missing resources, resulting in server crashes (500) where PROD correctly identifies the state as 404.

| Bug Description | Expected | Actual (DEV) | Actual (PROD) | Affected Stages |
|---|---|---|---|---|
| Missing user returns 500 | 404 Not Found | 500 Error | 404 (Correct) | **DEV Only** |
| Invalid Email accepted | 400 Bad Request | 201 Created | 400 (Correct) | **DEV Only** |
| Auth Bypass on Delete | 401 Unauthorized | 204 Deleted | 401 (Correct) | **DEV Only** |
| Duplicate Create Error | 409 Conflict | 500 Error | 500 Error | **Both** |
| Conflict Msg Mismatch | "Duplicate email" | "Email exists" | "Email exists" | **Both** |

---

## 5. Coverage Gaps & Recommendations

### Immediate Recommendations
1. **Sync DEV Validation Logic:** Update the DEV environment to include the email regex validation and Authentication filters currently active in PROD.
2. **Global Exception Mapping:** In both environments, refactor the persistence layer to catch unique constraint violations and map them to HTTP 409 (Conflict) instead of unhandled 500 errors.
3. **Standardize Error Responses:** Update the DEV error-handling middleware to return a 404 instead of a 500 when a database query returns null.
4. **Contract Alignment:** Update the error response body for duplicate updates to match the documentation ("Duplicate email").

### Test Coverage Recommendations
* **TC-10 (Age as String):** Add a negative test case to verify the API rejects non-integer age values.
* **TC-14 (Delete Non-Existent):** Implement a test to verify that deleting a user that does not exist returns a 404.
* **Test Data Cleanup:** Ensure that the DEV environment is cleared between runs to prevent TC-16 (State) from becoming unreliable due to high user counts.