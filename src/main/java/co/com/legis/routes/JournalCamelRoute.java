package co.com.legis.routes;

import java.io.SerializablePermission;

import org.apache.camel.BeanInject;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JournalCamelRoute extends RouteBuilder {

    @BeanInject("loggerRef")
    private Logger LOG = LoggerFactory.getLogger(JournalCamelRoute.class);

    @Override
    public void configure() throws Exception {
        //@formatter:off
        onException(Exception.class)
            .handled(true)
            .bean("asyncBaseHalResponse", "GetHalRepresentation(${exchange}, 400, 'Bad Request', '${exception.class} - ${exception.message}','https://httpstatuses.com/400')")
            .setBody(simple("${property.responseBody}"))
            .removeHeaders("*")
            .unmarshal().json(JsonLibrary.Jackson)
        ;
		 
        from("direct:getJournal").routeId("GetJournalRoute").streamCaching()
        .description("Realiza la consulta de Journal por message id")
            .log(LoggingLevel.INFO, "Ingresó a la ruta")
            .choice()
                .when().simple("${headers.CamelHttpPath} contains 'message-id'")
                    .log(LoggingLevel.INFO, LOG, "Consulting JournalMessage by MessageID")
                    .bean("buildQuery", "getJournalByMessage(${exchange})")
                    .setProperty("TypeId").simple("message-id")
                    .setProperty("IdFilter").simple("${headers.messageId}")
                    .toD("jdbc:sqlDS")
                .endChoice()
                .when().simple("${headers.CamelHttpPath} contains 'journal-id'")
                    .log(LoggingLevel.INFO, LOG, "Consulting JournalMessage by JournalID")
                    .setProperty("TypeId").simple("journal-id")
                    .setProperty("IdFilter").simple("${headers.journalId}")
                    .toD("sql:{{journal.query.journal.id}}")
                .endChoice()
                .otherwise()
                .endChoice()
	    .end()
	    .log(LoggingLevel.INFO, LOG, "Total records found ${body.size}")
	    .choice()
	        .when().simple("${body.size} == 0")
	            .bean("asyncBaseHalResponse", "GetHalRepresentation(${exchange}, 404, 'Not Found', 'Records not found with the ${property.TypeId} : ${property.IdFilter}','https://httpstatuses.com/404')")
	            .setBody(simple("${property.responseBody}"))
	            .removeHeaders("*")
	            .unmarshal().json(JsonLibrary.Jackson)
	            .setHeader("Content-Type").simple("application/json; charset=UTF-8")
	        .endChoice()
	        .otherwise()
	            .to("direct:mappingJournalId")
	        .endChoice()
	    .end()
	    
	    .log(LoggingLevel.INFO, LOG, "End route journal")
	;
        
        from("direct:mappingJournalId").routeId("Route-mappingJournalId").streamCaching()
        .choice()
            .when().simple("${headers.CamelHttpPath} contains 'journal-id'")
                .setBody().simple("${body?[0]['ENTRY_PAYLOAD']}")
                .log(LoggingLevel.INFO, LOG, "Result BD=>${body}")
                .process("journalBodyProcessor")
                .removeHeaders("*", "Transfer-Encoding")
                .to("direct:validateDataType")
            .endChoice()
            .otherwise()
                .setProperty("listResponse").spel("#{new java.util.ArrayList()}")
                .log(LoggingLevel.INFO, LOG, "Mapping Results")
                .split().simple("${body}")
                    .process("journalProcessor")
                .end()
                .setBody().simple("${property.listResponse}")
                .log(LoggingLevel.INFO, LOG, "Mapping response")
                .process("mapperResponseProcessor")
                .unmarshal().json(JsonLibrary.Jackson)
            .endChoice()
        .end()
        ;
        from("direct:validateDataType").routeId("Route-validateDataType").streamCaching()
            
            .choice()
                .when().simple("${property.dataType} contains 'json'")
                    .unmarshal().json(JsonLibrary.Jackson)
                    .setHeader("Content-Type").simple("application/json; charset=UTF-8")
                .endChoice()
                .when().simple("${property.dataType} contains 'array'")
                    .setHeader("Content-Type").simple("application/json; charset=UTF-8")
                .endChoice()
                .when().simple("${property.dataType} contains 'html'")
                    .setHeader("Content-Type").simple("text/html")
                .endChoice()
                .when().simple("${property.dataType} contains 'string'")
                    .setHeader("Content-Type").simple("text/plain")
                .endChoice()
                .when().simple("${property.dataType} contains 'vacioNull'")
                    .setHeader("Content-Type").simple("text/plain")
                .endChoice()
                .otherwise()
                    .log(LoggingLevel.ERROR, LOG, "Error Data type ${property.dataType}")
                .endChoice()
            .end()
            .log(LoggingLevel.INFO, LOG, "Data type ${property.dataType}")
            .log(LoggingLevel.INFO, LOG, "End body=>${body}")                    
        ;
        //@formatter:on
    }

}
