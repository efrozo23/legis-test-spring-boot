package co.com.legis.dto;

import java.util.ArrayList;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect
public class ResponseSalesforceDto {

    @JsonProperty("message")
    @NotNull(message = "Problema de estructura objeto (message) nulo")
    @NotEmpty(message = "Problema de estructura objeto (message) vacio")
    private String message;

    @JsonProperty("errorCode")
    @NotNull(message = "Problema de estructura objeto (errorCode) nulo")
    @NotEmpty(message = "Problema de estructura objeto (errorCode) vacio")
    private String errorCode;

    @JsonProperty("fields")
    @NotNull(message = "Problema de estructura objeto (fields) nulo")
    @NotEmpty(message = "Problema de estructura objeto (fields) vacio")
    @JsonInclude(Include.NON_EMPTY)
    private ArrayList<String> fields;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public ArrayList<String> getFields() {
        return fields;
    }

    public void setFields(ArrayList<String> fields) {
        this.fields = fields;
    }
}
