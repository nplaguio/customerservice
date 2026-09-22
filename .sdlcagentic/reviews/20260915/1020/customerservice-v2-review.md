# Code Review Report

## Metadata
| Field                   | Value                                                     |
|-------------------------|-----------------------------------------------------------|
| Reviewed At             | 2026-09-18 16:21                                          |
| Reviewed By             | AI Agent (swe-review-mr-code)                             |
| Requested By            | n/a                                                       |
| Review Type             | Staged/Working Changes                                    |
| MR/PR Link              | n/a                                                       |
| Base Git Branch         | master                                                    |
| Base Git Commit         | 262a5e608920d4a5871e3aa891a7ec7aeb01dfaf                 |

> **Note:** This is a follow-up review of the same `customerservice` project. The previous review (`.sdlcagentic/reviews/20260915/1002/customerservice-review.md`) identified blockers that have since been partially addressed. This review covers the updated working tree.

---

## What Changed Since the Previous Review

Several issues from the previous review have been fixed:
- ✅ `CustomerController` now uses `ResponseConverter.convert()` and `CustomerRequest`/`CustomerResponse` DTOs
- ✅ API service layer moved to `service.api.*` with proper `RestResponseContentWrapper<HttpHeaders, T>` return types
- ✅ `service.ws.*` wrapper layer added for both REST and SOAP integrations
- ✅ `InquireCustomerSoapImpl` replaced with `InquireCustomerSoapServiceImpl` (singleton port via `SoapEndpointConfiguration`)
- ✅ `InquireBranchApiConfigProperties` moved to `config.properties` subpackage
- ✅ `BranchInquiryController` now uses `ResponseConverter.convert()` and correct service layer
- ✅ `BranchInquiryMasterServiceImpl` no longer silently returns `null`
- ✅ All controllers now declare all 4 standard headers
- ✅ Integration tests restructured into success/error classes under `flow.*`

The following issues from the prior review **remain unfixed** or have **new variants** introduced. Only net-new or still-unresolved issues are reported below.

---

## Final Verdict

**Result:** 🔴 FAIL

One or more blocking issues must be resolved before this change can merge.

### Summary Table

| Category                    | Blocking | Warning | Suggestion |
|-----------------------------|----------|---------|------------|
| Functional Alignment        | 2        | 3       | 0          |
| Code Quality                | 8        | 7       | 1          |
| Potential Bug or Risky Area | 2        | 3       | 1          |
| Security                    | 3        | 2       | 0          |
| Performance                 | 0        | 1       | 0          |

---

## Review Comments

---

### RC-1: `CustomerNotFoundException` handler returns HTTP 400 — tests assert 400 but comment says 404

| Field        | Value                                                                              |
|--------------|------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/exception/GlobalExceptionHandler.java:L22-29` |
| **Severity** | 🚨 Blocking                                                                         |
| **Category** | Functional Alignment                                                               |

**Comment:**
The handler is annotated with the comment `// 404` but returns `HttpStatus.BAD_REQUEST`. `CustomerIntegrationErrorTest` asserts `status().isBadRequest()` for customer not found — so the test is aligned with the *implementation* but contradicts the comment intent. More critically, a missing resource is semantically a 404 per REST conventions, and the comment confirms the developer intended 404. The inconsistency between the comment, the status code, and the REST semantic makes the contract incorrect. Either the comment is wrong (remove it) or the HTTP status code is wrong (fix it to `NOT_FOUND`).

**Reference(s):**
- `src/test/java/com/bpi/customerservice/flow/customer/CustomerIntegrationErrorTest.java:L29-36` — asserts `isBadRequest()` for a not-found customer
- REST standard: 404 is the correct status for a resource that does not exist

**Suggested Fix:**
Change the HTTP status to `NOT_FOUND` and update the test assertions accordingly:
```java
return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND); // was BAD_REQUEST
```
Update tests to assert `status().isNotFound()`.

---

### RC-2: `InquireCustomerSoapServiceImpl` hardcodes endpoint URL in the method body, ignoring `SoapEndpointConfiguration`

| Field        | Value                                                                                            |
|--------------|--------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/ws/impl/InquireCustomerSoapServiceImpl.java:L28-30` |
| **Severity** | 🚨 Blocking                                                                                       |
| **Category** | Functional Alignment / Code Quality                                                              |

**Comment:**
`SoapEndpointConfiguration` correctly creates a singleton `WSRVALNGPort` bean wired to `bpi.soap.wsrvalng.endpoint-url` (from `InquireBranchSoapConfigProperties`). However, `InquireCustomerSoapServiceImpl.callSoapService()` then *overrides* the endpoint URL on every call by casting the port to `BindingProvider` and hardcoding `"http://localhost:3000/soap/validateAcctNumber"`. This makes the `SoapEndpointConfiguration`/properties approach entirely redundant — the URL injected by the config is discarded every call, and the service always targets the hardcoded localhost address regardless of the environment.

**Reference(s):**
- `src/main/java/com/bpi/customerservice/config/SoapEndpointConfiguration.java:L22-24` — correctly configures port from properties
- `src/main/resources/application.properties:L24` — `bpi.soap.wsrvalng.endpoint-url` property is defined but bypassed

**Suggested Fix:**
Remove the `BindingProvider` override. The `WSRVALNGPort` bean already points to the correct URL from `InquireBranchSoapConfigProperties`. Also — the `accountNumber` argument is still never set on `WsrvalngInput` (pre-existing bug retained from previous version):
```java
@Override
public WsrvalngOutput callSoapService(String accountNumber) {
    try {
        WsrvalngInput requestInput = new WsrvalngInput();
        // TODO: set accountNumber on requestInput using the correct generated field
        // e.g. requestInput.setWsInAccount(accountNumber);
        log.info("Calling WSRVALNG SOAP service for account: {}", accountNumber);
        return port.wsrvalngOperation(requestInput);
    } catch (WebServiceException e) {
        log.error("SOAP communication error calling WSRVALNG service", e);
        throw new BranchServiceException(CustomerServiceErrorCode.SERVICE_ERROR);
    }
}
```

---

### RC-3: SOAP `accountNumber` still never set on `WsrvalngInput`

| Field        | Value                                                                                            |
|--------------|--------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/ws/impl/InquireCustomerSoapServiceImpl.java:L24` |
| **Severity** | 🚨 Blocking                                                                                       |
| **Category** | Functional Alignment / Potential Bug or Risky Area                                               |

**Comment:**
`WsrvalngInput requestInput = new WsrvalngInput()` is created with no fields set. The `accountNumber` parameter is accepted by `callSoapService(String accountNumber)` but is never written into the request input object. Every SOAP call is sent with an empty input, querying either nothing or the wrong account. This pre-existing bug from the previous review was **not fixed**.

**Reference(s):**
- Previous review RC-2 (unfixed)
- `src/test/java/com/bpi/customerservice/service/ws/InquireCustomerSoapImplTest.java:L31` — test stubs `any(WsrvalngInput.class)`, so it does not detect this bug

**Suggested Fix:**
Set the account number on the generated input DTO before invoking the port. Check the generated `WsrvalngInput` class for the correct setter name:
```java
WsrvalngInput requestInput = new WsrvalngInput();
requestInput.setWsInAccount(accountNumber); // use the correct generated setter
```

---

### RC-4: All three controllers have `@EnableAccessPolicies` commented out

| Field        | Value                                                                                                                                                                         |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/controller/customer/CustomerController.java:L16`<br>`src/main/java/com/bpi/customerservice/controller/branchinquiry/BranchInquiryController.java:L13` |
| **Severity** | 🚨 Blocking                                                                                                                                                                    |
| **Category** | Security / Code Quality                                                                                                                                                       |

**Comment:**
`@EnableAccessPolicies` is present but commented out (`//@EnableAccessPolicies`) on both `CustomerController` and `BranchInquiryController`. `InquireCustomerSoapController` has no `@EnableAccessPolicies` at all. This annotation is **mandatory** per the Gold Standards — omitting it means the BPI Framework's authentication and access policy enforcement is bypassed on all three controllers, leaving all endpoints unauthenticated.

The fact that it is commented out (rather than absent) suggests it was intentionally disabled, possibly for local testing convenience, but it must not reach a merge in this state.

**Reference(s):**
- Gold Standard §Controller Implementation: "`@EnableAccessPolicies` — **mandatory** on every controller. Omitting this annotation breaks the framework's security handling."
- Previous review RC-7 (still unresolved)

**Suggested Fix:**
Uncomment and retain `@EnableAccessPolicies` on all three controllers:
```java
@EnableAccessPolicies   // ← uncomment this
@RestController
@RequestMapping("/api/customers")
```
Apply the same to `InquireCustomerSoapController`.

---

### RC-5: Encrypted secrets file and cryptographic key still committed to git

| Field        | Value                                                                                          |
|--------------|------------------------------------------------------------------------------------------------|
| **Location** | `src/main/resources/policies/global.secret.enc.properties:L1-6`<br>`src/main/resources/application.properties:L21` |
| **Severity** | 🚨 Blocking                                                                                     |
| **Category** | Security                                                                                       |

**Comment:**
`global.secret.enc.properties` (encrypted keystore/truststore passwords + API auth tokens) and the decryption key `app.certKeyPart` in `application.properties` are both still git-tracked — unchanged from the previous review. All credentials in that file must be treated as compromised and rotated.

**Reference(s):**
- Previous review RC-5 and RC-6 (still unresolved)
- Security Rule: **NEVER commit secrets, credentials, or cryptographic key material to version control**

**Suggested Fix:**
1. `git rm --cached src/main/resources/policies/global.secret.enc.properties`
2. Add `global.secret.enc.properties` to `.gitignore`
3. Remove `app.certKeyPart` from `application.properties`
4. Rotate all `apiAuth.*`, `KEYSTORE.password`, and `TRUSTSTORE.password` values
5. Inject these at runtime via a secrets manager or environment-specific configuration outside the repository

---

### RC-6: `CustomerServiceImpl` is in `repository.impl` — wrong package for a service class

| Field        | Value                                                                                          |
|--------------|------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/repository/impl/CustomerServiceImpl.java:L1`           |
| **Severity** | 🚨 Blocking                                                                                     |
| **Category** | Code Quality                                                                                   |

**Comment:**
The implementation of `CustomerService` (a `service.biz` interface) lives in `repository.impl`. The `repository` package is reserved for Spring Data repository interfaces and DAOs — placing a service class there is a fundamental package structure violation that misrepresents the class's responsibility to every reader. It should live in `service.biz.impl` or a `service.biz.customer.impl` sub-package.

**Reference(s):**
- Gold Standard §Package Structure: "`repository.<database_type>` — Isolates storage/retrieval for specific database technologies. `service.biz.<domain>` — Houses reusable, core domain logic."

**Suggested Fix:**
Move `CustomerServiceImpl` to `service.biz.impl` (or `service.biz.customer.impl`) alongside `BranchInquiryMasterServiceImpl`, `CustomerMasterServiceImpl`, and `InquireCustomerMasterServiceImpl`.

---

### RC-7: `CustomerService` placed in `service.biz` — violates API service layer convention

| Field        | Value                                                                                     |
|--------------|-------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/biz/CustomerService.java:L1`              |
| **Severity** | 🚨 Blocking                                                                                |
| **Category** | Code Quality                                                                              |

**Comment:**
`CustomerService` is the interface that `CustomerController` directly injects and that returns `RestResponseContentWrapper<HttpHeaders, T>`. This makes it an **API service** (the Gold Standard `service.api.<domain>` layer) — not a business service. However, it is placed in `service.biz`, which is the wrong layer. Controllers must only depend on `service.api.<domain>` services. Business services (`service.biz`) must not return `RestResponseContentWrapper` or any HTTP-specific wrapper.

Currently `CustomerController` → `CustomerService` (in `service.biz`) → `CustomerServiceImpl` (in `repository.impl`). There is no `service.api.customer` intermediary, and the `service.biz.CustomerService` interface breaks the business service's HTTP-agnosticism rule.

**Reference(s):**
- Gold Standard §API Services: "Interface and implementation must reside in `service.api.<domain>`. Return type must be `RestResponseContentWrapper<HttpHeaders, T>`."
- Gold Standard §Business Services: "Method parameters and return types must be plain business models, never `RestResponseContentWrapper`, `ResponseEntity`, `HttpHeaders`, or any HTTP-specific type."

**Suggested Fix:**
Move `CustomerService` (interface) and `CustomerServiceImpl` (impl) to `service.api.customer` and `service.api.customer.impl` respectively. If there is reusable CRUD business logic separate from orchestration, extract it into a new `service.biz.customer` class.

---

### RC-8: Error code classes split into two subpackages — violates one-class-per-microservice rule

| Field        | Value                                                                                                                                         |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/errorcode/customer/CustomerServiceErrorCode.java`<br>`src/main/java/com/bpi/customerservice/errorcode/branch/InquireBranchErrorCode.java` |
| **Severity** | 🚨 Blocking                                                                                                                                    |
| **Category** | Code Quality                                                                                                                                  |

**Comment:**
The Gold Standards require **exactly one `ErrorCode` class per microservice** in the `errorcode` package. Two classes in domain-specific subpackages (`errorcode/customer/` and `errorcode/branch/`) violate this rule. Additionally, both classes:
- Instantiate constants using raw constructors instead of category factory methods (`internal()`, `service()`, `business()`, `validation()`)
- Have no static `Map<String, ErrorCode>` and no `parse(String)` method

Also notable: `InquireBranchErrorCode.SERVICE_ERROR` uses the prefix `SKSSE999` — this prefix (`SKS`) does not belong to this microservice and appears to be copied from a different service.

**Reference(s):**
- Gold Standard §Error Code: "Each microservice defines exactly **one** class... Must expose a static `parse(String code)` method backed by a static `Map`."
- Previous review RC-14 (still unresolved)

**Suggested Fix:**
Consolidate into a single `CustomerServiceErrorCode` in `errorcode` (no subdirectory), add factory methods and `parse()`:
```java
public class CustomerServiceErrorCode extends ErrorCode {
    public static final ErrorCode CUSTOMER_NOT_FOUND = business("001", "Customer not found");
    public static final ErrorCode INVALID_ACCOUNT    = validation("001", "Invalid Account Number");
    public static final ErrorCode SERVICE_ERROR      = service("999", "External Service Error");
    public static final ErrorCode INTERNAL_ERROR     = internal("999", "Internal Error");
    // Add branch inquiry service error here too, not in a separate class:
    public static final ErrorCode BRANCH_SERVICE_ERROR = service("001", "Inquire Branch service unavailable");

    private static final Map<String, ErrorCode> map;
    static { map = getErrorCodes(CustomerServiceErrorCode.class); }

    public CustomerServiceErrorCode(String code, String message) { super(code, message); }

    private static ErrorCode internal(String n, String m)   { return new CustomerServiceErrorCode("CUSIE" + n, m); }
    private static ErrorCode service(String n, String m)    { return new CustomerServiceErrorCode("CUSSE" + n, m); }
    private static ErrorCode business(String n, String m)   { return new CustomerServiceErrorCode("CUSBE" + n, m); }
    private static ErrorCode validation(String n, String m) { return new CustomerServiceErrorCode("CUSVE" + n, m); }

    public static ErrorCode parse(String code) { return map.get(code); }
}
```

---

### RC-9: `InquireBranchApiConfigProperties` — `@Component`, wrong prefix, redeclared parent fields, missing annotations

| Field        | Value                                                                                                    |
|--------------|----------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/config/properties/InquireBranchApiConfigProperties.java:L1-22`   |
| **Severity** | 🚨 Blocking                                                                                               |
| **Category** | Code Quality / Performance                                                                               |

**Comment:**
This class has been moved to `config.properties` (progress), but the following violations remain:
1. **`@Component` present** — must not be used; `@ConfigurationProperties` alone is sufficient (and registered via `@EnableConfigurationProperties` or component scan)
2. **Wrong prefix** — uses `bpi.api.inquire-branch` instead of the Gold Standard `bpi.microservice.inquire-branch`
3. **Redeclares `basePath`, `connectTimeout`, `socketTimeout`** — these fields already exist in `HttpApiConfigProperties`. Spring binds properties to the subclass field, but `AbstractErrorAwareRestTemplate` reads the **parent class field** — the configured values are never applied, so the HTTP client has **no effective timeout**
4. **`@EqualsAndHashCode(callSuper = false)`** — must be `callSuper = true`
5. **Missing `@ToString(callSuper = true)` and `@Validated`**

**Reference(s):**
- Gold Standard §HTTP Client Configuration Properties
- Previous review RC-12 (partially addressed — moved to right package, but other issues remain)
- `src/main/resources/application.properties:L26-28` — properties use `bpi.api.inquire-branch.*` prefix

**Suggested Fix:**
```java
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("bpi.microservice.inquire-branch")
@Validated
public class InquireBranchApiConfigProperties extends HttpApiConfigProperties {
    // Remove basePath, connectTimeout, socketTimeout — inherited
    // Add only integration-specific endpoint path fields here (if any)
}
```
Update `application.properties` key prefix to `bpi.microservice.inquire-branch.*`.

---

### RC-10: `InquireBranchSoapConfigProperties` — same `@Component` and `callSuper = false` issues

| Field        | Value                                                                                                  |
|--------------|--------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/config/properties/InquireBranchSoapConfigProperties.java:L1-14` |
| **Severity** | ⚠️ Warning                                                                                             |
| **Category** | Code Quality                                                                                           |

**Comment:**
Same class-level issues as `InquireBranchApiConfigProperties`: uses `@Component` (should not), `@EqualsAndHashCode(callSuper = false)` (should be `true`). Missing `@ToString(callSuper = true)` and `@Validated`.

**Reference(s):**
- Gold Standard §HTTP Client Configuration Properties

**Suggested Fix:**
Remove `@Component`, change `callSuper = false` to `true`, add `@ToString(callSuper = true)` and `@Validated`.

---

### RC-11: `InquireBranchWsServiceImpl` — wrong naming, setter injection, missing Web Service naming convention

| Field        | Value                                                                                                |
|--------------|------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/ws/impl/InquireBranchWsServiceImpl.java:L13-39`      |
| **Severity** | ⚠️ Warning                                                                                           |
| **Category** | Code Quality                                                                                         |

**Comment:**
Three issues:
1. **Wrong naming** — Gold Standards name the raw HTTP client `<Integration>RestTemplate`. The class here is named `InquireBranchWsServiceImpl`, which conflates the HTTP client and the Web Service wrapper into one name. The HTTP client should be `InquireBranchRestTemplate`; a separate wrapper `InquireBranchServiceImpl` would wrap it.
2. **Setter injection** — `@Autowired` on a setter for `InquireBranchApiConfigProperties`. Gold Standards require field injection for config properties.
3. **`post(...).getBody()` without null check** — if the downstream returns no body, `getBody()` returns `null`, which is then used in `BranchInquiryMasterServiceImpl` (the null check there now throws, but it throws `InvalidAccountException` with `CUSTOMER_NOT_FOUND` — an incorrect error code for a downstream connectivity issue).

**Reference(s):**
- Gold Standard §HTTP Client Implementation: "Must be named `<Integration>RestTemplate`"
- `src/main/java/com/bpi/customerservice/service/biz/impl/BranchInquiryMasterServiceImpl.java:L40-42`

**Suggested Fix:**
1. Rename to `InquireBranchRestTemplate`, move class to `service.ws.inquirebranch` package
2. Switch from setter to field injection for `InquireBranchApiConfigProperties`
3. Use `BranchServiceException(InquireBranchErrorCode.SERVICE_ERROR)` for downstream null/empty responses, not `CustomerNotFoundException`

---

### RC-12: `BranchInquiryMasterServiceImpl` throws wrong exception type on null/invalid downstream response

| Field        | Value                                                                                                    |
|--------------|----------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/biz/impl/BranchInquiryMasterServiceImpl.java:L40-47`     |
| **Severity** | ⚠️ Warning                                                                                               |
| **Category** | Functional Alignment / Potential Bug or Risky Area                                                      |

**Comment:**
When `branchResponse` is null, or when the response code is not `"0"`, the method throws `InvalidAccountException(CustomerServiceErrorCode.CUSTOMER_NOT_FOUND)`. This is semantically wrong on two counts:
1. A null branch response is a **downstream connectivity/data issue**, not proof that the customer was not found.
2. `CUSTOMER_NOT_FOUND` is a business error code for when a customer entity doesn't exist in the database — it should not be thrown for downstream validation failures.

The handler for `InvalidAccountException` returns HTTP 400, but a downstream service returning invalid data should produce a different error code (e.g., `INVALID_ACCOUNT`). A null response warrants a service error (503).

**Reference(s):**
- `src/main/java/com/bpi/customerservice/errorcode/customer/CustomerServiceErrorCode.java:L5-8`
- `src/main/java/com/bpi/customerservice/exception/GlobalExceptionHandler.java:L55-61`

**Suggested Fix:**
```java
if (branchResponse == null || branchResponse.getInquireBranchOperationResponse() == null) {
    throw new BranchServiceException(InquireBranchErrorCode.SERVICE_ERROR); // 503, not 400
}
// ...
if (output == null || !"0".equals(output.getResponseCode())) {
    throw new InvalidAccountException(CustomerServiceErrorCode.INVALID_ACCOUNT); // correct error code
}
```

---

### RC-13: `CustomerMasterServiceImpl` validation logic is overly permissive

| Field        | Value                                                                                               |
|--------------|-----------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/biz/impl/CustomerMasterServiceImpl.java:L19-28`     |
| **Severity** | ⚠️ Warning                                                                                          |
| **Category** | Potential Bug or Risky Area                                                                         |

**Comment:**
The validation logic `isAccountValid` evaluates to `true` if **either** the `accounts` list contains the account number **OR** `customer.getCustomerNumber() != null`. The second condition (`customer.getCustomerNumber() != null`) is always true for any customer that exists in the database, so the entire validation is effectively a no-op — any account number will pass for any existing customer. The intent appears to be to validate the account number belongs to the customer, but the `|| (customer.getCustomerNumber() != null)` clause defeats that check entirely.

**Reference(s):**
- `src/main/java/com/bpi/customerservice/service/biz/impl/CustomerMasterServiceImpl.java:L22-27`
- Previous review RC-23 (partially addressed but logic is still wrong)

**Suggested Fix:**
The correct check depends on the actual data model. If `CustomerResponse.accounts` contains the customer's associated accounts, the check should be:
```java
boolean isAccountValid = customer.getAccounts() != null &&
    customer.getAccounts().stream()
        .anyMatch(account -> accountNumber.equals(account.getAccountNumber()));

if (!isAccountValid) {
    throw new InvalidAccountException(CustomerServiceErrorCode.INVALID_ACCOUNT);
}
```

---

### RC-14: `CustomerControllerApi` and `BranchInquiryControllerApi` missing `@Tag` annotation

| Field        | Value                                                                                                                                           |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/controller/customer/CustomerControllerApi.java:L1`<br>`src/main/java/com/bpi/customerservice/controller/branchinquiry/BranchInquiryControllerApi.java:L1` |
| **Severity** | ⚠️ Warning                                                                                                                                       |
| **Category** | Code Quality                                                                                                                                    |

**Comment:**
`CustomerControllerApi` uses `tags = {"Customer Management"}` inside `@Operation` but is missing the class-level `@Tag(name = "Customer Management")` annotation. `BranchInquiryControllerApi` has `tags = {"Branch Inquiry"}` in `@Operation` but no `@Tag` either. `InquireCustomerSoapControllerApi` has no Swagger documentation at all. The Gold Standards require `@Tag` at the interface level for Swagger grouping.

**Reference(s):**
- Gold Standard §API Interface: "Annotate the interface with `@Tag(name = '<Domain><Action>')`"

**Suggested Fix:**
Add `@Tag(name = "Customer Management")` to `CustomerControllerApi`, `@Tag(name = "BranchInquiry")` to `BranchInquiryControllerApi`, and full Swagger documentation (`@Tag`, `@Operation`, `@ApiResponses`) to `InquireCustomerSoapControllerApi`.

---

### RC-15: `CustomerControllerApi` and all API interfaces missing 401 and 403 response documentation

| Field        | Value                                                                                     |
|--------------|-------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/controller/customer/CustomerControllerApi.java:L18-50` |
| **Severity** | ⚠️ Warning                                                                                |
| **Category** | Code Quality                                                                              |

**Comment:**
All `@ApiResponses` blocks on `CustomerControllerApi` methods omit the mandatory `401 Unauthorized` and `403 Forbidden Access` responses. `BranchInquiryControllerApi` does include them. `InquireCustomerSoapControllerApi` has none at all.

**Reference(s):**
- Gold Standard §API Interface: "Declare `@ApiResponses` covering, at minimum: 200, 400, 401, 403, 404, and 500."

**Suggested Fix:**
Add to every operation in `CustomerControllerApi`:
```java
@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
@ApiResponse(responseCode = "403", description = "Forbidden Access", content = @Content)
```

---

### RC-16: Response DTOs missing mandatory static `Model` inner class

| Field        | Value                                                                                                                                                                |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/model/api/CustomerResponse.java:L12`<br>`src/main/java/com/bpi/customerservice/model/api/InquireBranchResponse.java:L12`<br>`src/main/java/com/bpi/customerservice/model/api/InquireCustomerResponse.java:L12` |
| **Severity** | ⚠️ Warning                                                                                                                                                           |
| **Category** | Code Quality                                                                                                                                                         |

**Comment:**
None of the three response DTOs define a `public static class <Response>Model extends Response<<Response>> {}` inner class. Without it, Swagger's `@Schema(implementation = ...)` reference in `@ApiResponses` cannot properly resolve the `Response<T>` generic wrapper schema, breaking OpenAPI documentation for the 200 response.

**Reference(s):**
- Gold Standard §Standard Response Structure: "Every response DTO must declare a `public static class <Response>Model extends Response<<Response>> {}` inner class."
- Previous review RC-18 (still unresolved)

**Suggested Fix:**
```java
// Add to CustomerResponse, InquireBranchResponse, InquireCustomerResponse:
public static class CustomerResponseModel extends Response<CustomerResponse> {}
```
Then reference `CustomerResponseModel.class` in the `@Schema(implementation = ...)` on the API interface.

---

### RC-17: `BranchInquiryControllerApi` uses wrong `@Schema` implementation class for 200 response

| Field        | Value                                                                                                  |
|--------------|--------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/controller/branchinquiry/BranchInquiryControllerApi.java:L20`  |
| **Severity** | ⚠️ Warning                                                                                             |
| **Category** | Code Quality                                                                                           |

**Comment:**
The `@ApiResponse(responseCode = "200")` schema references `InquireCustomerResponse.class` — but `BranchInquiryController` actually returns `Response<InquireBranchResponse>`. The 200 schema points to the wrong DTO. This generates incorrect Swagger documentation.

**Reference(s):**
- `src/main/java/com/bpi/customerservice/controller/branchinquiry/BranchInquiryControllerApi.java:L20`

**Suggested Fix:**
Once `InquireBranchResponse` has its `Model` inner class, update the 200 schema to:
```java
@ApiResponse(responseCode = "200", description = "Success",
    content = {@Content(schema = @Schema(implementation = InquireBranchResponse.InquireBranchResponseModel.class))})
```

---

### RC-18: `dupllicate jacoco-maven-plugin` declaration still present in `pom.xml`

| Field        | Value                       |
|--------------|-----------------------------|
| **Location** | `pom.xml:L109` and `L139`   |
| **Severity** | ⚠️ Warning                  |
| **Category** | Code Quality                |

**Comment:**
Two `jacoco-maven-plugin` declarations remain in `pom.xml` — the new one (with CXF exclusions and `prepare-agent`/`report` goals) and the original one (with the coverage `check` goal). Maven will execute both declarations, potentially running goals twice or causing configuration conflicts.

**Reference(s):**
- Previous review RC-10 (still unresolved)

**Suggested Fix:**
Merge both declarations into one, combining the `excludes` configuration, `prepare-agent`, `report`, and `check` goals.

---

### RC-19: Raw `RestTemplate` bean on `CustomerserviceApplication` — still present

| Field        | Value                                                                               |
|--------------|-------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/CustomerserviceApplication.java:L13-16`     |
| **Severity** | ⚠️ Warning                                                                          |
| **Category** | Code Quality / Performance                                                          |

**Comment:**
The bare `RestTemplate` bean declared directly on the main application class (using fully qualified annotation names) is still present. It is not connected to any integration and has no connection pool or timeouts. All HTTP client beans must be wired in a dedicated `RestTemplateConfig` class using `RestApiConfigurerComponent`.

**Reference(s):**
- Gold Standard §HTTP Client Configuration: "There must be exactly one `RestTemplateConfig` class per microservice containing all HTTP client beans."
- Previous review RC-11 (still unresolved)

**Suggested Fix:**
Remove the bean. Move all HTTP client wiring to `config/RestTemplateConfig.java`.

---

### RC-20: `InquireCustomerSoapErrorTest` — null-response test uses vague `is4xxClientError()`

| Field        | Value                                                                                                      |
|--------------|------------------------------------------------------------------------------------------------------------|
| **Location** | `src/test/java/com/bpi/customerservice/flow/inquirecustomer/InquireCustomerSoapErrorTest.java:L44-56`     |
| **Severity** | 💡 Suggestion                                                                                              |
| **Category** | Potential Bug or Risky Area                                                                               |

**Comment:**
`testInquireCustomer_NullResponse` only asserts `status().is4xxClientError()`, which matches any status between 400 and 499. This hides any future regression from 400 to 404 or 422 and does not verify the intended error code. It should pin the exact expected status and error body.

**Reference(s):**
- Gold Standard §Unit Testing: "Use explicit, hardcoded values for expected outcomes."
- Previous review RC-25 (still unresolved)

**Suggested Fix:**
```java
.andExpect(status().isBadRequest())
.andExpect(jsonPath("$.code").value("CUSVE001"))
.andExpect(jsonPath("$.message").value("Invalid Account Number"));
```

---

### RC-21: `CustomerMasterServiceImpl` triggers audit log write for every internal validation call

| Field        | Value                                                                                              |
|--------------|----------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/biz/impl/CustomerMasterServiceImpl.java:L18`        |
| **Severity** | ⚠️ Warning                                                                                         |
| **Category** | Performance                                                                                        |

**Comment:**
`customerService.getCustomer(customerNumber, "internal-req", "system")` routes through `CustomerServiceImpl.getCustomer()`, which writes an audit log entry to MongoDB on every call. Every branch inquiry therefore triggers a MongoDB write for an internal system-to-system validation check, with hardcoded `"internal-req"` and `"system"` values that pollute the audit trail and add unnecessary write latency.

**Reference(s):**
- `src/main/java/com/bpi/customerservice/repository/impl/CustomerServiceImpl.java:L45-50`
- Previous review RC-22 (still unresolved)

**Suggested Fix:**
Add a read-only lookup path that bypasses audit logging for internal validation, or pass the real request context down from the controller boundary. At minimum, do not generate audit records for system-internal validation calls.

---

*End of review.*
