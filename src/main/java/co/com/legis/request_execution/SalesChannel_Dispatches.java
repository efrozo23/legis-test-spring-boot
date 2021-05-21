package co.com.legis.request_execution;

import org.apache.camel.Exchange;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SalesChannel_Dispatches implements IRequestExecution
{	
	@Override
	public void DoRequest(Exchange exchange) 
	{					
		String msgOrigin = String.valueOf(exchange.getIn().getHeader("source_system"));
		exchange.getIn().setHeader("MSG_ORIGIN", msgOrigin);
		exchange.getOut().setHeader("JMSType", "VTEX_DISPATCH");
		exchange.getOut().setHeader("MSG_ENTITY", "VTEX_DISPATCH");
		exchange.getOut().setHeader("MSG_ID", exchange.getExchangeId());
		exchange.getOut().setHeader("entityContext", "VtexDispatchAdapter");
		
		String jsonString = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			jsonString = objectMapper.writeValueAsString(exchange.getIn().getBody());
		} catch (JsonProcessingException e) {
			JSONObject jsonObject = new JSONObject(exchange.getIn().getBody());
			jsonString = jsonObject.toString();
		}


		exchange.getOut().setBody(jsonString);				
	}
}
