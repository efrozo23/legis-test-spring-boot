package co.com.legis.rest.base.mapper;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.camel.BeanInject;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.legis.dto.SyncItemDTO;
import co.legis.dto.SyncParamsDTO;
import co.legis.dto.SyncPendingDTO;

public class SyncJournalMapper {

	@BeanInject("loggerRef")
	private Logger LOG = LoggerFactory.getLogger(SyncJournalMapper.class);

	public void syncJournalBodyMapper(Exchange exchange, String oldBody, String originMsgId, String msgOrigin,
			String msgEntity) throws Exception {

		LOG.debug("Mapeando objeto en VtexAdapter para SyncItemDTO");
		SyncItemDTO syncItem = new SyncItemDTO();

		SyncParamsDTO params = new SyncParamsDTO();
		params.setMsgOrigin(msgOrigin);
		params.setMsgEntity(msgEntity);

		syncItem.setPayload(oldBody);
		syncItem.setCamelMsgID(originMsgId);
		syncItem.setSyncParam(params);

		ObjectMapper mapper = new ObjectMapper();
		String fb = mapper.writeValueAsString(syncItem);
		LOG.debug("Respuesta de la ruta=>\n {}", fb);
		exchange.getIn().setBody(fb);
	}

	public void syncSyncPendingBodyMapper2(Exchange exchange, String oldBody, String originMsgId, String msgOrigin,
			String msgEntity) throws Exception {

		LOG.debug("Mapeando objeto en VtexAdapter para SyncItemDTO");
		SyncPendingDTO syncPendingDTO = new SyncPendingDTO();
		syncPendingDTO.setIdPendingMessage(BigDecimal.ZERO);
		syncPendingDTO.setMsgOrigin(msgOrigin);
		syncPendingDTO.setMsgEntity(msgEntity);

		syncPendingDTO.setPayload(oldBody);

		syncPendingDTO.setMsgError("Ocurrio un error de pruebas");
		syncPendingDTO.setAutoRetry("Y");
		syncPendingDTO.setCamelMsgId(originMsgId);

		long retires = 2;
		long parameterizedId = 10004;
		syncPendingDTO.setParameterizedId(BigDecimal.valueOf(parameterizedId));
		syncPendingDTO.setRetries(BigDecimal.valueOf(retires));
		syncPendingDTO.setCreateDate(new Date());
		syncPendingDTO.setModifyDate(new Date());

		LOG.info("Los reintentos enviados son {}", syncPendingDTO.getRetries().intValue());
		LOG.info("Los El payload son {}", syncPendingDTO.getPayload());

		ObjectMapper mapper = new ObjectMapper();
		String fb = mapper.writeValueAsString(syncPendingDTO);
		LOG.debug("Respuesta de la ruta=>\n {}", fb);
		exchange.getOut().setHeader("MSG_DESTINATION", msgEntity);
		exchange.getOut().setHeader("NORMALIZED", "true");
		exchange.getOut().setBody(fb);
	}
	
	
	public void syncSyncPendingBodyMapper(Exchange exchange, String oldBody, String originMsgId, String msgOrigin,
			String msgEntity) throws Exception {

		LOG.debug("Mapeando objeto en VtexAdapter para SyncItemDTO");
		SyncItemDTO syncItem = new SyncItemDTO();

		SyncParamsDTO params = new SyncParamsDTO();
		params.setMsgOrigin(msgOrigin);
		params.setMsgEntity(msgEntity);

		syncItem.setPayload(oldBody);
		syncItem.setCamelMsgID(originMsgId);
		syncItem.setSyncParam(params);
		syncItem.setRetries(BigDecimal.ZERO);
		syncItem.setException("Ocurrio un error al consumir SAP.");
		// syncItem.setHeaders("Prueba Headers");

		ObjectMapper mapper = new ObjectMapper();
		String fb = mapper.writeValueAsString(syncItem);
		LOG.debug("Respuesta de la ruta=>\n {}", fb);
		exchange.getOut().setHeader("MSG_DESTINATION", msgEntity);
		exchange.getOut().setHeader("MSG_ORIGIN", "VTEX");
		exchange.getOut().setHeader("MSG_ENTITY", "SAP_PRICES_RESPONSE");
		exchange.getOut().setHeader("NORMALIZED", "true");
		exchange.getOut().setHeader("MSG_ID", originMsgId);
		exchange.getOut().setHeader("PARAMETERIZEDID", "10007");
		exchange.getOut().setBody(fb);
	}

}
