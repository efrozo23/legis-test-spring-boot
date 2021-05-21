package co.com.legis.request_execution;

import org.apache.camel.Exchange;

public class SalesChannel_Prices implements IRequestExecution{
	
	@Override
	public void DoRequest(Exchange exchange) 
	{					
		
		//String msgOrigin = String.valueOf(exchange.getIn().getHeader("source_system"));
		//exchange.getIn().setHeader("MSG_ORIGIN", msgOrigin);
	//	exchange.getIn().setHeader("JMSType", "VTEX_PRICES");
	//	exchange.getIn().setHeader("MSG_ENTITY", "VTEX_PRICES");
	//	exchange.getIn().setHeader("MSG_ID", exchange.getExchangeId());
	//	exchange.getIn().setHeader("entityContext", "");

		
//		
//		
//		
//		String jsonString = null;
//		ObjectMapper objectMapper = new ObjectMapper();
//		try {
//			jsonString = objectMapper.writeValueAsString(exchange.getIn().getBody());
//		} catch (JsonProcessingException e) {
//			JSONObject jsonObject = new JSONObject(exchange.getIn().getBody());
//			jsonString = jsonObject.toString();
//		}
//
//		exchange.getIn().setBody(jsonString);	
	}
}
