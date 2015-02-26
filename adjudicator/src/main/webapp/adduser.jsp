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
  	ResultSet rs = stmt.executeQuery("SELECT role_name FROM adjudicator.user_roles");
  	ResultSetMetaData resMetaData = rs.getMetaData();
  	String role = null;
%>
	<form id="add-user-form" action="/adduser" method="post">
		<fieldset>
			<h1>Add User</h1>
			<div>Username</div>
			<div>
				<input type="text" id="username" placeholder="johndoe" />
			</div>
			<div>Password</div>
			<div>
				<input type="password" id="password" placeholder="password" />
			</div>
			<div>Repeat Password</div>
			<div>
				<input type="password" id="repeat-password" placeholder="password" />
			</div>
			<!-- Dynamically retrieve all role possibilities from database. -->
			<div>Choose User Role/s</div>
			<div>
				<select id="user-role">
					<% while (rs.next()) { 
					    role = rs.getString(1);
					%>
					<option value="<%= role %>"><%= role %></option>
					<%}%>
				</select>
			</div>
			<div>Choose Group Role/s</div>
			<div>
				<select id="group-role">
				<%
			  		stmt = conn.createStatement();
			  		rs = stmt.executeQuery("SELECT role_name FROM adjudicator.roles");
			  		resMetaData = rs.getMetaData();
				%>
					<% while (rs.next()) { 
					    role = rs.getString(1);
					%>
					<option value="<%= role %>"><%= role %></option>
					<%}%>
				</select>
			</div>
			<br />
			<input type="submit" value="Create User" id="submit" />
		</fieldset>
	</form>
<%
  conn.close();
 %>
</body>
</html>
