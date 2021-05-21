package co.com.legis.request_execution;

import org.apache.camel.Exchange;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SalesChannel_Inventory_Sku implements IRequestExecution {

	@Override
	public void DoRequest(Exchange exchange) {

		// TODO CAMBIAR ORIGENES Y DESTINOS
		String msgOrigin = String.valueOf(exchange.getIn().getHeader("source_system"));
		exchange.getIn().setHeader("MSG_ORIGIN", msgOrigin);
		exchange.getIn().setHeader("JMSType", "VTEX_INVENTORY");
		exchange.getIn().setHeader("MSG_ENTITY", "VTEX_INVENTORY");
		exchange.getIn().setHeader("MSG_ID", exchange.getExchangeId());
		exchange.getIn().setHeader("entityContext", "VtexInventoryAdapter");
		String jsonString = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			jsonString = objectMapper.writeValueAsString(exchange.getIn().getBody());
		} catch (JsonProcessingException e) {
			JSONObject jsonObject = new JSONObject(exchange.getIn().getBody());
			jsonString = jsonObject.toString();
		}

		exchange.getIn().setBody(jsonString);	
	}

}
