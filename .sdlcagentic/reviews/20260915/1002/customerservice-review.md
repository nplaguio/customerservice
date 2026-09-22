# Code Review Report

## Metadata
| Field                   | Value                                                        |
|-------------------------|--------------------------------------------------------------|
| Reviewed At             | 2026-09-15 10:02                                             |
| Reviewed By             | AI Agent (swe-review-mr-code)                                |
| Requested By            | n/a                                                          |
| Review Type             | Staged/Working Changes                                        |
| MR/PR Link              | n/a                                                          |
| Base Git Branch         | master                                                       |
| Base Git Commit         | 262a5e608920d4a5871e3aa891a7ec7aeb01dfaf                    |

---

## Final Verdict

**Result:** 🔴 FAIL

One or more blocking issues must be resolved before this change can merge.

### Summary Table

| Category                    | Blocking | Warning | Suggestion |
|-----------------------------|----------|---------|------------|
| Functional Alignment        | 4        | 4       | 0          |
| Code Quality                | 18       | 10      | 0          |
| Potential Bug or Risky Area | 4        | 6       | 1          |
| Security                    | 6        | 3       | 1          |
| Performance                 | 2        | 2       | 0          |
| Others                      | 0        | 0       | 0          |

---

## Review Comments

---

### RC-1: `CustomerNotFoundException` handler returns HTTP 400 instead of 404

| Field        | Value                                                                                         |
|--------------|-----------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/exception/GlobalExceptionHandler.java:L25-30`         |
| **Severity** | 🚨 Blocking                                                                                    |
| **Category** | Functional Alignment / Potential Bug or Risky Area                                            |

**Comment:**
The `CustomerNotFoundException` handler is annotated with the comment `// 404` but returns `HttpStatus.BAD_REQUEST` (400). The integration tests `testGetCustomer_NotFound_Returns404`, `testUpdateCustomer_NotFound_Returns404`, and `testInquireBranch_CustomerNotFound_Returns404` all assert `status().isNotFound()` — these tests will fail as written. A missing resource is semantically a 404, not a 400.

**Reference(s):**
- `src/test/java/com/bpi/customerservice/CustomerIntegrationTest.java:L160` — `testGetCustomer_NotFound_Returns404` expects `status().isNotFound()`
- Gold Standard §Error Handling — customer not found is a business error, correctly returned as a non-400 by convention

**Suggested Fix:**
```java
@ExceptionHandler(CustomerNotFoundException.class)
public ResponseEntity<Map<String, String>> handleCustomerNotFound(CustomerNotFoundException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("code", ex.getErrorCode().getCode());
    errorResponse.put("message", ex.getErrorCode().getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND); // was BAD_REQUEST
}
```

---

### RC-2: SOAP request sent with empty input — `accountNumber` is never set on `WsrvalngInput`

| Field        | Value                                                                                                |
|--------------|------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/soap/InquireCustomerSoapImpl.java:L17-27`            |
| **Severity** | 🚨 Blocking                                                                                           |
| **Category** | Functional Alignment / Potential Bug or Risky Area                                                   |

**Comment:**
`callSoapService(String accountNumber)` accepts the account number but never sets it on `WsrvalngInput requestInput`. The SOAP service is always invoked with a blank/default request payload, causing every inquiry to query the wrong (or no) account. The feature is non-functional as shipped.

**Reference(s):**
- `src/main/java/com/bpi/customerservice/service/soap/InquireCustomerSoapImpl.java:L17-20`

**Suggested Fix:**
Identify the field on `WsrvalngInput` that corresponds to the account number (the generated class will have a setter for it) and set it before calling the SOAP operation:
```java
WsrvalngInput requestInput = new WsrvalngInput();
requestInput.setWsInAccount(accountNumber); // use the correct generated field name
```

---

### RC-3: SOAP response content is ignored — `mapToApiResponse` always returns hardcoded success

| Field        | Value                                                                                              |
|--------------|----------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/biz/impl/InquireCustomerMasterImpl.java:L37-51`    |
| **Severity** | 🚨 Blocking                                                                                         |
| **Category** | Functional Alignment / Potential Bug or Risky Area                                                  |

**Comment:**
`mapToApiResponse(WsrvalngOutput soapResponse, String accountNumber)` ignores every field in the SOAP response and always returns `responseCode = "0"` and `responseDescription = "SUCCESS"`. Downstream error codes, customer data, and non-success signals from the SOAP service are silently discarded. The feature will always report success regardless of what the SOAP service returns.

**Reference(s):**
- `src/main/java/com/bpi/customerservice/service/biz/impl/InquireCustomerMasterImpl.java:L41-51`

**Suggested Fix:**
Read the actual fields from `soapResponse` and map them into the response, checking for non-success status codes and throwing the appropriate exception:
```java
private InquireCustomerResponse mapToApiResponse(WsrvalngOutput soapResponse, String accountNumber) {
    // Read actual fields from soapResponse (use generated getters)
    String replyCode = soapResponse.getWsOutReplyCode(); // adjust to actual getter name
    if (!"0".equals(replyCode)) {
        throw new InvalidAccountException(CustomerServiceErrorCode.INVALID_ACCOUNT);
    }
    InquireCustomerResponse.InquireCustomerOutput output = InquireCustomerResponse.InquireCustomerOutput.builder()
        .accountNumber(accountNumber)
        .responseCode(replyCode)
        .responseDescription(soapResponse.getWsOutReplyDesc())
        // ... map other fields
        .build();
    // ...
}
```

---

### RC-4: `BranchInquiryMasterImpl` silently returns `null` on partial downstream response

| Field        | Value                                                                                              |
|--------------|----------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/biz/impl/BranchInquiryMasterImpl.java:L41-54`      |
| **Severity** | 🚨 Blocking                                                                                         |
| **Category** | Functional Alignment / Potential Bug or Risky Area                                                  |

**Comment:**
When `callBranchService` succeeds (no exception) but returns a null, partially-null, or structurally incomplete response, all three guard clauses fall through and the method returns `null`. The controller then calls `ResponseEntity.ok(null)`, sending HTTP 200 with an empty body. A downstream failure is misrepresented as a successful response.

**Reference(s):**
- `src/main/java/com/bpi/customerservice/service/biz/impl/BranchInquiryMasterImpl.java:L54` — `return null;`

**Suggested Fix:**
Replace the silent `return null` with an explicit exception:
```java
// at the end of executeBranchInquiry, instead of return null:
throw new BranchServiceException(InquireBranchErrorCode.SERVICE_ERROR);
```

---

### RC-5: Encrypted secrets file committed to git

| Field        | Value                                                                  |
|--------------|------------------------------------------------------------------------|
| **Location** | `src/main/resources/policies/global.secret.enc.properties:L1-6`       |
| **Severity** | 🚨 Blocking                                                             |
| **Category** | Security                                                               |

**Comment:**
`global.secret.enc.properties` contains encrypted keystore/truststore passwords and encrypted API authentication credentials (`apiAuth.*`). This file is git-tracked (status `A` — staged). Even though values are encrypted, the encryption key `app.certKeyPart` is also committed in `application.properties` (see RC-6), meaning the material to decrypt these secrets is co-located in the same repository. Anyone with repo access can decrypt all credentials. All affected credentials should be considered compromised and must be rotated immediately.

**Reference(s):**
- Security Rule: **NEVER commit secrets, passwords, API keys, or tokens to version control**
- `src/main/resources/application.properties:L21` — encryption key `app.certKeyPart` co-located

**Suggested Fix:**
1. Remove the file from git tracking: `git rm --cached src/main/resources/policies/global.secret.enc.properties`
2. Add it to `.gitignore`
3. Rotate all leaked credentials (keystore/truststore passwords, all `apiAuth.*` values)
4. Load secrets from a secure secrets manager (HashiCorp Vault, IBM Key Protect) at runtime

---

### RC-6: Cryptographic key committed in `application.properties`

| Field        | Value                                                             |
|--------------|-------------------------------------------------------------------|
| **Location** | `src/main/resources/application.properties:L21`                  |
| **Severity** | 🚨 Blocking                                                        |
| **Category** | Security                                                          |

**Comment:**
`app.certKeyPart` contains a large Base64-encoded cryptographic key/certificate material directly in `application.properties`, which is committed to the repository. This key is the decryption credential for the secrets file in RC-5 — its exposure in source control defeats the entire encryption protection. This must be treated as a leaked secret.

**Reference(s):**
- Security Rule: **NEVER hardcode secrets, passwords, API keys, or tokens in code**

**Suggested Fix:**
Rotate the key material and inject `app.certKeyPart` through environment variables or a secrets manager at runtime. Never commit cryptographic key material to source control.

---

### RC-7: All three controllers missing `@EnableAccessPolicies` — endpoints are unauthenticated

| Field        | Value                                                                                                                                                                                                          |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/controller/customer/CustomerController.java:L12`<br>`src/main/java/com/bpi/customerservice/controller/branchinquiry/BranchInquiryController.java:L11`<br>`src/main/java/com/bpi/customerservice/controller/inquirecustomer/InquireCustomerController.java:L11` |
| **Severity** | 🚨 Blocking                                                                                                                                                                                                     |
| **Category** | Security / Code Quality                                                                                                                                                                                         |

**Comment:**
None of the three controllers carry `@EnableAccessPolicies`. The BPI Framework documentation states that omitting this annotation **breaks the framework's security handling** — `apiKey`/`apiSecret` authentication is not enforced, and authorization policies are bypassed. All customer, branch inquiry, and customer inquiry endpoints are effectively unauthenticated.

**Reference(s):**
- Gold Standard §Controller Implementation: "`@EnableAccessPolicies` — **mandatory** on every controller. Omitting this annotation breaks the framework's security handling."

**Suggested Fix:**
```java
@EnableAccessPolicies   // ADD THIS
@RestController
@RequestMapping("/api/customers")
public class CustomerController implements CustomerControllerApi { ... }
```
Apply the same fix to `BranchInquiryController` and `InquireCustomerController`.

---

### RC-8: All controllers missing `apiKey` and `apiSecret` standard headers

| Field        | Value                                                                                                                                                                                                    |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/controller/customer/CustomerControllerApi.java:L22-63`<br>`src/main/java/com/bpi/customerservice/controller/branchinquiry/BranchInquiryControllerApi.java:L11-16` |
| **Severity** | 🚨 Blocking                                                                                                                                                                                               |
| **Category** | Security / Code Quality / Functional Alignment                                                                                                                                                            |

**Comment:**
The Gold Standards require all four standard headers on every API method: `apiKey`, `apiSecret`, `requestUID`, `resourceOwnerId`. `CustomerControllerApi` declares only `requestUID` and `resourceOwnerID`. `BranchInquiryControllerApi` declares none. The BPI Framework security component cannot locate the credentials required to authenticate the request.

**Reference(s):**
- Gold Standard §Standard Headers: "Every controller API method must declare all 4 headers as method parameters, regardless of whether the underlying service logic uses them."

**Suggested Fix:**
Add to all API interface and controller method signatures:
```java
@RequestHeader String apiKey,
@RequestHeader String apiSecret,
@RequestHeader String requestUID,
@RequestHeader String resourceOwnerId
```

---

### RC-9: `JaxWsProxyFactoryBean` created on every SOAP request — resource leak and per-request overhead

| Field        | Value                                                                                              |
|--------------|----------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/soap/InquireCustomerSoapImpl.java:L22-26`          |
| **Severity** | 🚨 Blocking                                                                                         |
| **Category** | Performance / Code Quality                                                                          |

**Comment:**
`callSoapService()` creates a new `JaxWsProxyFactoryBean`, initializes the endpoint, and creates a new SOAP proxy client on every single invocation. Creating a JAX-WS proxy involves WSDL parsing, thread pool allocation, and connection setup. This creates significant per-request overhead and may not properly clean up associated threads and connections, constituting a resource leak under sustained load.

**Reference(s):**
- `src/main/java/com/bpi/customerservice/service/soap/InquireCustomerSoapImpl.java:L22-26`

**Suggested Fix:**
Create the `WSRVALNGPort` proxy once as a Spring-managed singleton bean in a config class and inject it:
```java
// In a @Configuration class:
@Bean
public WSRVALNGPort wsrvalngPort(@Value("${soap.inquire-customer.endpoint-url}") String url) {
    JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(WSRVALNGPort.class);
    factory.setAddress(url);
    return (WSRVALNGPort) factory.create();
}

// In InquireCustomerSoapImpl:
@Autowired
private WSRVALNGPort wsrvalngPort;

@Override
public WsrvalngOutput callSoapService(String accountNumber) {
    WsrvalngInput input = new WsrvalngInput();
    input.setWsInAccount(accountNumber);
    return wsrvalngPort.wsrvalngOperation(input);
}
```

---

### RC-10: Duplicate `jacoco-maven-plugin` declaration in `pom.xml`

| Field        | Value                                     |
|--------------|-------------------------------------------|
| **Location** | `pom.xml:L109` and `pom.xml:L139`         |
| **Severity** | ⚠️ Warning                                |
| **Category** | Code Quality                              |

**Comment:**
There are two separate `jacoco-maven-plugin` declarations in the build section. The first (added) includes CXF exclusion configuration and both `prepare-agent` and `report` goals. The second (pre-existing) includes the check goal for coverage enforcement. Maven may execute duplicated goals, and the configurations will interfere. The intent was likely to merge/update the existing declaration, not add a second one.

**Reference(s):**
- `pom.xml:L86-133` (new declaration) vs `pom.xml:L135-152` (original)

**Suggested Fix:**
Merge all JaCoCo configuration into a single plugin declaration combining the exclusions, `prepare-agent`, `report`, and `check` goals.

---

### RC-11: Raw `RestTemplate` bean on `CustomerserviceApplication` violates HTTP client conventions

| Field        | Value                                                                                   |
|--------------|-----------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/CustomerserviceApplication.java:L13-16`         |
| **Severity** | ⚠️ Warning                                                                               |
| **Category** | Code Quality / Performance                                                              |

**Comment:**
A bare `RestTemplate` bean is registered on the main application class with fully qualified annotation names (`@org.springframework.context.annotation.Bean`). The Gold Standards require all HTTP client beans to be wired in a dedicated `RestTemplateConfig` class using `RestApiConfigurerComponent`. The raw bean has no connection pool and no timeouts — under load, it will exhaust file descriptors and can block request threads indefinitely.

**Reference(s):**
- Gold Standard §HTTP Client Configuration: "There must be exactly **one** `RestTemplateConfig` class per microservice, containing the bean definitions for **all** downstream HTTP clients."

**Suggested Fix:**
Remove the bean from `CustomerserviceApplication`. Move all HTTP client configuration to `config/RestTemplateConfig.java` using the standard `RestApiConfigurerComponent.configure(...)` pattern.

---

### RC-12: `InquireBranchApiConfigProperties` violates multiple Gold Standards rules

| Field        | Value                                                                                    |
|--------------|------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/config/InquireBranchApiConfigProperties.java:L1-21` |
| **Severity** | 🚨 Blocking                                                                               |
| **Category** | Code Quality / Performance                                                               |

**Comment:**
Multiple violations in this class:
1. **Wrong package** — it is in `config` root, must be in `config.properties` subpackage.
2. **`@Component` present** — config properties classes must not use `@Component`; the annotation conflicts with Spring Boot's `@ConfigurationProperties` binding.
3. **Wrong prefix** — uses `bpi.api.inquire-branch` instead of `bpi.microservice.inquire-branch`.
4. **Redeclares inherited fields** — `basePath`, `connectTimeout`, `socketTimeout` already exist in `HttpApiConfigProperties`. Redeclaring them in the subclass causes the bound values to be in the wrong field — Spring binds to the subclass field, but the framework reads from the parent field. **The configured timeouts are never applied**, causing the HTTP client to have no effective timeout.
5. **Missing annotations** — `@ToString(callSuper = true)` and `@Validated` are absent.
6. **`@EqualsAndHashCode(callSuper = false)`** — must be `callSuper = true`.

**Reference(s):**
- Gold Standard §HTTP Client Configuration Properties

**Suggested Fix:**
```java
package com.bpi.customerservice.config.properties; // move to subpackage

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("bpi.microservice.inquire-branch") // correct prefix
@Validated
public class InquireBranchApiConfigProperties extends HttpApiConfigProperties {
    // Remove basePath, connectTimeout, socketTimeout — inherited from parent
    // Only declare integration-specific endpoint paths here
}
```
Also update `application.properties` key prefix to `bpi.microservice.inquire-branch.*`.

---

### RC-13: `InquireBranchApiImpl` violates HTTP client naming and injection conventions

| Field        | Value                                                                                  |
|--------------|----------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/rest/InquireBranchApiImpl.java:L13-35` |
| **Severity** | 🚨 Blocking                                                                             |
| **Category** | Code Quality                                                                           |

**Comment:**
Three violations:
1. **Wrong class name** — must be named `InquireBranchRestTemplate` per Gold Standards.
2. **Wrong package** — `service.rest` is non-standard; must be in `service.ws.inquirebranch`.
3. **Setter injection** — config properties are injected via an `@Autowired` setter; Gold Standards require field injection.
4. **Missing Web Service wrapper** — the business layer (`BranchInquiryMasterImpl`) calls the raw HTTP client directly. Gold Standards mandate a `InquireBranchService`/`InquireBranchServiceImpl` wrapper between the raw HTTP client and the business service to handle integration-specific error interpretation.

**Reference(s):**
- Gold Standard §HTTP Client Implementation: "Must be named `<Integration>RestTemplate`"
- Gold Standard §Web Services: "Every downstream integration's raw HTTP client is wrapped by a Web Service. It is never bypassed to call the HTTP client directly."

**Suggested Fix:**
1. Rename to `InquireBranchRestTemplate`, move to `service.ws.inquirebranch`.
2. Switch to field injection for config properties.
3. Create `InquireBranchService` / `InquireBranchServiceImpl` in the same package, have the biz layer depend on the service wrapper.

---

### RC-14: Error code classes violate the one-class-per-microservice rule and lack factory methods / `parse()`

| Field        | Value                                                                                                                             |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/errorcode/customer/CustomerServiceErrorCode.java:L1-15`<br>`src/main/java/com/bpi/customerservice/errorcode/branch/InquireBranchErrorCode.java:L1-12` |
| **Severity** | 🚨 Blocking                                                                                                                        |
| **Category** | Code Quality                                                                                                                      |

**Comment:**
The Gold Standards are explicit: **one `ErrorCode` class per microservice** in the `errorcode` package root. Two separate classes split by domain violate this. Additionally, both classes:
- Instantiate constants with raw constructors instead of category factory methods (`internal()`, `service()`, `business()`, `validation()`)
- Lack a static `Map<String, ErrorCode>` and `parse(String)` method required for error code lookup

**Reference(s):**
- Gold Standard §Error Code: "Each microservice defines exactly one class... Must expose a static `parse(String code)` method"

**Suggested Fix:**
Consolidate into a single `CustomerServiceErrorCode` in `errorcode` (not a subdirectory) and implement the required pattern:
```java
public class CustomerServiceErrorCode extends ErrorCode {
    public static final ErrorCode CUSTOMER_NOT_FOUND = business("001", "Customer not found");
    public static final ErrorCode INVALID_ACCOUNT    = validation("001", "Invalid Account Number");
    public static final ErrorCode BRANCH_SERVICE_ERROR = service("001", "Inquire Branch service unavailable");
    public static final ErrorCode SERVICE_ERROR      = service("999", "External Service Error");
    public static final ErrorCode INTERNAL_ERROR     = internal("999", "Internal Error");

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

### RC-15: API service layer violates Gold Standards — wrong package, naming, and return type

| Field        | Value                                                                                                                                           |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/BranchInquiryMainApi.java:L1`<br>`src/main/java/com/bpi/customerservice/service/inquirecustomermain/InquireCustomerMainApi.java:L1`<br>`src/main/java/com/bpi/customerservice/service/customer/CustomerService.java:L1` |
| **Severity** | 🚨 Blocking                                                                                                                                      |
| **Category** | Code Quality                                                                                                                                    |

**Comment:**
All three API service interfaces violate the Gold Standards:
1. **Wrong packages** — `service`, `service.inquirecustomermain`, `service.customer` instead of `service.api.<domain>`
2. **Wrong naming** — `BranchInquiryMainApi`, `InquireCustomerMainApi` instead of `<Domain><Action>Service`
3. **Wrong return type** — all return response DTOs directly instead of `RestResponseContentWrapper<HttpHeaders, T>`

**Reference(s):**
- Gold Standard §API Services: "Interface and implementation must reside in `service.api.<domain>`. Return type must be `RestResponseContentWrapper<HttpHeaders, T>`."

**Suggested Fix:**
Relocate to `service.api.branchinquiry`, `service.api.inquirecustomer`, and `service.api.customer`. Rename to `BranchInquiryService`, `InquireCustomerService`, `CustomerManagementService`. Change return types to `RestResponseContentWrapper<HttpHeaders, InquireCustomerResponse>` etc.

---

### RC-16: Controllers do not use `ResponseConverter.convert()` and depend on wrong service layers

| Field        | Value                                                                                                                                   |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/controller/customer/CustomerController.java:L21-61`<br>`src/main/java/com/bpi/customerservice/controller/branchinquiry/BranchInquiryController.java:L18-25`<br>`src/main/java/com/bpi/customerservice/controller/inquirecustomer/InquireCustomerController.java:L18-25` |
| **Severity** | 🚨 Blocking                                                                                                                              |
| **Category** | Code Quality                                                                                                                            |

**Comment:**
Controller implementations construct `ResponseEntity` directly instead of delegating through `ResponseConverter.convert(service.process(request))`. `CustomerController` also directly injects `service.customer.CustomerService` (a business-layer service) — controllers must only depend on `service.api.<domain>` layer.

**Reference(s):**
- Gold Standard §Controller Implementation: "Its method body should consist of a single delegation call to the injected service, wrapped in `ResponseConverter.convert(...)`."
- Gold Standard §Dependency Injection: "Autowire exactly one service dependency from `service.api.<domain>` per controller."

**Suggested Fix:**
```java
@Override
@PostMapping
public ResponseEntity<Response<CustomerResponse>> createCustomer(...) {
    return ResponseConverter.convert(customerManagementService.createCustomer(request));
}
```

---

### RC-17: `CustomerControllerApi` exposes JPA `Customer` entity directly as request/response type

| Field        | Value                                                                                      |
|--------------|--------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/controller/customer/CustomerControllerApi.java:L22` |
| **Severity** | 🚨 Blocking                                                                                 |
| **Category** | Code Quality                                                                               |

**Comment:**
All four API methods use the JPA `Customer` entity as both the request body and response type. The `CustomerRequest` and `CustomerResponse` DTOs already exist in `dto.api` but are unused. Exposing the entity directly:
- Leaks database schema structure to API consumers
- Binds the API contract to the persistence model
- Prevents independent evolution of each

**Reference(s):**
- Gold Standard §DTO: "Request DTOs: Represent the payload received from API clients. Response DTOs: Represent the payload returned to API clients."

**Suggested Fix:**
Replace entity parameters/return types with `CustomerRequest` (request body) and `CustomerResponse` (response). Add the mandatory static `Model` inner class to `CustomerResponse`:
```java
public static class CustomerResponseModel extends Response<CustomerResponse> {}
```

---

### RC-18: Response DTOs missing mandatory static `Model` inner class

| Field        | Value                                                                                                                          |
|--------------|--------------------------------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/dto/api/InquireCustomerResponse.java:L9`<br>`src/main/java/com/bpi/customerservice/dto/api/CustomerResponse.java:L12` |
| **Severity** | 🚨 Blocking                                                                                                                     |
| **Category** | Code Quality                                                                                                                   |

**Comment:**
`InquireCustomerResponse` and `CustomerResponse` both lack the mandatory static `Model` inner class. Without it, Swagger cannot properly render the `Response<T>` generic wrapper for the 200 response schema in generated API documentation, and the `@Schema(implementation = ...)` reference in the API interface will break.

**Reference(s):**
- Gold Standard §Standard Response Structure: "Every response DTO must declare a `public static class <Response>Model extends Response<<Response>> {}` inner class."

**Suggested Fix:**
```java
// Add to InquireCustomerResponse:
public static class InquireCustomerResponseModel extends Response<InquireCustomerResponse> {}

// Add to CustomerResponse:
public static class CustomerResponseModel extends Response<CustomerResponse> {}
```

---

### RC-19: `BranchInquiryControllerApi` and `InquireCustomerControllerApi` missing Swagger metadata

| Field        | Value                                                                                                                                     |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/controller/branchinquiry/BranchInquiryControllerApi.java:L1`<br>`src/main/java/com/bpi/customerservice/controller/inquirecustomer/InquireCustomerControllerApi.java:L1` |
| **Severity** | ⚠️ Warning                                                                                                                                 |
| **Category** | Code Quality                                                                                                                              |

**Comment:**
Both controller API interfaces are missing the `@Tag` annotation (required for Swagger grouping), `@Operation`, and `@ApiResponses` (including mandatory 200, 400, 401, 403, 404, 500 responses). `InquireCustomerControllerApi` also does not declare the `@PostMapping` route — the URL mapping is only on the implementation, not the contract.

**Reference(s):**
- Gold Standard §API Interface (Swagger Documentation): "`@Tag(name = ...)` ... `@Operation(description = ...)` ... `@ApiResponses` covering at minimum: 200, 400, 401, 403, 404, and 500."

**Suggested Fix:**
Add `@Tag`, `@Operation`, and a complete `@ApiResponses` block to both interfaces. Add the `@PostMapping` route declaration to `InquireCustomerControllerApi`.

---

### RC-20: `CustomerControllerApi` missing 401 and 403 response documentation

| Field        | Value                                                                                    |
|--------------|------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/controller/customer/CustomerControllerApi.java:L16-63` |
| **Severity** | ⚠️ Warning                                                                               |
| **Category** | Code Quality / Functional Alignment                                                      |

**Comment:**
The `@ApiResponses` on all four `CustomerControllerApi` methods omit the `401 Unauthorized` and `403 Forbidden Access` response declarations. The Gold Standards require at minimum these standard response codes documented on every protected endpoint.

**Reference(s):**
- Gold Standard §API Interface: "Declare `@ApiResponses` covering, at minimum: 200, 400, 401, 403, 404, and 500."

**Suggested Fix:**
Add to all methods:
```java
@ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = Response.class))}),
@ApiResponse(responseCode = "403", description = "Forbidden Access", content = {@Content(schema = @Schema(implementation = Response.class))})
```

---

### RC-21: Business service layer placed in wrong packages and uses non-standard naming

| Field        | Value                                                                                                                              |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/biz/BranchInquiryMasterApi.java`<br>`src/main/java/com/bpi/customerservice/service/biz/CustomerMasterService.java`<br>`src/main/java/com/bpi/customerservice/service/biz/InquireCustomerMasterApi.java` |
| **Severity** | ⚠️ Warning                                                                                                                         |
| **Category** | Code Quality                                                                                                                      |

**Comment:**
Business services should reside in `service.biz.<domain>` (with a domain subdirectory), not the flat `service.biz` root. The naming `BranchInquiryMasterApi`, `InquireCustomerMasterApi` does not follow the `<Capability>Service` convention. `CustomerMasterService` is a better fit but should be `CustomerValidationService`.

**Reference(s):**
- Gold Standard §Business Services: "Interface and implementation must reside in `service.biz.<domain>`. Name the interface/implementation after the specific **capability** it provides."

**Suggested Fix:**
Reorganize to `service.biz.branchinquiry`, `service.biz.customer`, etc. Rename `BranchInquiryMasterApi` → `BranchInquiryService`, `InquireCustomerMasterApi` → `InquireCustomerService`, `CustomerMasterService` → `CustomerValidationService`.

---

### RC-22: `CustomerMasterServiceImpl` passes hardcoded strings as audit context when calling `getCustomer()`

| Field        | Value                                                                                         |
|--------------|-----------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/biz/impl/CustomerMasterServiceImpl.java:L20`  |
| **Severity** | ⚠️ Warning                                                                                    |
| **Category** | Code Quality                                                                                  |

**Comment:**
`customerService.getCustomer(customerNumber, "internal-req", "system")` uses hardcoded literals for `requestUID` and `resourceOwnerID`. This also triggers an audit log write (in `CustomerServiceImpl`) with meaningless static values, polluting the audit trail and adding an unnecessary MongoDB write per validation call.

**Reference(s):**
- `src/main/java/com/bpi/customerservice/service/customer/impl/CustomerServiceImpl.java:L30-35`

**Suggested Fix:**
Either introduce a no-audit repository lookup method for internal validation, or propagate the real request context from the controller boundary through the service layers.

---

### RC-23: Account number validation passes when customer has no stored account number

| Field        | Value                                                                                          |
|--------------|-----------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/biz/impl/CustomerMasterServiceImpl.java:L21-23` |
| **Severity** | ⚠️ Warning                                                                                    |
| **Category** | Potential Bug or Risky Area                                                                   |

**Comment:**
The validation condition `if (customer.getAccountNumber() != null && !customer.getAccountNumber().equals(accountNumber))` is a permissive guard — if the customer's stored account number is `null`, the condition short-circuits and any caller-supplied account number passes unchecked. This allows a customer with no account number configured to be used for any branch inquiry.

**Reference(s):**
- `src/main/java/com/bpi/customerservice/service/biz/impl/CustomerMasterServiceImpl.java:L21-23`

**Suggested Fix:**
Treat null as invalid — require the stored account number to be non-null AND equal to the requested one:
```java
if (!accountNumber.equals(customer.getAccountNumber())) {
    throw new InvalidAccountException(CustomerServiceErrorCode.INVALID_ACCOUNT);
}
```

---

### RC-24: `InquireBranchApiImpl` response body accessed without null check

| Field        | Value                                                                                    |
|--------------|------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/rest/InquireBranchApiImpl.java:L32-34`   |
| **Severity** | ⚠️ Warning                                                                               |
| **Category** | Potential Bug or Risky Area                                                              |

**Comment:**
`post(...).getBody()` is called directly without validating the `ResponseEntity`. If the downstream server returns a 204 or an empty body, `.getBody()` returns `null`. This null propagates to `BranchInquiryMasterImpl` where it compounds the RC-4 issue (silent null return → HTTP 200 empty body).

**Reference(s):**
- `src/main/java/com/bpi/customerservice/service/biz/impl/BranchInquiryMasterImpl.java:L30-34`

**Suggested Fix:**
Validate the response body before returning it, or let `BranchInquiryMasterImpl` treat a null response as a service error (covered by RC-4).

---

### RC-25: `InquireCustomerIntegrationTest` null-response test uses vague `is4xxClientError()` assertion

| Field        | Value                                                                                             |
|--------------|---------------------------------------------------------------------------------------------------|
| **Location** | `src/test/java/com/bpi/customerservice/InquireCustomerIntegrationTest.java:L57-66`               |
| **Severity** | 💡 Suggestion                                                                                     |
| **Category** | Potential Bug or Risky Area                                                                       |

**Comment:**
`testInquireCustomer_NullResponse` asserts `status().is4xxClientError()` — this matches any status from 400 to 499. If the behavior changes from 400 to 422 to 404, the test passes regardless. Assertions should pin the exact expected status code and error body.

**Reference(s):**
- Gold Standard §Unit Testing — Assertions: "Use explicit, hardcoded values for expected outcomes rather than relying on... indirect references."

**Suggested Fix:**
```java
.andExpect(status().isBadRequest()) // or the correct intended status
.andExpect(jsonPath("$.code").value("CUSVE001"))
.andExpect(jsonPath("$.message").value("Invalid Account Number"));
```

---

### RC-26: Missing unit test coverage for critical SOAP and branch inquiry paths

| Field        | Value                                              |
|--------------|----------------------------------------------------|
| **Location** | `src/test/java/com/bpi/customerservice/`           |
| **Severity** | ⚠️ Warning                                         |
| **Category** | Potential Bug or Risky Area                        |

**Comment:**
No unit tests exist for:
- `InquireCustomerSoapImpl` — the account number propagation bug (RC-2) would be caught immediately by a unit test asserting the SOAP request input fields.
- `BranchInquiryMasterImpl` null/partial response paths (RC-4).
- `InquireCustomerMasterImpl.mapToApiResponse()` — the hardcoded-success bug (RC-3) would be caught by asserting response field values.

All integration tests use `@SpringBootTest` which loads the full context — the Gold Standards prefer focused unit tests with `@ExtendWith(MockitoExtension.class)` for service-layer behavior.

**Reference(s):**
- Gold Standard §Unit Testing: "Do not use Spring Boot's `@SpringBootTest` for unit tests."

**Suggested Fix:**
Add `@ExtendWith(MockitoExtension.class)` unit tests for the three service classes above, using fixtures per the standard.

---

### RC-27: H2 database uses default credentials — unsuitable outside development

| Field        | Value                                             |
|--------------|---------------------------------------------------|
| **Location** | `src/main/resources/application.properties:L6-7` |
| **Severity** | ⚠️ Warning                                        |
| **Category** | Security                                          |

**Comment:**
`spring.datasource.username=sa` and `spring.datasource.password=password` are the well-known H2 defaults. If this configuration is accidentally promoted to a non-development environment or if the H2 console is accessible, the database can be accessed with publicly known credentials.

**Reference(s):**
- Security Rule: use strong, non-default credentials; inject via environment variables or secrets manager.

**Suggested Fix:**
Restrict the H2 profile to a development/test Spring profile (`spring.config.activate.on-profile=dev`). Inject credentials via environment variables rather than hardcoding them.

---

### RC-28: Both downstream integrations configured with plaintext HTTP

| Field        | Value                                                                                       |
|--------------|---------------------------------------------------------------------------------------------|
| **Location** | `src/main/resources/application.properties:L26` and `L30`                                  |
| **Severity** | ⚠️ Warning                                                                                  |
| **Category** | Security                                                                                    |

**Comment:**
`bpi.api.inquire-branch.basePath=http://localhost:3000/...` and `soap.inquire-customer.endpoint-url=http://localhost:3000/...` both use `http://`. For local development this is acceptable, but the same configuration pattern applied to any real environment will send customer account data and authentication credentials in plaintext. There is no guard that prevents an `http://` production URL from being substituted.

**Reference(s):**
- Security Rule: "MUST use TLS 1.2 or higher for all network communications."

**Suggested Fix:**
Use environment-specific configuration (`application-prod.properties`) with HTTPS URLs for non-local profiles, and consider adding a startup validation that rejects `http://` URLs in non-development profiles.

---

### RC-29: `InquireCustomerMasterImpl` throws `BranchServiceException` for SOAP failure — wrong exception type

| Field        | Value                                                                                           |
|--------------|-------------------------------------------------------------------------------------------------|
| **Location** | `src/main/java/com/bpi/customerservice/service/biz/impl/InquireCustomerMasterImpl.java:L26-28` |
| **Severity** | ⚠️ Warning                                                                                      |
| **Category** | Potential Bug or Risky Area                                                                     |

**Comment:**
SOAP service failures in `InquireCustomerMasterImpl` throw `BranchServiceException`, which is a branch-inquiry-specific exception. The handler returns 503 with a branch-specific error message for a SOAP failure unrelated to the branch inquiry service. A dedicated exception type or a more general `ServiceUnavailableException` should be used.

**Reference(s):**
- `src/main/java/com/bpi/customerservice/service/biz/impl/InquireCustomerMasterImpl.java:L26-28`
- `src/main/java/com/bpi/customerservice/exception/BranchServiceException.java`

**Suggested Fix:**
Create a generic downstream service exception, or reuse `CustomerServiceErrorCode.SERVICE_ERROR` with an appropriate exception that maps to 503 for any downstream connectivity failure.

---

*End of review.*
