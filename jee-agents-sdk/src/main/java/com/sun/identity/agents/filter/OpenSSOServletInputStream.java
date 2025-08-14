/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: OpenSSOServletInputStream.java,v 1.1 2008/10/07 17:36:32 huacui Exp $
 *
 * Portions Copyrighted 2025 3A Systems LLC.
 */

package com.sun.identity.agents.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.InputStream;
import java.io.IOException;

/**
 * A helper class used to manage the servlet request content. 
 */
public class OpenSSOServletInputStream extends ServletInputStream
{
    InputStream is;

    public OpenSSOServletInputStream(InputStream is) {
        this.is = is;
    }

    public int read() throws IOException {
        return is.read();
    }

    @Override
    public boolean isFinished() {
        try {
            return is.available() == 0;
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
    }
}
