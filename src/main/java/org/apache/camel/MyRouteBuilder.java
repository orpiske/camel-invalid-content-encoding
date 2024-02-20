package org.apache.camel;

import org.apache.camel.builder.RouteBuilder;

/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {

        from("jetty:http://127.0.0.1:9000")
				.id("http-test-endpoint")
                .log("INCOMING: Charset-Name=${exchangeProperty.CamelCharsetName} Content-Encoding=${header[Content-Encoding]}")
				// If the body is an InputStream then the faulty logic is bypassed.  If it is a String, then we hit it
				//  (although there is another complex scenario where it might get bypassed -
				//   when GZIPHelper.isGzip(exchange.getIn()) is true)
				.to("direct:check-content-encoding")
				.convertBodyTo(String.class)
                .to("http://localhost:9000/downstream1?bridgeEndpoint=true")
                .setBody(constant("done"))
                ;

        from("jetty:http://127.0.0.1:9000/downstream1")
				.id("http-downstream-endpoint")
                .log("INCOMING[downstream1]: Charset-Name=${exchangeProperty.CamelCharsetName} Content-Encoding=${header[Content-Encoding]}")
				.to("direct:check-content-encoding")
                .setBody(constant("dowstream1 done"))
                ;

		from("direct:check-content-encoding")
				.id("check-content-encoding-subroute")
				.choice()
					.when(header(Exchange.CONTENT_ENCODING).isNull()).endChoice()
					.when(header(Exchange.CONTENT_ENCODING).isEqualTo("")).endChoice()
					.when(header(Exchange.CONTENT_ENCODING).isEqualTo("gzip")).endChoice()
					.when(header(Exchange.CONTENT_ENCODING).isEqualTo("compress")).endChoice()
					.when(header(Exchange.CONTENT_ENCODING).isEqualTo("deflate")).endChoice()
					.when(header(Exchange.CONTENT_ENCODING).isEqualTo("br")).endChoice()
					.when(header(Exchange.CONTENT_ENCODING).isEqualTo("identity")).endChoice()

					// All others are invalid
					.otherwise().log("*** INVALID CONTENT ENCODING DETECTED ***").endChoice()
				.end()
				;

		from("scheduler:initiate-test?repeatCount=1&initialDelay=1000")
				.id("initiate-test-request")
				.log("INITIATING A TEST REQUEST")
				.setBody(constant("test-body"))
				.setHeader(Exchange.CONTENT_TYPE, constant("text/plain;charset=UTF-8"))
				.to("http://127.0.0.1:9000")
				;
    }
}
