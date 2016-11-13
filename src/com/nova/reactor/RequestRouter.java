package com.nova.reactor;

import com.nova.reactor.models.Build;
import com.nova.reactor.models.BuildRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class RequestRouter
{
	private static final String USER_AGENT = "Mozilla/5.0";
	
	@Value("${app.clientId}")
	private String clientId;
	
	@Value("${app.clientSecret}")
	private String clientSecret;
	
	@Autowired BuildRepo buildRepo;
	
	@CrossOrigin(origins = "*")
	@RequestMapping(method = RequestMethod.GET, value = { "/builds/{repo}", "/builds/{repo}/{commit}" })
	public Build[] builds(/*@RequestHeader("Authorization") String auth, */@PathVariable String repo, @PathVariable Optional<String> commit)
	{
		if (commit.isPresent())
		{
			return buildRepo.getBuilds(repo, commit.get());
		}
		
		return buildRepo.getBuilds(repo);
	}
	
	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/auth/github/callback")
	public String githubOAuthCallback(@RequestParam String code, HttpServletResponse response) throws SQLException, IOException
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
		StringBuffer r = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
			r.append(inputLine);
		}
		in.close();
		
		System.out.println("Access codE: " + r);
		
		Map<String, String> values = splitQuery(r.toString());
		
		String accessToken = values.get("access_token");
		
		if (accessToken != null)
		{
			response.setHeader("access_token", accessToken);
			
			return
				"<html>\n" +
				"<body>\n" +
				"	success... you can close this window.\n\n" +
				"	<script>\n" +
				"	function createCookie(name,value,days) {\n" +
				"    if (days) {\n" +
				"        var date = new Date();\n" +
				"        date.setTime(date.getTime()+(days*24*60*60*1000));\n" +
				"        var expires = \"; expires=\"+date.toGMTString();\n" +
				"    }\n" +
				"    else var expires = \"\";\n" +
				"    document.cookie = name+\"=\"+value+expires+\"; path=/\";\n" +
				"}\n" +
				"\n" +
				"function readCookie(name) {\n" +
				"    var nameEQ = name + \"=\";\n" +
				"    var ca = document.cookie.split(';');\n" +
				"    for(var i=0;i < ca.length;i++) {\n" +
				"        var c = ca[i];\n" +
				"        while (c.charAt(0)==' ') c = c.substring(1,c.length);\n" +
				"        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);\n" +
				"    }\n" +
				"    return null;\n" +
				"}\n" +
				"\n" +
				"function eraseCookie(name) {\n" +
				"    createCookie(name,\"\",-1);\n" +
				"}" +
				"		createCookie('github_access_token', '" + accessToken + "', 10);\n" +
				"		window.close();\n" +
				"	</script>\n" +
				"</body>\n" +
				"</html>";
			//return new GithubAuthResponse();
		}
		
		return
			"<html>\n" +
			"<body>\n" +
			"	Failed.\n" +
			"</body>\n" +
			"</html>";
	}
	
	public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException
	{
		Map<String, String> query_pairs = new LinkedHashMap<>();
		String[] pairs = query.split("&");
		
		for (String pair : pairs)
		{
			int idx = pair.indexOf("=");
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		
		return query_pairs;
	}
}