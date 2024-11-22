package org.integra;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.support.processor.validation.SchemaValidationException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CamelRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(SchemaValidationException.class)
                .handled(true)
                .process(exchange -> {
                    SchemaValidationException exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, SchemaValidationException.class);

                    String violations = exception.getErrors().stream()
                            .map(SAXException::getMessage)
                            .collect(Collectors.joining("|"));

                    StringBuilder stringBuilder = new StringBuilder();
                    for(String violation : violations.split("\\|")) {
                        stringBuilder.append("<error>");
                        stringBuilder.append(violation);
                        stringBuilder.append("</error>");
                    }

                    String xmlMessage = "<status><message>Validation Failed!!</message><errors>"+ stringBuilder.toString() +"</errors></status>";
                    exchange.getIn().setBody(xmlMessage);
                    exchange.getIn().setHeader("Content-Type", MediaType.APPLICATION_XML);
                    exchange.getIn().setHeader("CamelHttpResponseCode", 400);
                });

        // Define a REST endpoint to accept XML input via HTTP POST
        rest("/validate")
            .post("/xml")
            .consumes(MediaType.APPLICATION_XML)
            .produces(MediaType.TEXT_PLAIN)
            .to("direct:validateXml");

        // Route to validate XML against XSD
        from("direct:validateXml")
            .log("Received XML for validation")
            .toD("validator:schemas/${header.xsd-schema}") // Validate against XSD
            .log("Validation successful")
            .setBody(simple("<status><message>Validation successful!!</message></status>"))
            .setHeader("Content-Type", constant(MediaType.APPLICATION_XML))
            .setHeader("CamelHttpResponseCode", constant(200));

    }


}
