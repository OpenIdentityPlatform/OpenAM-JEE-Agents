package org.openidentityplatform.identity.agents;

import com.sun.identity.agents.filter.AmAgentFilter;
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.testng.annotations.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRemove_IT {

    @Test
    public void test() throws Exception {
        Server jetty = new Server(8081); // You can change the port

        // Create a ServletContextHandler to manage servlets and filters
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/"); // Set the context path for your application
        jetty.setHandler(context);

        // Add your custom servlet
        context.addServlet(new ServletHolder(new EmbeddedContainer_IT.DemoServlet()), "/demo/");

        // Start the server
        jetty.start();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/demo/"))
                 .GET()
                .build();
        var client = HttpClient.newHttpClient();
        HttpResponse<String> authResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(authResponse.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);

        jetty.stop();
    }
}
