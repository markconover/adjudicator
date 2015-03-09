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
<%@page import="java.util.ArrayList" %>
<html>
<head>
	<title>User's data</title>
	<!-- Latest compiled and minified CSS -->
	<link rel="stylesheet" href="css/bootstrap/bootstrap.min.css">
	
	<!-- Optional theme -->
	<link rel="stylesheet" href="css/bootstrap/bootstrap-theme.min.css">
	
	<!-- Latest compiled and minified JavaScript -->
	<script src="js/bootstrap/bootstrap.min.js"></script>
</head>
<body>
<h2>Readable data from the database for this user</h2>
<%
	String user = request.getRemoteUser();
// TODO: Get data that "user" can read from the database!
//       For each row of data in the database:
//           1. Get the user roles.
//           2. Get the group roles.
//           3. Get the "user"'s user roles.
//           4. Get the "user"'s group roles.
//           5. If the one of the user's user roles and/or group roles are listed as one of the user/group roles for the data then the user can read the data.
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
  	
  	ResultSet rs = stmt.executeQuery("select roles.role_name " +
		"from users, roles, user_roles " +
		"where users.id=user_roles.user_id " + "and users.user_name='" + user + 
		"' and roles.id=user_roles.role_id");
  	
  	ArrayList<String> roleList = new ArrayList<String>();
  	while (rs.next()) {
  		roleList.add(rs.getString("role_name"));
  	}
  	
  	if (roleList.contains("Player")) {
/* 		rs = stmt.executeQuery("select data_table.data " +
  	  		"from data_table, roles " +
  	  		"where roles.id=data_table.role_id " + 
  	  		"and roles.role_name='Player'"); */
  	} else if (roleList.contains("Assistant Coach")) {
/* 		rs = stmt.executeQuery("select data_table.data " +
  	  		"from data_table, roles " +
  	  		"where roles.id=data_table.role_id " + 
  	  		"and roles.role_name='Player' or " + 
  	  		"where roles.id=data_table.role_id " + 
  	  		"and roles.role_name='Assistant Coach'");  	 */	
  	} else if (roleList.contains("Head Coach")) {
/* 		rs = stmt.executeQuery("select data_table.data " +
  	  		"from data_table, roles " +
  	  		"where roles.id=data_table.role_id " + 
  	  		"and roles.role_name='Player' or " + 
  	  		"where roles.id=data_table.role_id " +
  	  		"and roles.role_name='Assistant Coach' or " +
  	  		"where roles.id=data_table.role_id " +
  	  		"and roles.role_name='Head Coach'");  */ 	
  	  	// 1. get all the user's role id's
  	  	// 2. parse out the hierarchial role id
  	  	// 3. if "head coach" get the role id's for hierachial roles under
  	  	//    "head coach" i.e. "assistant coach", "player"
  	  	// 4. select all data that is labeled with the non-hierachial roles
  	  	//    (i.e. the roles besides "head coach", "assistant coach", and 
  	  	//    "player")
  	  	// 5. Parse out the data of the set from step #4 that contains the
  	  	//    hierarchical roles the user is priveleged to see.
  	  	// NOTE: Use "UNION", "INTERSECT", "JOIN"
 		rs = stmt.executeQuery("select data_table.data " +
	  		"from data_table, roles " + 
	  		"where roles.id=data_table.role_id " +
	  		"and roles.role_name='Assistant Coach'");
  	}
  	String data = null;
%>	
	<% while (rs.next()) { 
    	data = rs.getString("data");
	%>
	<div><%= data %></div>
	<%}%>
</body>
</html>
