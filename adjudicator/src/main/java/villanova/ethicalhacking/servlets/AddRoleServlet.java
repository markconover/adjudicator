package villanova.ethicalhacking.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
    private static final Logger LOGGER = LoggerFactory.getLogger(
		AddRoleServlet.class);  
    
 	/**
 	 * Handles the HTTP POST
 	 *
 	 * @param req
 	 *            servlet request
 	 * @param resp
 	 *            servlet response
 	 * @throws ServletException
 	 *             if a servlet-specific error occurs
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	@Override
 	protected void doPost(HttpServletRequest req,
 			HttpServletResponse resp) throws ServletException, IOException {
 		processRequest(req, resp);
 	}

 	/**
 	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
 	 * methods.
 	 *
 	 * @param req
 	 *            servlet request
 	 * @param resp
 	 *            servlet response
 	 * @throws ServletException
 	 *             if a servlet-specific error occurs
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	protected void processRequest(HttpServletRequest req,
 			HttpServletResponse resp) throws ServletException, IOException {
 		String role = req.getParameter("role");

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
 			LOGGER.error("Error when adding group role to the database " + e);
 		}
 		
 		finally {
 		   try {
 		       stmt.close();
 		   } catch (SQLException e) {
 		       LOGGER.error("Error when closing statement.\n" + e);
 		   }
 		   try {
 		       connection.close();
 		   } catch (SQLException e) {
 		       LOGGER.error("Error when closing connection.\n" + e);
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
 		Connection connection = null;
 		try {
 			connection = ds.getConnection();
 		} catch (SQLException e) {
	       LOGGER.error("Unable to get connection to the database.\n" + e);
 		}
 		
 	    return connection;
      }
}