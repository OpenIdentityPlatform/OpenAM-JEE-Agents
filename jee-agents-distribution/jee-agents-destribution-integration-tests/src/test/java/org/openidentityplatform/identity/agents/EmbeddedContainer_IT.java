/**
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2025 3A Systems LLC.
 */

package org.openidentityplatform.identity.agents;

import com.sun.identity.agents.filter.AmAgentFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

//@Ignore
public class EmbeddedContainer_IT extends AbstractIntegrationTest {

    @BeforeClass
    public void setProperties() {
        String resourceName = "embedded";

        // Get the URL of the resource
        URL resourceUrl = this.getClass().getClassLoader().getResource(resourceName);

        if (resourceUrl != null) {
            String absolutePath = resourceUrl.getPath();
            System.out.println("Absolute path of '" + resourceName + "': " + absolutePath);
            System.setProperty("openam.agents.bootstrap.dir", absolutePath);
        } else {
            throw new RuntimeException("resource not found");
        }
    }

    @Test
    public void testJetty() throws Exception {
        Server jetty = new Server(8081); // You can change the port

        // Create a ServletContextHandler to manage servlets and filters
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/"); // Set the context path for your application
        context.addFilter(AmAgentFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        jetty.setHandler(context);

        // Add your custom servlet
        context.addServlet(new ServletHolder(new DemoServlet()), "/demo/");

        // Start the server
        jetty.start();

        HttpResponse<String> unauthResponse = callDemoServlet("");
        assertThat(unauthResponse.statusCode()).isEqualTo(HttpURLConnection.HTTP_MOVED_TEMP);

        String token = getAuthenticationToken();

        HttpResponse<String> authResponse = callDemoServlet(token);
        assertThat(authResponse.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);

        jetty.stop();

    }

    public static class DemoServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType("text/plain");
            resp.getWriter().write("Hello World!");
        }
    }
}
