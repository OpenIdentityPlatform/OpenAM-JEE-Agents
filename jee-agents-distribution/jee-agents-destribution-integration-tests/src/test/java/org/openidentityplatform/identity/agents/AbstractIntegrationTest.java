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

import org.forgerock.openam.sdk.com.fasterxml.jackson.core.type.TypeReference;
import org.forgerock.openam.sdk.com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public abstract class AbstractIntegrationTest {

    final static OpenAmContainer openamContainer = new OpenAmContainer();
    static {
        openamContainer.start();
    }

    HttpClient client;
//    OpenAmContainer openamContainer;

    @BeforeClass
    public void setup() {
        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "host");
        client = HttpClient.newHttpClient();
    }


    protected HttpResponse<String> callDemoServlet(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/demo/"))
                .header("Cookie", "iPlanetDirectoryPro="+ token)
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected String getAuthenticationToken() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/openam/json/authenticate"))
                .header("X-OpenAM-Username", "demo")
                .header("Host", "openam.example.org:8080")
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
}
