package co.com.legis.routes.camel.salesForce;

import javax.validation.ValidationException;

import org.apache.camel.BeanInject;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.com.legis.common.ExceptionHandling;
import co.com.legis.constant.RestBaseConstant;
import co.com.legis.hal.utils.Async_Base_Hal_Response;
import co.com.legis.hal.utils.MonitoreoConstants;
import co.com.legis.processors.SapContactsAsync;

public class SalesForcesCrmCamelRouter extends RouteBuilder {

	@BeanInject("loggerRef")
	private Logger LOG = LoggerFactory.getLogger(SalesForcesCrmCamelRouter.class);
	
	

	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		ExceptionHandling exHandling = new ExceptionHandling();
		exHandling.AttachException(this);
		
		from("direct-vm:AsyncChangeContactCRM")
		     .routeId("AsyncChangeContactCRM")
			.description("Realiza las validaciones mínimas a los request de Upsert de todos los Endpoint")						
			.doTry()
				.choice()
					.when(header("source_system").isEqualTo(null))
						.log(LoggingLevel.INFO, LOG, ": Falta header source {date:now:HH:mm:ss}: : :")
						.bean("asyncBaseHalProblemResponse", "GetHalRepresentation(${exchange}, 400, 'Bad Request', 'Header ['source_system'] not fount.','https://httpstatuses.com/400')")
						.marshal().json(JsonLibrary.Jackson)
						.log(LoggingLevel.INFO, LOG, ": *****************${body}")
			        .otherwise()
						    .setProperty("CamelHttpPath",simple("${header.CamelHttpUrl}"))
						    .log(LoggingLevel.INFO,LOG,": : :Inicia ejecucion change contact ${date:now:HH:mm:ss}: : :")
							.to("bean-validator://beanValidation")	
							.log("salida de headers params: ${header.contact-id-crm}")
						    .to("direct-vm:getMsgSalesForceCrmId")
						    .log("salida de body ${body}")
							.process(new SapContactsAsync() )
					        .choice()
						        .when(header("MSG_ORIGIN").isEqualTo("TEST"))
						            .bean(Async_Base_Hal_Response.class, "GetHalRepresentation(${exchange}, 202, 'Accepted', 'The request was received correctly. It will pass to validation and processing.','https://httpstatuses.com/202')")
							        .to("mock:out")        
						        .otherwise()
									.convertBodyTo(String.class, "UTF-8")
									.to("activemq:queue:{{queuePendingName}}?exchangePattern=InOnly")
									.setHeader("CamelHttpPath",simple("${property.CamelHttpPath}"))
									.bean(Async_Base_Hal_Response.class, "GetHalRepresentation(${exchange}, 202, 'Accepted', 'The request was received correctly. It will pass to validation and processing.','https://httpstatuses.com/202')")		
									.setHeader(Exchange.CONTENT_TYPE, constant("application/hal+json"))
									.setHeader("messageID").simple("${exchangeId}")
							.to("mock:Out")
					    .end()
				 .end()				
		.endDoTry()			
		.doCatch(org.apache.camel.component.bean.validator.BeanValidationException.class, ValidationException.class)
		    .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("500"))
			.onWhen(exceptionMessage().contains("Severe"))
			.throwException(java.lang.Exception.class, "Bean validation error ${header.CamelHttpUri} has this errors: ${body}")		
		.end();
		
		
		from("direct-vm:AsyncAccountContactCRM")
		    .routeId("AsyncAccountContactCRM")
			.description("Realiza las validaciones mínimas a los request de Upsert de todos los Endpoint")	
			.doTry()
				.choice()
					.when(header("source_system").isEqualTo(null))
						.log(LoggingLevel.INFO, LOG, ": Falta header source {date:now:HH:mm:ss}: : :")
						.bean("asyncBaseHalProblemResponse", "GetHalRepresentation(${exchange}, 400, 'Bad Request', 'Header ['source_system'] not fount.','https://httpstatuses.com/400')")
						.marshal().json(JsonLibrary.Jackson)
						.log(LoggingLevel.INFO, LOG, ": *****************${body}")
			    .otherwise()
					    .setProperty("CamelHttpPath",simple("${header.CamelHttpUrl}"))
					    .log(LoggingLevel.INFO,LOG,": : :Inicia ejecucion change contact ${date:now:HH:mm:ss}: : :")
						.to("bean-validator://beanValidation")	
						.log("salida de headers: ${headers}")
					    .to("direct-vm:getMsgSalesForceCrmId")
					    .log("salida de body ${body}")
					    .process(new SapContactsAsync() )
				       .choice()
					        .when(header("MSG_ORIGIN").isEqualTo("TEST"))
					            .bean(Async_Base_Hal_Response.class, "GetHalRepresentation(${exchange}, 202, 'Accepted', 'The request was received correctly. It will pass to validation and processing.','https://httpstatuses.com/202')")
						        .to("mock:out")        
					        .otherwise()
								.convertBodyTo(String.class, "UTF-8")
								.to("activemq:queue:{{queuePendingName}}?exchangePattern=InOnly")
								.setHeader("CamelHttpPath",simple("${property.CamelHttpPath}"))
								.bean(Async_Base_Hal_Response.class, "GetHalRepresentation(${exchange}, 202, 'Accepted', 'The request was received correctly. It will pass to validation and processing.','https://httpstatuses.com/202')")		
								.setHeader(Exchange.CONTENT_TYPE, constant("application/hal+json"))		
					   .end()
			   .end()
		.endDoTry()			
		.doCatch(org.apache.camel.component.bean.validator.BeanValidationException.class, ValidationException.class)
		    .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("500"))
			.onWhen(exceptionMessage().contains("Severe"))
			.throwException(java.lang.Exception.class, "Bean validation error ${header.CamelHttpUri} has this errors: ${body}")		
		.to("mock:Out")
		.end();		


	    from("direct-vm:AsyncResponseSalesForceCRM").routeId("AsyncResponseSalesForceCRM").streamCaching()
	    .description("Realiza las validaciones necesarias para envio de Request en SAP")
	        .doTry()
	            .choice()
	                .when(header("source_system").isEqualTo(null))
	                    .log(LoggingLevel.INFO, LOG, ": Falta el header Source_System en el Request.  {date:now:HH:mm:ss}: : :")
	                    .bean("asyncBaseHalResponse", "GetHalRepresentation(${exchange}, 400, 'Bad Request', 'Header ['source_system'] not fount.','https://httpstatuses.com/400')")
	                    .marshal().json(JsonLibrary.Jackson)
	                    .log(LoggingLevel.INFO, LOG, ": *****************${body}")
	                .endChoice()
	                .otherwise()
	                    .setProperty("CamelHttpPath", simple("${header.CamelHttpUrl}"))
	                    .log(LoggingLevel.INFO, LOG, ": : : Inicia ejecucion entrada SalesForce${date:now:HH:mm:ss}: : :")
	                    .to("bean-validator://beanValidation")
	                    .to("direct-vm:getMsgSalesForceCrmId")
	                    .log(LoggingLevel.DEBUG, LOG, ": : : La salida del body es ${date:now:HH:mm:ss}: : :")
	                    .choice()
	                        .when(header("MSG_ORIGIN").isEqualTo("TEST"))
	                            .log(LoggingLevel.DEBUG, LOG, ": : : Entro a realizar test ${date:now:HH:mm:ss}: : :")
	                            .bean("asyncBaseHalResponse", "GetHalRepresentation(${exchange}, 400, 'Accepted', 'The request was received correctly. It will pass to validation and processing.','https://httpstatuses.com/202')")
	                            .to("mock:out")
	                        .endChoice()
	                        .otherwise()
	                        	.setHeader(RestBaseConstant.TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.salesforce}"))
	                        	.setProperty(MonitoreoConstants.PROPERTY_STATUS_REPORT, constant(MonitoreoConstants.PROPERTY_PROCESSED_DELIVER))
	                        	
	                        	.log(LoggingLevel.INFO, LOG, "PERSISTIO EN REPORTE: ${body}")
	                        	
	                            .process("SapAccountAsync")
	                            .setProperty(MonitoreoConstants.PROPERTY_MSG_ORIGIN, header(MonitoreoConstants.PROPERTY_MSG_ORIGIN))
	                            .setProperty(MonitoreoConstants.PROPERTY_MSG_ENTITY, header(MonitoreoConstants.PROPERTY_MSG_ENTITY))
	                        	.setProperty(MonitoreoConstants.PROPERTY_STATUS_REPORT, constant("incoming"))
	                        	.setProperty(MonitoreoConstants.PROPERTY_BODY, body())
	                            .setProperty(MonitoreoConstants.PROPERTY_ORIGIN_MSG_ID, header("ORIGIN_MSG_ID"))
	                            .to(MonitoreoConstants.STATUS_ORDER_SILENIO_ROUTE)
	                            .setBody(exchangeProperty(MonitoreoConstants.PROPERTY_BODY))
	                            .log(LoggingLevel.INFO, LOG, "Los headers son ${headers}")
                                .setProperty("HttpUriErp").simple("${header.CamelHttpPath}")
                               
	                            .to("activemq:queue:{{queuePendingName}}?exchangePattern=InOnly")
                                .setHeader("CamelHttpPath").simple("${property.HttpUriErp}")
	                            .bean(Async_Base_Hal_Response.class, "GetHalRepresentation(${exchange}, 202, 'Accepted', 'The request was received correctly. It will pass to validation and processing.','https://httpstatuses.com/202')")			                            .setHeader(Exchange.CONTENT_TYPE, constant("application/hal+json"))
	                            .setHeader("messageID").simple("${exchangeId}")
	                            .setHeader("ContextInfo").simple("Referer: ${header.Referer} | User-Agent: ${header.User-Agent}")
	                            .log(LoggingLevel.DEBUG, "co.com.legis.util", "Inicia UpsertValidation")
	                        .endChoice()
	                    .end()
	                .endChoice()
	            .end()
	        .endDoTry()
	        .doCatch(org.apache.camel.component.bean.validator.BeanValidationException.class, ValidationException.class)
	            .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("500"))
	            .onWhen(exceptionMessage().contains("Severe"))
	                .throwException(Exception.class, "Bean validation error ${header.CamelHttpUri} has this errors: ${body}")
	            .end()
	        .end()
	    ;
		
		
		from("direct-vm:AsyncPatchOpportunityCRM").routeId("AsyncPatchOpportunityCRM").streamCaching()
			.description("Realiza las validaciones mínimas a los request de Upsert de todos los Endpoint")
			.doTry()
				.setProperty("oldBodyObject", simple("${body}"))
				.setProperty("CamelHttpPath",simple("${headers.CamelHttpPath}"))
				.log(LoggingLevel.INFO,LOG,": : :Inicia ejecucion change opportunity ${date:now:HH:mm:ss}: : :")
				.to("bean-validator://beanValidation")	
				.log(LoggingLevel.DEBUG,LOG, "salida de headers: ${headers}")
				.to("direct-vm:getMsgSalesForceCrmId")
				.log(LoggingLevel.DEBUG,LOG, "salida de body ${body}")
				.setHeader(RestBaseConstant.TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.salesforce}"))
				.process("salesforcePatchOpportunityAsync")
				.choice()
					.when(header("MSG_ORIGIN").isEqualTo("TEST"))
						.bean(Async_Base_Hal_Response.class, "GetHalRepresentation(${exchange}, 202, 'Accepted', 'The request was received correctly. It will pass to validation and processing.','https://httpstatuses.com/202')")
					.otherwise()
						.convertBodyTo(String.class, "UTF-8")
						.to("activemq:queue:{{queuePendingName}}?exchangePattern=InOnly")
						.setBody(simple("${property.oldBodyObject}"))
//						.removeHeaders("*")
						.setHeader("Content-Type", simple("application/json;charset=UTF-8"))
//						.setHeader("MSG_ID", simple("${exchangeId}"))
//						.setHeader("ORIGIN_MSG_ID", simple("${exchangeId}"))
						.setHeader("CamelHttpPath", simple("${property.CamelHttpPath}"))
						.bean("asyncBaseHalResponse", "GetHalRepresentation(${exchange}, 202, 'Accepted', 'The request was received correctly. It will pass to validation and processing.','https://httpstatuses.com/202')")		
				.end()
			.endDoTry()
			.doCatch(org.apache.camel.component.bean.validator.BeanValidationException.class, ValidationException.class)
				.setHeader(Exchange.HTTP_RESPONSE_CODE, simple("500"))
				.onWhen(exceptionMessage().contains("Severe"))
				.throwException(java.lang.Exception.class, "Bean validation error ${header.CamelHttpUri} has this errors: ${body}")
			.to("mock:Out")
		.end();
		
		from("direct-vm:SyncGetOpportunityCRM").routeId("SyncGetOpportunityCRM").streamCaching()
	    	.description("Get Opportunity By Id")
	    	.to("direct-vm:opportunityGetSFAdapter")
	    	.removeHeaders("*")
	    	.unmarshal().json(JsonLibrary.Jackson)
    	.end();
	    
	    from("direct-vm:getMsgSalesForceCrmId").routeId("route-getMsgSalesForceCrmId").streamCaching()
	    .description("Get o put the message id")
	        .choice()
	            .when(simple("${header.MSG_ID} == null or ${header.MSG_ID} != ''"))
	                .setHeader("MSG_ID", simple("${exchangeId}"))
	                .setHeader("ORIGIN_MSG_ID", simple("${header.MSG_ID}"))
	                .log(LoggingLevel.DEBUG, "Proceso para el ID :: ${headers.MSG_ID} ")
	            .endChoice()
	        .end();

	    // @formatter:on
	}

}
