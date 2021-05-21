package co.com.legis.processors;

import org.apache.camel.BeanInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.PropertyInject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import co.com.legis.request_models.salesforce.opportunity.RequestOpportunityDTO;

//import co.com.legis.request_models.salesforce.opportunityCrm.OpportunityDTO;

public class SalesforcePatchOpportunityAsync implements Processor {

	@BeanInject("loggerRef")
	private Logger LOG = LoggerFactory.getLogger(SalesforcePatchOpportunityAsync.class);

	@PropertyInject("{{MSG.ORIGIN.PATCH.OPPORTUNITY.CRM}}")
	private String msgOriginCRM;
	@PropertyInject("{{MSG.ENTITY.PATCH.OPPORTUNITY.CRM}}")
	private String msgEntityCRM;
	@PropertyInject("{{JMS.TYPE.PATCH.OPPORTUNITY.CRM}}")
	private String jmsTypeCRM;
	@PropertyInject("{{ENTITY.CONTEXT.PATCH.OPPORTUNITY.CRM}}")
	private String entityContextCRM;

	@Override
	public void process(Exchange exchange) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		String bodySalesforce = null;

		RequestOpportunityDTO body = exchange.getIn().getBody(RequestOpportunityDTO.class);
		bodySalesforce = mapper.writeValueAsString(body);

		exchange.getOut().setHeader("MSG_ORIGIN", msgOriginCRM);
		exchange.getOut().setHeader("MSG_ENTITY", msgEntityCRM);
		exchange.getOut().setHeader("JMSType", jmsTypeCRM);
		exchange.getOut().setHeader("entityContext", entityContextCRM);

		exchange.getOut().setHeader("Content-Type", "application/json;charset=UTF-8");
		exchange.getOut().setHeader("MSG_ID", exchange.getIn().getHeader("ORIGIN_MSG_ID"));
		exchange.getOut().setHeader("ORIGIN_MSG_ID", exchange.getIn().getHeader("ORIGIN_MSG_ID"));
		exchange.getOut().setHeader("OPPORTUNITY-ID", exchange.getIn().getHeader("opportunity-id"));
		exchange.getOut().setBody(bodySalesforce);
		 
		LOG.debug("SALIDA DE DATOS OPPORTUNITY SALESFORCE" + bodySalesforce);
	}

}
