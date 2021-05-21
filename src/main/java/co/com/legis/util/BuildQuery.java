package co.com.legis.util;

import org.apache.camel.Exchange;
import org.apache.camel.PropertyInject;

public class BuildQuery {

	@PropertyInject("{{journal.query.message.id}}")
	private String updateDownload;

	public void getJournalByMessage(Exchange exchange) {
		String query = updateDownload;
		query = query.replace(":#messageId", String.valueOf(exchange.getIn().getHeader("messageId")));
		exchange.getIn().setBody(query);
	}
}
