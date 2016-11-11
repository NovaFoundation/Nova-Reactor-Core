package com.nova.reactor.models.authentication;

import java.sql.SQLException;

public class GithubAuthResponse
{
	public static GithubAuthResponse getLoginResponse() throws SQLException
	{
		
		return new GithubAuthResponse();
	}
}