Galaxy BBS — Project Overview

Galaxy BBS is a Reddit-style bulletin board system built with Java Servlets, JSP, and Oracle Database.
It supports posts, nested comments, votes, polls, media uploads, user profiles, notifications, and light/dark mode.

------------------------------------------------------------
1. Project Summary
------------------------------------------------------------

Main Features:
- User registration and login (bcrypt password hashing)
- Create posts (text, image, video, or poll)
- Upvote / downvote posts
- Nested comments
- Poll voting with dynamic updates
- Notifications (comments, mentions)
- Profile and settings management
- Light / Dark theme toggle

Architecture:
- MVC-style structure
  - Servlets = Controllers
  - JSP = Views
  - DAO = Data layer
  - Oracle DB = Persistence

------------------------------------------------------------
2. Request Flow
------------------------------------------------------------

High-Level Flow:
User → Tomcat → Filters → Servlet → DAO → Oracle DB
                          ↓
                        JSP
                          ↓
                      HTML Response

Authentication:
- AuthFilter protects /posts/*
- Login:
  - AuthServlet → UserDAO.checkLogin()
  - Password verified with jBCrypt
  - Session stores "user"
- Register:
  - Validation → bcrypt hash → DB insert
  - Welcome email via Jakarta Mail
- Password reset:
  - SHA-256 token stored in password_reset_tokens
  - Reset link sent via email

Posts & Feed:
- /posts/feed → PostDAO.findAll() or search()
- Create post:
  - multipart/form-data for media
  - Media saved to disk
  - Poll and options inserted into DB
- Post detail:
  - Load post and comments
  - buildCommentTree() for nested replies

Voting & Polls (AJAX):
- Post vote:
  - fetch('/vote')
  - VoteServlet → VoteDAO
  - Returns JSON (success, newCount, userVote)
- Poll vote:
  - XHR → /polls/vote
  - Updates progress bars dynamically

Comments:
- CommentServlet → CommentDAO.insert()
- Creates notification for post author

Notifications:
- NotificationFilter adds unreadCount
- Header polls /notifications/count every 30 seconds
- NotificationServlet lists and marks as read

Profile & Settings:
- /profile → Profile page
- Profile image upload (multipart)
- /settings → Update user settings

------------------------------------------------------------
3. Tech Stack
------------------------------------------------------------

Runtime: Java (JDK 11+)
Server: Apache Tomcat 10
Database: Oracle Database
Web Layer: Jakarta Servlets
View: JSP + JSTL
Security: jBCrypt (bcrypt)
Email: Jakarta Mail
JDBC: Oracle ojdbc8 / ojdbc11
Connection Pool: Tomcat JDBC Pool (JNDI)
Frontend: HTML, CSS, Vanilla JavaScript
Build: javac + batch scripts

------------------------------------------------------------
4. How Technologies Are Used
------------------------------------------------------------

Backend Structure:
controller/ → Servlets
dao/ → Database access (UserDAO, PostDAO, etc.)
model/ → Entity classes
filter/ → AuthFilter, NotificationFilter
util/ → DBUtil, PasswordUtil, EmailUtil, ValidationUtil, etc.

Database Tables:
- users
- password_reset_tokens
- posts
- comments (nested via parent_id)
- votes
- polls, poll_options, poll_votes
- notifications
- tags

Database Connection:
- META-INF/context.xml → JNDI DataSource (jdbc/GalaxyBBS)
- DBUtil uses JNDI (Tomcat) or DriverManager fallback (env vars)

Frontend Assets:
CSS:
- reset.css, theme.css, main.css, feed.css, post.css, auth.css

JavaScript:
- theme.js (dark mode toggle)
- vote.js (AJAX voting)
- poll.js (poll voting)
- auth.js, profile.js

Build Scripts:
- download_deps.bat (download dependencies)
- compile_classes.bat (compile Java)
- run.bat (main menu)
- set_smtp_env.bat (SMTP environment variables)

------------------------------------------------------------
5. Design Patterns
------------------------------------------------------------

- MVC-style architecture
- DAO pattern (one DAO per entity)
- Filter chain (AuthFilter → NotificationFilter)
- Session-based authentication
- JNDI connection pooling with fallback

------------------------------------------------------------
6. Directory Structure
------------------------------------------------------------

Galaxy_bbs/
├── index.jsp
├── auth/
├── common/
├── assets/
│   ├── css/
│   ├── js/
│   └── img/
├── META-INF/context.xml
├── WEB-INF/
│   ├── web.xml
│   ├── classes/
│   ├── lib/
│   ├── src/
│   └── views/
├── SQL/schema.sql
├── run.bat
├── download_deps.bat
├── compile_classes.bat
├── set_smtp_env.bat
├── SETUP.md
└── README.md

------------------------------------------------------------
7. Quick Setup
------------------------------------------------------------

1. Clone the repository
2. Run download_deps.bat
3. Add ojdbc8.jar or ojdbc11.jar to WEB-INF/lib
4. Run compile_classes.bat check
5. Run compile_classes.bat
6. Execute SQL/schema.sql in Oracle
7. Configure DB (context.xml or environment variables)
8. (Optional) Set SMTP environment variables
9. Deploy to Tomcat
10. Open http://localhost:8080/Galaxy_bbs/

See SETUP.md for detailed setup and troubleshooting.

------------------------------------------------------------
Presentation Guide
------------------------------------------------------------

1. Project overview
2. Architecture diagram
3. Tech stack
4. Example request flow (login or create post)
5. Where technologies are used
6. Directory structure
7. Live demo
8. Setup summary
