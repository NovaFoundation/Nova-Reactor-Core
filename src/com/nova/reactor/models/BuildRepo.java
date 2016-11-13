package com.nova.reactor.models;

import com.nova.reactor.util.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;

@Repository
public class BuildRepo
{
	@Autowired
	JdbcTemplate connection;
	
	public Build[] getBuilds(String repo, String commit)
	{
		final ArrayList<Build> builds = new ArrayList<>();
		
		Object[] params = new Object[] {
			repo.toLowerCase(),
			commit.toLowerCase()
		};
		
		connection.query("SELECT * FROM repo_data.builds WHERE LOWER(repo)=? AND LOWER(commit)=?", params, (ResultSetExtractor)x -> {
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
	
	public Build[] getBuilds(String repo)
	{
		final ArrayList<Build> builds = new ArrayList<>();
		
		Object[] params = new Object[] {
			repo.toLowerCase()
		};
		
		connection.query("SELECT * FROM repo_data.builds WHERE LOWER(repo)=?", params, (ResultSetExtractor)x -> {
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
}