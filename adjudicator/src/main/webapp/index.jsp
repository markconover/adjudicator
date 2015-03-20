<%@page language="java" contentType="text/html"%>
<%@page import="java.sql.Connection" %>
<%@page import="java.sql.SQLException" %>
<%@page import="java.sql.PreparedStatement" %>
<%@page import="java.sql.ResultSet" %>
<%@page import="java.sql.ResultSetMetaData" %>
<%@page import="javax.naming.Context"%>
<%@page import="javax.naming.InitialContext"%>
<%@page import="javax.naming.NamingException"%>
<%@page import="javax.sql.DataSource" %>
<%@page import="java.util.ArrayList" %>
<%@page import="org.slf4j.Logger" %>
<%@page import="org.slf4j.LoggerFactory" %>
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
    final Logger LOGGER = LoggerFactory.getLogger(
   		"villanova.ethicalhacking.index-jsp");
	
	String user = request.getRemoteUser();
	
    LOGGER.info("Getting readable data from the database for user (" + user + 
   		").");
		
	Class.forName("com.mysql.jdbc.Driver").newInstance();

    // Get DataSource
    Context ctx = null;
    try {
        ctx = new InitialContext();
    } catch (NamingException e) {
       LOGGER.error("Unable to get initial context.\n" + e);
    }
    DataSource ds = null;
    String jdbcDataSource = "java:comp/env/jdbc/adjudicator"; 
    try {
        ds = (DataSource)ctx.lookup(jdbcDataSource);
    } catch (NamingException e) {
       LOGGER.error("Unable to lookup the datasource name: " + 
           jdbcDataSource + "\n" + e);
    }
    
    // Get Connection and Statement
    Connection conn = null;
    try {
        conn = ds.getConnection();
    } catch (SQLException e) {
       LOGGER.error("Unable to get connection to the database.\n" + e);
    }

	ResultSet rs = null;
    PreparedStatement stmt = null;
    ArrayList<String> readableData = null;
    String stmtText = null;
    
    // the user's non-hierarchial role/s
    ArrayList<String> userNonHierarchyRoles = null;
    
    // the user's hierarchial role
    String userHierarchyRole = null;
    
    try {
    	// Get the user's role id's
  	    String userRolesListSql = "select roles.* " +
			  "from users, roles, user_roles " +
			  "where users.id=user_roles.user_id " + 
			  "and users.user_name=? and roles.id=user_roles.role_id";
        stmt = conn.prepareStatement(userRolesListSql);
        stmt.setString(1, user);
        
        // Log the statement being executed
        stmtText = stmt.toString();
        userRolesListSql = stmtText.substring(stmtText.indexOf( ": " 
       		) + 2);
        LOGGER.debug("Executing the following sql query for user (" + user + 
       		"): \n" + userRolesListSql);
        
        rs = stmt.executeQuery(); 
        String roleID = null;
        
        userNonHierarchyRoles = new ArrayList<String>();
        
        userHierarchyRole = null;

        while (rs.next()) {
      	  roleID = rs.getString("id");
      	  if (roleID.equals("3") || roleID.equals("4") 
  			  || roleID.equals("5")) {       		  
      		  
      		  userHierarchyRole = roleID;
      	  } else {
      		  userNonHierarchyRoles.add(roleID);
      	  }
        }
    } catch (SQLException e) {
        LOGGER.error("Unable to get readable data from the database.\n" + e);
    } finally {
         try {
             rs.close();
         } catch (SQLException e) {
             LOGGER.error("Error when closing result set.\n" + e);
         }
         try {
             stmt.close();
         } catch (SQLException e) {
             LOGGER.error("Error when closing statement.\n" + e);
         }
    }
    
    try {
        String dataRoleListSql = "select data_table.id as 'data_id', " +
  		  "group_concat(data_roles.role_id separator ',') as 'role_ids' " +
  		  ", data_table.data " +
  		  "from data_table " + 
  		  "inner join data_roles " +
  		  "on data_table.id=data_roles.data_id " +
  		  "group by data_id";
        
        stmt = conn.prepareStatement(dataRoleListSql);
        
        // Log the statement being executed
        stmtText = stmt.toString();
        dataRoleListSql = stmtText.substring(stmtText.indexOf( ": " 
       	    ) + 2);
        LOGGER.debug("Executing the following sql query:\n" + dataRoleListSql);
        
        rs = stmt.executeQuery(); 
        String dataID = null;
        String roleIDs = null;
        String[] dataRoles = null;
        boolean readData = true;
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
      		  } else if (!userNonHierarchyRoles.contains(dataRoles[i])) {
      			  // user does not contain this non-hierarchial role and
      			  // therefore cannot read this data
      			  readData = false;
      			  break;
      		  }
      	  }
      	  
      	  if (readData && dataHierarchialRoles.contains(userHierarchyRole)) {     		  
      		  readableData.add(rs.getString("data"));
      	  }
      	  
      	  readData = true;
        }
    } catch (SQLException e) {
        LOGGER.error("Unable to get readable data from the database.\n" + e);
    } finally {
         try {
             rs.close();
         } catch (SQLException e) {
             LOGGER.error("Error when closing result set.\n" + e);
         }
         try {
             stmt.close();
         } catch (SQLException e) {
             LOGGER.error("Error when closing statement.\n" + e);
         }
         try {
             conn.close();
         } catch (SQLException e) {
             LOGGER.error("Error when closing connection.\n" + e);
         }
     }
%>	
	<% for (String data : readableData) {
	%>
	<div><%= data %></div>
	<%}%>
</body>
</html>
