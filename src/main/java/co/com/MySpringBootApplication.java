package co.com;

import javax.servlet.Servlet;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MySpringBootApplication {

    /**
     * A main method to start this application.
     */
    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApplication.class, args);
    }
    
    @Bean
    public ServletRegistrationBean<Servlet> servletRegistrationBean() {
        ServletRegistrationBean<Servlet> registration = new ServletRegistrationBean<>(new CamelHttpTransportServlet(), "/*");
        registration.setName("CamelServlet");
        return registration;
    }

}
