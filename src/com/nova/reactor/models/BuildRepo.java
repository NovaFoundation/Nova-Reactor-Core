package com.nova.reactor.models;

import com.nova.reactor.util.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Repository
public class BuildRepo
{
	@Autowired
	JdbcTemplate connection;
	
	public Build[] getBuilds(String host, String username, String repo, String commit)
	{
		final ArrayList<Build> builds = new ArrayList<>();
		
		ArrayList paramsList = new ArrayList();
		
		paramsList.add((host + "/" + username + "/" + repo).toLowerCase());
		
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
	
	public Build build(String host, String username, String repo, String commit) throws IOException, InterruptedException
	{
		File userFolder = new File(System.getProperty("user.home") + "/repos/" + host + "/" + username);
		
		userFolder.mkdirs();
		
		String ext = ".com";
		String accessToken = null;
		String password = accessToken != null ? ":" + accessToken : "";
		
		String url = "https://" + username + password + "@" + host + ext + "/" + username + "/" + repo;
		
		System.out.println("url: " + url);
		
		ProcessBuilder builder = new ProcessBuilder("git", "clone", url);
		builder.directory(userFolder);
		builder.redirectErrorStream(true);
		
		Process p = builder.start();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
		String line = null;
		
		while ((line = reader.readLine()) != null)
		{
			System.out.println(line);
		}
		
		int value = p.waitFor();
		
		reader.close();
		
		System.out.println("Received value " + value);
		
		/*Map<String, Object> keys = DataUtils.keysFromUpdate(connection,
			"INSERT INTO users.users(username, full_name, password, email, phone_number) VALUES(?, ?, digest(digest(?, 'sha256'), 'sha256'), ?, ?)",
			request.getUsername(), request.getFullName(), request.getPassword(), request.getEmail(), request.getPhoneNumber());
		
		connection.update("INSERT INTO users.profiles(user_id) VALUES(?)", keys.get("id"));
		
		response.setSuccess(keys.size() > 0);
		
		return response;*/
		
		return null;
	}
}