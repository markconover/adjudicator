package villanova.ethicalhacking;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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


public class AddUserServlet extends HttpServlet {

	private static final long serialVersionUID = -2655972628571555177L;
	
    private static Logger LOGGER = Logger.getLogger(
		AddUserServlet.class.getName()); 

	/**
	 * Handles the HTTP GET
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
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO: Deny GET requests.
		//processRequest(request, response);
	}

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
		String username = request.getParameter("username");
		String pw = request.getParameter("password");
		String userRole = request.getParameter("user-role");
		String groupRole = request.getParameter("group-role");

		// TODO: Verify that the username/password/rolename does not already
		// exist/is valid!
		
		String sql = "INSERT INTO adjudicator.users (user_name, user_pass) " +
			"VALUES (?, ?)";
		Connection connection = null;
		PreparedStatement stmt = null;
  
		try {
		    connection = getConnection();
		    stmt = connection.prepareStatement(sql);
		    stmt.setString(1, username);
		    stmt.setString(2, pw);
		    stmt.executeUpdate();
		} catch (Exception e) {
			LOGGER.fine("Error when adding user to the database " + e);
			e.printStackTrace();
		} 
		
		// TODO: Add userRole to the user_roles database if one was specified.
		// The userRole must be an existing user_roles in the database table.  
		// If it is not deny the user.
		
		// TODO: Add groupRole to the roles database table if one was specified. 
		// The groupRole must be an existing roles in the database table.  
		// If it is not deny the user.
		
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