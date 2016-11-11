package com.nova.reactor;

import com.nova.reactor.models.authentication.*;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.sql.SQLException;

@RestController
public class RequestRouter
{
	private static final String USER_AGENT = "Mozilla/5.0";
	
	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/auth/github/callback")
	public String loginUser(@RequestParam String code) throws SQLException, IOException
	{
		System.getenv().forEach((x, y) -> {
			System.out.println("x: " + x + ", y: " + y);
		});
		
		String clientId = System.getenv("REACTOR_CLIENT_ID");
		String clientSecret = System.getenv("REACTOR_CLIENT_SECRET");
		
		String url = "https://github.com/login/oauth/access_token";
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		
		//add request header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		
		String urlParameters = "client_id=" + clientId + "&client_secret=" + clientSecret + "&code=" + code;
		
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
		
		BufferedReader in = new BufferedReader(
			new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		System.out.println("Access codE: " + response);
		
		return "<html><body>success... closing window.<script>if (opener && \"\" != opener.location) { opener.handler(); } window.close();</script></body></html>";
		//return new GithubAuthResponse();
	}
}