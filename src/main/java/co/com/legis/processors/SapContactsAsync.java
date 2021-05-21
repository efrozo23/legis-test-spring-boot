package co.com.legis.processors;

import org.apache.camel.BeanInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.PropertyInject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import co.com.legis.request_models.salesforce.AccountsRequestModel;

public class SapContactsAsync implements Processor {

	@BeanInject("loggerRef")
	private Logger LOG = LoggerFactory.getLogger(SapContactsAsync.class);
	
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
	
	
	@PropertyInject("{{MSG.ORIGIN.CC_ERP}}")
	private static String msgOriginCCERP;
	@PropertyInject("{{MSG.ENTITY.CC_ERP}}")
	private static String msgEntityCCERP; 
	@PropertyInject("{{JMS.TYPE.CC_ERP}}")
	private static String jmsTypeCCERP; 
	@PropertyInject("{{ENTITY.CONTEXT.CC_ERP}}")
	private static String entityContextCCERP; 
	
	@PropertyInject("{{MSG.ORIGIN.AC_ERP}}")
	private static String msgOriginACERP; 
	@PropertyInject("{{MSG.ENTITY.AC_ERP}}")
	private static String msgEntityACERP; 
	@PropertyInject("{{JMS.TYPE.AC_ERP}}")
	private static String jmsTypeACERP; 
	@PropertyInject("{{ENTITY.CONTEXT.AC_ERP}}")
	private static String entityContextACERP;
	
	
	@PropertyInject("{{MSG.ORIGIN.CC_CRM}}")
	private static String msgOriginCCCRM; 
	@PropertyInject("{{MSG.ENTITY.CC_CRM}}")
	private static String msgEntityCCCRM; 
	@PropertyInject("{{JMS.TYPE.CC_CRM}}")
	private static String jmsTypeCCCRM; 
	@PropertyInject("{{ENTITY.CONTEXT.CC_CRM}}")
	private static String entityContextCCCRM; 
	
	
	@PropertyInject("{{MSG.ORIGIN.AC_CRM}}")
	private static String msgOriginACCRM; 
	@PropertyInject("{{MSG.ENTITY.AC_CRM}}")
	private static String msgEntityACCRM; 
	@PropertyInject("{{JMS.TYPE.AC_CRM}}")
	private static String jmsTypeACCRM;
	@PropertyInject("{{ENTITY.CONTEXT.AC_CRM}}")
	private static String entityContextACCRM; 
	
	
	
	Gson gson = new Gson();
	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void process(Exchange exchange) throws Exception {
		 String bodySalesforce = null;
		
	     LOG.debug("salida de url:"+ exchange.getIn().getHeader("CamelServletContextPath"));
			if (String.valueOf(exchange.getIn().getHeader("CamelServletContextPath")).contains("/events/erp/updates/customers/{customer-id-erp}")) {
				exchange.getOut().setHeader("ID_PARAM", exchange.getIn().getHeader("customer-id-erp"));
				AccountsRequestModel body = (AccountsRequestModel) exchange.getIn().getBody(AccountsRequestModel.class);
				bodySalesforce = gson.toJson(body);
				LOG.debug("Json enviado a la Queue:\n" + bodySalesforce);
			}


			if (String.valueOf(exchange.getIn().getHeader("CamelServletContextPath")).contains("/events/erp/updates/customers/{customer-id-erp}")) {
				exchange.getOut().setHeader("MSG_ORIGIN", msgOriginCRM);
				exchange.getOut().setHeader("MSG_ENTITY", msgEntityCRM);
				exchange.getOut().setHeader("JMSType", jmsTypeCRM);
				exchange.getOut().setHeader("entityContext", entityContextCRM);
			}

			if (String.valueOf(exchange.getIn().getHeader("CamelServletContextPath")).equalsIgnoreCase("/api/data/erp/accounts/{id}")) {
				exchange.getOut().setHeader("MSG_ORIGIN", msgOriginERP);
				exchange.getOut().setHeader("MSG_ENTITY", msgEntityERP);
				exchange.getOut().setHeader("JMSType", jmsTypeERP);
				exchange.getOut().setHeader("entityContext", entityContextERP);
			}
			
			if (String.valueOf(exchange.getIn().getHeader("CamelServletContextPath")).contains("/api/events/erp/updates/contact-relationships/{contact-relationship-id-erp}")) {
				exchange.getOut().setHeader("MSG_ORIGIN", msgOriginACERP);
				exchange.getOut().setHeader("MSG_ENTITY", msgEntityACERP);
				exchange.getOut().setHeader("JMSType", jmsTypeACERP);
				exchange.getOut().setHeader("entityContext", entityContextACERP);
				LOG.debug("Accountcontacts erp:"+ exchange.getIn().getHeader("contact-relationship-id-erp") );
				exchange.getOut().setHeader("contact-relationship-id-erp",exchange.getIn().getHeader("contact-relationship-id-erp"));
				exchange.getOut().setHeader("MSG_ID",exchange.getIn().getHeader("MSG_ID"));
			    bodySalesforce = objectMapper.writeValueAsString(exchange.getIn().getBody());
				
			}
			
			if (String.valueOf(exchange.getIn().getHeader("CamelServletContextPath")).contains("/api/events/crm/updates/contact-relationships/{contact-relationship-id-crm}")) {
				exchange.getOut().setHeader("MSG_ORIGIN", msgOriginACCRM);
				exchange.getOut().setHeader("MSG_ENTITY", msgEntityACCRM);
				exchange.getOut().setHeader("JMSType", jmsTypeACCRM);
				exchange.getOut().setHeader("entityContext", entityContextACCRM);
				exchange.getOut().setHeader("contact-relationship-id-crm",exchange.getIn().getHeader("contact-relationship-id-crm"));
				exchange.getOut().setHeader("MSG_ID",exchange.getIn().getHeader("MSG_ID"));
			    bodySalesforce = objectMapper.writeValueAsString(exchange.getIn().getBody());
			}
			
			
			if (String.valueOf(exchange.getIn().getHeader("CamelServletContextPath")).contains("/api/events/crm/updates/contacts/{contact-id-crm}")) {
				exchange.getOut().setHeader("MSG_ORIGIN", msgOriginCCCRM);
				exchange.getOut().setHeader("MSG_ENTITY", msgEntityCCCRM);
				exchange.getOut().setHeader("JMSType", jmsTypeCCCRM);
				exchange.getOut().setHeader("entityContext", entityContextCCCRM);
				LOG.debug("changecontacts crm:"+ exchange.getIn().getHeader("contact-id-crm") );
				exchange.getOut().setHeader("contact-id-crm",exchange.getIn().getHeader("contact-id-crm"));
				exchange.getOut().setHeader("MSG_ID",exchange.getIn().getHeader("MSG_ID"));
				bodySalesforce = objectMapper.writeValueAsString(exchange.getIn().getBody());
			}		
			
			if (String.valueOf(exchange.getIn().getHeader("CamelServletContextPath")).contains("/api/events/erp/updates/contacts/{contact-id-erp}")) {
				
				exchange.getOut().setHeader("MSG_ORIGIN", msgOriginCCERP);
				exchange.getOut().setHeader("MSG_ENTITY", msgEntityCCERP);
				exchange.getOut().setHeader("JMSType", jmsTypeCCERP);
				exchange.getOut().setHeader("entityContext", entityContextCCERP);
				exchange.getOut().setHeader("contact-id-erp",exchange.getIn().getHeader("contact-id-erp"));
				LOG.debug("changecontacts erp:"+ exchange.getIn().getHeader("contact-id-erp") );
				exchange.getOut().setHeader("contact-id-erp",exchange.getIn().getHeader("contact-id-erp"));
				exchange.getOut().setHeader("MSG_ID",exchange.getIn().getHeader("MSG_ID"));
				bodySalesforce = objectMapper.writeValueAsString(exchange.getIn().getBody());
			}

			exchange.getOut().setHeader("Content-Type", "application/json;charset=UTF-8");
			exchange.getOut().setHeader("MSG_ID", exchange.getIn().getHeader("ORIGIN_MSG_ID"));
			exchange.getOut().setHeader("ORIGIN_MSG_ID", exchange.getIn().getHeader("ORIGIN_MSG_ID"));

			LOG.debug("sALIDA DE DATOS ENTRADA"+ bodySalesforce );
//			JSONObject jsonObject = new JSONObject(bodySalesforce);
			exchange.getOut().setBody(bodySalesforce);


	}

}