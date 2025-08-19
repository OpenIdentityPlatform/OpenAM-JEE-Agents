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
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.MountableFile;

public class Jetty11Container extends WebContainer {

    public Jetty11Container() {
        this.container = new FixedHostPortGenericContainer<>("jetty:11.0")
                .withFixedExposedPort(8081, 8080)
                .withExposedPorts(8080)
                .withNetwork(network)
                .withCreateContainerCmdModifier(it -> it.withHostName(AGENT_HOST_NAME))
                .withLogConsumer(new Slf4jLogConsumer(logger))
                .withCopyToContainer(
                        MountableFile.forClasspathResource("docker/index.html"),
                        "/var/lib/jetty/webapps/demo/index.html"
                )
                .withCopyToContainer(
                        MountableFile.forClasspathResource("docker/jetty/11/webdefault.xml"),
                        "/usr/local/jetty/etc/webdefault.xml")
                .withCopyToContainer(MountableFile.forClasspathResource("/"), "/var/lib/jetty/resources/")
                .waitingFor(Wait.forHttp("/demo/").forPort(8080))
                ;
    }

    @Override
    public void mount(Transferable transferable) {
        container.withCopyToContainer(transferable, "/var/lib/jetty/lib/ext/");
    }
}
