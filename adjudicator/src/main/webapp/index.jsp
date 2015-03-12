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
    	// For each data_id:
    	// 1. Get the role_id's for the data_id
    	// 2. Get the user's role id's.
    	// 3. Flatten the user's role id's (e.g. if the user is 
  	    //    "Head Coach" then their roles become "Head Coach", 
  	    //    "Assistant Coach" and "Player".
	    	// 4. Compare the data_id's role_id's to the user's flattened role id's.  
  	  	//       If they are the same the user can read the data.  
  	    //       If they are not the same the user cannot read the data.
  	  	// NOTE: Use "UNION", "INTERSECT", "JOIN"
  	    // NOTE: Need to account for roles being added dynamically!
  	  
  	  
  	  // Get the user's role id's and flatten the role id's.
  	  String userRolesListSql = "select roles.* " +
			  "from users, roles, user_roles " +
			  "where users.id=user_roles.user_id " + 
			  "and users.user_name=? and roles.id=user_roles.role_id";
        stmt = conn.prepareStatement(userRolesListSql);
        stmt.setString(1, user);
        rs = stmt.executeQuery(); 
        String roleID = null;
        
        // the user's roles
        ArrayList<String> userRoles = new ArrayList<String>();
        
        while (rs.next()) {
      	  roleID = rs.getString("id");
      	  // Check if "Head Coach"
      	  if (roleID.equals("3")) {
      		  // Add "Head Coach" to user's roles
      		  userRoles.add("3");
      		  
      		  // Add "Assistant Coach" to user's roles
      		  userRoles.add("4");
      		  
      		  // Add "Player" to user's roles
      		  userRoles.add("5");
      	  } else if (roleID.equals("4")) {
      		  // Add "Assistant Coach" to user's roles
      		  userRoles.add("4");
      		  
      		  // Add "Player" to user's roles
      		  userRoles.add("5");
      	  } else {
      		  userRoles.add(roleID);
      	  }
        }
  	  
        // Get the each row of data and it's associated role's
//  	  String dataRoleListSql = "select data_table.data, " +
//			  "data_roles.data_id, data_roles.role_id " + 
//  		  "from data_table inner " +
//			  "inner join data_roles " +
//  		  "on data_table.id=data_roles.data_id " +
//			  "order by data_roles.data_id, data_roles.role_id";
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
        while (rs.next()) {
      	  dataID = rs.getString("data_id");
      	  roleIDs = rs.getString("role_ids");
      	  System.out.println(dataID + "     " + roleIDs);
      	  dataRoles = roleIDs.split(",");
      	  
      	  // If the user's roles contain all the roles that the current data
      	  // row is labeled with, then the user can read the data from the 
      	  // current data row.
      	  // NOTE: the user may have additional roles but needs to contain
      	  //       at least the set of roles that the current data row is 
      	  //       labeled with.
      	  for (int i = 0; i < dataRoles.length; i++) {
      		  if (!userRoles.contains(dataRoles[i])) {
      			  addData = false;
      			  break;
      		  }
      	  }
      	  
      	  if (addData) {
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
