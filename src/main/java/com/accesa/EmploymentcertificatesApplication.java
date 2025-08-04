package com.accesa;


import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = { "com.accesa", "com.accesa.controller", "com.accesa.service", "com.accesa.model", "com.accesa.exception" })
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class EmploymentcertificatesApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmploymentcertificatesApplication.class, args);
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(EmploymentcertificatesApplication.class);
	}

}
