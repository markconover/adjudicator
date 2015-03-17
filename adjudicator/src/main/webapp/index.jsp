<%@page language="java" contentType="text/html"%>
<%@page import="java.sql.Connection" %>
<%@page import="java.sql.SQLException" %>
<%@page import="java.sql.PreparedStatement" %>
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
	
	<script src="js/jquery/jquery-2.1.3.min.js"></script>
	
	<!-- Latest compiled and minified JavaScript -->
	<script src="js/bootstrap/bootstrap.min.js"></script>
</head>
<body>
<h2>Readable data from the database for this user</h2>
<%
	String user = request.getRemoteUser();
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

	ResultSet rs = null;
    PreparedStatement stmt = null;
    ArrayList<String> readableData = null;
    try {
  	  // Get the user's role id's
  	  String userRolesListSql = "select roles.* " +
			  "from users, roles, user_roles " +
			  "where users.id=user_roles.user_id " + 
			  "and users.user_name=? and roles.id=user_roles.role_id";
        stmt = conn.prepareStatement(userRolesListSql);
        stmt.setString(1, user);
        rs = stmt.executeQuery(); 
        String roleID = null;
        
        // the user's non-hierarchial role/s
        ArrayList<String> userRoles = new ArrayList<String>();
        
        // the user's hierarchial role
        String userHierarchyRole = null;

        while (rs.next()) {
      	  roleID = rs.getString("id");
      	  if (roleID.equals("3") || roleID.equals("4") 
  			  || roleID.equals("5")) {       		  
      		  userHierarchyRole = roleID;
      	  } else {
      		  userRoles.add(roleID);
      	  }
        }

        String dataRoleListSql = "select data_table.id as 'data_id', " +
  		  "group_concat(data_roles.role_id separator ',') as 'role_ids' " +
  		  ", data_table.data " +
  		  "from data_table " + 
  		  "inner join data_roles " +
  		  "on data_table.id=data_roles.data_id " +
  		  "group by data_id";

        stmt = conn.prepareStatement(dataRoleListSql);
        rs = stmt.executeQuery(); 
        String dataID = null;
        String roleIDs = null;
        String[] dataRoles = null;
        boolean addData = true;
        readableData = new ArrayList<String>();
        ArrayList<String> dataHierarchialRoles = null;
        while (rs.next()) {
      	  dataID = rs.getString("data_id");
      	  roleIDs = rs.getString("role_ids");
      	  System.out.println(dataID + "     " + roleIDs);
      	  dataRoles = roleIDs.split(",");
      	  dataHierarchialRoles = new ArrayList<String>();
      	  
      	  // If the user's hierarchical role is contained in the data's
      	  // hierarchical roles and the user's non-hierarchical roles 
      	  // matches the data's non-hierarchical roles then the user can
      	  // read the data for the current row.
      	  for (int i = 0; i < dataRoles.length; i++) {
      		  if (dataRoles[i].equals("3") || dataRoles[i].equals("4")
  				  || dataRoles[i].equals("5")) {
      			  
      			  dataHierarchialRoles.add(dataRoles[i]);
      		  } else if (!userRoles.contains(dataRoles[i])) {
      			  // user does not contain this non-hierarchial role and
      			  // therefore cannot read this data
      			  addData = false;
      			  break;
      		  }
      	  }
      	  
      	  if (addData && dataHierarchialRoles.contains(userHierarchyRole)) {     		  
      		  readableData.add(rs.getString("data"));
      	  }
      	  
      	  addData = true;
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
         try {
             rs.close();
         } catch (SQLException e) {
         }
         try {
             stmt.close();
         } catch (SQLException e) {
         }
         try {
             conn.close();
         } catch (SQLException e) {
         }
     }
%>	
	<% for (String data : readableData) {
	%>
	<div><%= data %></div>
	<%}%>
</body>
</html>
