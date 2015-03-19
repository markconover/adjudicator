package villanova.ethicalhacking.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

public class AddRoleServlet extends HttpServlet {

	private static final long serialVersionUID = 957462709932298088L;

	private static final Logger LOGGER = LoggerFactory
		.getLogger(AddRoleServlet.class);

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
		String role = req.getParameter("role");

		// TODO: Verify that the group role does not already
		// exist/is valid!

		String sql = "INSERT INTO adjudicator.roles (role_name) VALUES (?)";
		Connection connection = null;
		PreparedStatement stmt = null;
		boolean roleAdded = false;

		try {
			connection = getConnection();
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, role);

			LOGGER.info("Adding role (" + role + ") to the database.");
			LOGGER.debug("Executing the following sql to add role (" + role
					+ ") to the database:\n" + sql);

			stmt.executeUpdate();
			roleAdded = true;
		} catch (LoginException e) {
			LOGGER.error("Error when adding role to the database.\n" + e);
		} catch (SQLException e) {
			LOGGER.error("Error when adding role to the database.\n" + e);
		} finally {			
			if (roleAdded == false) {
				// Return successful response message
				resp.setContentType("text/html;charset=UTF-8");
				PrintWriter out = null;
				try {
					out = resp.getWriter();
				} catch (IOException e) {
					LOGGER.error("Unable to get a PrintWriter to return the " + 
						"add role response.\n" + e);
				}
				try {
					out.println("<html>");
					out.println("<head>");
					out.println("<title>Servlet</title>");
					out.println("</head>");
					out.println("<body>");
					out.println("<h1>The following role was NOT added to " + 
						"the database due to server problems:<br />" + role + 
						"</h1>");
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

		// Return successful response message
		resp.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		try {
			out = resp.getWriter();
		} catch (IOException e) {
			LOGGER.error("Unable to get a PrintWriter to return the add role "
					+ "response.\n" + e);
		}
		try {
			out.println("<html>");
			out.println("<head>");
			out.println("<title>Servlet</title>");
			out.println("</head>");
			out.println("<body>");
			out.println("<h1>The following role was successfully added to "
					+ "the database:<br />" + role + "</h1>");
			out.println("<br/>");
			out.println("</body>");
			out.println("</html>");
		} finally {
			out.close();
		}
	}

	/**
	 * Returns JDBC connection
	 * 
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
			ds = (DataSource) ctx.lookup(jdbcDataSource);
		} catch (NamingException e) {
			LOGGER.error("Unable to lookup the datasource name: "
					+ jdbcDataSource + "\n" + e);
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