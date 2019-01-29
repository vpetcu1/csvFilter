package com.warwick.csv.filter.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;


@ApplicationPath("/api")
@OpenAPIDefinition(info = @Info(
        title = "Example CSV Filtering application", 
        version = "1.0.0-SNAPSHOT", 
        contact = @Contact(
                name = "Vasilica Petcu", 
                email = "vpetcu1@gmail.com")
        ),
        servers = {
            @Server(url = "/csv-filter",description = "localhost")
        }
)
public class CSVFilterRestApplication extends Application {

}
