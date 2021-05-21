package co.com.legis.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class UtilsProcessor  implements Processor
{
	private static final Logger LOG = LoggerFactory.getLogger("co.com.legis.processors.UtilsProcessor");
	
	@Override
	public void process(Exchange exchange) throws Exception {
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
