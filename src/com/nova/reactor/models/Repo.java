package com.nova.reactor.models;

public class Repo
{
	private Integer id;
	
	public Repo()
	{
		this(0);
	}
	
	public Repo(Integer id)
	{
		this.id = id;
	}
	
	public Integer getId()
	{
		return id;
	}
	
	public void setId(Integer id)
	{
		this.id = id;
	}
}