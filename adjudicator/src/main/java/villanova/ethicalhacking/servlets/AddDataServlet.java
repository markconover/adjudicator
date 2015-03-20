package villanova.ethicalhacking.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.commons.httpclient.HttpStatus;
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

public class AddDataServlet extends HttpServlet {

	private static final long serialVersionUID = 957462709932298088L;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AddDataServlet.class);

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
			HttpServletResponse resp) throws ServletException, IOException {
		
		String data = req.getParameter("data");
		
		// Get the role/s to be assigned to the data
		@SuppressWarnings("unchecked")
		Enumeration<String> parameterNames = req.getParameterNames();

		String paramName = null;
		ArrayList<String> roles = new ArrayList<String>();
		while (parameterNames.hasMoreElements()) {
			paramName = parameterNames.nextElement();

			if (!paramName.equals("data")) {

				roles.add(req.getParameter(paramName));
			}
		}
		
		// Deny the add data request if no hierarchical role was assigned to 
		// the data or if the data is null/empty
		if (data == null || data.trim().equals("") 
			|| (!roles.contains("Head Coach") 
				&& !roles.contains("Assistant Coach") 
				&& !roles.contains("Player"))) {
			try {
				resp.sendError(HttpStatus.SC_BAD_REQUEST, "The data must not " +
					"be null/empty string and the data must be assigned at " + 
					"least one hierarchical role.");
			} catch (IOException e) {
				LOGGER.error("Unable to send HTTP response type " + 
					HttpStatus.SC_BAD_REQUEST + ".\n" + e);
			} catch (IllegalStateException e) {
				LOGGER.error("Unable to send HTTP response type " + 
					HttpStatus.SC_BAD_REQUEST + ".\n" + e);
			}
		} else {
		
			// TODO: Verify the roles exist in the database.
			
			boolean dataAdded = false;
	
			String sql = "INSERT INTO adjudicator.data_table (data) " +
				"VALUES (?)";
			Connection connection = null;
			PreparedStatement stmt = null;
			int rowID = 0;
			ResultSet rs = null;
			String roleID = null;
			String stmtText = null;
			try {
				connection = getConnection();
				String[] returnColumnNames = {"id"};
				stmt = connection.prepareStatement(sql,returnColumnNames);
				stmt.setString(1, data);
				
				LOGGER.info("Adding data (" + data + ") to the database.");
				
				// Log the statement being executed
			    stmtText = stmt.toString();
			    sql = stmtText.substring(stmtText.indexOf( ": " 
			        ) + 2);
				LOGGER.debug("Executing the following sql to add data (" + 
					data + ") to the database:\n" + sql);
				
				stmt.executeUpdate();
				rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					rowID = rs.getInt(1);
				}
			} catch (LoginException e) {
				LOGGER.error("Unable to connect to database.\n" + e);
			} catch (SQLException e) {
				LOGGER.error("Unable to add data to the database.\n" + e);
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
			
			// Add "read down" privileges
			if (roles.contains("Player")) {
				// "Head Coach" role
				
				if (!roles.contains("Head Coach")) {
					roles.add("Head Coach");
				}
				
				if (!roles.contains("Assistant Coach")) {
					roles.add("Assistant Coach");
				}
			}			
			if (roles.contains("Assistant Coach") 
				&& !roles.contains("Head Coach")) {
				roles.add("Head Coach");
			}
	
			for (String role : roles) {
				// Get the role_id for each role and add the role_id, data_id
				// junction to the "data_roles" junction table.
				sql = "SELECT * FROM adjudicator.roles WHERE roles.role_name=?";
				try {
					connection = getConnection();
					stmt = connection.prepareStatement(sql);
					stmt.setString(1, role);
					
					LOGGER.info("Getting the id for role (" + role + 
						") from the database.");
					
					// Log the statement being executed
				    stmtText = stmt.toString();
				    sql = stmtText.substring(stmtText.indexOf( ": " 
				        ) + 2);
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
				
				sql = "INSERT INTO adjudicator.data_roles (data_id, role_id)" +
					" VALUES (?, ?)";
				try {
					connection = getConnection();
					stmt = connection.prepareStatement(sql);
					stmt.setString(1, rowID + "");
					stmt.setString(2, roleID);
					
					LOGGER.info("Adding junction to the database for data id (" + 
						rowID + "), role id (" + roleID + ").");
					
					// Log the statement being executed
				    stmtText = stmt.toString();
				    sql = stmtText.substring(stmtText.indexOf( ": " 
				        ) + 2);
					LOGGER.debug("Executing the following sql to add the " + 
						"junction for data id (" + rowID + "), role id (" + roleID + 
						") to the database:\n" + sql);
					
					stmt.executeUpdate();
					dataAdded = true;
				} catch (LoginException e) {
					LOGGER.error("Unable to connect to database.\n" + e);
				} 
				catch (SQLException e) {
					LOGGER.error("Unable to add entry to data_roles database.\n" + 
						e);
				} finally {				
					if (dataAdded == false) {
						// Return successful response message
						resp.setContentType("text/html;charset=UTF-8");
						PrintWriter out = null;
						try {
							out = resp.getWriter();
						} catch (IOException e) {
							LOGGER.error("Unable to get a PrintWriter to return " +
								"the add data response.\n" + e);
						}
						try {
							out.println("<html>");
							out.println("<head>");
							out.println("<title>Servlet</title>");
							out.println("</head>");
							out.println("<body>");
							out.println("<h1>The following data was NOT added to " + 
								"the database due to server problems:<br />" + 
								data + "</h1>");
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
				LOGGER.error("Unable to get a PrintWriter to return the add data " +
					"response.\n" + e);
			}
			try {
				out.println("<html>");
				out.println("<head>");
				out.println("<title>Servlet</title>");
				out.println("</head>");
				out.println("<body>");
				out.println("<h1>The following data was successfully added to " + 
					"the database:<br />" + data + "</h1>");
				out.println("<br/>");
				out.println("</body>");
				out.println("</html>");
			} finally {
				out.close();
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