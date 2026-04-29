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
 * Copyright 2025-2026 3A Systems LLC.
 */

package org.openidentityplatform.identity.agents;

import com.sun.identity.agents.filter.AmAgentFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.openidentityplatform.identity.agents.filters.NotificationTestFilter;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbeddedContainer_IT extends AbstractIntegrationTest {



    @Test
    public void testJetty() throws Exception {
        Server jetty = new Server(8081);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addFilter(new FilterHolder(NotificationTestFilter.class), "/UpdateAgentCacheServlet", EnumSet.of(DispatcherType.REQUEST));

        FilterHolder amFilterHolder = new FilterHolder(AmAgentFilter.class);
        amFilterHolder.setInitParameter("com.iplanet.am.naming.url", "http://openam.example.org:8080/openam/namingservice");
        amFilterHolder.setInitParameter("com.sun.identity.agents.app.username", "amadmin");
        amFilterHolder.setInitParameter("com.iplanet.am.service.secret", "passw0rd");
        amFilterHolder.setInitParameter("com.sun.identity.agents.config.profilename", "myAgent");
        amFilterHolder.setInitParameter("com.iplanet.am.cookie.name", "iPlanetDirectoryPro");

        context.addFilter(amFilterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));


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

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/openam/json/sessions/?_action=logout"))
                .header("Host", "openam.example.org:8080")
                .header("iPlanetDirectoryPro", token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        //logout
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);

        HttpResponse<String> loggedOutTokenResponse = callDemoServlet(token);
        assertThat(loggedOutTokenResponse.statusCode()).isEqualTo(HttpURLConnection.HTTP_MOVED_TEMP);

        //test received notifications
        assertThat(NotificationTestFilter.getReceivedRequests().size()).isNotZero();

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
