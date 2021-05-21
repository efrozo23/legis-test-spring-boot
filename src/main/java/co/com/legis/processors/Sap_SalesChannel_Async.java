package co.com.legis.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.com.legis.request_execution.IRequestExecution;
import co.com.legis.request_execution.SalesChannel_Prices;

public class Sap_SalesChannel_Async implements Processor 
{
	private static final Logger LOG = LoggerFactory.getLogger("co.com.legis.processors.Sap_SalesChannel_Async");	
	
	@Override
	public void process(Exchange exchange) throws Exception 
	{			
		String className = exchange.getUnitOfWork().getOriginalInMessage().getBody().getClass().getSimpleName();
		co.com.legis.enums.ModelsEnum data =  co.com.legis.enums.ModelsEnum.valueOf(className);		
		
		IRequestExecution exec = null;
		LOG.error("validacion de salida de enum"+ data);
		exchange.getOut().setHeader("salida-funcion", data);
		switch(data)
		{

			case PriceRequestModel:			{ exec = new SalesChannel_Prices();			break; }		

		}	
	
		exec.DoRequest(exchange);
	}	
}


