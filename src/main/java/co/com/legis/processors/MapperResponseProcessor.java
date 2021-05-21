package co.com.legis.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.com.legis.basehal_models.JournalModel;

public class MapperResponseProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        List<JournalModel> listResponse = (List<JournalModel>) exchange.getProperty("listResponse");

        List<Map<String, Object>> mapJournalMessages = new ArrayList<>();
        for (JournalModel objJournal : listResponse) {
            ObjectMapper jsonHelper = new ObjectMapper();
            String json = jsonHelper.writeValueAsString(objJournal);
            Map<String, Object> mapJournal = jsonHelper.readValue(json, Map.class);
            mapJournalMessages.add(mapJournal);
        }
        //@formatter:off
        JSONObject jsonObject = new JSONObject()
				.put("_links", new JSONObject()
				.put("self", new JSONObject()
				.put("href", String.valueOf(exchange.getIn().getHeader("CamelHttpPath")))))
				.put("count", listResponse.size())
				.put("total", listResponse.size())
				.put("_embedded", new JSONObject().put("journal_messages", mapJournalMessages));
        //@formatter:on
        exchange.getOut().setBody(jsonObject.toString());
    }
}
