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

      Connection con = null;
      try {
         //loading driver
         Class.forName (dBDriver).newInstance();
         con = DriverManager.getConnection (dBUrl, dBUser, dBPassword);
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
    	  String user = "Kobe Bryant";
    	  String userRolesListSql = "select roles.* " +
			  "from users, roles, user_roles " +
			  "where users.id=user_roles.user_id " + 
			  "and users.user_name=? and roles.id=user_roles.role_id";
          stmt = con.prepareStatement(userRolesListSql);
          stmt.setString(1, user);
          rs = stmt.executeQuery(); 
          String roleID = null;
          String roleName = null;
          
          // the user's roles
          ArrayList<String> userRoles = new ArrayList<String>();
          
          while (rs.next()) {
        	  roleID = rs.getString("id");
        	  roleName = rs.getString("role_name");
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
          
          System.out.println("The user '" + user + "' has the following " +
    		  "roles:");
          for (String role : userRoles) {
        	  System.out.println(role);
          }
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

          stmt = con.prepareStatement(dataRoleListSql);
          rs = stmt.executeQuery(); 
          String dataID = null;
          String roleIDs = null;
          String[] dataRoles = null;
          boolean addData = true;
          ArrayList<String> readableData = new ArrayList<String>();
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
               con.close();
           } catch (SQLException e) {
           }
       }
	}

}
