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

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.Transferable;

public abstract class WebContainer implements AutoCloseable {

    protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    protected GenericContainer<?> container;

    protected Network network = new Network() {
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

    public GenericContainer<?> getContainer() {
        return this.container;
    }

    public void start() {
        this.container.start();
    }

    @Override
    public void close() {
        if (container != null) {
            container.close();
        }
    }

    public abstract void mount(Transferable transferable);

}
