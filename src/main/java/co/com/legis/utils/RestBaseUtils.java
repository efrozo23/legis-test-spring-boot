package co.com.legis.utils;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.com.legis.constant.RestBaseConstant;

public class RestBaseUtils {

	/**
	 * Get the json character string corresponding to the object contained in the
	 * body
	 * 
	 * @param objBody
	 * @return {@link String}
	 */
	public static String getStringJsonFromBodyObject(Object objBody) {

		String jsonString = RestBaseConstant.EMPTY_STRING;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			jsonString = objectMapper.writeValueAsString(objBody);
		} catch (JsonProcessingException e) {
			JSONObject jsonObject = new JSONObject(objBody);
			jsonString = jsonObject.toString();
		}

		return jsonString;
	}

}
