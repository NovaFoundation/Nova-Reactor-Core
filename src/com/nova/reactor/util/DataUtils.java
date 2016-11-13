package com.nova.reactor.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import static java.util.Arrays.stream;

public class DataUtils
{
	public static ArrayList<HashMap<String, Object>> resultSetToArrayList(ResultSet rs) throws SQLException
	{
		return resultSetToArrayList(new ArrayList<>(), rs);
	}
	
	public static ArrayList<HashMap<String, Object>> resultSetToArrayList(ArrayList<HashMap<String, Object>> list, ResultSet rs) throws SQLException
	{
		ResultSetMetaData md = rs.getMetaData();
		
		int columns = md.getColumnCount();
		
		do
		{
			HashMap<String, Object> row = new HashMap<String, Object>(columns);
			
			for (int i = 1; i <= columns; ++i)
			{
				row.put(md.getColumnLabel(i), rs.getObject(i));
			}
			
			list.add(row);
		}
		while (rs.next());
		
		return list;
	}
	
	public static HashMap<String, Object> jsonObjectToHashMap(JSONObject obj)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		if (obj == null)
		{
			return map;
		}
		
		for (Object key : obj.keySet())
		{
			Object value = obj.get(key);
			
			if (value instanceof JSONObject)
			{
				map.put((String)key, jsonObjectToHashMap((JSONObject)value));
			}
			else
			{
				map.put((String)key, value);
			}
		}
		
		return map;
	}
	
	public static HashMap<String, Object> mapFromUpdate(JdbcTemplate connection, String query, Object ... args)
	{
		Map<String, Object> keys = keysFromUpdate(connection, query, args);
		
		if (keys != null)
		{
			return new HashMap<>(keys);
		}
		
		return null;
	}
	
	public static Map<String, Object> keysFromUpdate(JdbcTemplate connection, String query, Object ... args)
	{
		KeyHolder key = new GeneratedKeyHolder();
		
		if (connection.update(con -> {
			PreparedStatement statement = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			
			final int[] i = { 1 };
			
			stream(args).forEach(UntilException.rethrowConsumer(param -> statement.setObject(i[0]++, param)));
			
			return statement;
		}, key) > 0)
		{
			return key.getKeys();
		}
		
		return null;
	}
}