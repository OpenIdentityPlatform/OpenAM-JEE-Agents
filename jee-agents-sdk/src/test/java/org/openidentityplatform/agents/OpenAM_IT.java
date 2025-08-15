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

package org.openidentityplatform.agents;

import com.sun.identity.agents.filter.AmAgentFilter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.forgerock.openam.sdk.com.fasterxml.jackson.core.type.TypeReference;
import org.forgerock.openam.sdk.com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static java.nio.file.Files.createTempDirectory;

@Ignore
public class OpenAM_IT {

    HttpClient client = HttpClient.newHttpClient();

    @Test
    public void test() throws Exception {
        int port = 8081;
        String contextPath = "";
        String appBase = ".";

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();
        tomcat.setBaseDir(createTempDirectory("tomcat").toAbsolutePath().toString());
        tomcat.getHost().setAppBase(appBase);
        tomcat.getHost().setAutoDeploy(true);
        tomcat.getHost().setDeployOnStartup(true);

        var context = tomcat.addContext(contextPath, new File(".").getAbsolutePath());

        // Add Filter
        Class<AmAgentFilter> filterClass = AmAgentFilter.class;
        String filterName = filterClass.getName();
        FilterDef def = new FilterDef();
        def.setFilterName(filterName);
        def.setFilter( new AmAgentFilter() );
        context.addFilterDef( def );
        FilterMap map = new FilterMap();
        map.setFilterName( filterName );
        map.addURLPattern( "/*" );
        context.addFilterMap( map );

        Tomcat.addServlet(context, "helloServlet", new HelloServlet());
        context.addServletMappingDecoded("/hello", "helloServlet");

        tomcat.start();
        System.out.println("Tomcat started on http://localhost:" + port + "/hello");

        String token = getAuthenticationToken();

        callServlet("");

//        tomcat.getServer().await();
    }

    private String getAuthenticationToken() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://openam.example.org:8080/openam/json/authenticate"))
                .header("X-OpenAM-Username", "demo")
                .header("X-OpenAM-Password", "changeit")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        String body = response.body();
        Map<String, String> responseMap = mapper.readValue(body, new TypeReference<>() {
        });

        return responseMap.get("tokenId");
    }

    private void callServlet(String token) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/hello"))
                .header("Cookie", "iPlanetDirectoryPro="+ token)
                //.header("iPlanetDirectoryPro", token)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body:\n" + response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType("text/plain");
            resp.getWriter().write("Hello from Embedded Tomcat!");
        }
    }

//    public static class LoggingFilter implements Filter {
//
//        @Override
//        public void init(FilterConfig filterConfig) {
//            System.out.println("LoggingFilter initialized.");
//        }
//
//        @Override
//        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//                throws IOException, ServletException {
//
//            HttpServletRequest httpReq = (HttpServletRequest) request;
//
//            System.out.println("[LoggingFilter] Request received: " + httpReq.getMethod() + " " + httpReq.getRequestURI());
//
//            // Continue filter chain
//            chain.doFilter(request, response);
//
//            System.out.println("[LoggingFilter] Response sent.");
//        }
//
//        @Override
//        public void destroy() {
//            System.out.println("LoggingFilter destroyed.");
//        }
//    }
}
