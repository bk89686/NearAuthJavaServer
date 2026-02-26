package com.humansarehuman.blue2factor.authentication;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		new DataAccess().addLog("resourceHandler");
		registry.addResourceHandler("{filename:\\.well-known}/**")
				.addResourceLocations("classpath:/resources/well-known/");
		registry.addResourceHandler("{filename:\\well-known}/**")
				.addResourceLocations("classpath:/resources/well-known/");
		registry.addResourceHandler("/.well-known/assetLinks.json")
				.addResourceLocations("classpath:/resources/well-known/assetlinks.json");
	}

//	@Override
//	public void addViewControllers(ViewControllerRegistry registry) {
//		registry.addViewController("/.well-known/apple-app-site-association")
//				.setViewName("forward:/resources/well-known/apple-app-site-association.json");
//	}
}