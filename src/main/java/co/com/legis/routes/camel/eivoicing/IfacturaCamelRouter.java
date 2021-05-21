package co.com.legis.routes.camel.eivoicing;

import javax.validation.ValidationException;

import org.apache.camel.BeanInject;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.com.legis.common.ExceptionHandling;
import co.com.legis.constant.RestBaseConstant;
import co.com.legis.hal.utils.Async_Base_Hal_Response;
import co.com.legis.processors.SapIfacturaAsync;

/**
 * @author JeysonCM
 * @apiNote Recepcion de factura electronica
 */
public class IfacturaCamelRouter  extends RouteBuilder{
	
	@BeanInject("loggerRef")//Invovamos el bean de logs 
	private Logger LOG = LoggerFactory.getLogger(IfacturaCamelRouter.class);
		

	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		ExceptionHandling exHandling = new ExceptionHandling();
		exHandling.AttachException(this);
		
		
		from("direct-vm:AsyncResponseEivoicing").routeId("EINVOICIONG")
		.description("Proceso de recepcion de factura electronica.") 
		.doTry()
		.choice()
		.when(header("source_system").isEqualTo(null))
			.log(LoggingLevel.INFO, LOG, ": Falta header source {date:now:HH:mm:ss}: : :")
			.bean("asyncBaseHalProblemResponse", "GetHalRepresentation(${exchange}, 400, 'Bad Request', 'Header ['source_system'] not fount.','https://httpstatuses.com/400')")
			.marshal().json(JsonLibrary.Jackson)
			.log(LoggingLevel.INFO, LOG, ": *****************${body}")
			.log(LoggingLevel.ERROR, LOG, "the order was not well received by restBase")
         .otherwise()
		    .setProperty("CamelHttpPath",simple("${header.CamelHttpUrl}"))
		    .log(LoggingLevel.INFO,LOG,": : :Inicia ejecucion einvoicing ${date:now:HH:mm:ss}: : :")
			.to("bean-validator://beanValidation")
	        .to("direct-vm:getMsgEinvoicingId")
			.process(new SapIfacturaAsync() )
	        .setProperty(RestBaseConstant.PROPERTY_MSG_ORIGIN,simple("${header.MSG_ORIGIN}"))
	        .setProperty(RestBaseConstant.PROPERTY_MSG_ENTITY,simple("${header.MSG_ENTITY}"))  
		        .choice()
			        .when(header("MSG_ORIGIN").isEqualTo("TEST"))
				        .bean(Async_Base_Hal_Response.class, "GetHalRepresentation(${exchange}, 202, 'Accepted', 'The request was received correctly. It will pass to validation and processing.','https://httpstatuses.com/202')")
				        .to("mock:out")        
			        .otherwise()
				        .setProperty("status_report", constant("processed_deliver"))
				        .setHeader("status_report", constant("processed_deliver"))
						.convertBodyTo(String.class, "UTF-8")
						.to("activemq:queue:{{queuePendingName}}?exchangePattern=InOnly")
						.setHeader("CamelHttpPath",simple("${property.CamelHttpPath}"))
						
					    .setProperty(RestBaseConstant.PROPERTY_MESSAGE_ID,simple("${header.MSG_ID}"))
					    .setProperty(RestBaseConstant.PROPERTY_INCOMING,constant(1))
				        .to(RestBaseConstant.ROUTE_INSERT_REPORT_SERVICE)
											
						.bean(Async_Base_Hal_Response.class, "GetHalRepresentation(${exchange}, 202, 'Accepted', 'The request was received correctly. It will pass to validation and processing.','https://httpstatuses.com/202')")			
						.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
					.end()	
		   .end()	
		.endDoTry()				
		.doCatch(BeanValidationException.class, ValidationException.class)
		    .log(LoggingLevel.INFO,LOG,": : :Error SAP-ESB-EINVOICING ${date:now:HH:mm:ss}: : :")// Creamos una inicializacion del evento para envio al log
		    .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("500"))
			.onWhen(exceptionMessage().contains("Severe"))
    		.log(LoggingLevel.ERROR, LOG, "the order was not well received by restBase")
    		.setProperty(RestBaseConstant.PROPERTY_ERROR_DETAIL, body())
    		 .setProperty(RestBaseConstant.PROPERTY_INCOMING,constant(1))
    		.setProperty(RestBaseConstant.PROPERTY_PROCESSED_NOTIFICATION, constant(0))
	        .setProperty(RestBaseConstant.PROPERTY_PROCESSED, constant(0))
	        .setProperty(RestBaseConstant.PROPERTY_PROCESSED_DELIVER, constant(0))
	        .setProperty(RestBaseConstant.PROPERTY_REJECTED, constant(1))
	        .to(RestBaseConstant.ROUTE_UPDATE_REPORT_SERVICE)
	        .to(RestBaseConstant.ROUTE_INSERT_REPORT_ERROR)			
			.throwException(java.lang.Exception.class, "Bean validation error ${header.CamelHttpUri} has this errors: ${property.RestBaseConstant.PROPERTY_ERROR_DETAIL}")			
		.end();
		
			
		
		from("direct-vm:getMsgEinvoicingId").routeId("ASYNC-RESPONSE-EIVOICING")
		.description("Get o put the message id")
	    .choice()
	        .when(simple("${header.MSG_ID} == null or ${header.MSG_ID} != ''"))
	        .setHeader("MSG_ID", simple("${exchangeId}"))
	        .setHeader("ORIGIN_MSG_ID", simple("${header.MSG_ID}"))
	        .log(LoggingLevel.DEBUG,"Proceso para el ID :: ${headers.MSG_ID} ") 
	        .otherwise()
		.end();
		
		
	}

}
