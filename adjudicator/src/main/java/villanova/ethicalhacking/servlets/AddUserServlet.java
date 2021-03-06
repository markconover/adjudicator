package villanova.ethicalhacking.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;

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

import org.apache.commons.httpclient.HttpStatus;

public class AddUserServlet extends HttpServlet {

	private static final long serialVersionUID = -2655972628571555177L;

	private static final Logger LOGGER = LoggerFactory.getLogger(
		AddUserServlet.class);

	/**
	 * Handles the HTTP GET
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
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Deny GET requests.
		resp.sendError(HttpStatus.SC_BAD_REQUEST);
	}

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
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
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
			HttpServletResponse resp) {
		String username = req.getParameter("username");
		String pw = req.getParameter("password");

		// Get the role/s to be assigned to the user
		@SuppressWarnings("unchecked")
		Enumeration<String> parameterNames = req.getParameterNames();

		String paramName = null;
		ArrayList<String> userRoles = new ArrayList<String>();
		while (parameterNames.hasMoreElements()) {
			paramName = parameterNames.nextElement();

			if (!paramName.equals("username") 
				&& !paramName.equals("password")) {

				userRoles.add(req.getParameter(paramName));
			}
		}

		// TODO: Validate the following:
		// * Deny if user already exists.
		// * Verify password is not null or empty string.
		// * Verify the user is assigned only one of hierarchical roles.
		// * Verify the roles exist in the database.
		// * Verify the user is assigned to only one team.
		if (username == null || pw == null || userRoles.size() < 1) {
			try {
				resp.sendError(HttpStatus.SC_BAD_REQUEST);
			} catch (IOException e) {
				LOGGER.error("Unable to send HTTP response type " + 
					HttpStatus.SC_BAD_REQUEST + ".\n" + e);
			}
		}
		
		boolean userAdded = false;

		String sql = "INSERT INTO adjudicator.users (user_name, user_pass) " +
			"VALUES (?, ?)";
		Connection connection = null;
		PreparedStatement stmt = null;
		int rowID = 0;
		ResultSet rs = null;
		String roleID = null;
		try {
			connection = getConnection();
			String[] returnColumnNames = {"id"};
			stmt = connection.prepareStatement(sql,returnColumnNames);
			stmt.setString(1, username);
			stmt.setString(2, pw);
			
			LOGGER.info("Adding user (" + username + ") to the database.");
			LOGGER.debug("Executing the following sql to add user (" + 
				username + ") to the database:\n" + sql);
			
			stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				rowID = rs.getInt(1);
			}
		} catch (LoginException e) {
			LOGGER.error("Unable to connect to database.\n" + e);
		} catch (SQLException e) {
			LOGGER.error("Unable to add user to the database.\n" + e);
		} finally {
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

		for (String role : userRoles) {
			// Get the role_id for each role and add the role_id, user_id
			// junction to the "user_roles" junction table.
			sql = "SELECT * FROM adjudicator.roles WHERE roles.role_name=?";
			try {
				connection = getConnection();
				stmt = connection.prepareStatement(sql);
				stmt.setString(1, role);
				
				LOGGER.info("Getting the id for role (" + role + 
					") from the database.");
				LOGGER.debug("Executing the following sql query to get the " + 
					"id for role (" + role + ") from the database.\n" + sql);
				
				rs = stmt.executeQuery();
				rs.next();
				roleID = rs.getString("id");
			} catch (LoginException e) {
				LOGGER.error("Unable to connect to database.\n" + e);
			} catch (SQLException e) {
				LOGGER.error("Unable to select adjudicator roles from the "
						+ "database.\n" + e);
			} finally {
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
			
			sql = "INSERT INTO adjudicator.user_roles (user_id, role_id)" +
				" VALUES (?, ?)";
			try {
				connection = getConnection();
				stmt = connection.prepareStatement(sql);
				stmt.setString(1, rowID + "");
				stmt.setString(2, roleID);
				
				LOGGER.info("Adding junction to the database for user id (" + 
					rowID + "), role id (" + roleID + ").");
				LOGGER.debug("Executing the following sql to add the " + 
					"junction for user id (" + rowID + "), role id (" + roleID + 
					") to the database:\n" + sql);
				
				stmt.executeUpdate();
				userAdded = true;
			} catch (LoginException e) {
				LOGGER.error("Unable to connect to database.\n" + e);
			} 
			catch (SQLException e) {
				LOGGER.error("Unable to add entry to user_roles database.\n" + 
					e);
			} finally {				
				if (userAdded == false) {
					// Return successful response message
					resp.setContentType("text/html;charset=UTF-8");
					PrintWriter out = null;
					try {
						out = resp.getWriter();
					} catch (IOException e) {
						LOGGER.error("Unable to get a PrintWriter to return " +
							"the add user response.\n" + e);
					}
					try {
						out.println("<html>");
						out.println("<head>");
						out.println("<title>Servlet</title>");
						out.println("</head>");
						out.println("<body>");
						out.println("<h1>The following user was NOT added to " + 
							"the database due to server problems:<br />" + 
							username + "</h1>");
						out.println("<br/>");
						out.println("</body>");
						out.println("</html>");
					} finally {
						out.close();
					}				
				}
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

		// Return successful response message
		resp.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		try {
			out = resp.getWriter();
		} catch (IOException e) {
			LOGGER.error("Unable to get a PrintWriter to return the add user " +
				"response.\n" + e);
		}
		try {
			out.println("<html>");
			out.println("<head>");
			out.println("<title>Servlet</title>");
			out.println("</head>");
			out.println("<body>");
			out.println("<h1>The following user was successfully added to " + 
				"the database:<br />" + username + "</h1>");
			out.println("<br/>");
			out.println("</body>");
			out.println("</html>");
		} finally {
			out.close();
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