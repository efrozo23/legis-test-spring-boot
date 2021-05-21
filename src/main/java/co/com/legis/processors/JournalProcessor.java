package co.com.legis.processors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import co.com.legis.basehal_models.JournalDTO;
import co.com.legis.basehal_models.JournalModel;
import co.com.legis.basehal_models.JournalModelLinks;
import co.com.legis.basehal_models.JournalModelSelf;

public class JournalProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        List<JournalModel> listResponse = (List<JournalModel>) exchange.getProperty("listResponse");
        ObjectMapper mapper = new ObjectMapper();
        Gson gson = new Gson();
        JournalModel journalModel = null;
        JournalDTO journalDTO = null;

        String json = gson.toJson(exchange.getIn().getBody(), LinkedHashMap.class);

        journalDTO = mapper.readValue(json, JournalDTO.class);

        journalModel = mapJournal(journalDTO);

        listResponse.add(journalModel);
        exchange.setProperty("listResponse", listResponse);
    }

    private JournalModel mapJournal(JournalDTO journalDTO) throws Exception {

        JournalModel objJournalModel = new JournalModel();
        JournalModelLinks links = new JournalModelLinks();
        JournalModelSelf self = new JournalModelSelf();
        String hRef = "";
        hRef = "/api/utils/logs/journal-messages/journal-id/" + journalDTO.getJournalId();
        self.sethRef(hRef);
        links.setSelf(self);

        objJournalModel.setLinks(links);
        objJournalModel.setJournalId(journalDTO.getJournalId());
        objJournalModel.setMsgOrigin(journalDTO.getMsgOrigin());
        objJournalModel.setMsgEntity(journalDTO.getMsgEntity());
        objJournalModel.setCamelMsgId(journalDTO.getCamelMsgId());
        objJournalModel.setEntryType(journalDTO.getEntryType());
        objJournalModel.setEntrySummary(journalDTO.getEntrySummary());
        objJournalModel.setEntryPayload(journalDTO.getEntryPayload());
        objJournalModel.setCreateUser(journalDTO.getCreateUser());

        if (journalDTO.getRegistrationDate() != null) {
            String strRegistrationDate = journalDTO.getRegistrationDate();
            Date registrationDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse(strRegistrationDate);

            objJournalModel.setRegistrationDate(registrationDate);
        }
        if (journalDTO.getCreateDate() != null) {
            String strCreateDate = journalDTO.getCreateDate();
            Date createDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse(strCreateDate);

            objJournalModel.setCreateDate(createDate);
        }

        if (journalDTO.getModifyDate() != null) {
            String strModifyDate = journalDTO.getModifyDate();
            Date modifyDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse(strModifyDate);

            objJournalModel.setModifyDate(modifyDate);
        }

        return objJournalModel;
    }

}
