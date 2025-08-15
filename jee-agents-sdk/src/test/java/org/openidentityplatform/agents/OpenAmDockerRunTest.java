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

import org.forgerock.openam.sdk.com.fasterxml.jackson.core.type.TypeReference;
import org.forgerock.openam.sdk.com.fasterxml.jackson.databind.ObjectMapper;
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
import org.testng.annotations.Test;

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

public class OpenAmDockerRunTest {

    final Logger logger = LoggerFactory.getLogger(OpenAmDockerRunTest.class);

    GenericContainer<?> openam;

    GenericContainer<?> tomcat;

    Network network = Network.newNetwork();
//    Network network = new Network() {
//        @Override
//        public String getId() {
//            return "openam";
//        }
//
//        @Override
//        public void close() {
//
//        }
//
//        @Override
//        public Statement apply(Statement base, Description description) {
//            return null;
//        }
//    };

    @BeforeClass
    public void setup() throws IOException, InterruptedException {
        setupOpenAmDockerContainer();

//        Thread.sleep(1000 * 60 * 10);

        setupTomcatDockerContainer();

        //Thread.sleep(1000 * 60 * 10);

    }

    private void setupTomcatDockerContainer() throws IOException {
        String userDirectory = FileSystems.getDefault()
                .getPath("")
                .toAbsolutePath()
                .toString();
        Path directory = Paths.get(userDirectory + "/target"); // Replace with your directory path
        String globPattern = "jee-agents-sdk-*-uber.jar"; // Example: files starting with "prefix" and ending with ".txt"

        String jeeAgentsSdkPath = null;
        try (var stream = Files.newDirectoryStream(directory, globPattern)) {
            for (Path entry : stream) {
                jeeAgentsSdkPath = entry.toAbsolutePath().toString();
            }
        }


        tomcat = new FixedHostPortGenericContainer<>("tomcat:10.1-jdk11")
                .withFixedExposedPort(8081, 8080)
                .withExposedPorts(8080)
                .withNetwork(network)
                .withLogConsumer(new Slf4jLogConsumer(logger))
                .withCopyToContainer(
                        MountableFile.forClasspathResource("docker/index.html"),
                        "/usr/local/tomcat/webapps/demo/index.html"
                )
                .withCopyToContainer(MountableFile.forHostPath(jeeAgentsSdkPath), "/usr/local/tomcat/lib/jee-agents-sdk-uber.jar")
                .withCopyToContainer(MountableFile.forClasspathResource("docker/tomcat/web.xml"), "/usr/local/tomcat/conf/web.xml")
                .withCopyToContainer(MountableFile.forClasspathResource("/"), "/usr/local/tomcat/lib/")
                .waitingFor(Wait.forHttp("/demo/").forPort(8080));

        tomcat.start();

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

    @AfterClass
    public void tearDown() {
        if (openam != null) {
            openam.close();
        }
        if (tomcat != null) {
            tomcat.close();
        }
    }

    HttpClient client = HttpClient.newHttpClient();

    @Test
    public void runDockerContainer() throws IOException, InterruptedException {
        //Thread.sleep(1000 * 60 * 10);
        callServlet("");
        String token = getAuthenticationToken();
        callServlet(token);

        Thread.sleep(1000 * 60 * 10);
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
                .uri(URI.create("http://localhost:8081/demo/"))
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
}
