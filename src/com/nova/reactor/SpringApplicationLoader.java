package com.nova.reactor;

import com.nova.reactor.argumentresolvers.RequestArgumentResolver;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
@PropertySource("file:../application.properties")
@SpringBootApplication
public class SpringApplicationLoader extends WebMvcConfigurerAdapter
{
    public static void main(String[] args)
    {
        
        
        
        new AnnotationConfigApplicationContext(AppConfig.class);
        SpringApplicationBuilder b = new SpringApplicationBuilder(SpringApplicationLoader.class);
        
        
        
        b/*.web(false)*/.run(args);
        
        
        
    }
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new RequestArgumentResolver());
    }
	
    /*public void run(String ... args)
    {
		
		
        /*List<User> users = connection.jdbcTemplate.query("SELECT id, username, full_name, location FROM users.users WHERE lower(username)=lower(?) AND password=digest(?, 'sha256')",
                new Object[] { "braden", "admin" },
                (rs, row) -> new User(rs.getInt("id"), rs.getString("username"), rs.getString("full_name"), rs.getString("location")));
    
        if (users.size() > 0)
        {
            User user = users.get(0);
        
        }
    }*/
}