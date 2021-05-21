package co.com.legis.processors;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONArray;
import org.json.JSONObject;

import co.com.legis.dto.ResponseSalesforceDto;

public class JournalBodyProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String inBody = (String) exchange.getIn().getBody();
        if (inBody.trim() != null && !inBody.trim().isEmpty()) {
            if (inBody.substring(0, 1).equals("{")) {
                exchange.setProperty("dataType", "json");
            } else {
                if (inBody.substring(0, 1).equals("[")) {
                    exchange.setProperty("dataType", "array");
                    List<ResponseSalesforceDto> arrayListResponse = new ArrayList<ResponseSalesforceDto>();
//                    List<String> arrayListResponse = new ArrayList<String>();
                    String rBody = "";
                    StringBuilder cadBody = new StringBuilder(inBody);
                    cadBody.setCharAt(inBody.length() - 1, ' ');
                    rBody = cadBody.toString();
                    rBody = rBody.replaceFirst("[\\[]", "").trim();

                    String[] b = rBody.split("\\}\\,");
                    for (int i = 0; i < b.length; i++) {
                        ResponseSalesforceDto jsonArray = new ResponseSalesforceDto();
                        JSONObject jsonObject = null;
                        JSONArray jArray = null;
                        if (b[i].substring(b[i].length() - 1, b[i].length()).equals("}")) {
                            jsonObject = new JSONObject(b[i]);
                        } else {
                            jsonObject = new JSONObject(b[i].concat("}"));
                        }

                        jsonArray.setMessage(jsonObject.getString("message") == null || jsonObject.getString("message").trim().equals("") ? null : jsonObject.getString("message"));
                        jsonArray.setErrorCode(jsonObject.getString("errorCode") == null || jsonObject.getString("errorCode").trim().equals("") ? null : jsonObject.getString("errorCode"));

                        ArrayList<String> listdata = new ArrayList<String>();

                        try {
                            jArray = jsonObject.getJSONArray("fields") == null ? null : jsonObject.getJSONArray("fields");
                        } catch (Exception e) {
                            jArray = null;
                        }
                        if (jArray != null) {
                            for (int j = 0; j < jArray.length(); j++) {
                                listdata.add(jArray.getString(j));
                            }
                        }
                        jsonArray.setFields(listdata);
                        arrayListResponse.add(jsonArray);
                    }
                    exchange.getIn().setBody(arrayListResponse);
                } else {
                    if (inBody.substring(0, 1).equals("<")) {
                        exchange.setProperty("dataType", "html");
                    } else {
                        exchange.setProperty("dataType", "string");
                    }
                }
            }
        } else {
            exchange.setProperty("dataType", "vacioNull");
        }
    }
}
