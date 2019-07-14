<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.Locale"%>
<%@ page import="java.util.ResourceBundle"%>
<% 

Locale loc = request.getLocale();
String lang = loc.getDisplayLanguage();

ResourceBundle rb = null;
try {
	rb = ResourceBundle.getBundle("MathSpring",loc);
}
catch (Exception e) {
//	logger.error(e.getMessage());
}
%>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>MathSpring | <%= rb.getString("student_registration")%></title>
    <link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png">
    <link rel="icon" type="image/png" href="/favicon-32x32.png" sizes="32x32">
    <link rel="icon" type="image/png" href="/favicon-16x16.png" sizes="16x16">
    <link rel="manifest" href="/manifest.json">
    <meta name="theme-color" content="#ffffff">
    <script type="text/javascript" src="js/jquery-1.10.2.js"></script>
    <link rel="stylesheet" href="css/bootstrap.min.css">
    <link href="css/common_new.css" rel="stylesheet" type="text/css" />
    <link href="login/css/userRegSelectClass_new.css" rel="stylesheet" type="text/css" />
</head>
<body>
    <div class="main-content">
        <header class="site-header" role="banner">
            <div class="row" id="wrapper">
                <div class="navbar-header">
                    <img class="logo" src="img/ms_mini_logo_new.png">
                </div><!-- navbar-header -->
            </div>
        </header>
        <div class="row school-list-wrapper">
            <table class="table table-striped table-bordered school-table">
                <thead class="thead-inverse">
                <tr>
                    <th><%= rb.getString("teacher")%></th>
                    <th><%= rb.getString("school")%></th>
                    <th><%= rb.getString("class")%></th>
                    <th><%= rb.getString("town")%></th>
                    <th><%= rb.getString("choose")%></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="c" items="${classes}">
                    <tr>
                        <td>${c.teacherName}</td>
                        <td>${c.school}</td>
                        <td>${c.name}</td>
                        <td>${c.town}</td>
                        <td>
                            <a href="${pageContext.request.contextPath}/WoAdmin?action=UserRegistrationClassSelection&classId=${c.classid}&studId=${studId}&startPage=${startPage}&var=b">
                                <img src="images/arrow.gif" width="18" height="16" border="0">
                            </a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    <footer class="bottom-sticky-footer">
        &copy; <%= rb.getString("copyright")%>
    </footer>
</body>
</html>
