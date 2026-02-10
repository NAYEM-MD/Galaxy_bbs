<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%@ page import="dao.NotificationDAO" %>
<%
    User currentUser = (User) session.getAttribute("user");
    int unreadCount = 0;
    if (currentUser != null) {
        NotificationDAO notificationDAO = new NotificationDAO();
        unreadCount = notificationDAO.getUnreadCount(currentUser.getId());
    }
%>
<header class="app-header">
    <div class="header-left">
        <a href="${pageContext.request.contextPath}/posts/feed" class="logo-link">
            <img src="${pageContext.request.contextPath}/assets/img/logo.png?v=3" alt="Galaxy^bbs" class="header-logo" onerror="this.style.display='none';this.nextElementSibling.style.display='block'">
            <button class="logo-button" data-text="Galaxy_bbs">
                <span class="actual-text">&nbsp;Galaxy_bbs&nbsp;</span>
                <span aria-hidden="true" class="hover-text">&nbsp;Galaxy_bbs&nbsp;</span>
            </button>
        </a>
    </div>
    
    <div class="header-center">
        <div class="search-bar">
            <input type="text" placeholder="Search posts...">
        </div>
    </div>
    
    <div class="header-right">
        <a href="${pageContext.request.contextPath}/posts/create" class="create-post-btn">
            <span class="plus-icon">+</span> Create
        </a>
        
        <div class="user-actions">
            <% if (currentUser != null) { %>
            <a href="${pageContext.request.contextPath}/notifications" class="notification-bell-btn" title="Notifications">
                <svg viewBox="0 0 448 512" class="bell"><path d="M224 0c-17.7 0-32 14.3-32 32V49.9C119.5 61.4 64 124.2 64 200v33.4c0 45.4-15.5 89.5-43.8 124.9L5.3 377c-5.8 7.2-6.9 17.1-2.9 25.4S14.8 416 24 416H424c9.2 0 17.6-5.3 21.6-13.6s2.9-18.2-2.9-25.4l-14.9-18.6C399.5 322.9 384 278.8 384 233.4V200c0-75.8-55.5-138.6-128-150.1V32c0-17.7-14.3-32-32-32zm0 96h8c57.4 0 104 46.6 104 104v33.4c0 47.9 13.9 94.6 39.7 134.6H72.3C98.1 328 112 281.3 112 233.4V200c0-57.4 46.6-104 104-104h8zm64 352H224 160c0 17 6.7 33.3 18.7 45.3s28.3 18.7 45.3 18.7s33.3-6.7 45.3-18.7s18.7-28.3 18.7-45.3z"></path></svg>
                <span id="notification-badge" class="notification-badge" style="<%= unreadCount == 0 ? "display:none" : "" %>"><%= unreadCount %></span>
            </a>

            <!-- User Dropdown -->
            <div class="user-menu-container">
            <button id="user-menu-btn" class="user-menu-btn" onclick="toggleUserMenu(event)">
                    <div class="user-avatar-small">
                        <img src="${pageContext.request.contextPath}/assets/img/default-avatar.png" onerror="this.src='https://www.redditstatic.com/avatars/avatar_default_02_0079D3.png'" alt="<%= currentUser.getUsername() %>">
                    </div>
                    <div class="user-info-brief">
                        <span class="username-text"><%= currentUser.getUsername() %></span>
                    </div>
                    <span class="dropdown-arrow">▼</span>
                </button>

                <div id="user-dropdown" class="user-dropdown-content">
                    <a href="${pageContext.request.contextPath}/profile?username=<%= currentUser.getUsername() %>" class="dropdown-item">
                        <span class="item-icon">
                            <!-- Simple Profile Icon -->
                            <svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg" class="menu-icon"><path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"/></svg>
                        </span>
                        View Profile
                    </a>

                    <div class="dropdown-item" onclick="if(window.toggleDisplayMode){window.toggleDisplayMode(event);}">
                        <span class="item-icon">
                            <!-- Moon Icon -->
                            <svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg" class="menu-icon"><path d="M17.293 13.293A8 8 0 016.707 2.707a8.001 8.001 0 1010.586 10.586z"/></svg>
                        </span>
                        <span>Display Mode</span>
                        <div class="toggle-switch">
                            <input type="checkbox" id="darkModeToggle" onchange="if(window.applyTheme){window.applyTheme(this.checked);}">
                            <span class="slider round"></span>
                        </div>
                    </div>

                    <a href="${pageContext.request.contextPath}/settings" class="dropdown-item">
                        <span class="item-icon">
                            <!-- Settings Icon -->
                            <svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg" class="menu-icon"><path fill-rule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clip-rule="evenodd"/></svg>
                        </span>
                        Settings
                    </a>

                    <div class="dropdown-divider"></div>

                    <a href="${pageContext.request.contextPath}/auth/logout" class="dropdown-item">
                        <span class="item-icon">
                             <!-- Logout Icon -->
                             <svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg" class="menu-icon"><path fill-rule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" clip-rule="evenodd"/></svg>
                        </span>
                        Log Out
                    </a>
                </div>
            </div>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/auth/login" class="btn-primary btn-login-margin">Log In</a>
                <a href="${pageContext.request.contextPath}/auth/register" class="btn-secondary">Sign Up</a>
            <% } %>
        </div>
    </div>
</header>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">

<script>
    // Expose app context path for JS modules (vote.js, poll.js, etc.)
    window.appContextPath = "<%= request.getContextPath() %>";

    function toggleUserMenu(event) {
        event.stopPropagation();
        var dropdown = document.getElementById("user-dropdown");
        dropdown.classList.toggle("show");
    }
    
    // Theme toggle and applyTheme are provided by theme.js (loaded in head).

    // Close the user dropdown when clicking outside (capture phase so it runs before other handlers)
    document.addEventListener('click', function(event) {
        if (!event.target.closest('.user-menu-btn') && !event.target.closest('.user-dropdown-content')) {
            var dropdowns = document.getElementsByClassName("user-dropdown-content");
            for (var i = 0; i < dropdowns.length; i++) {
                var openDropdown = dropdowns[i];
                if (openDropdown.classList.contains('show')) {
                    openDropdown.classList.remove('show');
                }
            }
        }
    }, true);

    // Sync checkbox with stored theme (theme.js applies theme in head before paint)
    (function() {
        var checkbox = document.getElementById("darkModeToggle");
        if (checkbox) {
            try {
                checkbox.checked = localStorage.getItem('theme') === 'dark';
            } catch (e) {}
        }
    })();

    // Real-time notification count polling (when logged in)
    (function() {
        var badge = document.getElementById("notification-badge");
        if (!badge) return;
        var ctx = "<%= request.getContextPath() %>";
        function fetchCount() {
            fetch(ctx + "/notifications/count", { credentials: "same-origin" })
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    var c = data.count || 0;
                    badge.textContent = c;
                    badge.style.display = c > 0 ? "" : "none";
                })
                .catch(function() {});
        }
        fetchCount(); // immediate fetch on load
        setInterval(fetchCount, 30000); // then every 30 seconds
    })();
</script>