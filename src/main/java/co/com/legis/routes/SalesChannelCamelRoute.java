package co.com.legis.routes;

import javax.validation.ValidationException;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.apache.camel.model.dataformat.JsonLibrary;

import co.com.legis.common.ExceptionHandling;
import co.com.legis.constant.RestBaseConstant;
import co.com.legis.hal.utils.Async_Base_Hal_Response;

public class SalesChannelCamelRoute extends RouteBuilder {

	public final String MSG_ENTITY = "MSG_ENTITY";
	public final String MSG_ORIGIN = "MSG_ORIGIN";
	public final String JMS_TYPE = "JMSType";
	public final String TARGET_ID_QUEUE = "TARGET_ID_QUEUE";
	public final String NUMBER_SILENIO_ORDER = "number_silenio_order";

	@SuppressWarnings({ "unchecked" })
	@Override
	public void configure() throws Exception {
		ExceptionHandling exHandling = new ExceptionHandling();
		exHandling.AttachException(this);
		//@formatter:off
        from("direct-vm:SyncResponse").routeId("SYNC-RESPONSE").streamCaching()
            .doTry()
                .to("bean-validator://beanValidation")
        	    .choice()
    	        	.when(simple("${header.channel-name} contains '{{sc.vtex.id}}'"))
    	        		.to("direct-vm:getVtexSkuId")
    	        	.endChoice()
    	        	
        	        .when(simple("${header.channel-name} contains '{{sc.prestashop.id}}'"))
    	        		.to("direct-vm:getPrestashopSkuId")	
    	        	.endChoice()
    	        	
    	        .end()
                .setHeader(Exchange.CONTENT_TYPE, constant("application/hal+json"))	
            .endDoTry()				
            .doCatch(BeanValidationException.class, ValidationException.class)
                .onWhen(exceptionMessage().contains("Severe"))	
                .throwException(java.lang.Exception.class, "Bean validation error ${header.CamelHttpUri} has this errors: ${body}")
            .end()
        ;		
	from("direct-vm:AsyncResponse").routeId("ASYNC-RESPONSE").streamCaching()
	    .doTry()
	        .log(LoggingLevel.INFO,"Iniciando validacion de la entrada ${headers}") 
	        .to("bean-validator://beanValidation")		
	        .toD("direct://incommingroute")
	        .setProperty("status_report", constant("processed_deliver"))
	        .setHeader("status_report", constant("processed_deliver"))
	        .to("activemq:queue:{{queuePendingName}}?exchangePattern=InOnly")
	        .log("Enviado a la cola ${header.MSG_ENTITY} || ${header.JMSType}")
	        .log(LoggingLevel.INFO,"Mesagge id generated: ${header.ORIGIN_MSG_ID}") 
	        
	        //TODO GUARDAR LA ENTRADA
	       // .to("bean:bodyUtil?method=getStringJson(${exchange})")
	        .log(LoggingLevel.INFO,"Convierte body a string: ${body}") 
	        
	        .convertBodyTo(String.class)
	        .setProperty("strBody", simple("${body}"))
	        .log(LoggingLevel.INFO,"input Body:  ${property.strBody}") 
	        .log(LoggingLevel.INFO,"salida de message-id${header.MSG_ID}")
	        .setProperty(RestBaseConstant.PROPERTY_MESSAGE_ID,simple("${header.MSG_ID}"))
	        .setProperty(RestBaseConstant.PROPERTY_INCOMING,constant(1))
	        .setProperty(RestBaseConstant.PROPERTY_MSG_ORIGIN,simple("${property.MSG_ORIGIN}"))
	        .setProperty(RestBaseConstant.PROPERTY_MSG_ENTITY,simple("${property.MSG_ENTITY}"))   	       	        
            .to(RestBaseConstant.ROUTE_INSERT_REPORT_SERVICE)
	        .log(LoggingLevel.INFO,"Salida de propiedades hacia la cola::: ${property.ORIGIN_MSG_ID}, ${property.MSG_ORIGIN}, ${property.MSG_ENTITY}")
            .to("bean:syncJournalMapper?method=syncJournalBodyMapper(${exchange}, ${property.strBody}, ${property.ORIGIN_MSG_ID}, ${property.MSG_ORIGIN}, ${property.MSG_ENTITY})")
            .to("vm:JournalMsgsItemSync")
            // FIN GUARDAR LA ENTRADA
   
	        .bean(Async_Base_Hal_Response.class, "GetHalRepresentation(${exchange}, 202, 'Accepted', 'The request was received correctly. It will pass to validation and processing.','https://httpstatuses.com/202')")			
	        .setHeader(Exchange.CONTENT_TYPE, constant("application/hal+json"))	
     
	    .endDoTry()				
	    .doCatch(BeanValidationException.class, ValidationException.class)
	        .log(LoggingLevel.INFO,"-------entro??--------")
	        .onWhen(exceptionMessage().contains("Severe"))
	        .throwException(java.lang.Exception.class, "Bean validation error ${header.CamelHttpUri} has this errors: ${body}")
	        //podriamos colacar aqui para ver estadisticas de los que ingresan con errores de mapeo
	
	    .end()
	;		
	from("direct://incommingroute").routeId("INCOMMING").streamCaching()
	.description("Assign the corresponding headers to the configuration according to the process")
	    .choice()
		    /**
		     * CANALES DE VENTAS
		     */
	        .when(simple("${body} is 'co.com.legis.request_models.saleschannel.PriceRequestModel'"))
	        .description("Proceso de Actualizar precios de SKU en canal de ventas.")
	            .log(LoggingLevel.INFO,"Proceso de Actualizar precios de SKU en canal de ventas") 
	            .toD("direct-vm:getMsgId")
	            .marshal().json(JsonLibrary.Jackson)
	            .setHeader(MSG_ENTITY, simple("${properties:sc.prices.msg.entity.${header.channel-name}}"))
	            .setHeader(JMS_TYPE, simple("${properties:sc.prices.msg.entity.${header.channel-name}}"))
	            .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
	            .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.${header.channel-name}}"))
	        .endChoice()
	        
	        .when(simple("${body} is 'co.com.legis.request_models.saleschannel.sap.RequestSap'"))
	        .description("Proceso para realizar pedidos hacia SAP.")
	            .log(LoggingLevel.INFO,"Proceso de realizar pedidos hacia sap ") 
	            .toD("direct-vm:getMsgId")
	            .marshal().json(JsonLibrary.Jackson)
	            .setHeader(MSG_ENTITY, simple("${properties:sc.orders.msg.entity.${header.channel-name}}"))
	            .setHeader(MSG_ORIGIN, simple("${properties:sc.orders.msg.origin.${header.channel-name}}"))	
	            .setHeader(JMS_TYPE, simple("${properties:sc.orders.msg.entity.${header.channel-name}}"))
	            .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.${header.channel-name}}"))
	            .log(LoggingLevel.DEBUG,"Los Headers actuales son : ${headers}")
	        .endChoice()
	        
        	.when(simple("${body} is 'co.com.legis.request_models.saleschannel.InventoryRequestModel'"))
        	.description("Proceso de Actualizar inventario en canal de ventas para 1 almacen del SKU")
        	    .toD("direct-vm:getMsgId")
        	    .marshal().json(JsonLibrary.Jackson)
        	    .setHeader(MSG_ENTITY, simple("${properties:sc.inventory.msg.entity.${header.channel-name}}"))
        	    .setHeader(JMS_TYPE, simple("${properties:sc.inventory.msg.entity.${header.channel-name}}"))
        	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
        	    .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.${header.channel-name}}"))
        	.endChoice()
        	
        	.when(simple("${body} is 'co.com.legis.request_models.saleschannel.OrderDispatchRequestModel'"))
        	.description("Actualizar Estado Despacho de Pedido en Canal de Ventas")
        	    .toD("direct-vm:getMsgId")
        	    .marshal().json(JsonLibrary.Jackson)
        	    .setHeader(MSG_ENTITY, simple("${properties:sc.dispatches.msg.entity.${header.channel-name}}"))
        	    .setHeader(JMS_TYPE, simple("${properties:sc.dispatches.msg.entity.${header.channel-name}}"))
        	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
        	    .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.${header.channel-name}}"))
        	.endChoice()
        	
        	.when(simple("${body} is 'co.com.legis.request_models.saleschannel.OrderInvoiceRequestModel'"))
        	.description("Actualizar Factura de Pedido en Canal de Ventas")
        	    .toD("direct-vm:getMsgId")
        	    .marshal().json(JsonLibrary.Jackson)
        	    .setHeader(MSG_ENTITY, simple("${properties:sc.invoice.msg.entity.${header.channel-name}}"))
        	    .setHeader(JMS_TYPE, simple("${properties:sc.invoice.msg.entity.${header.channel-name}}"))
        	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
        	    .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.${header.channel-name}}"))
        	.endChoice()
        	
        	.when(simple("${body} is 'co.com.legis.request_models.saleschannel.OrderConfirmationRequestModel'"))
	        	.description("Confirm Order generation in ERP")
	        	.toD("direct-vm:getMsgId")
	        	.marshal().json(JsonLibrary.Jackson)
	        	.setHeader(MSG_ENTITY, simple("${properties:sc.order.confirmation.msg.entity.${header.channel-name}}"))
	        	.setHeader(JMS_TYPE, simple("${properties:sc.order.confirmation.msg.entity.${header.channel-name}}"))
	        	.setHeader(MSG_ORIGIN, simple("${header.source_system}"))
	        	.setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.${header.channel-name}}"))
        	.endChoice()
        	
        	
        	.when(simple("${body} is 'co.com.legis.request_models.saleschannel.BidOrderRequestModel'"))
	        	.description("Licitacion de pedido en Silenio")
	        	.log(LoggingLevel.INFO,"Ingreso a procesar solicitud con number_silenio_orders: ${headers.number_silenio_order}")
	        	.toD("direct-vm:getMsgId")
	        	.marshal().json(JsonLibrary.Jackson)
	        	.setHeader(MSG_ENTITY, simple("${properties:sc.order.bid.msg.entity.${header.channel-name}}"))
	        	.setHeader(JMS_TYPE, simple("${properties:sc.order.bid.msg.entity.${header.channel-name}}"))
	        	.setHeader(MSG_ORIGIN, simple("${header.source_system}"))
	        	.setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.${header.channel-name}}"))
	        	.setHeader(NUMBER_SILENIO_ORDER, simple("${headers.number_silenio_order}"))
	        	
        	.endChoice()
        	
        	
	        .when(simple("${body} is 'co.com.legis.request_models.saleschannel.CustomerRequestModel'"))
	        .description("Proceso de Actualizar cliente en los canal de ventas.")
	            .log(LoggingLevel.INFO,"Proceso de Actualizar clientes en los canal de ventas") 
	            .toD("direct-vm:getMsgId")
	            .marshal().json(JsonLibrary.Jackson)
	            .setHeader(MSG_ENTITY, simple("${properties:sc.customer.msg.entity.${header.channel-name}}"))
	            .setHeader(JMS_TYPE, simple("${properties:sc.customer.msg.entity.${header.channel-name}}"))
	            .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
	            .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.${header.channel-name}}"))
	        .endChoice()
	            
	            
	         .when(simple("${body} is 'co.com.legis.request_models.saleschannel.OrderListDispatchesModel'"))
	         .description("Envio de Orders LIST Dispatches ")
	    		    .log(LoggingLevel.INFO,"Incio a procesar OrderListDispatchesModel :::")
	    		    .log(LoggingLevel.INFO, "ETIQUETA: ${exchangeProperty.tag}")
		            .toD("direct-vm:getMsgId")
		            .marshal().json(JsonLibrary.Jackson)
		    	    .setHeader(MSG_ENTITY, simple("${properties:sc.order.dispatches.msg.entity.${header.channel-name}}"))
		    	    .setHeader(JMS_TYPE, simple("${properties:sc.order.dispatches.msg.entity.${header.channel-name}}"))
		    	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
		    	    .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.${header.channel-name}}"))
	            
	            
	        .endChoice()
	        
        	
// **************************************** INICIO SERVICIOS DE TRANSPORTISTAS **************************************** //        	
        	.when(simple("${body} is 'co.com.legis.request_models.transport.document.sap.DocumentTransport'"))
        	.description("Documento de transporte enviado desde SAP")
            	    .toD("direct-vm:getMsgId")
            	    .marshal().json(JsonLibrary.Jackson)
            	    .setHeader(MSG_ENTITY, simple("{{MSG_ENTITY.DOCUMENT.TRANSPORT}}_${header.courrier-company-name}"))
            	    .setHeader(JMS_TYPE, simple("{{JMS_TYPE.DOCUMENT.TRANSPORT}}_${header.courrier-company-name}"))
            	    .setHeader(MSG_ORIGIN, simple("${header.source_system}")) 
            	    .setHeader(TARGET_ID_QUEUE, simple("{{sc.queue.target.msg.entity.subs01}}"))
        	.endChoice()
        	
        	.when(simple("${body} is 'co.com.legis.request_models.transport.shipment.Shipment'"))
        	.description("Asignacion de guia desde Silenio")
        	    .toD("direct-vm:getMsgId")
        	    .marshal().json(JsonLibrary.Jackson)
        	    .setHeader(MSG_ENTITY, simple("{{MSG_ENTITY.ASSIGN.GUIDE}}"))
                    .setHeader(JMS_TYPE, simple("{{JMS_TYPE.ASSIGN.GUIDE}}"))
        	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
        	    .setHeader(TARGET_ID_QUEUE, simple("{{sc.queue.target.msg.entity.subs01}}"))
        	.endChoice()
        	
        	.when(simple("${body} is 'co.com.legis.request_models.transport.tracking.Tracking'"))
        	.description("Envio de novedad desde Silenio")
        	    .toD("direct-vm:getMsgId")
        	    .marshal().json(JsonLibrary.Jackson)
        	    .setHeader(MSG_ENTITY, simple("{{MSG_ENTITY.TRACKING}}"))
                .setHeader(JMS_TYPE, simple("{{JMS_TYPE.TRACKING}}"))
        	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
        	    .setHeader(TARGET_ID_QUEUE, simple("{{sc.queue.target.msg.entity.subs01}}"))
        	.endChoice()
        	
        	/**
        	 * Se agrega el when para validar en servicio de SILENIO-SAP
        	 */
        	 .when(simple("${body} is 'co.com.legis.request_models.saleschannel.OrderListMixModel'"))
	         .description("Envio de Orders LIST Dispatches ")
	    		    .log(LoggingLevel.INFO,"Se proceso la respuesta para order-mixed :::")
	    		    .log(LoggingLevel.INFO, "Etiqueta: ${exchangeProperty.tag}")
		            .toD("direct-vm:getMsgId")
		            .marshal().json(JsonLibrary.Jackson)
		    	    .setHeader(MSG_ENTITY, simple("${properties:sc.order.mix.msg.entity.${header.channel-name}}"))
		    	    .setHeader(JMS_TYPE, simple("${properties:sc.order.mix.msg.entity.${header.channel-name}}"))
		    	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
		    	    .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.${header.channel-name}}"))
	            
	        .endChoice()
	        /**
	         * Se agrego el when para generar el reporte en SALESFORCE
	         */
	        .when(simple("${body} is 'co.com.legis.request_models.saleschannel.salesforce.legis.GenerateReport'"))
	         .description("VALIDA QUE GENERE UN REPORTE")
	    		    .log(LoggingLevel.INFO,"Enviando mensaje a salesforce :::")
		            .toD("direct-vm:getMsgId")
		            .marshal().json(JsonLibrary.Jackson)
		    	    .setHeader(MSG_ENTITY, simple("${properties:crm.report.msg.entity}"))
		    	    .setHeader(JMS_TYPE, simple("${properties:crm.report.msg.entity}"))
		    	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
		    	    .setHeader(TARGET_ID_QUEUE, simple("${properties:crm.report.msg.queue}"))
	            
	        .endChoice()
	        
	        /**
	         * Se agrega el when para recaudo legis
	         */
	        .when(simple("${body} is 'co.com.legis.request_models.collect.RequestCollect'"))
	         .description("VALIDA EL RECAUDO LEGIS")
	    		    .log(LoggingLevel.INFO,"Enviando mensaje a Recaudo Legis :::")
		            .toD("direct-vm:getMsgId")
		            .marshal().json(JsonLibrary.Jackson)
		    	    .setHeader(MSG_ENTITY, simple("${properties:collect.legis.msg.entity}"))
		    	    .setHeader(JMS_TYPE, simple("${properties:collect.legis.msg.entity}"))
		    	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
		    	    .setHeader(TARGET_ID_QUEUE, simple("${properties:collect.legis.msg.entity.queue}"))
	            
	        .endChoice()
	        
	        /**
	         * Se agrega el when para la notificación número de la orden de la oportunidad
	         */
	        .when(simple("${body} is 'co.com.legis.request_models.saleschannel.NotificationOrderModel'"))
	         .description("VALIDA LA NOTIFICACIÓN DE LA OPORTUNIDAD")
	    		    .log(LoggingLevel.INFO,"Enviando mensaje a Salesforce :::")
		            .toD("direct-vm:getMsgId")
		            .marshal().json(JsonLibrary.Jackson)
		    	    .setHeader(MSG_ENTITY, simple("${properties:sc.notification.order.number.entity}"))
		    	    .setHeader(JMS_TYPE, simple("${properties:sc.notification.order.number.entity}"))
		    	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
		    	    .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.salesforce}"))
	            
	        .endChoice()
	        
	        /**
	         * Se agrega el when para la notificación información de la factura
	         */
	        .when(simple("${body} is 'co.com.legis.request_models.saleschannel.NotInfoFacturaModel'"))
	         .description("NOTIFICA LA INFORMACIÓN DE LA FACTURA")
	    		    .log(LoggingLevel.INFO,"Enviando mensaje a Salesforce :::")
		            .toD("direct-vm:getMsgId")
		            .marshal().json(JsonLibrary.Jackson)
		    	    .setHeader(MSG_ENTITY, simple("${properties:sc.notification.info.factura.entity}"))
		    	    .setHeader(JMS_TYPE, simple("${properties:sc.notification.info.factura.entity}"))
		    	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
		    	    .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.salesforce}"))
	            
	        .endChoice()
	        
	        /**
	         * Se agrega el when para la notificación estado de la oportunidad
	         */
	        .when(simple("${body} is 'co.com.legis.request_models.saleschannel.NotiStatusOportunidad'"))
	         .description("NOTIFICA LA INFORMACIÓN ESTADO DE LA OPORTUNIDAD")
	    		    .log(LoggingLevel.INFO,"Enviando mensaje a Salesforce :::")
		            .toD("direct-vm:getMsgId")
		            .marshal().json(JsonLibrary.Jackson)
		    	    .setHeader(MSG_ENTITY, simple("${properties:sc.notification.status.oportunidad.entity}"))
		    	    .setHeader(JMS_TYPE, simple("${properties:sc.notification.status.oportunidad.entity}"))
		    	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
		    	    .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.salesforce}"))
	            
	        .endChoice()
	        
	        /**
	         * Se agrega el when para la el número de entrega a printux desde SAP
	         */
	        .when(simple("${body} is 'co.com.legis.request_models.saleschannel.InsideNumberModel'"))
	         .description("NÚMERO DE LA ENTREGA A PRINTUX")
	    		    .log(LoggingLevel.INFO,"Enviando mensaje a Printux :::")
		            .toD("direct-vm:getMsgId")
		            .marshal().json(JsonLibrary.Jackson)
		    	    .setHeader(MSG_ENTITY, simple("${properties:sc.inside.number.printux}"))
		    	    .setHeader(JMS_TYPE, simple("${properties:sc.inside.number.printux}"))
		    	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
		    	    .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.printux}"))
	            
	        .endChoice()
	        
	        .when(simple("${body} is 'co.com.legis.request_models.remissions.ResponseSapRemissionModel'"))
	         .description("REENVIO NÚMERO DE RESERVA")
	    		    .log(LoggingLevel.INFO,"Enviando mensaje a Printux :::")
		            .toD("direct-vm:getMsgId")
		            .marshal().json(JsonLibrary.Jackson)
		    	    .setHeader(MSG_ENTITY, simple("${properties:sc.forwarding.reservation.printux}"))
		    	    .setHeader(JMS_TYPE, simple("${properties:sc.forwarding.reservation.printux}"))
		    	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
		    	    .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.printux}"))
	            
	        .endChoice()

	        .when(simple("${body} is 'co.com.legis.request_models.saleschannel.salesforce.legis.WalletCollect'"))
	         .description("Envía actualización de cartera en Salesforce")
	    		    .log(LoggingLevel.INFO,"Envío mensaje a salesforce")
		            .toD("direct-vm:getMsgId")
		            .marshal().json(JsonLibrary.Jackson)
		    	    .setHeader(MSG_ENTITY, simple("${properties:crm.salesforce.wallet.legis}"))
		    	    .setHeader(JMS_TYPE, simple("${properties:crm.salesforce.wallet.legis}"))
		    	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
		    	    .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.salesforce}"))
	            
	        .endChoice()

	        
	        //Reporte de recaudo para salesforce desde SAP
        	.when(simple("${body} is 'co.com.legis.request_models.saleschannel.salesforce.legis.CollectReportModel'"))
        	.description("Proceso de Actualizar el reporte del recaudo en salesforce")
	        	    .toD("direct-vm:getMsgId")
	        	    .marshal().json(JsonLibrary.Jackson)
	        	    .setHeader(MSG_ENTITY, simple("${properties:sc.report.msg.entity.salesforce.collect}"))
	        	    .setHeader(JMS_TYPE, simple("${properties:sc.report.msg.entity.salesforce.collect}"))
	        	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
	        	    .setHeader(TARGET_ID_QUEUE, simple("${properties:sc.queue.target.msg.entity.salesforce}"))
        	.endChoice()
        	
        	.when(simple("${body} is 'co.com.legis.request_models.collect.DrupalCollectDataModel'"))
        	.description("Proceso de envio de recaudo desde ambito juridico a recaudo")
	        	    .toD("direct-vm:getMsgId")
	        	    .marshal().json(JsonLibrary.Jackson)
	        	    .setHeader(MSG_ENTITY, simple("${properties:sc.report.msg.entity.drupal.collect}"))
	        	    .setHeader(JMS_TYPE, simple("${properties:sc.report.msg.entity.drupal.collect}"))
	        	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
	        	    .setHeader(TARGET_ID_QUEUE, simple("${properties:collect.legis.msg.entity.queue}"))
	        	    .log(LoggingLevel.INFO,log,"----------- salida de proceso::: ${header.MSG_ENTITY} || ${header.JMSType}")
        	.endChoice()

        	//Envía información desde recaudo a SSO
        	
        	.when(simple("${body} is 'co.com.legis.request_models.collect.RequestActivation'"))
        	.description("Proceso que envía información a SSO")
        		.log(LoggingLevel.INFO, "Ingreso por SSO")
        	    .toD("direct-vm:getMsgId")
        	    .marshal().json(JsonLibrary.Jackson)
        	    .setHeader(MSG_ENTITY, simple("${properties:sc.activation.legis.sso}"))
        	    .setHeader(JMS_TYPE, simple("${properties:sc.activation.legis.sso}"))
        	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
        	    .setHeader(TARGET_ID_QUEUE, simple("${properties:collect.legis.msg.entity.queue}"))
        	    .log(LoggingLevel.INFO, "FINALIZO SET DE HEADERS")
        	.endChoice()
        	
        	//Envía información desde recaudo a SAP
        	
        	.when(simple("${body} is 'co.com.legis.request_models.collect.ExternalOrder'"))
        	.description("Proceso que envía información de RECAUDO A SAP")
        		.log(LoggingLevel.INFO, "Ingreso por RECAUDO A SAP")
        	    .toD("direct-vm:getMsgId")
        	    .marshal().json(JsonLibrary.Jackson)
        	    .setHeader(MSG_ENTITY, simple("${properties:sc.activation.external.order}"))
        	    .setHeader(JMS_TYPE, simple("${properties:sc.activation.external.order}"))
        	    .setHeader(MSG_ORIGIN, simple("${header.source_system}"))
        	    .setHeader(TARGET_ID_QUEUE, simple("${properties:sap.legis.queue.entity}"))
        	    .log(LoggingLevel.INFO, "FINALIZO SET DE HEADERS")
        	.endChoice()
	        
// **************************************** FIN SERVICIOS DE TRANSPORTISTAS **************************************** //
            .end()
            
            .setProperty(MSG_ENTITY, simple(" ${header.MSG_ENTITY}"))
            .setProperty(JMS_TYPE, simple(" ${header.JMS_TYPE}"))
            .setProperty(MSG_ORIGIN, simple(" ${header.MSG_ORIGIN}"))
        ;	
	from("direct-vm:getMsgId").routeId("GETMESSAGEID").streamCaching()
	.description("Get o put the message id")
	    .log(LoggingLevel.DEBUG,"Asginando message id") 
	    .choice()
	        .when(simple("${header.MSG_ID} == null or ${header.MSG_ID} == ''"))
	            .setHeader("MSG_ID", simple("${exchangeId}"))
	            .setHeader("ORIGIN_MSG_ID", simple("${header.MSG_ID}"))
	        .endChoice()
	    .end()
	;	
	from("direct-vm:RegisterOrdersLog").routeId("RegisterSapOrdersLog").streamCaching()
	.description("Ruta para loguear confirmacion de la respuesta de Sap")
            .log(LoggingLevel.INFO,"La respesta de Sap-Orders es ${body}")
        ;
	
	from("direct:RouteTagMixOrders").routeId("ROUTETAGMIXED").streamCaching()
		.description("Esta ruta setea una property para identificar el rest order-mix-list")
		.setProperty("tag", constant("OK"))
    	.log(LoggingLevel.INFO, "INICIO RUTA DE SET PROPERTY")
    	.end();
	
	

	  }
	//@formatter:on
	}
	
	
