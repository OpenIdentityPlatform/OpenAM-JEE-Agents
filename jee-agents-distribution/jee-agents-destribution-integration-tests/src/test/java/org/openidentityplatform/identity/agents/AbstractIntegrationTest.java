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
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ExecConfig;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.ImagePullPolicy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

public abstract class AbstractIntegrationTest {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    GenericContainer<?> openam;

    GenericContainer<?> tomcat;

    Network network;

    HttpClient client;

    @BeforeClass
    public void setup() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        network = Network.newNetwork();
        network = getOpenAmNetwork();

//        setupOpenAmDockerContainer();
        setupTomcatDockerContainer();
        //Thread.sleep(1000 * 60 * 10);
    }

    @AfterClass
    public void tearDown() {
        if (openam != null) {
            openam.close();
        }
        if (tomcat != null) {
            tomcat.close();
        }
    }

    private void setupOpenAmDockerContainer() throws IOException, InterruptedException {
//        String imageName = "openam-configured:latest";
//        String imageName = "openam-configured-agents-policies";
        String imageName = "openidentityplatform/openam:latest";
        openam = new FixedHostPortGenericContainer<>(imageName)
                .withFixedExposedPort(8080, 8080)
                .withExposedPorts(8080)
                .withNetwork(network)
                .withImagePullPolicy(new NeverPullPolicy())
                .withCopyToContainer(
                        MountableFile.forClasspathResource("docker/openam.conf"),
                        "/usr/openam/ssoconfiguratortools/openam.conf"
                )
                .withCopyToContainer(
                        MountableFile.forClasspathResource("docker/agent.properties"),
                        "/usr/openam/ssoadmintools/agent.properties"
                )
                .withCopyToContainer(
                        MountableFile.forClasspathResource("docker/policies.json"),
                        "/usr/openam/ssoadmintools/policies.json"
                )
                .withLogConsumer(new Slf4jLogConsumer(logger))

                .withCreateContainerCmdModifier(it -> it.withHostName("openam.example.org"))
                //.waitingFor(Wait.forListeningPort())
                .waitingFor(Wait.forHealthcheck())
                .withStartupTimeout(Duration.ofSeconds(300))
        ;

        openam.start();

        executeDockerCommand("java -jar openam-configurator-tool*.jar --file openam.conf",
                "/usr/openam/ssoconfiguratortools/");

        executeDockerCommand("./setup -p $OPENAM_DATA_DIR " +
                        "-d /usr/openam/ssoadmintools/debug -d /usr/openam/ssoadmintools/log --acceptLicense",
                "/usr/openam/ssoadmintools");

        executeDockerCommand("echo passw0rd > /tmp/pwd.txt && chmod 400 /tmp/pwd.txt", "");

        executeDockerCommand("./openam/bin/ssoadm create-agent " +
                        "--realm / --agentname myAgent --agenttype J2EEAgent --adminid amadmin " +
                        "--password-file /tmp/pwd.txt --datafile agent.properties",
                "/usr/openam/ssoadmintools");

        executeDockerCommand("./openam/bin/ssoadm policy-import " +
                        "--servername http://openam.example.org:8080/openam " +
                        "--realm / --jsonfile policies.json --adminid amadmin --password-file /tmp/pwd.txt",
                "/usr/openam/ssoadmintools");

    }

    private void setupTomcatDockerContainer() throws IOException {

        String userDirectory = FileSystems.getDefault()
                .getPath("")
                .toAbsolutePath()
                .toString();
        Path directory = Paths.get(userDirectory + "/../jee-agents-distribution-jar-with-lib/target");
        String globPattern = "jee-agent-jar-with-lib_*";

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

        tomcat = new FixedHostPortGenericContainer<>("tomcat:10.1-jdk21")
                .withFixedExposedPort(8081, 8080)
                .withExposedPorts(8080)
                .withNetwork(network)
                .withLogConsumer(new Slf4jLogConsumer(logger))
                .withCopyToContainer(
                        MountableFile.forClasspathResource("docker/index.html"),
                        "/usr/local/tomcat/webapps/demo/index.html"
                )
                .withCopyToContainer(MountableFile.forHostPath(distPath), "/usr/local/tomcat/lib/")
                .withCopyToContainer(
                        MountableFile.forClasspathResource("OpenSSOAgentBootstrap.properties"),
                        "/usr/local/tomcat/lib/OpenSSOAgentBootstrap.properties")
                .withCopyToContainer(
                        MountableFile.forClasspathResource("debugconfig.properties"),
                        "/usr/local/tomcat/lib/debugconfig.properties")
                .withCopyToContainer(
                        MountableFile.forClasspathResource("docker/tomcat/web.xml"),
                        "/usr/local/tomcat/conf/web.xml")

                .waitingFor(Wait.forHttp("/demo/").forPort(8080));

        tomcat.start();

    }

    protected HttpResponse<String> callServlet(String token) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/demo/"))
                .header("Cookie", "iPlanetDirectoryPro="+ token)
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected String getAuthenticationToken() throws IOException, InterruptedException {
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


    private Network getOpenAmNetwork() {
        return new Network() {
            @Override
            public String getId() {
                return "openam";
            }

            @Override
            public void close() {

            }

            @Override
            public Statement apply(Statement base, Description description) {
                return null;
            }
        };
    }

    private void executeDockerCommand(String command, String workdir) throws IOException, InterruptedException {
        var execConfig = ExecConfig.builder()
                .user("openam")
                .command(new String[]{"bash", "-c", command})
                .workDir(workdir).build();
        Container.ExecResult setupExecRes = openam.execInContainer(execConfig);
        logger.info("command result: code {}, stdout: {}, stderr: {}",  setupExecRes.getExitCode(), setupExecRes.getStdout(), setupExecRes.getStderr());
        if (setupExecRes.getExitCode() > 0) {
            throw new IOException(setupExecRes.toString());
        }
    }

    public static class NeverPullPolicy implements ImagePullPolicy {
        @Override
        public boolean shouldPull(DockerImageName imageName) {
            return false;
        }
    }

}
