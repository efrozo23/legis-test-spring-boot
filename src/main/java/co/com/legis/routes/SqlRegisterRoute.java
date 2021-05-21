package co.com.legis.routes;

import org.apache.camel.BeanInject;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlRegisterRoute extends RouteBuilder {

    @BeanInject("loggerRef")
    private Logger LOG = LoggerFactory.getLogger(SqlRegisterRoute.class);

    @Override
    public void configure() throws Exception {
        //@formatter:off
        onException(Exception.class)
            .handled(true)
            .log(LoggingLevel.ERROR,LOG,"Error Generic.....")
            .log(LoggingLevel.ERROR,LOG,"Headers in Exception:\n${headers}")
            .log(LoggingLevel.ERROR,LOG,"Exception Stack: ${exception.stacktrace}")
            .bean("asyncBaseHalResponse", "GetHalRepresentation(${exchange}, 400, 'Bad Request', '${exception.class} - ${exception.message}','https://httpstatuses.com/400')")
            .setBody(simple("${property.responseBody}"))
            .removeHeaders("*")
            .unmarshal().json(JsonLibrary.Jackson)
        ;
        
        from("direct:RegisterSqlTransport").routeId("Route-RegisterSqlTransport").streamCaching()
            .log(LoggingLevel.INFO,LOG,": : :Start Route-RegisterSqlTransport ${date:now:HH:mm:ss}: : :")
            .log(LoggingLevel.DEBUG,LOG,"Input headers in Route-RegisterSqlTransport:\n${headers}")
            .setProperty("bodyRestBase", simple("${body}"))
            .choice()
                .when(simple("${body} is 'co.com.legis.request_models.transport.document.sap.DocumentTransport'"))
                    .marshal().json(JsonLibrary.Jackson)
                    .setProperty("deliveryId").jsonpath("$.delivery_id")
                    .setProperty("transportCompanyName").jsonpath("$.transport_company_name")
                    .to("direct-vm:insertTransportDocument")
                .endChoice()
                .when(simple("${body} is 'co.com.legis.request_models.transport.shipment.Shipment'"))
                    .marshal().json(JsonLibrary.Jackson)
                    .setProperty("trackingNumber").jsonpath("$.tracking_number")
                    .setProperty("deliveryId").jsonpath("$.delivery_id")
                    .to("direct-vm:updateTracking")
                .endChoice()
                .when(simple("${body} is 'co.com.legis.request_models.transport.tracking.Tracking'"))
                    .marshal().json(JsonLibrary.Jackson)
                    .setProperty("status").jsonpath("$.status_code")
                    .setProperty("eventDescription").jsonpath("$.status_details")
                    .setProperty("trackingNumber").jsonpath("$.tracking_number")
                    .to("direct-vm:updateEvent")
                .endChoice()
            .end()
            .setBody().simple("${property.bodyRestBase}")
            .to("direct-vm:AsyncResponse")
        ;
        //@formatter:on
    }

}
