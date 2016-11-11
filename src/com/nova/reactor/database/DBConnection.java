package com.nova.reactor.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class used to create and maintain a connection to a database.
 * 
 * Date: 2/24/2014 6:00:00 PM
 */
public class DBConnection
{
	private Connection									connection;
	
	private static boolean								driverLoaded;
	
	private static final HashMap<String, DBConnection>	connections;
	
	private static final String							DATABASE_DRIVER_NAME = "org.postgresql.Driver";//"com.mysql.jdbc.Driver";//"org.mariadb.jdbc.Driver";
	
	private static final String DATABASE_IP = "52.11.163.243";
//	private static final String DATABASE_IP = "localhost";
	
	// Initialize the static data.
	static
	{
		connections = new HashMap<String, DBConnection>();
	}
	
	/**
	 * Create a Database Connection using the specified username and
	 * password.
	 * 
	 * @param username The username to the database.
	 * @param password The password to the database.
	 * @throws ClassNotFoundException Thrown if the database driver could
	 * 		not be found.
	 * @throws SQLException Thrown if a connection to the database could
	 * 		not be made.
	 */
	/*public DBConnection(String username, String password) throws ClassNotFoundException, SQLException
	{
		this("", username, password);
	}*/
	
	/**
	 * Create a Database Connection using the specified username and
	 * password.
	 * 
	 * @param databaseName The database to form a connection with.
	 * @param username The username to the database.
	 * @param password The password to the database.
	 * @throws ClassNotFoundException Thrown if the database driver could
	 * 		not be found.
	 * @throws SQLException Thrown if a connection to the database could
	 * 		not be made.
	 */
	public DBConnection(String databaseName, String username, String password) throws ClassNotFoundException, SQLException
	{
		loadDriver();
		
		connection = DriverManager.getConnection("jdbc:postgresql://" + DATABASE_IP + "/" + databaseName, username, password);
	}
	
	/**
	 * Execute a query on the database connection and return the result.
	 * 
	 * @param sql The SQL query to execute.
	 * @return An object containing the results of the query.
	 * @throws SQLException Thrown if an error was thrown during the
	 * 		execution of the query to the database.
	 */
	public ResultSet query(String sql) throws SQLException
	{
		Statement s = connection.createStatement();
		
		return s.executeQuery(sql);
	}
	
	/**
	 * Execute a query on the database connection and return the result.
	 * 
	 * @param sql The SQL query to execute.
	 * @return An object containing the results of the query.
	 * @throws SQLException Thrown if an error was thrown during the
	 * 		execution of the query to the database.
	 */
	public ResultSet query(String sql, Object[] queryParameters) throws SQLException
	{
		PreparedStatement s = connection.prepareStatement(sql);
		
		for (int i = 0; i < queryParameters.length; i++)
		{
			s.setObject(i + 1, queryParameters[i]);
		}
		
		return s.executeQuery();
	}
	
	public Statement createStatement() throws SQLException
	{
		return connection.createStatement();
	}
	
	public ResultSet performQuery(String sql) throws SQLException
	{
		return performQuery(sql, new Object[0]);
	}

	public ResultSet performQuery(String sql, Object[] parameters) throws SQLException
	{
		PreparedStatement s = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		
		for (int i = 0; i < parameters.length; i++)
		{
			s.setObject(i + 1, parameters[i]);
		}
		
		s.execute();
		
		return s.getGeneratedKeys();
	}
	
	public PreparedStatement prepareStatement(String sql) throws SQLException
	{
		PreparedStatement statement = connection.prepareStatement(sql);
		
		return statement;
	}
	
	/**
	 * Load the Database driver, if it has not already been loaded.
	 * 
	 * @throws ClassNotFoundException Thrown if the database driver was
	 * 		not found.
	 */
	private static void loadDriver() throws ClassNotFoundException
	{
		if (driverLoaded)
		{
			return;
		}
		
		Class.forName(DATABASE_DRIVER_NAME);
		
		driverLoaded = true;
	}
	
	/**
	 * Get the landing page's Database connection of the server.
	 * 
	 * @return The default Database connection.
	 */
	public static DBConnection getLandingDataConnection()
	{
		String data[] = null;
		
		try
		{
			data = getConnectionData();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		
		if (data == null)
		{
			return null;
		}
		
		String username = data[0];
		String password = data[1];
		String database = data[2];
		
		return getConnection(database, username, password);
	}
	
	public static DBConnection getWolfpaqConnection()
	{
		return getDataConnection("wolfpaq");
	}
	
	public static DBConnection getExistingConnection(String database)
	{
		DBConnection connection = connections.get(database);
		
		try
		{
			ResultSet results = connection.query("select 1 as connection_successful");
			
			if (results.next() && results.getBoolean(1))
			{
				return connection;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return getDataConnection(database, true);
	}
	
	public static DBConnection getDataConnection(String database)
	{
		return getDataConnection(database, false);
	}
	
	public static DBConnection getDataConnection(String database, boolean reconnect)
	{
		if (!reconnect && connections.containsKey(database))
		{
			return getExistingConnection(database);
		}
		
		String data[] = null;
		
		try
		{
			data = getConnectionData();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		
		if (data == null)
		{
			return null;
		}
		
		String username = data[0];
		String password = data[1];
		
		return getConnection(database, username, password);
	}
	
	/**
	 * Get a connection to the specified database. The connection also
	 * needs the username and password for the mysql users to connect.
	 * 
	 * @param database The name of the database to connect to.
	 * @param username The username for the mysql users.
	 * @param password The password for the mysql users.
	 * @return The DBConnection instance for the specified arguments.
	 */
	public static DBConnection getConnection(String database, String username, String password)
	{
		try
		{
			DBConnection connection = new DBConnection(database, username, password);
			
			connections.put(database, connection);
			
			return connection;
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get the Authority database connection.
	 * 
	 * @return The DBConnection instance for the Authority database.
	 */
	public static DBConnection getAuthorityConnection()
	{
		String data[] = null;
		
		try
		{
			data = getConnectionData();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		
		if (data == null)
		{
			return null;
		}
		
		DBConnection authority = getConnection("authority", data[0], data[1]);
		
		return authority;
	}
	
	/**
	 * Get the default database connection credentials.
	 * 
	 * @return The credentials used to connect to the default database.
	 */
	private static String[] getConnectionData() throws IOException
	{
		return null;//getConnectionData(Location.PROJECT_ROOT + "/serverdata.xml");
	}
	
	/**
	 * Get the default database connection credentials.
	 * 
	 * @param file The file to get the credentials from.
	 * @return The credentials used to connect to the default database.
	 */
	public static String[] getConnectionData(String file) throws IOException
	{
		String data[] = new String[3];
		
		try
		{
			File xmlFile = new File(file);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
			
			NodeList dbNode = doc.getElementsByTagName("database");
			Element  db     = (Element)dbNode.item(0);
			
			data[0] = getValue("username", db);
			data[1] = getValue("password", db);
			data[2] = getValue("landingDatabase", db);
		}
		catch (ParserConfigurationException e)
		{
			throw new RuntimeException(e);
		}
		catch (SAXException e)
		{
			throw new RuntimeException(e);
		}
		
		return data;
	}
	
	/**
	 * Get the value of the value of a specified tag name.
	 * 
	 * @param tag The name of the tag to get the value from.
	 * @param element The parent Element to search for the tags in.
	 * @return The value of the specified tag.
	 */
	private static String getValue(String tag, Element element)
	{
		NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
		Node     node  = nodes.item(0);
		
		return node.getNodeValue();
	}
    
    /**
     * Get the number of rows in a ResultSet Object.
     * 
     * @param set The ResultSet to get the number of rows from.
     * @return The number of rows in the ResultSet Object.
     * @throws SQLException Thrown if there is a problem accessing the
     * 		number of rows.
     */
    public static int getNumRows(ResultSet set) throws SQLException
    {
    	int prev = set.getRow();
    	
    	set.last();
    	
    	int rows = set.getRow();
    	
    	set.absolute(prev);
    	
    	return rows;
    }
}
