package co.com.legis.request_execution;

import org.apache.camel.Exchange;

public interface IRequestExecution 
{
	void DoRequest(Exchange exchange);
}
