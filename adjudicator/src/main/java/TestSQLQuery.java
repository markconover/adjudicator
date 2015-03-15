import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TestSQLQuery {
	public static void main(String[] args) {
      String dBUser = "root";
      String dBPassword = "password";
      String dBUrl = "jdbc:mysql://localhost:3306/adjudicator?autoReconnect=true";
      String dBDriver = "com.mysql.jdbc.Driver";

      Connection conn = null;
      try {
         //loading driver
         Class.forName (dBDriver).newInstance();
         conn = DriverManager.getConnection (dBUrl, dBUser, dBPassword);
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
      }
      
      ResultSet rs = null;
      PreparedStatement stmt = null;
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
    	  //String user = "Byron Scott";
    	  //String user = "Mark Madsen";
    	  String user = "admin";
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
          
//          while (rs.next()) {
//        	  roleID = rs.getString("id");
//        	  // Check if "Head Coach"
//        	  if (roleID.equals("3")) {
//        		  // Add "Head Coach" to user's roles
//        		  userRoles.add("3");
//        		  
//        		  // Add "Assistant Coach" to user's roles
//        		  userRoles.add("4");
//        		  
//        		  // Add "Player" to user's roles
//        		  userRoles.add("5");
//        	  } else if (roleID.equals("4")) {
//        		  // Add "Assistant Coach" to user's roles
//        		  userRoles.add("4");
//        		  
//        		  // Add "Player" to user's roles
//        		  userRoles.add("5");
//        	  } else {
//        		  userRoles.add(roleID);
//        	  }
//          }
          while (rs.next()) {
        	  roleID = rs.getString("id");
        	  if (roleID.equals("3") || roleID.equals("4") 
    			  || roleID.equals("5")) {       		  
        		  userHierarchyRole = roleID;
        	  } else {
        		  userRoles.add(roleID);
        	  }
          }
          
          System.out.println("The user '" + user + "' has the following " +
    		  "non-hierarchial roles:");
          for (String role : userRoles) {
        	  System.out.println(role);
          }
          System.out.println("The user '" + user + "' has the following " +
    		  "hierarchial role:");
    	  System.out.println(userHierarchyRole);
          System.out.println("");
    	  
          System.out.println("The following data is in the database:");
          // Get the each row of data and it's associated role's
//    	  String dataRoleListSql = "select data_table.data, " +
//			  "data_roles.data_id, data_roles.role_id " + 
//    		  "from data_table inner " +
//			  "inner join data_roles " +
//    		  "on data_table.id=data_roles.data_id " +
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
          ArrayList<String> readableData = new ArrayList<String>();
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
          System.out.println("");
          
          System.out.println("The user can read the following data:");
          for (String data : readableData) {
        	  System.out.println(data);
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
	}

}
