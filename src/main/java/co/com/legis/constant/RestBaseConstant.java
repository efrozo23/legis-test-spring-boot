package co.com.legis.constant;

public class RestBaseConstant {
	
	private RestBaseConstant() {
		
	}

	/**
	 * Empty character string
	 */
	public static final String EMPTY_STRING = "";
	
	public static final String TARGET_ID_QUEUE = "TARGET_ID_QUEUE";
	
	
	
	public static final String PROPERTY_MESSAGE_ID = "message_id";
	
	public static final String PROPERTY_MSG_ORIGIN = "MSG_ORIGIN";
	
	public static final String PROPERTY_BODY = "BODY_OLD";
	
	public static final String PROPERTY_BODY_SIZE = "BODY_SIZE";
	
	public static final String PROPERTY_MSG_ENTITY = "MSG_ENTITY";
	
	public static final String PROPERTY_INCOMING = "incoming";

	public static final String PROPERTY_PROCESSED_DELIVER = "processed_deliver";

	public static final String PROPERTY_PROCESSED_NOTIFICATION = "processed_notification";

	public static final String PROPERTY_PROCESSED = "processed";

	public static final String PROPERTY_REJECTED = "rejected";

	public static final String PROPERTY_ERROR_DETAIL ="error_detail";
	
	public static final String ROUTE_UPDATE_REPORT_SERVICE = "direct-vm:update-report-service-integracion";
	
	public static final String ROUTE_INSERT_REPORT_ERROR = "direct-vm:persist-detail-report-error";
	
	public static final String ROUTE_QUERY_REPORT_SERVICE= "direct-vm:query-exists-row-report";
	
	public static final String ROUTE_INSERT_REPORT_SERVICE = "direct-vm:persist-report-service";
	
	public static final String PROPERTY_STATUS_REPORT = "status_report";
	
	public static final String PROPERTY_MSG_ID_ORIGIN = "MSG_ID_ORIGIN";
	
	public static final String PROPERTY_REQUEST_ERROR = "request";
}
