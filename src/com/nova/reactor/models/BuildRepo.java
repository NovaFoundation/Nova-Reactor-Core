package com.nova.reactor.models;

import com.nova.reactor.util.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Repository
public class BuildRepo
{
	@Autowired
	JdbcTemplate connection;
	
	public Build[] getBuilds(String repo)
	{
		return getBuilds(repo, null);
	}
	
	public Build[] getBuilds(String repo, String commit)
	{
		final ArrayList<Build> builds = new ArrayList<>();
		
		ArrayList paramsList = new ArrayList();
		
		paramsList.add(repo.toLowerCase());
		
		String extra = "";
		
		if (commit != null)
		{
			paramsList.add(commit.toLowerCase());
			
			extra = "AND LOWER(commit)=?";
		}
		
		Object[] params = paramsList.toArray(new Object[0]);
		
		connection.query("SELECT * FROM repo_data.builds WHERE LOWER(repo)=? " + extra, params, (ResultSetExtractor)x -> {
			if (x.next())
			{
				ArrayList<HashMap<String, Object>> maps = DataUtils.resultSetToArrayList(x);
				
				for (int i = 0; i < maps.size(); i++)
				{
					builds.add(new Build(maps.get(i)));
				}
			}
			
			return null;
		});
		
		return builds.toArray(new Build[0]);
	}
	
	public Build build(String repo, String commit)idk
	{
		/*Map<String, Object> keys = DataUtils.keysFromUpdate(connection,
			"INSERT INTO users.users(username, full_name, password, email, phone_number) VALUES(?, ?, digest(digest(?, 'sha256'), 'sha256'), ?, ?)",
			request.getUsername(), request.getFullName(), request.getPassword(), request.getEmail(), request.getPhoneNumber());
		
		connection.update("INSERT INTO users.profiles(user_id) VALUES(?)", keys.get("id"));
		
		response.setSuccess(keys.size() > 0);
		
		return response;*/
		
		return null;
	}
}