package com.nova.reactor;

import com.nova.reactor.models.authentication.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.sql.SQLException;

@RestController
public class RequestRouter
{
	private static final String USER_AGENT = "Mozilla/5.0";
	
	@Value("${app.clientId}")
	private String clientId;
	
	@Value("${app.clientSecret}")
	private String clientSecret;
	
	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/auth/github/callback")
	public String loginUser(@RequestParam String code) throws SQLException, IOException
	{
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
		
		return
			"<html>\n" +
			"<body>\n" +
			"	success... closing window.\n\n" +
			"	<script>\n" +
			"		var myVariable = 'hello??';\n" +
			"		if (opener && \"\" != opener.location) {\n" +
			"			opener.handler(myVariable);\n" +
			"		}\n" +
			"		window.close();\n" +
			"	</script>\n" +
			"</body>\n" +
			"</html>";
		//return new GithubAuthResponse();
	}
}