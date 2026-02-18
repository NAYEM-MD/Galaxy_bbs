# Galaxy BBS — Project Overview & Presentation Guide

> **Galaxy BBS** is a Reddit-style bulletin board system built with Java servlets, JSP, and Oracle Database. It supports posts, comments, votes, polls, media uploads, user profiles, and notifications.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Overall Project Flow](#2-overall-project-flow)
3. [Technologies, Tools & Libraries](#3-technologies-tools--libraries)
4. [Where & How Each Technology Is Used](#4-where--how-each-technology-is-used)
5. [Architecture & Design Patterns](#5-architecture--design-patterns)
6. [Directory Structure](#6-directory-structure)
7. [Quick Setup](#7-quick-setup)

---

## 1. Project Overview

**Galaxy BBS** is a web-based forum/bulletin board system where users can:

- Create accounts and log in
- Create posts (text, images, video, or polls)
- Vote on posts (upvote/downvote)
- Comment on posts (with nested replies)
- Vote on poll options
- View notifications (comments, mentions)
- Manage profile and settings
- Use light/dark display mode

The application follows a **traditional MVC-style** architecture with Servlets as controllers, JSPs for views, and DAOs for data access.

---

## 2. Overall Project Flow

### High-Level Request Flow

```
User Request → Tomcat → Filters → Servlet → DAO → Oracle DB
                    ↓
              JSP View ← Model Data
                    ↓
              HTML Response
```

### Step-by-Step Flow by Feature

#### A. Initial Access & Authentication

1. User visits `/` → `index.jsp` redirects to `/posts/feed`
2. `AuthFilter` checks session for `/posts/*`:
   - **Not logged in** → Redirect to `/auth/login`
   - **Logged in** → Proceed
3. **Login** (`/auth/login` POST):
   - `AuthServlet` receives email + password
   - `UserDAO.checkLogin()` validates with `PasswordUtil.verifyPassword()` (bcrypt)
   - On success: `session.setAttribute("user", user)` → Redirect to feed
4. **Register** (`/auth/register` POST):
   - Validation via `ValidationUtil`
   - Password hashed with `PasswordUtil.hashPassword()` (bcrypt)
   - `UserDAO.create()` inserts user
   - `EmailUtil.sendEmail()` sends welcome email (Jakarta Mail)
5. **Forgot/Reset Password**:
   - Token generated, hashed (SHA-256), stored in `password_reset_tokens`
   - Email sent with reset link
   - `AuthServlet` validates token, updates password

#### B. Posts & Feed

1. **Feed** (`/posts/feed`):
   - `PostServlet` → `PostDAO.findAll(currentUserId)` or `search(q, currentUserId)`
   - `posts` + `searchQuery` set in request
   - Forward to `/WEB-INF/views/posts/feed.jsp`
2. **Create Post** (`/posts/create`):
   - Form with `enctype="multipart/form-data"` for media
   - `PostServlet` handles text, media (image/video), or poll
   - Media saved to disk; poll + options inserted into DB
   - Redirect to feed or detail
3. **Post Detail** (`/posts/detail?id=...`):
   - `PostDAO.findById()`, `CommentDAO.findByPostId()`
   - `buildCommentTree()` for nested comments
   - Forward to `detail.jsp`

#### C. Votes & Polls

1. **Post Vote** (AJAX `POST /vote`):
   - `vote.js` calls `fetch()` with `postId` and `value` (1 or -1)
   - `VoteServlet` → `VoteDAO`
   - Returns JSON `{ success, newCount, userVote }`
2. **Poll Vote** (AJAX `POST /polls/vote`):
   - `poll.js` uses `XMLHttpRequest` with `pollId` and `optionId`
   - `PollServlet` → `PollDAO` / `VoteDAO`
   - Returns JSON; UI updates progress bars and percentages

#### D. Comments

1. User submits comment on post detail page
2. `CommentServlet` → `CommentDAO.insert()`
3. Creates notification for post author (if not self-comment)
4. Redirect back to post detail

#### E. Notifications

1. **NotificationFilter** runs on every `/*` request:
   - If logged in: `NotificationDAO.getUnreadCount(userId)` → `request.setAttribute("unreadCount")`
2. `header.jsp` displays badge; `fetch("/notifications/count")` polls every 30 seconds
3. `/notifications` → `NotificationServlet` lists notifications, marks as read

#### F. Profile & Settings

- `/profile` → `ProfileServlet` → User profile page
- `/profile/upload` → `ProfilePhotoUploadServlet` (multipart) → Save profile image
- `/settings` → `SettingsServlet` → Update user settings

---

## 3. Technologies, Tools & Libraries

| Category | Technology / Library | Purpose |
|----------|---------------------|---------|
| **Runtime** | Java (JDK 11+) | Application logic |
| **Web Server** | Apache Tomcat 10.x | Servlet container, Jakarta EE |
| **Database** | Oracle Database | Data persistence |
| **Database Driver** | Oracle JDBC (ojdbc8 / ojdbc11) | DB connectivity |
| **Web Layer** | Jakarta Servlets | Controllers, request handling |
| **View** | JSP (JavaServer Pages) | Server-side HTML generation |
| **View** | JSTL (Jakarta Tags: core, fn) | Logic in JSP (c:if, c:forEach, fn:substring, etc.) |
| **Security** | jBCrypt (0.4) | Password hashing (bcrypt) |
| **Email** | Jakarta Mail 2.x | SMTP (welcome, password reset) |
| **Build** | javac (manual) + batch scripts | Compilation |
| **Frontend** | HTML, CSS, JavaScript (vanilla) | UI, theme toggle, voting, polls |
| **Connection Pool** | Tomcat JDBC Pool | JNDI DataSource |

---

## 4. Where & How Each Technology Is Used

### Java & Jakarta EE

| Component | Location | Usage |
|-----------|----------|-------|
| **Servlets** | `WEB-INF/src/controller/*.java` | Handle HTTP GET/POST, forward to JSPs, process forms |
| **Filters** | `WEB-INF/src/filter/*.java` | `AuthFilter` protects `/posts/*`; `NotificationFilter` adds unread count |
| **Models** | `WEB-INF/src/model/*.java` | `User`, `Post`, `Comment`, `Poll`, `Vote`, `Notification`, etc. |
| **DAOs** | `WEB-INF/src/dao/*.java` | `UserDAO`, `PostDAO`, `CommentDAO`, `NotificationDAO`, etc. |
| **Utils** | `WEB-INF/src/util/*.java` | `DBUtil`, `PasswordUtil`, `EmailUtil`, `ValidationUtil`, `TimeAgoUtil` |

### JSP & JSTL

| File / Tag | Where | Purpose |
|------------|-------|---------|
| `index.jsp` | Project root | Redirect to `/posts/feed` |
| `auth/*.jsp` | `auth/` | Login, register, forgot-password, reset-password |
| `WEB-INF/views/*.jsp` | `views/` | Feed, create post, post detail, profile, settings, notifications |
| `common/header.jsp` | Included in views | Header, search, user dropdown, notification badge |
| `taglib uri="jakarta.tags.core"` | JSPs | `<c:if>`, `<c:forEach>`, `<c:choose>`, `<c:out>` |
| `taglib uri="jakarta.tags.functions"` | feed.jsp, etc. | `fn:substring`, `fn:trim`, `fn:length` |

### Database (Oracle)

| Table | Purpose |
|-------|---------|
| `users` | User accounts, profile image |
| `password_reset_tokens` | Password reset flow |
| `tags` | Post categories (General, Technology, Gaming, etc.) |
| `posts` | Post content, media, tag |
| `polls`, `poll_options`, `poll_votes` | Polls and voting |
| `comments` | Nested comments (parent_id) |
| `votes` | Post upvotes/downvotes |
| `notifications` | User notifications |

**Connection:**

- `META-INF/context.xml` → Tomcat JNDI resource `jdbc/GalaxyBBS`
- `DBUtil.java` → Uses JNDI when in Tomcat; falls back to DriverManager with env vars

### jBCrypt

| Location | Usage |
|----------|-------|
| `util.PasswordUtil` | `hashPassword()` for registration; `verifyPassword()` for login |
| `AuthServlet` | Calls `UserDAO.checkLogin()` which uses `PasswordUtil` |

### Jakarta Mail

| Location | Usage |
|----------|-------|
| `util.EmailUtil` | `sendEmail(to, subject, body)` |
| `AuthServlet` | Welcome email on register; password reset email |

Uses `SMTP_USER` and `SMTP_PASSWORD` env vars (e.g. via `set_smtp_env.bat`).

### Oracle JDBC

| Location | Usage |
|----------|-------|
| `util.DBUtil` | `getConnection()` — JNDI or DriverManager |
| All DAOs | Use `DBUtil.getConnection()` for SQL execution |

### Frontend (CSS & JavaScript)

| Asset | Purpose |
|-------|---------|
| `assets/css/reset.css` | CSS reset |
| `assets/css/theme.css` | Light/dark theme variables |
| `assets/css/main.css`, `feed.css`, `post.css`, `auth.css`, etc. | Layout and styling |
| `assets/js/theme.js` | Theme toggle, `localStorage` persistence |
| `assets/js/auth.js` | Password visibility toggle, form helpers |
| `assets/js/vote.js` | `vote(postId, value, element)` → `fetch('/vote')` |
| `assets/js/poll.js` | `votePoll(pollId, optionId)` → XHR to `/polls/vote` |
| `assets/js/profile.js` | Profile page behavior |
| `header.jsp` | Inline scripts: user dropdown, notification polling, scroll-to-top |

### Build & Deployment Scripts

| Script | Purpose |
|--------|---------|
| `download_deps.bat` | Fetches jbcrypt, jakarta.mail to `WEB-INF/lib` (PowerShell) |
| `compile_classes.bat` | Compiles Java sources to `WEB-INF/classes` using Tomcat libs |
| `run.bat` | Menu: download, check, compile, SMTP, start Tomcat |
| `set_smtp_env.bat` | Sets `SMTP_USER`, `SMTP_PASSWORD` for current session |

---

## 5. Architecture & Design Patterns

- **MVC-style**: Servlets = Controllers, JSPs = Views, DAOs = Data layer
- **DAO pattern**: One DAO per entity (User, Post, Comment, etc.)
- **Filter chain**: AuthFilter → NotificationFilter → Servlet
- **Session-based auth**: `user` in HttpSession
- **JNDI + fallback**: DB connection via Tomcat pool or env-based DriverManager

---

## 6. Directory Structure

```
Galaxy_bbs/
├── index.jsp                 # Redirect to feed
├── auth/                     # Login, register, forgot/reset password JSPs
├── common/                   # header.jsp, error.jsp
├── assets/
│   ├── css/                  # reset, theme, main, feed, post, auth, profile
│   ├── js/                   # theme, auth, vote, poll, profile
│   └── img/                  # logo, default-avatar
├── META-INF/
│   └── context.xml           # Tomcat JNDI DataSource config
├── WEB-INF/
│   ├── web.xml               # Servlets, filters, mappings, error page
│   ├── classes/              # Compiled .class (from src)
│   ├── lib/                  # ojdbc, jbcrypt, jakarta.mail
│   ├── src/
│   │   ├── controller/       # AuthServlet, PostServlet, CommentServlet, etc.
│   │   ├── dao/              # UserDAO, PostDAO, CommentDAO, etc.
│   │   ├── filter/           # AuthFilter, NotificationFilter
│   │   ├── model/            # User, Post, Comment, Poll, etc.
│   │   └── util/             # DBUtil, PasswordUtil, EmailUtil, etc.
│   └── views/                # feed.jsp, create.jsp, detail.jsp, profile.jsp, etc.
├── SQL/
│   └── schema.sql            # Oracle schema
├── run.bat                   # Main menu
├── download_deps.bat         # Fetch JARs
├── compile_classes.bat       # Compile Java
├── set_smtp_env.bat          # SMTP env vars
├── SETUP.md                  # Setup guide
└── README.md                 # This file
```

---

## 7. Quick Setup

1. Clone the repo and `cd Galaxy_bbs`
2. Run `download_deps.bat` — fetches jbcrypt, Jakarta Mail
3. Add `ojdbc8.jar` or `ojdbc11.jar` to `WEB-INF/lib` (manual)
4. Run `compile_classes.bat check` — verify JARs
5. Run `compile_classes.bat` — compile
6. Execute `SQL/schema.sql` in Oracle
7. Configure `META-INF/context.xml` or env vars (`DB_URL`, `DB_USER`, `DB_PASSWORD`)
8. Optionally run `set_smtp_env.bat` for email
9. Deploy to Tomcat `webapps` and start Tomcat
10. Open `http://localhost:8080/Galaxy_bbs/`

See **SETUP.md** for detailed instructions and troubleshooting.

---

## Presentation Tips

- **Slide 1**: Project name + one-line description  
- **Slide 2**: High-level architecture diagram (User → Tomcat → Filters → Servlets → DAO → Oracle)  
- **Slide 3**: Tech stack table (Section 3)  
- **Slide 4**: Request flow for one feature (e.g. login or create post)  
- **Slide 5**: Where technologies are used (Section 4)  
- **Slide 6**: Directory structure  
- **Slide 7**: Live demo (login, create post, vote, comment)  
- **Slide 8**: Setup summary (Section 7)
