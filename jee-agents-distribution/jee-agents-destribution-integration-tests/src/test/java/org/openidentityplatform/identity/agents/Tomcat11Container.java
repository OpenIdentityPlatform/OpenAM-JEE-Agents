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

import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.MountableFile;

public class Tomcat11Container extends WebContainer {

    public Tomcat11Container() {
        this.container = new FixedHostPortGenericContainer<>("tomcat:11.0")
                .withFixedExposedPort(8081, 8080)
                .withExposedPorts(8080)
                .withNetwork(Network.SHARED)
                .withCreateContainerCmdModifier(it -> it.withHostName(AGENT_HOST_NAME))
                .withLogConsumer(new Slf4jLogConsumer(logger))
                .withCopyToContainer(
                        MountableFile.forClasspathResource("docker/index.html"),
                        "/usr/local/tomcat/webapps/demo/index.html"
                )
                .withCopyToContainer(
                        MountableFile.forClasspathResource("OpenSSOAgentBootstrap.properties"),
                        "/usr/local/tomcat/lib/OpenSSOAgentBootstrap.properties")
                .withCopyToContainer(
                        MountableFile.forClasspathResource("debugconfig.properties"),
                        "/usr/local/tomcat/lib/debugconfig.properties")
                .withCopyToContainer(
                        MountableFile.forClasspathResource("docker/tomcat/11/web.xml"),
                        "/usr/local/tomcat/conf/web.xml")
                .waitingFor(Wait.forHttp("/demo/").forPort(8080));
    }

    @Override
    public void mount(Transferable transferable) {
        container.withCopyToContainer(transferable, "/usr/local/tomcat/lib/");
    }
}
