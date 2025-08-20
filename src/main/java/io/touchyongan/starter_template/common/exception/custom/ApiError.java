package io.touchyongan.starter_template.common.exception.custom;

/**
 * Represents a standardized API error used for business logic and validation responses.
 * <p>
 * <b>Error Code & Message Key Naming Convention:</b>
 * <ul>
 *     <li><b>Error Code</b>
 *         <ul>
 *             <li>Format: {@code <MODULE>_<CATEGORY>_<DETAIL>}</li>
 *             <li>{@code <MODULE>} — System or domain area (e.g., {@code USER}, {@code ORDER}, {@code PAYMENT}).</li>
 *             <li>{@code <CATEGORY>} — Error type (e.g., {@code VALIDATION}, {@code BUSINESS}, {@code SYSTEM}).</li>
 *             <li>{@code <DETAIL>} — Short, uppercase snake_case description of the specific error (e.g., {@code MISSING_EMAIL}, {@code INSUFFICIENT_FUNDS}).</li>
 *             <li>Example: {@code USER_VALIDATION_MISSING_EMAIL}, {@code ORDER_BUSINESS_INVALID_STATUS}.</li>
 *         </ul>
 *     </li>
 *
 *     <li><b>Message Key</b>
 *         <ul>
 *             <li>Format: {@code <module>.<category>.<detail>}</li>
 *             <li>Lowercase with dots as separators.</li>
 *             <li>Example: {@code user.validation.missing_email}, {@code order.business.invalid_status}.</li>
 *         </ul>
 *     </li>
 *
 *     <li><b>Message Text</b>
 *         <ul>
 *             <li>Write in a user-friendly, clear, and concise way.</li>
 *             <li>Use placeholders for dynamic values: {@code {field}}, {@code {minValue}}, etc.</li>
 *             <li>Example: {@code "The email address is required."}, {@code "Order status '{status}' is not allowed."}.</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <b>Localization:</b>
 * <ul>
 *     <li>Message keys map to entries in i18n properties files.</li>
 *     <li>Organize messages in folders based on type (e.g., {@code messages/error}, {@code messages/info}).</li>
 *     <li>Ensure every error code has a corresponding message key in all supported locales.</li>
 * </ul>
 */
public interface ApiError {

    String getErrorCode();

    String getMessageKey();
}
