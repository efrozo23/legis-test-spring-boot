package co.com.legis.routes;

import javax.validation.ValidationException;


import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import co.com.legis.constant.RestBaseConstant;

@Component
public class RouteCollect extends RouteBuilder {
	
	public final String MSG_ENTITY = "MSG_ENTITY";
	public final String MSG_ORIGIN = "MSG_ORIGIN";
	public final String JMS_TYPE = "JMSType";
	public final String TARGET_ID_QUEUE = "TARGET_ID_QUEUE";
	public final String NUMBER_SILENIO_ORDER = "number_silenio_order";

	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		
		
		from("direct-vm:collect-response-sync").routeId("COLLECT-SYNC-RESPONSE").streamCaching()
	    .doTry()
	        .log(LoggingLevel.INFO,"Iniciando validacion de la entrada ${headers}") 
	        .to("bean-validator://beanValidation")		
	        .toD("direct-vm:getMsgId")
	        .marshal().json(JsonLibrary.Jackson)
	        .setHeader(MSG_ENTITY, simple("${properties:sc.report.msg.entity.drupal.collect}"))
    	    .setHeader(JMS_TYPE, simple("${properties:sc.report.msg.entity.drupal.collect}"))
    	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
    	    .setHeader(TARGET_ID_QUEUE, simple("${properties:collect.legis.msg.entity.queue}"))
	        .setProperty(MSG_ENTITY, simple(" ${header.MSG_ENTITY}"))
            .setProperty(JMS_TYPE, simple(" ${header.JMS_TYPE}"))
            .setProperty(MSG_ORIGIN, simple(" ${header.MSG_ORIGIN}"))
	        .setProperty("status_report", constant("processed_notification"))
	        .setHeader("status_report", constant("processed_notification"))
	       
	        .log(LoggingLevel.INFO,"Mesagge id generated: ${header.ORIGIN_MSG_ID}") 
	        
	        //TODO GUARDAR LA ENTRADA
	       // .to("bean:bodyUtil?method=getStringJson(${exchange})")
	        .log(LoggingLevel.INFO,"Convierte body a string: ${body}") 
	        
	        .convertBodyTo(String.class)
	        .setProperty("strBody", simple("${body}"))
	        .log(LoggingLevel.INFO,"input Body:  ${exchangeexchangeProperty.strBody}") 
	        .log(LoggingLevel.INFO,"salida de message-id${header.MSG_ID}")
	        .setProperty(RestBaseConstant.PROPERTY_MESSAGE_ID,simple("${header.MSG_ID}"))
	        .setProperty(RestBaseConstant.PROPERTY_INCOMING,constant(1))
	        .setProperty(RestBaseConstant.PROPERTY_MSG_ORIGIN,simple("${exchangeProperty.MSG_ORIGIN}"))
	        .setProperty(RestBaseConstant.PROPERTY_MSG_ENTITY,simple("${exchangeProperty.MSG_ENTITY}"))   	       	        
            .to(RestBaseConstant.ROUTE_INSERT_REPORT_SERVICE)
	        .log(LoggingLevel.INFO,"Salida de propiedades hacia la cola::: ${exchangeProperty.ORIGIN_MSG_ID}, ${exchangeProperty.MSG_ORIGIN}, ${exchangeProperty.MSG_ENTITY}")
            .to("bean:syncJournalMapper?method=syncJournalBodyMapper(${exchange}, ${exchangeexchangeProperty.strBody}, ${exchangeProperty.ORIGIN_MSG_ID}, ${exchangeProperty.MSG_ORIGIN}, ${exchangeProperty.MSG_ENTITY})")
//            .to("vm:JournalMsgsItemSync")
            // FIN GUARDAR LA ENTRADA
            .setBody(exchangeProperty("strBody"))
//            .to("direct-vm:drupalCreateOrderAdapter")
            .to("velocity://response.json")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
            .choice()
            	.when(exchangeProperty(Exchange.HTTP_RESPONSE_CODE).startsWith(2))
            		.unmarshal().json(JsonLibrary.Jackson)
            .end()
	        .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))	
	        .setHeader(Exchange.HTTP_RESPONSE_CODE, exchangeProperty(Exchange.HTTP_RESPONSE_CODE))
	        .log(LoggingLevel.INFO,"Body: ${body}")
	        .log(LoggingLevel.INFO,"Headers: ${headers}")
     
	    .endDoTry()				
	    .doCatch(BeanValidationException.class, ValidationException.class)
	        .log(LoggingLevel.INFO,"-------entro??--------")
	        .onWhen(exceptionMessage().contains("Severe"))
	        .throwException(java.lang.Exception.class, "Bean validation error ${header.CamelHttpUri} has this errors: ${body}")
	        //podriamos colacar aqui para ver estadisticas de los que ingresan con errores de mapeo
	
	    .end()
	;
		
		from("direct-vm:collect-cancelSuscripcion-response-sync").routeId("COLLECT-CANCEL-SUSCRIPCION-SYNC-RESPONSE").streamCaching()
	    .doTry()
	        .log(LoggingLevel.INFO,"Iniciando validacion de la entrada ${headers}") 	
	        .toD("direct-vm:getMsgId")
	        .marshal().json(JsonLibrary.Jackson)
	        .setHeader(MSG_ENTITY, simple("${properties:sc.report.msg.entity.drupal.collect.cancel}"))
    	    .setHeader(JMS_TYPE, simple("${properties:sc.report.msg.entity.drupal.collect.cancel}"))
    	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
    	    .setHeader(TARGET_ID_QUEUE, simple("${properties:collect.legis.msg.entity.queue}"))
	        .setProperty(MSG_ENTITY, simple(" ${header.MSG_ENTITY}"))
            .setProperty(JMS_TYPE, simple(" ${header.JMS_TYPE}"))
            .setProperty(MSG_ORIGIN, simple(" ${header.MSG_ORIGIN}"))
            .setHeader("epayco_id",simple("${header.susp-epayco-id}"))
	        .setProperty("status_report", constant("processed_notification"))
	        .setHeader("status_report", constant("processed_notification"))
	       
	        .log(LoggingLevel.INFO,"Mesagge id generated: ${header.ORIGIN_MSG_ID}") 
	        
	        //TODO GUARDAR LA ENTRADA
	       // .to("bean:bodyUtil?method=getStringJson(${exchange})")
	        .log(LoggingLevel.INFO,"Codigo epayco: {header.susp-epayco-id} origin ${exchangeProperty.MSG_ORIGIN} entity ${exchangeProperty.MSG_ENTITY}") 
	        
	        .convertBodyTo(String.class)
	        .setProperty("strBody", simple("${body}"))
	        .log(LoggingLevel.INFO,"input Body:  ${exchangeexchangeProperty.strBody}") 
	        .log(LoggingLevel.INFO,"salida de message-id${header.MSG_ID}")
	        .setProperty(RestBaseConstant.PROPERTY_MESSAGE_ID,simple("${header.MSG_ID}"))
	        .setProperty(RestBaseConstant.PROPERTY_INCOMING,constant(1))
	        .setProperty(RestBaseConstant.PROPERTY_MSG_ORIGIN,simple("${exchangeProperty.MSG_ORIGIN}"))
	        .setProperty(RestBaseConstant.PROPERTY_MSG_ENTITY,simple("${exchangeProperty.MSG_ENTITY}"))   	       	        
            .to(RestBaseConstant.ROUTE_INSERT_REPORT_SERVICE)
	        .log(LoggingLevel.INFO,"Salida de propiedades hacia la cola::: ${exchangeProperty.ORIGIN_MSG_ID}, ${exchangeProperty.MSG_ORIGIN}, ${exchangeProperty.MSG_ENTITY}")
            .to("bean:syncJournalMapper?method=syncJournalBodyMapper(${exchange}, ${exchangeexchangeProperty.strBody}, ${exchangeProperty.ORIGIN_MSG_ID}, ${exchangeProperty.MSG_ORIGIN}, ${exchangeProperty.MSG_ENTITY})")
            .to("vm:JournalMsgsItemSync")
            // FIN GUARDAR LA ENTRADA
            .setBody(exchangeProperty("strBody"))
            .to("direct-vm:drupalCancelSuscriptionAdapter")	
            .choice()
            	.when(exchangeProperty(Exchange.HTTP_RESPONSE_CODE).startsWith(2))
            		.unmarshal().json(JsonLibrary.Jackson)
            .end()
	        .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))	
	        .setHeader(Exchange.HTTP_RESPONSE_CODE, exchangeProperty(Exchange.HTTP_RESPONSE_CODE))
	        .log(LoggingLevel.INFO,"Body collect-cancelSuscripcion-response-sync: ${body}")
	        .log(LoggingLevel.INFO,"Headers collect-cancelSuscripcion-response-sync: ${headers}")
     
	    .endDoTry()				
	    .doCatch(BeanValidationException.class, ValidationException.class)
	        .log(LoggingLevel.INFO,"-------entro??--------")
	        .onWhen(exceptionMessage().contains("Severe"))
	        .throwException(java.lang.Exception.class, "Bean validation error ${header.CamelHttpUri} has this errors: ${body}")
	        //podriamos colacar aqui para ver estadisticas de los que ingresan con errores de mapeo
	
	    .end()
	;
		
		from("direct-vm:getMsgId").streamCaching()
		.routeId("MESSAGE-ID")
		.description("Get o put the message id")
		.log(LoggingLevel.INFO,"Asginando message id") 
	
	        	.setHeader("MSG_ID", simple("${exchangeId}"))
	        	.setHeader("ORIGIN_MSG_ID", simple("${header.MSG_ID}"))
	    
		.end();
		
	}

}
