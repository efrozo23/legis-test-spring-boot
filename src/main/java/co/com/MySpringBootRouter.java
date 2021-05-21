package co.com;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */

public class MySpringBootRouter extends RouteBuilder {

    @Override
    public void configure() {
        from("timer:hello?period=2000").routeId("hello")
            .transform().constant("Hello World")
            .filter(simple("${body} contains 'foo'"))
                .to("log:foo")
            .end()
            .to("stream:out");
    }

}
