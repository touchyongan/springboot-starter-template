## Exception Handling Guideline

This project provides a consistent and extensible way to handle exceptions 
with support for i18n (internationalization) and API error codes.
The goal is to make all errors predictable, developer-friendly, and easy to localize for UI.

---

### Convention

1. Always extend `BaseApiException` for custom exceptions.
   - Each exception type has its own class in `common.exception.custom`. 
   - Keeps exceptions organized, readable, and consistent.

2. Always define errors in an `enum` implementing `ApiError`.
   - Provides structured `errorCode` (for API clients) and `messageKey` (for `i18n` message resolution).
   - Prevents hard-coded strings in exceptions.

3. Error Code Convention
   - Format: `DOMAIN_CONTEXT_DETAIL`
        - Example: `RESOURCE_VALIDATION_NOT_FOUND`
        - `RESOURCE` → domain 
        - `VALIDATION` → context or category 
        - `NOT_FOUND` → detail of error

4. Message Key Convention
   - Format: `error.<domain>.<detail>`
     - Example: `error.resource.not_found`
   - Matches `messages_errors.properties` for localization.

### Example

#### Step 1: Create Custom Exception
```java
public class ResourceNotFoundException extends BaseApiException {

    public ResourceNotFoundException(final Object... args) {
        super(ResourceNotFoundError.NOT_FOUND, args);
    }
}
```
- Extends BaseApiException
- Accepts args for dynamic message formatting (optional)

#### Step 2: Define Error Enum
```java
public enum ResourceNotFoundError implements ApiError {
    NOT_FOUND("RESOURCE_VALIDATION_NOT_FOUND", "error.resource.not_found");

    private final ApiError apiError;

    ResourceNotFoundError(final String errorCode, final String messageKey) {
        this.apiError = new ApiErrorImpl(errorCode, messageKey);
    }

    @Override
    public String getErrorCode() {
        return apiError.getErrorCode();
    }

    @Override
    public String getMessageKey() {
        return apiError.getMessageKey();
    }
}
```
- `errorCode` → used in API response for debugging / client-side error mapping
- `messageKey` → used to resolve localized messages from `messages_errors.properties`

Example `messages_errors.properties`:
```properties
error.resource.not_found=The resource {0} with ID={1} not found.
```

#### Step 3: Handle Exception Globally

All exceptions are caught in GlobalExceptionController to keep controllers clean.
```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(final ResourceNotFoundException e) {
    final var errorResp = createCommonError(404, e);
    return ResponseEntity.ok(errorResp);
}
```
- Maps exception → API error response
- Returns standardized JSON payload

Example API Response:
```json
{
  "status": 404,
  "errorCode": "RESOURCE_VALIDATION_NOT_FOUND",
  "message": "The requested resource could not be found."
}
```

### Benefits

- Consistency → all exceptions follow same structure
- Maintainability → centralized error handling
- Localization → message keys enable multilingual support
- Client-friendly → stable error codes for API consumers

---

## DTO (Data Transfer Object) Guideline

This project enforces a standard response and request structure to improve consistency, 
readability, and client experience.

---

### API Response Wrapper

All API responses must be wrapped in the generic `ApiResponse<T>`:

```java
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(final T data) {
        this.status = 200;
        this.data = data;
    }
}
```

#### Conventions

Always use `ResponseEntity<ApiResponse<DTO>>` as the return type for controllers.
This ensures all endpoints follow a consistent structure.

- `status` → HTTP status code
- `message` → Human-readable message (can come from i18n if needed)
- `data` → The actual payload (response DTO)

#### Example Response
```json
{
  "status": 200,
  "message": "User fetched successfully",
  "data": {
    "id": "123",
    "username": "john.doe"
  }
}
```

### Naming Conventions

To make DTOs self-explanatory and consistent:

1. Response DTOs → ENTITY + Data 
   - Used when sending data back to the client.
   - Example:
```java
public class AppUserData {
    private String id;
    private String username;
}
```

2. Request DTOs → ACTION + ENTITY + Request

   - Used when receiving data from the client. 
   - Example:
```java
public class CreateAppUserRequest {
    private String username;
    private String password;
}
```

3. Action words → `Create`, `Update`, `Delete`, `Search`, etc.
   - Clearly indicate the intention of the request.

### Controller Usage Example

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppUserData>> getUser(@PathVariable String id) {
        var userData = new AppUserData(id, "john.doe");
        var response = new ApiResponse<>(userData);
        response.setStatus(200);
        response.setMessage("User fetched successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppUserData>> createUser(
            @RequestBody CreateAppUserRequest request) {

        var newUser = new AppUserData("123", request.getUsername());
        var response = new ApiResponse<>(newUser);
        response.setStatus(201);
        response.setMessage("User created successfully");
        return ResponseEntity.status(201).body(response);
    }
}
```

### Benefits

- Consistency → all endpoints return the same response structure
- Clarity → DTO naming convention makes intent obvious
- Maintainability → easy to evolve response/request models
- Client-friendly → predictable API response schema

---

## Data Validation Guideline

#### Principles

- Prefer Bean Validation annotations (`@NotBlank`, `@NotNull`, `@Email`, `@Size`, …) on request DTOs.
- When no standard annotation fits, create a custom constraint (annotation + ConstraintValidator).
- Feature-level validator components live in `validator` (e.g., `AppUserValidator`) for complex/business checks that may span multiple fields/records.
- Favor annotations on inputs so validation is declarative and enforced at the boundary.

### Package & Naming Conventions

- DTOs: `feature/.../data`
- Custom constraints: `feature/.../validator/constraint`
- Field-level: `@Existing{Entity}Id`, `@Unique{Entity}{Field}`, `@Valid{Concept}`
- Class-level: `@Valid{Action}{Entity}` (e.g., `@ValidCreateAppUser`)
- Validator class: `<AnnotationName>Validator`
- Feature validator component: `feature/.../validator/{Entity}Validator` (e.g., `AppUserValidator`) annotated with `@Component`.

### Controller Setup

- Annotate controller classes with `@Validated`.
- Use `@Valid` on `@RequestBody` and apply custom annotations to fields or parameters.
- Return `ResponseEntity<ApiResponse<DTO>>` everywhere.

### Custom Validation Guideline

#### 1. Field-Level: Optional but must follow format

Goal: A date field is optional, but if provided, it must match yyyy-MM-dd.

**Step 1: Create annotation**

```java
package io.touchyongan.starter_template.common.validation.constraint;

import io.touchyongan.starter_template.common.validation.validator.ValidDataValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDataValidator.class)
public @interface ValidDate {
    String message() default "custom.validation.date.invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // Note: The order of placeholder position in i18n will be based on asc of property name field.
    // e.g.: custom.validation.date.invalid="{0}" Invalid date format, expected "{2}".
    // Assume property name `startDate` then the message will be:
    // "startDate" Invalid date format, expected "yyyy-MM-dd".

    /** Whether null/blank values are allowed */
    boolean optional() default true;

    /** Expected date format (default yyyy-MM-dd) */
    String pattern() default "yyyy-MM-dd";
}
```

**Step 2: Implement validator**

```java
package io.touchyongan.starter_template.common.validation.validator;

import io.touchyongan.starter_template.common.util.AppUtil;
import io.touchyongan.starter_template.common.validation.constraint.ValidDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidDataValidator implements ConstraintValidator<ValidDate, String> {
    private boolean optional;
    private DateTimeFormatter formatter;

    @Override
    public void initialize(final ValidDate constraint) {
        this.optional = constraint.optional();
        this.formatter = DateTimeFormatter.ofPattern(constraint.pattern());
    }

    @Override
    public boolean isValid(final String value,
                           final ConstraintValidatorContext context) {
        if (AppUtil.isBlank(value)) {
            return optional; // allow empty if optional=true
        }
        try {
            LocalDate.parse(value, formatter);
            return true;
        } catch (final DateTimeParseException e) {
            return false;
        }
    }
}
```

**Step 3: Add i18n message**
```properties
custom.validation.date.invalid="{0}" Invalid date format, expected "{2}".
```

**Step 4: Use in DTO**

```java
public class SearchUserRequest {

    @ValidDate(optional = true, pattern = "yyyy-MM-dd")
    private String birthDate;

    // getters/setters
}
```

#### 2. Class-Level: Cross-field validation

Goal: If fieldA = "A", then fieldB must not be null/blank.

**Step 1: Create annotation**

```java
package io.touchyongan.starter_template.common.validation.constraint;

import io.touchyongan.starter_template.common.validation.validator.ConditionalRequiredValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalRequiredValidator.class)
public @interface ConditionalRequired {

    String message() default "custom.validation.conditional.required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Field that triggers condition */
    String field();

    /** Value that triggers requirement */
    String expectedValue();

    /** Field that becomes required */
    String requiredField();
}
```

**Step 2: Implement validator**

```java
package io.touchyongan.starter_template.common.validation.validator;

import io.touchyongan.starter_template.common.validation.constraint.ConditionalRequired;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Objects;

public class ConditionalRequiredValidator implements ConstraintValidator<ConditionalRequired, Object> {
    private String field;
    private String expectedValue;
    private String requiredField;

    @Override
    public void initialize(final ConditionalRequired constraint) {
        this.field = constraint.field();
        this.expectedValue = constraint.expectedValue();
        this.requiredField = constraint.requiredField();
    }

    @Override
    public boolean isValid(final Object obj,
                           final ConstraintValidatorContext context) {
        final var beanWrapper = new BeanWrapperImpl(obj);

        final var fieldValue = beanWrapper.getPropertyValue(field);
        final var requiredValue = beanWrapper.getPropertyValue(requiredField);

        if (Objects.nonNull(fieldValue) && expectedValue.equals(fieldValue.toString())) {
            final var valid = Objects.nonNull(requiredValue) && !requiredValue.toString().isBlank();
            if (!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(requiredField)
                        .addConstraintViolation();
            }
            return valid;
        }
        return true; // no condition triggered
    }
}
```

**Step 3: Add i18n message**
```properties
custom.validation.conditional.required="{2}" is required when "{0}"="{1}".
```

**Step 4: Use in DTO**
```java
@ConditionalRequired(field = "fieldA", expectedValue = "A", requiredField = "fieldB")
public class ExampleRequest {

    private String fieldA;

    private String fieldB;

    // getters/setters
}
```

### Benefits of this Pattern

- Reusable: Designed for multiple fields and features, not one-off use cases.
- Declarative: Rules live on the DTO via annotations, making them self-documenting.
- Flexible: optional flag, pattern attribute, and field mapping make validators general purpose.
- Consistent: All validations surface through the same global exception handling and i18n error messages.

> NOTE:
> For custom annotation constraint, `message`, `groups` and `payload` is the standard fields and must declare 
> exact name like example above.
---

## Permission Management Guideline

This project enforces centralized, declarative permission control to ensure every feature has consistent 
authorization checks.

---

### Base Permission Constants

All permissions are defined per entity using constants.

**Base Constant:**
```java
public class BaseConstant {

    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String READ   = "READ";
}
```

**Entity Constant Example**
```java
public class AppUserConstant extends BaseConstant {
    private AppUserConstant() {}

    public static final String ENTITY = "APP_USER";
}
```
- Naming rule: EntityName + Constant 
- ENTITY field: Uppercase, snake-case format (`APP_USER`, `CASE_FILE`, `WORKFLOW_RULE`)

### Usage in Controllers

Permissions are typically enforced at the controller layer using the custom annotation `@CustomPreAuthorize`.

**Example:**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    @CustomPreAuthorize(entity = AppUserConstant.ENTITY, action = BaseConstant.CREATE)
    public ResponseEntity<ApiResponse<AppUserData>> createUser(@RequestBody CreateAppUserRequest req) {
        // business logic
    }

    @GetMapping("/{id}")
    @CustomPreAuthorize(entity = AppUserConstant.ENTITY, action = BaseConstant.READ)
    public ResponseEntity<ApiResponse<AppUserData>> getUser(@PathVariable String id) {
        // business logic
    }
}
```

**Benefits:**
- Permission logic is explicit at the API entry point
- Easy to review what permissions are required per endpoint

### Custom Permission Logic

Authorization checks are implemented in `CustomSecurityExpressionRoot`.

Current method used:
```java
public boolean customCheckPermission() {}
```
- Called automatically via `@CustomPreAuthorize(entity, action)`

### Adding a New Feature

Whenever a new feature (entity) is introduced:

1. Create permission constant class
```java
public class WorkflowConstant extends BaseConstant {
    private WorkflowConstant() {}
    public static final String ENTITY = "WORKFLOW";
}
```

2. Add permissions to database
Run:
```shell
make migration NAME=new_feature
```
- This generates a new migration file under `db/migration`. 
- Add SQL to insert permissions into `permissions` table:
```text
INSERT INTO permissions (entity, action,...) VALUES
  ('WORKFLOW', 'CREATE',...),
  ('WORKFLOW', 'UPDATE',...),
  ('WORKFLOW', 'DELETE',...),
  ('WORKFLOW', 'READ',...);
```

4. Use in controllers
```text
@CustomPreAuthorize(entity = WorkflowConstant.ENTITY, action = BaseConstant.CREATE)
```

### Best Practices

- Always enforce permission on controller methods → This ensures a clear boundary between request and business logic.
- Reuse constants → Never hard-code entity or action strings in annotations.
- Organize by feature → Each entity should have its own `EntityConstant` file.
- Keep DB and code aligned → Always add migration for new entities before using them in code.
- Centralize complex rules → Use `CustomSecurityExpressionRoot` for advanced checks (role hierarchies, dynamic ownership rules, etc.).

---

## JPA Usage Guideline

This project standardizes how repositories, specifications, and filters are implemented to ensure consistency, 
reusability, and maintainability across all features.

---

### 1. Base Repository

We provide a base interface to ensure every repository supports CRUD and dynamic filtering.
```java
public interface BaseRepository<T> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {}
```

**Rule**
- Every repository must extend `BaseRepository`

Example:
```java
public interface AppUserRepository extends BaseRepository<AppUser> {}
```

### 2. Projection Repository (Direct DTO Mapping)

For performance and convenience, we provide `BaseProjectionRepository`, which maps query results 
directly into DTO classes without requiring a mapper.

**Usage**
```java
public interface AuditTrailRepository
        extends BaseRepository<AuditTrail>, BaseProjectionRepository<AuditTrail> {}
```

**When to use**
- When you need to return DTOs directly from the query
- Avoids overhead of mapping entity → DTO manually

### 3. Specification Pattern (Dynamic Filters)

We use the Specification pattern with **Criteria API** for dynamic query conditions.

**Base Implementation**
```java
public class GenericSpecification<T> implements Specification<T> {
    // supports dynamic criteria with SearchCriteria
}
```

**Search Criteria**
```java
public class SearchCriteria {
    private final String key;          // entity property name (⚠️ not DB column name)
    private final Object value;
    private final List<Object> values;
    private final SearchOperator operator;
    private final List<String> keys;
}
```
> ⚠️ Important: key must be the entity property name, not the table column name.

### 4. Pagination + Filter

We provide a base class to simplify pagination + specification queries using the **Template Method pattern**.

**Base Filter**
```java
public abstract class BaseFilterWithPagination<T> {
    protected PaginationRequest paginationRequest;
    protected List<SearchCriteria> searchCriteriaList = new ArrayList<>();

    public Pageable getPageable() {
        return paginationRequest.getPageable();
    }

    public Specification<T> getSpecification() {
        initialSearchCriteriaList();
        return new GenericSpecification<>(searchCriteriaList);
    }

    public abstract void initialSearchCriteriaList(); // template method
}
```

#### Naming Rule

- Filter class must be named: Entity + Filter

**Example:**
```java
public class AuditTrailFilter extends BaseFilterWithPagination<AuditTrail> {
    public static final Set<String> SUPPORTED_FIELDS = Set.of("id", "action", "entity", "createdAt", "username");

    private List<Long> resourceIds;
    private String action;
    private String entity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String username;
    private String status;

    @Override
    public void initialSearchCriteriaList() {
        searchCriteriaList.add(filterResourceIds());
        searchCriteriaList.add(filterAction());
        searchCriteriaList.add(filterEntity());
        searchCriteriaList.add(filterUsername());
        if (StringUtils.hasText(status)) {
            searchCriteriaList.add(filterStatus());
        }
        searchCriteriaList.add(filterStartDate());
        searchCriteriaList.add(filterEndDate());

        // Example: exclude audit trail of viewing audit logs
        searchCriteriaList.add(SearchCriteria.newSearchCriteria(
                "entity", SearchOperator.NOT_EQUAL, AuditTrailConstant.ENTITY));
    }

    private SearchCriteria filterResourceIds() {
        return SearchCriteria.newSearchCriteriaIn("resourceId", SearchOperator.IN, resourceIds);
    }
    private SearchCriteria filterAction() {
        return SearchCriteria.newSearchCriteria("action", SearchOperator.EQUAL, action);
    }
    private SearchCriteria filterEntity() {
        return SearchCriteria.newSearchCriteria("entity", SearchOperator.EQUAL, entity);
    }
    private SearchCriteria filterUsername() {
        return SearchCriteria.newSearchCriteria("username", SearchOperator.EQUAL, username);
    }
    private SearchCriteria filterStatus() {
        final var statusEnum = ActionStatus.from(status);
        return SearchCriteria.newSearchCriteria("status", SearchOperator.EQUAL, statusEnum);
    }
    private SearchCriteria filterStartDate() {
        return SearchCriteria.newSearchCriteria("createdAt", SearchOperator.GREATER_THAN_OR_EQUAL, startDate);
    }
    private SearchCriteria filterEndDate() {
        return SearchCriteria.newSearchCriteria("createdAt", SearchOperator.LESS_THAN_OR_EQUAL, endDate);
    }
}
```

### 5. Service Layer Example

Filters and specifications are applied in the service layer:

```java
public CustomPage<AuditTrailData> getAllAuditTrails(final AuditTrailFilter filter) {
    final var spec = filter.getSpecification();
    final var pageable = filter.getPageable();
    return auditTrailRepository.findAllWithSpecification(spec, pageable, AuditTrailData.class, AuditTrail.class);
}
```

- `findAllWithSpecification` comes from `BaseProjectionRepository` → returns DTO directly
- Keeps service clean and declarative

### Best Practices

1. Always use `EntityFilter` for pagination + filtering
   - Never embed filtering logic directly in service
2. Always declare supported fields in filter (`SUPPORTED_FIELDS`)
   - Helps validate user input and avoid SQL injection
3. Keep criteria field names aligned with entity property names
   - Not DB column names
4. Always call initialSearchCriteriaList inside getSpecification()
   - Subclasses only implement filtering logic
5. Use enums for status-like fields
   - Validate in filter (throw B`aseApiException` if invalid)

---

## Advanced JPA Projection Guideline

This section explains how to handle DTO projection with relationships and custom joins using our `BaseProjectionRepository`.

### 1. Ignore Non-Entity DTO Fields

Sometimes a DTO may contain additional fields that do not exist in the entity.
Use `@IgnoreFieldSelection` to exclude them from projection mapping.

```java
@Getter
@Setter
public class StudentDTO {
    private Long id;
    private String name;

    private List<CourseDTO> courses; // populated via JoinConfig

    private AddressDTO address; // populated via JoinConfig

    @IgnoreFieldSelection
    private String extraComputedField; // ignored from projection
}
```

### 2. Projection with Relationships

For DTOs with nested relationships (e.g., `Student` → `Courses`, `Address`), use the method:
```java
public <DT> CustomPage<DT> findAllWithSpecificationWithRelationshipProjection(
        final Specification<ET> specification,
        final Pageable pageable,
        final List<JoinConfig> joinConfigs,
        final Class<ET> clsEntity,
        final Class<DT> clsDTO) {}
```

### 3. JoinConfig

`JoinConfig` defines how entity relations are joined and mapped into DTO fields.
```java
public class JoinConfig {
    private final String joinPropertyName;  // property path for join (entity side)
    private final List<JoinType> joinType;  // join types (e.g., LEFT, INNER)
    private final String entityPropertyName; // property name in entity relation
    private final String dtoPropertyName;    // corresponding DTO field
}
```
**Example:**
```text
joinConfigs.add(
    new JoinConfig(
        "studentCourses.course",                   // path: student → studentCourses → course
        List.of(JoinType.LEFT, JoinType.LEFT),     // LEFT join twice
        "course",                                  // property in relation
        "courses"                                  // target field in DTO
    )
);

joinConfigs.add(
    new JoinConfig(
        "address",                                 // direct relation: student → address
        List.of(JoinType.LEFT),                    
        "address",                                 
        "address"
    )
);
```
**👉 This means:**
- Student LEFT JOIN StudentCourse
- StudentCourse LEFT JOIN Course
- Then map Course into DTO field courses.
- Additionally, join Student → Address and map into DTO field address.

### 4. Example: Student with Courses + Address
   
#### Entities

- Student → StudentCourse → Course (many-to-many via join entity)
- Student → Address (one-to-one)

### DTOs
```java
@Getter
@Setter
public class StudentData {
    private Long id;
    private String name;
    private List<CourseData> courses;  // mapped from join
    private AddressData address;       // mapped from join
}
```

#### Controller Example

```java
import java.util.Collections;

@RestController
@AllArgsConstructor
public class StudentController {
    private final BasedProjectionRepositoryImpl<Student> basedProjectionRepository;

    @GetMapping("/students/{id}")
    public Page<StudentDTO> getStudent(@PathVariable("id") final Long id) {
        final var searches = new ArrayList<SearchCriteria>();
        searches.add(new SearchCriteria("id", SearchOperator.EQUAL, id));

        final var spec = new GenericSpecification<Student>(searches);
        final var pageable = PageRequest.newPaginationRequest(0, 20, Collections.emptyList(), Collections.emptyList());

        final var joinConfigs = new ArrayList<JoinConfig>();
        joinConfigs.add(new JoinConfig("studentCourses.course",
                List.of(JoinType.LEFT, JoinType.LEFT), "course", "courses"));
        joinConfigs.add(new JoinConfig("address",
                List.of(JoinType.LEFT), "address", "address"));

        return basedProjectionRepository.findAllWithSpecificationWithRelationshipProjection(
                spec, pageable, joinConfigs, Student.class, StudentData.class);
    }
}
```

### Best Practices

- Always prefer DTOs over exposing entities.
- Use `@IgnoreFieldSelection` for computed / transient DTO fields.
- Keep `joinPropertyName` aligned with entity property paths, not DB column names.
- Use multiple `JoinTypes` if the relation chain spans multiple associations.
  - e.g. `List.of(JoinType.LEFT, JoinType.LEFT)` means two levels deep.
- Keep `entityPropertyName` and `dtoPropertyName` consistent with field names.
- Add all joins into joinConfigs to avoid `LazyInitializationException`.

---

## Audit Logging Guideline

This project implements automatic audit logging into the database using AOP (`AuditLogAspect`).
The audit system helps track who did what, when, and what changed.

### 1. Default Behavior

- Audit logs are recorded automatically for all HTTP methods:
  - POST (Create)
  - PUT (Update)
  - DELETE (Delete)
- The AuditLogAspect captures request metadata and changes.

👉 If you need to **disable audit logging globally**, simply comment out `@Component` on `AuditLogAspect`.

#### 2. Ignoring Audit Logs

#### `@IgnoreSaveAuditTrail`

Use this annotation on controller/service methods when:

- The action is not useful for auditing.
- Logging would waste resources (e.g., health check endpoints).
```java
@PostMapping("/internal/cache/refresh")
@IgnoreSaveAuditTrail
public void refreshCache() {
    cacheService.refresh();
}
```

### 3. Logging Anonymous Actions

#### `@LogActionAnonymous`

For public APIs that don’t require authentication, but you still want audit consistency:
```java
@GetMapping("/public/download/{id}")
@LogActionAnonymous(action = "DOWNLOAD_FILE", entity = "FileResource")
public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
    return fileService.download(id);
}
```

### 4. Tracking Field Changes

We provide `AuditUtil` to compare DTO vs Entity and log differences.

#### Methods

- `auditUpdateAndSetLogChangeContext(entity, dto)`
  - Use when you just need to detect & store changes automatically.
- `auditAndUpdate(entity, dto)`
  - Use when you need to intercept the change set for custom logic.
  - After processing, call setLogChangeContext manually.

### 5. Field-Level Audit with `@AuditChange`

Use `@AuditChange` on DTO fields to control how changes are detected and mapped to the entity.
```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditChange {
    String entityField() default "";           // map DTO → entity property
    boolean ignoreUpdateNullValue() default false; // skip nulls
    boolean ignoreField() default false;       // exclude field from audit
}
```

**When to use configs:**

- `ignoreField = true` → Field should not be updated or audited.
- `ignoreUpdateNullValue = true` → Ignore null/empty values from DTO.
- `entityField` → Use when DTO field name differs from entity field.

### 6. Example Usage

#### Example: Entity
```java
@Entity
@Table(name = "users")
@Getter @Setter
public class User extends CustomAbstractPersistable {
    private String username;
    private String email;
    private String status;
}
```

#### Example: DTO with AuditChange
```java
@Getter @Setter
public class UserUpdateDTO {
    @AuditChange // same field name, default mapping
    private String username;

    @AuditChange(entityField = "email") // DTO → entity mapping
    private String emailAddress;

    @AuditChange(ignoreUpdateNullValue = true) // skip null values
    private String status;

    @AuditChange(ignoreField = true) // won't update entity or log
    private String uiThemePreference;
}
```

#### Example: Service Method
```java
@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User updateUser(Long id, UserUpdateDTO dto) {
        User user = userRepository.findByIdThrowExceptionIfNotFound(id, User.class);

        // Automatically compare & log changes
        AuditUtil.auditUpdateAndSetLogChangeContext(user, dto);

        // Save updated entity (fields updated by audit util, except complex relationships)
        return userRepository.save(user);
    }
}
```
👉 With this approach:
- Audit log automatically records old/new values.
- Developer doesn’t need to manually set fields (unless it’s complex relationships like one-to-many).

### Best Practices

- Use `ignoreUpdateNullValue = true` when null means “not provided”.
- Use `ignoreField = true` for frontend-only fields.
- Use `auditAndUpdate()` when you need to add extra logic before setting the audit context.
- Keep entity vs DTO mapping consistent with `entityField`.