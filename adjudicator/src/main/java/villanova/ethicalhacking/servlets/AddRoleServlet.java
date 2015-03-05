package villanova.ethicalhacking.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;


public class AddRoleServlet extends HttpServlet {

	private static final long serialVersionUID = 957462709932298088L;
	
    private static Logger LOGGER = Logger.getLogger(
		AddRoleServlet.class.getName()); 
    
    
 	/**
 	 * Handles the HTTP POST
 	 *
 	 * @param request
 	 *            servlet request
 	 * @param response
 	 *            servlet response
 	 * @throws ServletException
 	 *             if a servlet-specific error occurs
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request, response);
 	}

 	/**
 	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
 	 * methods.
 	 *
 	 * @param request
 	 *            servlet request
 	 * @param response
 	 *            servlet response
 	 * @throws ServletException
 	 *             if a servlet-specific error occurs
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	protected void processRequest(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		String role = request.getParameter("role");

 		// TODO: Verify that the group role does not already
 		// exist/is valid!
 		
 		String sql = "INSERT INTO adjudicator.roles (role_name) VALUES (?)";
 		Connection connection = null;
 		PreparedStatement stmt = null;
   
 		try {
 		    connection = getConnection();
 		    stmt = connection.prepareStatement(sql);
 		    stmt.setString(1, role);
 		    stmt.executeUpdate();
 		} catch (Exception e) {
 			LOGGER.fine("Error when adding group role to the database " + e);
 			e.printStackTrace();
 		}
 		
 		finally {
 		   try {
 		       stmt.close();
 		   } catch (SQLException e) {
 		       LOGGER.fine("Error when closing statement." + e);
 		   }
 		   try {
 		       connection.close();
 		   } catch (SQLException e) {
 		       LOGGER.fine("Error when closing connection." + e);
 	       }
 	   }
 	}

 	/**
 	 * Returns JDBC connection
 	 * @return JDBC connection
 	 * @throws LoginException
 	 */
 	 private Connection getConnection() throws LoginException {
 		 
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
 		Connection connection = null;
 		try {
 			connection = ds.getConnection();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	    return connection;
      }
}