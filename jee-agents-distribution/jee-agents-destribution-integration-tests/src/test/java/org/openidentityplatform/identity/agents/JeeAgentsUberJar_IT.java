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

import org.testcontainers.utility.MountableFile;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class JeeAgentsUberJar_IT extends AbstractIntegrationTest {

    @Test
    public void testJetty11() throws Exception {
        try(WebContainer webContainer = new Jetty11Container()) {
            doTest(webContainer);
        }
    }
    @Test
    public void testJetty12() throws Exception {
        try(WebContainer webContainer = new Jetty12Container()) {
            doTest(webContainer);
        }
    }

    @Test
    public void testTomcat10() throws Exception {
        try(WebContainer webContainer = new Tomcat10Container()) {
            doTest(webContainer);
        }
    }

    @Test
    public void testTomcat11() throws Exception {
        try(WebContainer webContainer = new Tomcat11Container()) {
            doTest(webContainer);
        }
    }

    private void doTest(WebContainer webContainer) throws InterruptedException, IOException {

        String userDirectory = FileSystems.getDefault()
                .getPath("")
                .toAbsolutePath()
                .toString();
        Path directory = Paths.get(userDirectory + "/../jee-agents-distribution-uberjar/target");
        String globPattern = "jee-agent-uberjar_*";

        String distPath = null;
        try (var stream = Files.newDirectoryStream(directory, globPattern)) {
            for (Path entry : stream) {
                if(!entry.toFile().isDirectory()) {
                    continue;
                }
                distPath = entry.toAbsolutePath().toString();
                break;
            }
        }

        webContainer.mount(MountableFile.forHostPath(distPath));
        webContainer.start();

        HttpResponse<String> unauthResponse = callDemoServlet("");
        assertThat(unauthResponse.statusCode()).isEqualTo(HttpURLConnection.HTTP_MOVED_TEMP);

        String token = getAuthenticationToken();

        HttpResponse<String> authResponse = callDemoServlet(token);
        assertThat(authResponse.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
    }


}
