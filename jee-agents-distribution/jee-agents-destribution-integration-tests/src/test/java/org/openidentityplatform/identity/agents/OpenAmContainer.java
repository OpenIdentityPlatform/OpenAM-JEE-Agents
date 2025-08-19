package org.openidentityplatform.identity.agents;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ExecConfig;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OpenAmContainer extends WebContainer {

    public OpenAmContainer() {

        //String TEST_IMAGE_NAME = "openam-configured-agents-policies";
        String TEST_IMAGE_NAME = "openam-configured-agent-test";
        String TEST_IMAGE_NAME_WITH_TAG = TEST_IMAGE_NAME.concat(":latest");

        DockerClient dockerClient = DockerClientFactory.instance().client();
        List<Image> images = dockerClient.listImagesCmd().exec();
        Image configuredImage = null;
        for(Image img : images) {
            Set<String> tagSet = Arrays.stream(img.getRepoTags())
                    .collect(Collectors.toSet());
            if (tagSet.contains(TEST_IMAGE_NAME_WITH_TAG)) {
                configuredImage = img;
                break;
            }
        }

        if(configuredImage == null) {
            GenericContainer<?> tmpContainer = new GenericContainer<>("openidentityplatform/openam:latest")
                    .withCopyToContainer(
                            MountableFile.forClasspathResource("docker/openam/openam.conf"),
                            "/usr/openam/ssoconfiguratortools/openam.conf"
                    )
                    .withCopyToContainer(
                            MountableFile.forClasspathResource("docker/openam/agent.properties"),
                            "/usr/openam/ssoadmintools/agent.properties"
                    )
                    .withCopyToContainer(
                            MountableFile.forClasspathResource("docker/openam/policies.json"),
                            "/usr/openam/ssoadmintools/policies.json"
                    )
                    .withCopyToContainer(
                            MountableFile.forClasspathResource("docker/openam/openam-configuration.sh"),
                            "/usr/openam/openam-configuration.sh"
                    )
                    .withLogConsumer(new Slf4jLogConsumer(logger))
                    .withCreateContainerCmdModifier(it -> it.withHostName("openam.example.org"))
                    .waitingFor(Wait.forHealthcheck())
                    .withStartupTimeout(Duration.ofSeconds(300));

            tmpContainer.start();
            try {
                executeDockerCommand(tmpContainer,"chmod +x openam-configuration.sh", "/usr/openam/", "root");
                logger.info("Running OpenAM configuration script");
                //Thread.sleep(1000 * 60 * 20);
                executeDockerCommand(tmpContainer, "./openam-configuration.sh", "/usr/openam/", "openam");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            dockerClient.stopContainerCmd(tmpContainer.getContainerId());
            dockerClient.commitCmd(tmpContainer.getContainerId()).withRepository(TEST_IMAGE_NAME).withTag("latest").exec();
            tmpContainer.stop();
        }

        container = new FixedHostPortGenericContainer<>(TEST_IMAGE_NAME_WITH_TAG)
                .withFixedExposedPort(8080, 8080)
                .withExposedPorts(8080)
                .withNetwork(Network.SHARED)
                .withImagePullPolicy(new NeverPullPolicy())
                .withLogConsumer(new Slf4jLogConsumer(logger))
                .withCreateContainerCmdModifier(it -> it.withHostName("openam.example.org"))
                .waitingFor(Wait.forHealthcheck())
                .withStartupTimeout(Duration.ofSeconds(300))
        ;
    }

    private void executeDockerCommand(Container<?> dockerContainer, String command, String workdir, String user)
            throws IOException, InterruptedException {
        var execConfig = ExecConfig.builder()
                .user(user)
                .command(new String[]{"bash", "-c", command})
                .workDir(workdir).build();
        Container.ExecResult setupExecRes = dockerContainer.execInContainer(execConfig);
        logger.info("command result: code {}, stdout: {}, stderr: {}",  setupExecRes.getExitCode(), setupExecRes.getStdout(), setupExecRes.getStderr());
        if (setupExecRes.getExitCode() > 0) {
            throw new IOException(setupExecRes.toString());
        }
    }

    @Override
    public void mount(Transferable transferable) {}
}
