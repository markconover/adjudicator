package villanova.ethicalhacking;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

public class ContextListener implements ServletContextListener {
	
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
//		// Get DataSource
//		Context ctx = null;
//		try {
//			ctx = new InitialContext();
//		} catch (NamingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		DataSource ds = null;
//		try {
//			ds = (DataSource)ctx.lookup("java:comp/env/jdbc/mydatabase");
//		} catch (NamingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		// Get Connection and Statement
//		Connection c = null;
//		try {
//			c = ds.getConnection();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
