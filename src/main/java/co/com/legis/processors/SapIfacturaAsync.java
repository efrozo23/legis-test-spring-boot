package co.com.legis.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SapIfacturaAsync implements Processor {
	private static final Logger LOG = LoggerFactory.getLogger("co.com.legis.processors.SapIfacturaAsync");

	@Override
	public void process(Exchange exchange) throws Exception {
		String msgOrigin = String.valueOf(exchange.getIn().getHeader("source_system"));
		exchange.getOut().setHeader("Content-Type", "application/json;charset=UTF-8");
		exchange.getOut().setHeader("MSG_ORIGIN", msgOrigin);
		exchange.getOut().setHeader("JMSType", "Eivoicing_DOCUMENT");
		exchange.getOut().setHeader("MSG_ENTITY", "Eivoicing_DOCUMENT");
		exchange.getOut().setHeader("MSG_ID", exchange.getIn().getHeader("ORIGIN_MSG_ID"));
		exchange.getOut().setHeader("entityContext", "IFacturaDocumentAdapter");
		exchange.getOut().setHeader("ORIGIN_MSG_ID", exchange.getIn().getHeader("ORIGIN_MSG_ID"));
		exchange.getOut().setHeader("TARGET_ID_QUEUE", "eInvoicingQueueNormalizer");

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonString = objectMapper.writeValueAsString(exchange.getIn().getBody());
		exchange.getOut().setBody(jsonString);

	}

}
