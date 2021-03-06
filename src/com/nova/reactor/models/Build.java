package com.nova.reactor.models;

import java.util.HashMap;

public class Build
{
	private String language, commit, repo;
	
	private long startTime;
	private Long endTime;
	
	public Build()
	{
		
	}
	
	public Build(HashMap<String, Object> map)
	{
		this(map, "language", "repo", "commit", "start_time", "end_time");
	}
	
	public Build(HashMap<String, Object> map, String language, String repo, String commit, String startTime, String endTime)
	{
		this((String)map.get(language),
			(String)map.get(repo),
			(String)map.get(commit),
			(long)map.get(startTime),
			(Long)map.get(endTime));
	}
	
	public Build(String language, String repo, String commit, long startTime, Long endTime)
	{
		this.language = language;
		this.repo = repo;
		this.commit = commit;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public String getLanguage()
	{
		return language;
	}
	
	public void setLanguage(String language)
	{
		this.language = language;
	}
	
	public String getCommit()
	{
		return commit;
	}
	
	public void setCommit(String commit)
	{
		this.commit = commit;
	}
	
	public String getRepo()
	{
		return repo;
	}
	
	public void setRepo(String repo)
	{
		this.repo = repo;
	}
	
	public long getStartTime()
	{
		return startTime;
	}
	
	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}
	
	public Long getEndTime()
	{
		return endTime;
	}
	
	public void setEndTime(Long endTime)
	{
		this.endTime = endTime;
	}
}