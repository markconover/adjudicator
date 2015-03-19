<%@page language="java" contentType="text/html"%>
<%@page import="java.sql.Connection" %>
<%@page import="java.sql.SQLException" %>
<%@page import="java.sql.Statement" %>
<%@page import="java.sql.ResultSet" %>
<%@page import="java.sql.ResultSetMetaData" %>
<%@page import="javax.naming.Context"%>
<%@page import="javax.naming.InitialContext"%>
<%@page import="javax.naming.NamingException"%>
<%@page import="javax.sql.DataSource" %>
<%@page import="org.slf4j.Logger" %>
<%@page import="org.slf4j.LoggerFactory" %>
<html>
<head>
    <title>Add Data</title>
    
    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="css/bootstrap/bootstrap.min.css">

    <!-- Optional theme -->
    <link rel="stylesheet" href="css/bootstrap/bootstrap-theme.min.css">

    <script src="js/jquery/jquery-2.1.3.min.js"></script>
    
    <!-- Latest compiled and minified JavaScript -->
    <script src="js/bootstrap/bootstrap.min.js"></script>
</head>
<body>
<% 
    final Logger LOGGER = LoggerFactory.getLogger(
        "villanova.ethicalhacking.adddata-jsp");
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

    Statement stmt = conn.createStatement();
    String sql = "SELECT role_name FROM adjudicator.roles";
        
    LOGGER.info("Getting all available roles from the database.");
    LOGGER.debug("Executing the following sql query:\n" + sql);             
 
    ResultSet rs = stmt.executeQuery(sql);
    ResultSetMetaData resMetaData = rs.getMetaData();
    String role = null;
 %>
    <form id="add-data-form" action="adddata" method="post">
        <fieldset>
            <h1>Add Data</h1>
            <div>Data</div>
            <div>
                <input type="text" name="data"/>
            </div>    
            <br />     
            <div>Assign Role/s to the Data:</div>
            <% while (rs.next()) { 
                role = rs.getString(1);
            %>
            <div class="checkbox">
                <label>
                    <input name="<%= role %>" type="checkbox" value="<%= role %>"><%= role %>
                </label>
            </div>
            <%}%>
            <br />
            <input type="submit" value="Create Data" />
        </fieldset>
    </form>
<%
try {
    conn.close();
} catch (SQLException e) {
    LOGGER.error("Error when closing connection.\n" + e);
}
%>
</body>
</html>