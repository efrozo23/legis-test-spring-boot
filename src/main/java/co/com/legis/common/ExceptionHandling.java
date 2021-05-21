package co.com.legis.common;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import co.com.legis.hal.utils.Base_Hal_Problem_Response;

public class ExceptionHandling 
{
	@SuppressWarnings("unchecked")
	public void AttachException(RouteBuilder route)
	{
		route.onException(	com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException.class,
							com.fasterxml.jackson.databind.exc.InvalidFormatException.class,
							com.fasterxml.jackson.core.JsonParseException.class,
							java.lang.Exception.class)
		.handled(true)
		.log(LoggingLevel.ERROR	, "EXCEPCION ENCONTRADA : ${exception.stacktrace}")
		.bean(Base_Hal_Problem_Response.class, "GetHalRepresentation(${exchange}, 400, 'Bad Request', 'Invalid data format', 'https://httpstatuses.com/400')")		
		.setHeader(Exchange.CONTENT_TYPE, route.constant("application/problem+json"))			
		.marshal().json(JsonLibrary.Jackson);
		
     
		
		
	}
}


