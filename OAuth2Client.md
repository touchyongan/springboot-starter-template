## Guide to Configuring an OAuth 2.0 Client in Spring Boot

To integrate OAuth 2.0 authentication with providers like Google and GitHub 
in your Spring Boot application, follow this two-part configuration guide. 
First, you'll configure your application properties, and second, you'll configure the OAuth provider.

---

### 1. Spring Boot Configuration

You've already implemented the necessary code, so the key is to ensure your `application.properties` 
or `application.yml` file is set up correctly. This involves providing the client ID and secret 
for each provider you want to use. Spring Boot’s `spring-boot-starter-oauth2-client` dependency automatically 
detects these properties.

`application.properties` Template  
This is a standard template that you can copy and paste into your configuration file. 
Remember to replace the placeholder values with your actual client IDs and secrets.

```properties
# Spring Security OAuth 2.0 Client Configuration
#
# Use this template to configure your application to work with various OAuth 2.0 providers.
# The `client-id` and `client-secret` values must be obtained from each provider's
# developer console.
#
# Note: For production environments, it is highly recommended to use environment variables
# or a secrets management service to store these values securely.
#
# Example:
#
# GOOGLE_CLIENT_ID=your-google-client-id
# GOOGLE_CLIENT_SECRET=your-google-client-secret
# GITHUB_CLIENT_ID=your-github-client-id
# GITHUB_CLIENT_SECRET=your-github-client-secret
#
# To use these in your application, ensure you have set them as environment variables
# and then reference them below using the following syntax.

# Google OAuth 2.0 Client
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID:your-google-client-id}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:your-google-client-secret}
spring.security.oauth2.client.registration.google.scope=openid,profile,email

# GitHub OAuth 2.0 Client
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID:your-github-client-id}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET:your-github-client-secret}
spring.security.oauth2.client.registration.github.scope=user:email,read:user

# Facebook OAuth 2.0 Client
spring.security.oauth2.client.registration.facebook.client-id=${FACEBOOK_CLIENT_ID:your-facebook-client-id}
spring.security.oauth2.client.registration.facebook.client-secret=${FACEBOOK_CLIENT_SECRET:your-facebook-client-secret}
spring.security.oauth2.client.registration.facebook.scope=email,public_profile

# Additional Customizations (Optional but Recommended)
# If your application requires a custom callback URL, specify it here.
# Note: This URL must be registered in the provider's developer console.
#
# Example:
# spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/google
#
# If not specified, Spring will use the default redirect URI pattern:
# /login/oauth2/code/{registrationId}
```

---

### 2. OAuth Provider Configuration

This is the most crucial step. You must register your application with each OAuth provider you want to use.

#### Google

1. Go to the Google Cloud Console. 
2. Navigate to APIs & Services > Credentials. 
3. Click Create Credentials > OAuth client ID. 
4. Select Web application. 
5. Give it a name (e.g., "My Spring App"). Under Authorized redirect URIs, 
add the following URL: http://127.0.0.1:8080/login/oauth2/code/google. For production, use your domain name.
6. Click Create. Google will provide you with a Client ID and Client Secret. 
Use these values in your application.properties.

#### GitHub

1. Go to your GitHub Settings > Developer settings > OAuth Apps.
2. Click New OAuth App. 
3. Fill in the Application name and Homepage URL (e.g., http://127.0.0.1:8080).
4. In the Authorization callback URL field, add: http://127.0.0.1:8080/login/oauth2/code/github.
5. Click Register application. You will get a Client ID. Now, click Generate a new client secret to get your Client Secret.

#### Facebook

1. Go to Facebook for Developers > My Apps. 
2. Create a new app and set up Facebook Login. 
3. Navigate to Facebook Login > Settings. 
4. Add your Valid OAuth Redirect URIs. For local development, this will be: http://127.0.0.1:8080/login/oauth2/code/facebook.

---

### The OAuth 2.0 Login Flow

The flow you've designed is effective and common for single-page applications (SPAs) or mobile apps. 
Here is a breakdown of how it works:

1. **UI Gets OAuth Clients**: The UI makes a GET request to your `/auth/oauth2-clients` endpoint. 
The server responds with a list of providers (e.g., Google, GitHub) and their corresponding **authorization URLs**.
2. **User Clicks Login Button**: The user clicks a button for a provider (e.g., "Login with Google"). 
The UI redirects the user's browser to the authorization URL provided by your server.  
This takes the user to the provider's login page to grant your application access.
3. **Authentication and Code Exchange:**
   - The user logs in and consents on the provider's site. 
   - The provider then redirects the user's browser back to the redirect URI you configured (e.g., `/login/oauth2/code/google`). 
   - Spring Security's `OAuth2LoginAuthenticationFilter` intercepts this request, 
exchanges the authorization code for an access token, and retrieves the user's information.
   - Spring Security then invokes your custom `OAuth2ClientAuthenticationSuccessHandler` (`handleOAuth2UserOnLoginSuccess`).
4. Custom Logic and Temp Token Generation:
   - Your `handleOAuth2UserOnLoginSuccess` method extracts user data, saves it to the database if the user is new, 
and generates a short-lived temporary JWT token.
   - Crucially, this method performs a server-side redirect to the UI's callback endpoint, attaching the temporary token as a query parameter. 
Example: `http://localhost:8081/auth/oauth2/callback?token=....`
5. **UI Receives Temp Token:**
   - The browser is redirected to the UI's callback page. The front-end code on this page accesses 
the URL to extract the temporary `token` from the query parameter.
   - The UI then immediately makes an API call to your `/oauth2/exchange` endpoint, passing this temporary token.
6. **Final Token Exchange:**
   - Your custom `OAuth2TempTokenFilter` intercepts the `/oauth2/exchange` request.
   - It validates the temporary token and, if successful, generates the final, long-lived access token and refresh token.
   - The filter writes these tokens directly to the response body, which the UI receives and saves.
   - The UI can now use the new access token for all future API calls, completing the authentication process.