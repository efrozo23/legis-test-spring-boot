package co.com.legis.processors;

import org.apache.camel.BeanInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.PropertyInject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.com.legis.constant.RestBaseConstant;
import co.com.legis.hal.utils.MonitoreoConstants;
import co.com.legis.request_models.salesforce.AccountErpRequestModel;
import co.com.legis.request_models.salesforce.AccountsRequestModel;

public class SapAccountAsync implements Processor {

	@BeanInject("loggerRef")
	private Logger LOG = LoggerFactory.getLogger(SapAccountAsync.class);
	
	@PropertyInject("{{MSG.ORIGIN.CRM}}")
	private String msgOriginCRM;
	
	@PropertyInject("{{MSG.ENTITY.CRM}}")
	private String msgEntityCRM;
	
	@PropertyInject("{{JMS.TYPE.CRM}}")
	private String jmsTypeCRM;
	
	@PropertyInject("{{ENTITY.CONTEXT.CRM}}")
	private String entityContextCRM;

	@PropertyInject("{{MSG.ORIGIN.ERP}}")
	private String msgOriginERP;
	@PropertyInject("{{MSG.ENTITY.ERP}}")
	private String msgEntityERP;
	@PropertyInject("{{JMS.TYPE.ERP}}")
	private String jmsTypeERP;
	@PropertyInject("{{ENTITY.CONTEXT.ERP}}")
	private String entityContextERP;

	@Override
	public void process(Exchange exchange) throws Exception {
			
		if (String.valueOf(exchange.getIn().getHeader("CamelServletContextPath")).contains("/events/erp/updates/customers/{customer-id-erp}")) {
			exchange.getOut().setHeader("ID_PARAM", exchange.getIn().getHeader("customer-id-erp"));

			ObjectMapper mapper = new ObjectMapper();
			
			AccountsRequestModel body = (AccountsRequestModel) exchange.getIn().getBody(AccountsRequestModel.class);
			String bodySalesforce = mapper.writeValueAsString(body);

			LOG.debug("Json enviado a la Queue:\n" + bodySalesforce);

			if (String.valueOf(exchange.getIn().getHeader("CamelServletContextPath")).contains("/events/erp/updates/customers/{customer-id-erp}")) {
				exchange.getOut().setHeader("MSG_ORIGIN", msgOriginCRM);
				exchange.getOut().setHeader("MSG_ENTITY", msgEntityCRM);
				exchange.getOut().setHeader("JMSType", jmsTypeCRM);
				exchange.getOut().setHeader("entityContext", entityContextCRM);
			}

			exchange.getOut().setHeader("Content-Type", "application/json;charset=UTF-8");
			exchange.getOut().setHeader("MSG_ID", exchange.getIn().getHeader("ORIGIN_MSG_ID"));
			exchange.getOut().setHeader("ORIGIN_MSG_ID", exchange.getIn().getHeader("ORIGIN_MSG_ID"));
			exchange.getOut().setHeader(RestBaseConstant.TARGET_ID_QUEUE, exchange.getIn().getHeader(RestBaseConstant.TARGET_ID_QUEUE));
			exchange.getOut().setHeader(MonitoreoConstants.PROPERTY_STATUS_REPORT, exchange.getProperty(MonitoreoConstants.PROPERTY_STATUS_REPORT));
			exchange.getOut().setBody(bodySalesforce);
			//ERP
		}else if (String.valueOf(exchange.getIn().getHeader("CamelServletContextPath")).contains("/events/crm/updates/accounts/{account-id-crm}")) {
			exchange.getOut().setHeader("ID_PARAM", exchange.getIn().getHeader("customer-id-crm"));

			ObjectMapper mapper = new ObjectMapper();
			AccountErpRequestModel body = (AccountErpRequestModel) exchange.getIn().getBody(AccountErpRequestModel.class);

			String bodySap = mapper.writeValueAsString(body);

			LOG.debug("Json enviado a la Queue:\n" + bodySap);
			if (String.valueOf(exchange.getIn().getHeader("CamelServletContextPath")).contains("/events/crm/updates/accounts/{account-id-crm}")) {
				exchange.getOut().setHeader("MSG_ORIGIN", msgOriginERP);
				exchange.getOut().setHeader("MSG_ENTITY", msgEntityERP);
				exchange.getOut().setHeader("JMSType", jmsTypeERP);
				exchange.getOut().setHeader("entityContext", entityContextERP);
			}

			exchange.getOut().setHeader("Content-Type", "application/json;charset=UTF-8");
			exchange.getOut().setHeader("MSG_ID", exchange.getIn().getHeader("ORIGIN_MSG_ID"));
			exchange.getOut().setHeader("ORIGIN_MSG_ID", exchange.getIn().getHeader("ORIGIN_MSG_ID"));
			exchange.getOut().setHeader(RestBaseConstant.TARGET_ID_QUEUE, exchange.getIn().getHeader(RestBaseConstant.TARGET_ID_QUEUE));
			exchange.getOut().setHeader(MonitoreoConstants.PROPERTY_STATUS_REPORT, exchange.getProperty(MonitoreoConstants.PROPERTY_STATUS_REPORT));
			exchange.getOut().setBody(bodySap);

		}
	}

}