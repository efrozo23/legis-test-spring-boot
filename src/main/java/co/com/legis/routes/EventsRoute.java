package co.com.legis.routes;

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
import co.com.legis.hal.utils.Async_Base_Hal_Response;
import co.com.legis.hal.utils.BodyUtil;

public class EventsRoute extends RouteBuilder {
	
	public final String MSG_ENTITY = "MSG_ENTITY";
	public final String MSG_ORIGIN = "MSG_ORIGIN";
	public final String JMS_TYPE = "JMSType";
	public final String TARGET_ID_QUEUE = "TARGET_ID_QUEUE";
	public final String NUMBER_SILENIO_ORDER = "number_silenio_order";

	@BeanInject("loggerRef")
	private Logger LOG = LoggerFactory.getLogger(EventsRoute.class);

	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		ExceptionHandling exHandling = new ExceptionHandling();
		exHandling.AttachException(this);

		// @formatter:off
		from("direct-vm:AsyncResponseEvents").streamCaching()
			.routeId("ASYNC-RESPONSE-EVENTS")
			.doTry()
				.log(LoggingLevel.INFO, LOG, "Recepcion de mensaje de Eventos")
				.to("bean-validator://beanValidation")
				.setProperty("servicetype", constant("service"))
				.log(LoggingLevel.DEBUG, LOG, "Mensaje a la Cola {{queuePendingName}}")
				.log(LoggingLevel.DEBUG, LOG, "Headers ${headers}")
				.to("direct-vm:getMsgId")
				.setHeader("MSG_ORIGIN", constant("PUBLICACION_ELECTRONICAS"))
				.setHeader("JMSType", constant("USAGE_EVENT")).setHeader("MSG_ENTITY", constant("USAGE_EVENT"))
				.setHeader("MSG_ID", simple("${headers.JMSMessageID}"))
				.setHeader("entityContext", simple("${headers.urlRequest}")).removeHeaders("Camel*")
				.removeProperties("Camel*").removeProperty("Authorization")
				.bean(BodyUtil.class, "getStringJson(${exchange}")
				.log(LoggingLevel.DEBUG, LOG, "Body convertido ${property.strBody}")
				.setBody(simple("${property.strBody}"))
				.setHeader("originalPayload", simple("${property.strBody}"))
				.to("activemq:queue:{{queuePendingName}}?exchangePattern=InOnly")
				.setHeader("CamelHttpResponseCode", simple("202"))
				.log(LoggingLevel.DEBUG, LOG, "Enviado el mensaje ${headers.MSG_ID} a la cola:{{queuePendingName}}")
				.log(LoggingLevel.DEBUG, LOG, "Headers ${headers}")
				.log(LoggingLevel.DEBUG, LOG, "JMSMessageID = ${header.JMSMessageID}").to("direct:endMessageEvents")
			.endDoTry()
			.doCatch(BeanValidationException.class, ValidationException.class)
				.onWhen(exceptionMessage().contains("Severe"))
					.throwException(java.lang.Exception.class, "Bean validation error ${header.CamelHttpUri} has this errors: ${body}")
				.to("mock://endRoute")
			.end()
		;

		from("direct:endMessageEvents").streamCaching().log(LoggingLevel.DEBUG, LOG, "endMessageEvents")
				.log(LoggingLevel.DEBUG, LOG, "Location: ${header.location}")
				.log(LoggingLevel.DEBUG, LOG, "ContentType: ${header.Content-Type}")
				.log(LoggingLevel.DEBUG, LOG, "Context Type: ${header.Context-Type}")
				.log(LoggingLevel.DEBUG, LOG, "Camel httpResponse ${header.CamelHttpResponseCode}").choice()
				.when(simple("${header.location} == 'http://{{rest.base.events.EventosIp}}/swagger/index.html'"))
				.description(".........")
				.setHeader("location", simple(
						"http://${exchangeProperty.domainRequest}:{{sap.host.port}}/api/eventos/utilitarios/swagger/index.html"))
				.when(simple(
						"${header.Content-Type} contains 'application/json' or ${header.Content-Type} contains 'application/xml'"))
				.description(".........")
				.setHeader("urlRequest", simple("http://{{container.ip}}:{{sap.host.port}}/events/apps/"))
				.log(LoggingLevel.DEBUG, LOG, "Mensaje respuesta").log(LoggingLevel.DEBUG, LOG, "Cabeceras: ${headers}")
				.bean(Async_Base_Hal_Response.class,
						"GetHalRepresentation(${exchange}, 202, 'Accepted', 'The event object has been received and will be processed.','https://httpstatuses.com/202')")

				.setHeader(Exchange.CONTENT_TYPE, constant("application/hal+json"))
				.when(simple("${header.CamelHttpResponseCode} &gt; 399 and ${header.CamelHttpResponseCode} &lt; 600"))
				.setHeader("urlRequest", simple("http://localhost:9001/events/apps/"))
				.setHeader("Content-Type", simple("application/problem+json"))
				.setHeader("CamelHttpResponseCode", simple("400")).otherwise().end()

		;
		
		from("direct-vm:AsyncResponseSapEdi").routeId("AsyncResponseSapEdi").streamCaching()
			.description("Process reception number order aceptation in Sap for EDI.")
			.doTry()
			.choice()
			.when(header("source_system").isEqualTo(null))
				 .log(LoggingLevel.INFO, LOG, ": Falta header source {date:now:HH:mm:ss}: : :")
				 .bean("asyncBaseHalProblemResponse", "GetHalRepresentation(${exchange}, 400, 'Bad Request', 'Header ['source_system'] not fount.','https://httpstatuses.com/400')")
				 .marshal().json(JsonLibrary.Jackson)
				 .log(LoggingLevel.INFO, LOG, ": *****************${body}")
	         .otherwise()
		 	      .setProperty("CamelHttpPath",simple("${header.CamelHttpUrl}"))
			      .log(LoggingLevel.INFO,LOG,": : :Inicia ejecucion einvoicing ${date:now:HH:mm:ss}: : :")
				  .to("bean-validator://beanValidation")
		          .toD("direct-vm:getMsgId")
		          .marshal().json(JsonLibrary.Jackson)
		 	      .setHeader(MSG_ENTITY, simple("${properties:edi.number.legis.msg.entity}"))
		 	      .setHeader(JMS_TYPE, simple("${properties:edi.number.legis.msg.entity}"))
		 	      .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
		 	      .setHeader(TARGET_ID_QUEUE, simple("NormalizedSapSyncPendingEvents"))
		 	      .setProperty(MSG_ENTITY, simple(" ${header.MSG_ENTITY}"))
		          .setProperty(JMS_TYPE, simple(" ${header.JMS_TYPE}"))
		          .setProperty(MSG_ORIGIN, simple(" ${header.MSG_ORIGIN}"))
		          .to("activemq:queue:{{queuePendingName}}?exchangePattern=InOnly")
			      .log("Enviado a la cola")
			      .log(LoggingLevel.INFO,"Mesagge id generated: ${header.ORIGIN_MSG_ID}") 
		
			      .convertBodyTo(String.class)
			      .setProperty("strBody", simple("${body}"))
			      .log(LoggingLevel.INFO,"input Body:  ${property.strBody}") 
			       .log(LoggingLevel.INFO,"Salida de propiedades hacia la cola::: ${property.ORIGIN_MSG_ID}, ${property.MSG_ORIGIN}, ${property.MSG_ENTITY}")
		          .to("bean:syncJournalMapper?method=syncJournalBodyMapper(${exchange}, ${property.strBody}, ${property.ORIGIN_MSG_ID}, ${property.MSG_ORIGIN}, ${property.MSG_ENTITY})")
		          .to("vm:JournalMsgsItemSync")

	              .bean(Async_Base_Hal_Response.class, "GetHalRepresentation(${exchange}, 202, 'Accepted', 'The request was received correctly. It will pass to validation and processing.','https://httpstatuses.com/202')")			
	              .setHeader(Exchange.CONTENT_TYPE, constant("application/hal+json"))			
	              .endDoTry()				
		    .doCatch(BeanValidationException.class, ValidationException.class)
		        .onWhen(exceptionMessage().contains("Severe"))
		        .throwException(java.lang.Exception.class, "Bean validation error ${header.CamelHttpUri} has this errors: ${body}")		
		    .end()
	;		

		// @formatter:on

	}

}
