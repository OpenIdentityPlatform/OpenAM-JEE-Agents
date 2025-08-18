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
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.EnumSet;

import static java.nio.file.Files.createTempDirectory;
import static org.assertj.core.api.Assertions.assertThat;

public class EmbeddedContainer_IT extends AbstractIntegrationTest {

    private final int CONTAINER_PORT = 8081;

    @BeforeClass
    public void setProps() {
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
    public void testTomcat() throws IOException, LifecycleException, InterruptedException {
        Tomcat tomcat = startTomcat();

        HttpResponse<String> unauthResponse = callServlet("");
        assertThat(unauthResponse.statusCode()).isEqualTo(HttpURLConnection.HTTP_MOVED_TEMP);

        String token = getAuthenticationToken();

        HttpResponse<String> authResponse = callServlet(token);
        assertThat(authResponse.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);

        tomcat.stop();
    }

    @Test
    public void testJetty() throws Exception {
        Server jetty = new Server(CONTAINER_PORT); // You can change the port

        // Create a ServletContextHandler to manage servlets and filters
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/"); // Set the context path for your application
        context.addFilter(AmAgentFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        jetty.setHandler(context);

        // Add your custom servlet
        context.addServlet(new ServletHolder(new DemoServlet()), "/demo/");

        // Start the server
        jetty.start();

        HttpResponse<String> unauthResponse = callServlet("");
        assertThat(unauthResponse.statusCode()).isEqualTo(HttpURLConnection.HTTP_MOVED_TEMP);

        String token = getAuthenticationToken();

        HttpResponse<String> authResponse = callServlet(token);
        assertThat(authResponse.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);

        jetty.stop();

    }

    private Tomcat startTomcat() throws LifecycleException, IOException {

        String contextPath = "";
        String appBase = ".";

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(CONTAINER_PORT);
        tomcat.getConnector();
        tomcat.setBaseDir(createTempDirectory("tomcat").toAbsolutePath().toString());
        tomcat.getHost().setAppBase(appBase);
        tomcat.getHost().setAutoDeploy(true);
        tomcat.getHost().setDeployOnStartup(true);

        var context = tomcat.addContext(contextPath, new File(".").getAbsolutePath());

        Class<AmAgentFilter> filterClass = AmAgentFilter.class;
        String filterName = filterClass.getName();
        FilterDef def = new FilterDef();
        def.setFilterName(filterName);
        def.setFilter(new AmAgentFilter());
        context.addFilterDef( def );
        FilterMap map = new FilterMap();
        map.setFilterName(filterName);
        map.addURLPattern("/*");
        context.addFilterMap(map);

        Tomcat.addServlet(context, "demoServlet", new DemoServlet());
        context.addServletMappingDecoded("/demo/", "demoServlet");

        tomcat.start();
        return tomcat;
    }

    public static class DemoServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType("text/plain");
            resp.getWriter().write("Hello World!");
        }
    }
}
