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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ExecConfig;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.ImagePullPolicy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class OpenAmDockerRunTest {

    final Logger logger = LoggerFactory.getLogger(OpenAmDockerRunTest.class);

    GenericContainer<?> openam;

    @BeforeClass
    public void setup() throws IOException, InterruptedException {
        //setupOpenAmDockerContainer();
        setupTomcatDockerContainer();

        Thread.sleep(1000 * 60 * 10);

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


        GenericContainer<?> tomcat = new FixedHostPortGenericContainer<>("tomcat:10.1-jdk11")
                .withFixedExposedPort(8081, 8080)
                .withLogConsumer(new Slf4jLogConsumer(logger))
                .withCopyToContainer(
                        MountableFile.forClasspathResource("docker/index.html"),
                        "/usr/local/tomcat/webapps/demo/index.html"
                )
                .withCopyToContainer(MountableFile.forHostPath(jeeAgentsSdkPath), "/usr/local/tomcat/lib/jee-agents-sdk-uber.jar")
                .withCopyToContainer(MountableFile.forClasspathResource("docker/tomcat/web.xml"), "/usr/local/tomcat/conf/web.xml")
                .withCopyToContainer(MountableFile.forClasspathResource("/"), "/usr/local/tomcat/lib/")
                .waitingFor(Wait.forListeningPort());

        tomcat.start();

    }

    private void setupOpenAmDockerContainer() throws IOException, InterruptedException {
        String imageName1 = "openam-configured:latest";
        String imageName = "openidentityplatform/openam:latest";
        openam = new GenericContainer<>(DockerImageName.parse(imageName1))
                .withImagePullPolicy(new NoPullPolicy())
                .withCopyToContainer(
                        MountableFile.forClasspathResource("docker/openam.conf"),
                        "/usr/openam/ssoconfiguratortools/openam.conf"
                )
                .withCopyToContainer(
                        MountableFile.forClasspathResource("docker/agent.properties"),
                        "/usr/openam/ssoadmintools/agent.properties"
                )
                .withLogConsumer(new Slf4jLogConsumer(logger))
                .withExposedPorts(8080)
                .withCreateContainerCmdModifier(it -> it.withHostName("openam.example.org"))
                //.waitingFor(Wait.forListeningPort())
                .waitingFor(Wait.forHealthcheck())
                .withStartupTimeout(Duration.ofSeconds(300))
        ;

        openam.start();
//        executeDockerCommand("java -jar openam-configurator-tool*.jar --file openam.conf",
//                "/usr/openam/ssoconfiguratortools/");

        executeDockerCommand("./setup -p $OPENAM_DATA_DIR " +
                        "-d /usr/openam/ssoadmintools/debug -d /usr/openam/ssoadmintools/log --acceptLicense",
                "/usr/openam/ssoadmintools");

        executeDockerCommand("echo passw0rd > /tmp/pwd.txt && chmod 400 /tmp/pwd.txt", "");

        executeDockerCommand("./openam/bin/ssoadm create-agent " +
                        "--realm / --agentname myAgent --agenttype J2EEAgent --adminid amadmin " +
                        "--password-file /tmp/pwd.txt --datafile agent.properties"
                , "/usr/openam/ssoadmintools");
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

    public static class NoPullPolicy implements ImagePullPolicy {

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
    }

    @Test
    public void runDockerContainer() throws IOException {
    }
}
