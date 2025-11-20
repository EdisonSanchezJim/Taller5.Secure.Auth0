# Spring Boot Security with Auth0 - Tutorial

A comprehensive tutorial on implementing OAuth2 authentication and authorization in a Spring Boot application using Auth0 as the identity provider.

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Learning Objectives](#learning-objectives)
4. [Architecture](#architecture)
5. [Setup Instructions](#setup-instructions)
6. [Step-by-Step Tutorial](#step-by-step-tutorial)
7. [Understanding the Code](#understanding-the-code)
8. [Testing the Application](#testing-the-application)
9. [Security Concepts Explained](#security-concepts-explained)
10. [Troubleshooting](#troubleshooting)
11. [Further Improvements](#further-improvements)

---

## Overview

This tutorial demonstrates how to secure a Spring Boot web application using **OAuth 2.0** and **OpenID Connect (OIDC)** with **Auth0** as the authentication provider. Students will learn how to implement user login, logout, and profile display in a modern enterprise application.

### What You'll Build

- A Spring Boot web application with OAuth2 authentication
- Integration with Auth0 for identity management
- Secure endpoints with role-based access
- User profile display using OIDC claims
- Proper logout handling with Auth0

---

## Prerequisites

### Required Knowledge
- Basic Java programming (Java 17+)
- Understanding of Spring Boot framework
- Basic concepts of web development (HTTP, cookies, sessions)
- Basic understanding of Maven

### Software Requirements
- **JDK 17 or higher** - [Download here](https://adoptium.net/)
- **Maven 3.6+** - [Download here](https://maven.apache.org/download.cgi)
- **IDE** - IntelliJ IDEA, Eclipse, or VS Code
- **Auth0 Account** - [Sign up free](https://auth0.com/signup)
- **Web Browser** - Chrome, Firefox, or Safari

---

## Learning Objectives

By the end of this tutorial, students will be able to:

1. Understand OAuth 2.0 and OpenID Connect protocols
2. Configure Auth0 as an identity provider
3. Integrate Spring Security with OAuth2
4. Implement secure login and logout flows
5. Access and display user profile information
6. Secure application endpoints
7. Handle authentication states in web applications

---

## Architecture

### Authentication Flow

```
┌─────────┐                                    ┌─────────┐
│         │  1. Access Protected Resource      │         │
│  User   │──────────────────────────────────> │  Spring │
│ Browser │                                    │   App   │
└─────────┘                                    └─────────┘
     │                                              │
     │  2. Redirect to Auth0 Login                  │
     │<─────────────────────────────────────────────┘
     │
     │  3. User enters credentials
     ↓
┌─────────┐
│ Auth0   │
│ Login   │
│  Page   │
└─────────┘
     │
     │  4. Auth0 validates credentials
     │
     │  5. Redirect back with authorization code
     ↓
┌─────────┐
│  Spring │
│   App   │  6. Exchange code for tokens
└─────────┘──────────────────────────────────> ┌─────────┐
     │                                         │ Auth0   │
     │  7. Return ID token & Access token      │ Server  │
     │<────────────────────────────────────────└─────────┘
     │
     │  8. Display protected content
     ↓
┌─────────┐
│  User   │
│ Browser │
└─────────┘
```

### Components

- **Spring Boot Application**: Your web application with protected resources
- **Spring Security**: Framework handling security configurations
- **Auth0**: Identity provider managing user authentication
- **OAuth2 Client**: Spring component handling OAuth2 protocol
- **Thymeleaf**: Template engine for rendering HTML views

---

## Setup Instructions

### Step 1: Configure Auth0

#### 1.1 Create an Auth0 Account
1. Go to [Auth0 Signup](https://auth0.com/signup)
2. Create a free account
3. Choose your region (e.g., US, EU)

#### 1.2 Create an Application
1. In Auth0 Dashboard, go to **Applications** → **Applications**
2. Click **Create Application**
3. Name: `Spring Boot Tutorial App`
4. Choose: **Regular Web Applications**
5. Click **Create**

#### 1.3 Configure Application Settings
1. Go to **Settings** tab
2. Note down:
   - **Domain** (e.g., `your-tenant.auth0.com`)
   - **Client ID** (e.g., `IcdoRRTpe...`)
   - **Client Secret** (e.g., `kPok5MzW...`)

3. Configure **Allowed Callback URLs**:
   ```
   http://localhost:3000/login/oauth2/code/okta
   ```

4. Configure **Allowed Logout URLs**:
   ```
   http://localhost:3000
   ```

5. Configure **Allowed Web Origins**:
   ```
   http://localhost:3000
   ```

6. Click **Save Changes**

#### 1.4 Create a Test User (Optional)
1. Go to **User Management** → **Users**
2. Click **Create User**
3. Enter email and password
4. Click **Create**

### Step 2: Clone and Configure the Project

#### 2.1 Project Structure
```
securetutorialauth0/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── co/edu/escuelaing/securetutorialauth0/
│       │       ├── Securetutorialauth0.java      # Main application
│       │       ├── SecurityConfig.java           # Security configuration
│       │       └── HomeController.java           # Home page controller
│       └── resources/
│           ├── application.yml                   # Configuration file
│           └── templates/
│               └── index.html                    # Home page template
└── README.md
```

#### 2.2 Update Configuration

Edit `src/main/resources/application.yml`:

```yaml
okta:
  oauth2:
    issuer: https://YOUR-DOMAIN.auth0.com/
    client-id: YOUR-CLIENT-ID
    client-secret: YOUR-CLIENT-SECRET

server:
  port: 3000
```

**IMPORTANT**: Replace the placeholders with your actual Auth0 credentials:
- `YOUR-DOMAIN`: Your Auth0 domain (without `https://`)
- `YOUR-CLIENT-ID`: Client ID from Auth0 application
- `YOUR-CLIENT-SECRET`: Client Secret from Auth0 application

---

## Step-by-Step Tutorial

### Step 1: Understanding Dependencies

Open `pom.xml` and examine the key dependencies:

```xml
<!-- Okta Spring Boot Starter: Simplifies Auth0/Okta integration -->
<dependency>
    <groupId>com.okta.spring</groupId>
    <artifactId>okta-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>

<!-- Spring Web: For web application functionality -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- OAuth2 Client: Handles OAuth2 authentication flow -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- Thymeleaf: Template engine for HTML rendering -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- Thymeleaf Security Extras: Security integration in templates -->
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

### Step 2: The Main Application Class

**File**: `Securetutorialauth0.java`

```java
@SpringBootApplication  // Marks this as a Spring Boot application
public class Securetutorialauth0 {
    public static void main(String[] args) throws Throwable {
        // Launches the Spring Boot application
        SpringApplication.run(Securetutorialauth0.class, args);
    }
}
```

**Purpose**: Entry point that bootstraps the Spring Boot application with all auto-configurations.

### Step 3: Security Configuration

**File**: `SecurityConfig.java`

This is the core of your security setup. Let's break it down:

```java
@Configuration          // Marks this as a Spring configuration class
@EnableWebSecurity     // Enables Spring Security web security features
public class SecurityConfig {

    // Inject Auth0 configuration from application.yml
    @Value("${okta.oauth2.issuer}")
    private String issuer;
    
    @Value("${okta.oauth2.client-id}")
    private String clientId;
```

#### 3.1 Security Filter Chain

```java
@Bean
public SecurityFilterChain configure(HttpSecurity http) throws Exception {
    http
        // Configure authorization rules
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/", "/images/**").permitAll()  // Public endpoints
            .anyRequest().authenticated()                    // All others require auth
        )
        
        // Enable OAuth2 login with default settings
        .oauth2Login(withDefaults())

        // Configure custom logout handler
        .logout(logout -> logout
            .addLogoutHandler(logoutHandler()));
            
    return http.build();
}
```

**Key Concepts**:
- `requestMatchers()`: Defines URL patterns
- `permitAll()`: Allows access without authentication
- `authenticated()`: Requires user to be logged in
- `oauth2Login()`: Enables OAuth2/OIDC authentication

#### 3.2 Custom Logout Handler

```java
private LogoutHandler logoutHandler() {
    return (request, response, authentication) -> {
        try {
            // Get the base URL of the application
            String baseUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();
            
            // Redirect to Auth0 logout endpoint
            // This ensures the user is logged out from Auth0 as well
            response.sendRedirect(
                issuer + "v2/logout?client_id=" + clientId + 
                "&returnTo=" + baseUrl
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };
}
```

**Why is this important?**
- Logging out from Spring app ≠ Logging out from Auth0
- This handler performs **federated logout** (logs out from both)
- Redirects back to your app after Auth0 logout completes

### Step 4: Home Controller

**File**: `HomeController.java`

```java
@Controller  // Marks this as a Spring MVC controller
public class HomeController {

    @GetMapping("/")  // Maps to the root URL
    public String home(Model model, 
                       @AuthenticationPrincipal OidcUser principal) {
        // If user is authenticated, add profile to model
        if (principal != null) {
            model.addAttribute("profile", principal.getClaims());
        }
        return "index";  // Returns the view name (index.html)
    }
}
```

**Key Points**:
- `@AuthenticationPrincipal`: Injects the authenticated user
- `OidcUser`: Contains user information from Auth0
- `getClaims()`: Returns user profile data (email, name, picture, etc.)
- `Model`: Passes data to the view template

### Step 5: The View Template

**File**: `index.html`

```html
<!DOCTYPE html>
<html lang="en" 
      xmlns:th="http://www.thymeleaf.org" 
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity5">
<body>
    <!-- Login button (shown when NOT authenticated) -->
    <a href="http://localhost:3000/oauth2/authorization/okta">Login</a>
    
    <!-- Content shown when authenticated -->
    <div sec:authorize="isAuthenticated()">
        <p>You are logged in!</p>
        
        <!-- Logout form -->
        <form name="logoutForm" th:action="@{/logout}" method="post">
            <button type="submit" value="Log out"/>
        </form>
    </div>
    
    <!-- User profile display -->
    <div sec:authorize="isAuthenticated()">
        <Frame>
            <img th:src="${profile.get('picture')}" 
                 th:attr="alt=${profile.get('name')}"/>
        </Frame>
        <h2 th:text="${profile.get('name')}"></h2>
        <p th:text="${profile.get('email')}"></p>
        <a th:href="@{/logout}">Log Out</a>
    </div>
</body>
</html>
```

**Thymeleaf Features**:
- `sec:authorize="isAuthenticated()"`: Shows content only if user is logged in
- `th:src="${profile.get('picture')}"`: Dynamically sets image source
- `th:text="${profile.get('name')}"`: Displays user name
- `th:action="@{/logout}"`: Creates logout URL

---

## Understanding the Code

This section explains how each part of the application works together.

### 1. Dependency Configuration (`pom.xml`)
Key starters provide:
- `okta-spring-boot-starter`: Auto-configures OAuth2/OIDC client, mapping Auth0 (treated as Okta-compatible) settings from `application.yml`.
- `spring-boot-starter-oauth2-client`: Handles authorization code flow, token exchange, session management.
- `spring-boot-starter-web`: MVC + embedded Tomcat for HTTP endpoints.
- `spring-boot-starter-thymeleaf`: Server-side rendering of HTML templates.
- `thymeleaf-extras-springsecurity6`: Security tags (e.g. `sec:authorize`).

### 2. Configuration (`application.yml`)
Auth0 tenant values (`issuer`, `client-id`, `client-secret`) drive OAuth discovery (well-known endpoints). The `issuer` must end with a trailing slash. Port 3000 sets the base URL used in redirects and logout.

### 3. Application Entry Point (`Securetutorialauth0.java`)
Bootstraps Spring context. Component scanning finds the controller and security config. No custom logic—kept minimal for clarity.

### 4. Security Pipeline (`SecurityConfig`)
- `authorizeHttpRequests`: Public root path `/`; all other routes require authentication.
- `oauth2Login(withDefaults())`: Enables authorization code flow; Spring builds the redirect URL and handles token exchange.
- Custom logout handler: Performs federated logout by redirecting to Auth0's `/v2/logout` endpoint including `client_id` and `returnTo` (post-logout redirect).
- Session: After successful login, an `OidcUser` is stored in the security context.

### 5. Controller (`HomeController`)
Single mapping for `/`. If authenticated, exposes OIDC claims (`name`, `email`, `picture`) to the view via `model.addAttribute("profile", principal.getClaims())`.

### 6. View Template (`index.html`)
Uses Thymeleaf + Spring Security dialect:
- `sec:authorize="isAuthenticated()"` conditionally shows protected blocks.
- Renders profile attributes directly from the `profile` map. Avoids client-side JavaScript for simplicity (server-side rendered secure content).
- Logout uses POST form (recommended) though an anchor to `/logout` also works because Spring generates a CSRF token unless disabled.

### 7. Authentication Lifecycle
1. Unauthenticated user requests protected resource → redirected to Auth0.
2. User authenticates at Auth0 → redirected back with authorization code.
3. Spring exchanges code for ID & access tokens via OAuth2 client.
4. `OidcUser` constructed from ID token; placed in security context.
5. Subsequent requests reuse session; controller reads `OidcUser`.
6. Logout triggers session invalidation locally + remote Auth0 logout.

### 8. OIDC Claims Usage
Only minimal claims are used (name, email, picture). Additional roles/permissions could be added via custom claims in Auth0 and inspected from `principal.getClaims()`.

### 9. Extensibility Points
- Add API endpoints: annotate with `@RestController` and secure with scopes.
- Role-based access: map Auth0 roles into authorities via a custom `OidcUserService`.
- Token propagation: access token available in `OAuth2AuthorizedClient` for downstream API calls.

### 10. Security Considerations
- Secrets: Move `client-secret` to environment variables for production (`SPRING_APPLICATION_JSON`, or a secrets manager).
- HTTPS: Use TLS in real deployments; Auth0 callbacks must use HTTPS except for localhost.
- CSRF: Default enabled for form logout; if adding state-changing REST endpoints for browser clients, keep CSRF protection.

## Testing the Application

### Build and Run

```bash
# Navigate to project directory
cd securetutorialauth0

# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

Expected output:
```
...
Tomcat started on port(s): 3000 (http)
Started Securetutorialauth0 in X.XXX seconds
```

### Test Steps

#### Test 1: Access Home Page (Unauthenticated)
1. Open browser: `http://localhost:3000`
2. **Expected**: You see the login button
3. **Reason**: Home page (`/`) is public

#### Test 2: Login Flow
1. Click **Login** button
2. **Expected**: Redirected to Auth0 login page
3. Enter your Auth0 credentials (or test user)
4. **Expected**: Redirected back to `http://localhost:3000`
5. **Expected**: See "You are logged in!" and your profile

#### Test 3: View Profile Information
1. After login, observe:
    - Profile picture displayed
    - Full name displayed
    - Email address displayed
2. **Reason**: OIDC claims from Auth0 ID token

#### Test 4: Logout
1. Click **Log Out**
2. **Expected**: Redirected to Auth0 logout
3. **Expected**: Redirected back to home page (unauthenticated state)
4. Try accessing the page again - should require re-login

#### Test 5: Session Persistence
1. Login to the application
2. Close the browser tab (not the browser)
3. Open new tab: `http://localhost:3000`
4. **Expected**: Still logged in (session cookie maintained)

---

## Security Concepts Explained

### OAuth 2.0 vs OpenID Connect

**OAuth 2.0**:
- Authorization framework
- Answers: "What can the user do?"
- Provides access tokens

**OpenID Connect (OIDC)**:
- Authentication layer on top of OAuth 2.0
- Answers: "Who is the user?"
- Provides ID tokens with user claims

**This application uses both**: OAuth2 for authorization + OIDC for authentication.

### Tokens Explained

#### ID Token (OIDC)
```json
{
  "sub": "auth0|123456789",
  "name": "John Doe",
  "email": "john@example.com",
  "picture": "https://...",
  "iss": "https://your-tenant.auth0.com/",
  "aud": "your-client-id",
  "exp": 1699999999
}
```
- Contains user identity information
- Signed by Auth0 (JWT format)
- Verified by Spring Security

#### Access Token (OAuth2)
- Used to access protected resources/APIs
- Short-lived (typically 1 hour)
- Can be sent to backend APIs

#### Refresh Token
- Used to obtain new access tokens
- Long-lived (days/weeks)
- Not used in this basic tutorial

### Security Filter Chain

Spring Security uses a chain of filters to process requests:

```
Request → SecurityContextPersistenceFilter
       → LogoutFilter
       → OAuth2LoginAuthenticationFilter
       → AuthorizationFilter
       → Your Controller
```

Each filter performs a specific security task.

### Session Management

- **Server-side session**: Spring stores user info in server memory
- **Session cookie**: Browser stores session ID
- **Session timeout**: Default 30 minutes (configurable)

---

## Troubleshooting

### Problem 1: "Invalid redirect_uri"

**Error Message**: 
```
error=invalid_request&error_description=The redirect_uri MUST match the registered callback URL for this application.
```

**Solution**:
1. Check Auth0 Application Settings
2. Ensure **Allowed Callback URLs** contains:
   ```
   http://localhost:3000/login/oauth2/code/okta
   ```
3. Note: URL is case-sensitive and must match exactly

### Problem 2: Application won't start

**Error**: `Port 3000 already in use`

**Solution**:
```bash
# Option 1: Kill process using port 3000
lsof -ti:3000 | xargs kill -9

# Option 2: Change port in application.yml
server:
  port: 8080
```

### Problem 3: 401 Unauthorized

**Symptoms**: Constant redirects to login page

**Solutions**:
1. Clear browser cookies for `localhost`
2. Check `application.yml` credentials are correct
3. Verify Auth0 application is enabled
4. Check Auth0 tenant domain (should end with `.auth0.com`)

### Problem 4: Profile not displaying

**Symptoms**: Logged in but no profile information

**Solutions**:
1. Check browser console for JavaScript errors
2. Verify `HomeController` is passing profile to model
3. Ensure user has profile information in Auth0
4. Check Thymeleaf syntax in `index.html`

### Problem 5: Logout not working

**Symptoms**: Clicking logout doesn't end session

**Solutions**:
1. Verify **Allowed Logout URLs** in Auth0 includes `http://localhost:3000`
2. Check `SecurityConfig.logoutHandler()` code
3. Ensure Auth0 issuer URL is correct (must end with `/`)
4. Clear browser cache and cookies

---

## Further Improvements

### Level 1: Beginner Enhancements

1. **Improve the UI**
   - Add Bootstrap or Tailwind CSS
   - Create a proper navigation bar
   - Style the login/logout buttons

2. **Add More User Information**
   - Display additional OIDC claims (birthdate, phone, etc.)
   - Show authentication time
   - Display access token expiration

3. **Error Handling**
   - Create custom error pages (401, 403, 404)
   - Add user-friendly error messages
   - Log authentication failures

### Level 2: Intermediate Enhancements

1. **Role-Based Access Control**
   ```java
   .authorizeHttpRequests(authorize -> authorize
       .requestMatchers("/admin/**").hasRole("ADMIN")
       .requestMatchers("/user/**").hasRole("USER")
       .anyRequest().authenticated()
   )
   ```

2. **API Integration**
   - Create REST API endpoints
   - Secure APIs with OAuth2 resource server
   - Use access tokens for API calls

3. **Custom Login Page**
   - Design your own login form
   - Implement Auth0 Universal Login customization
   - Add social login buttons (Google, GitHub, etc.)

### Level 3: Advanced Enhancements

1. **Multi-Factor Authentication**
   - Enable MFA in Auth0
   - Configure authentication policies
   - Implement step-up authentication

2. **Token Management**
   - Implement token refresh logic
   - Store tokens securely
   - Handle token expiration gracefully

3. **Microservices Security**
   - Implement API gateway with OAuth2
   - Service-to-service authentication
   - Token propagation across services

4. **Monitoring and Auditing**
   - Log all authentication events
   - Monitor failed login attempts
   - Implement security event notifications

---

## Additional Resources

### Documentation
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Auth0 Documentation](https://auth0.com/docs)
- [OAuth 2.0 RFC](https://tools.ietf.org/html/rfc6749)
- [OpenID Connect Specification](https://openid.net/specs/openid-connect-core-1_0.html)

### Tutorials
- [Spring Security OAuth2 Guide](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Auth0 Java Quickstart](https://auth0.com/docs/quickstart/webapp/java-spring-boot)
- [OAuth 2.0 Simplified](https://www.oauth.com/)

### Videos
- [OAuth 2.0 Explained](https://www.youtube.com/watch?v=996OiexHze0)
- [Spring Security Tutorial](https://www.youtube.com/watch?v=X79OihQ5p7A)

---

## Contributing

Students are encouraged to:
- Report issues or bugs
- Suggest improvements
- Submit pull requests with enhancements
- Share alternative implementations

---

## License

This tutorial is provided for educational purposes.

---

## Assessment Criteria

Students will be evaluated on:

1. **Understanding** (30%)
   - Can explain OAuth2/OIDC flow
   - Understands security configuration
   - Can identify security components

2. **Implementation** (40%)
   - Successfully runs the application
   - Implements additional features
   - Follows security best practices

3. **Problem-Solving** (30%)
   - Troubleshoots issues independently
   - Implements improvements
   - Documents changes clearly

---

## Checklist for Students

- [ ] Auth0 account created
- [ ] Auth0 application configured
- [ ] Application credentials updated in `application.yml`
- [ ] Application builds successfully (`mvn clean compile`)
- [ ] Application runs without errors
- [ ] Can login using Auth0
- [ ] Profile information displays correctly
- [ ] Logout works properly
- [ ] Understands OAuth2/OIDC flow
- [ ] Can explain security configuration
- [ ] Implemented at least one enhancement

---

**Happy Learning!**
