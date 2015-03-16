<%@page language="java" contentType="text/html"%>
<%@page import="java.sql.Connection" %>
<%@page import="java.sql.SQLException" %>
<%@page import="java.sql.Statement" %>
<%@page import="java.sql.ResultSet" %>
<%@page import="java.sql.ResultSetMetaData" %>
<%@page import="com.mysql.*"%>
<%@page import="javax.naming.Context"%>
<%@page import="javax.naming.InitialContext"%>
<%@page import="javax.naming.NamingException"%>
<%@page import="javax.sql.DataSource" %>
<html>
<head>
	<title>Add User</title>
	
	<!-- Latest compiled and minified CSS -->
	<link rel="stylesheet" href="css/bootstrap/bootstrap.min.css">

	<!-- Optional theme -->
	<link rel="stylesheet" href="css/bootstrap/bootstrap-theme.min.css">
	
	<script src="js/jquery/jquery-2.1.3.min.js"></script>

	<!-- Latest compiled and minified JavaScript -->
	<script src="js/bootstrap/bootstrap.min.js"></script>
</head>
<body>
<%
  	Class.forName("com.mysql.jdbc.Driver").newInstance();

	// Get DataSource
	Context ctx = null;
	try {
		ctx = new InitialContext();
	} catch (NamingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	DataSource ds = null;
	try {
		ds = (DataSource)ctx.lookup("java:comp/env/jdbc/adjudicator");
	} catch (NamingException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	// Get Connection and Statement
	Connection conn = null;
	try {
		conn = ds.getConnection();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

  	Statement stmt = conn.createStatement();
  	ResultSet rs = stmt.executeQuery(
      "SELECT role_name FROM adjudicator.roles");
  	ResultSetMetaData resMetaData = rs.getMetaData();
  	String role = null;
%>
	<form id="add-user-form" action="adduser" method="post">
		<fieldset>
			<h1>Add User</h1>
			<div>Username</div>
			<div>
				<input type="text" name="username" placeholder="johndoe" />
			</div>
			<div>Password</div>
			<div>
				<input type="password" name="password" placeholder="password" />
			</div>
			<!-- Dynamically retrieve all role possibilities from database. -->
			<div>Choose User Role/s</div>
			<% while (rs.next()) { 
			    role = rs.getString(1);
			%>
				<div class="checkbox">
					<label>
						<input name="<%= role %>" type="checkbox" value="<%= role %>"><%= role %>
					</label>
				</div>
			<%}%>
			<br />
			<input type="submit" value="Create User" />
		</fieldset>
	</form>
<%
  conn.close();
 %>
</body>
</html>
