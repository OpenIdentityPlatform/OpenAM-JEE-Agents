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

import com.iplanet.services.util.Crypt;
import com.sun.identity.install.tools.configurator.InstallException;
import com.sun.identity.install.tools.util.Debug;
import com.sun.identity.install.tools.util.LocalizedMessage;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static com.sun.identity.agents.install.handler.EncryptionHandler.LOC_TSK_ERR_ENCRYPT_PASSWORD_INVOKE_METHOD;
import static com.sun.identity.agents.install.handler.EncryptionHandler.STR_ENCRYPT_FUNCTION;
import static com.sun.identity.agents.install.handler.EncryptionHandler.STR_ENCRYPT_LOCAL_FUNCTION;

@Ignore
public class CrypTest {
    @Test
    public void test() throws Exception {


        // Try the AM 70 method, if failed try the AM 63 method
        Method method =  null;
        try {
            method = Crypt.class.getMethod(STR_ENCRYPT_LOCAL_FUNCTION,
                    new Class[]{String.class});
        } catch (Exception ex) {
            if (method == null) {
                Debug.log("EncryptionHandler.handleRequest() : failed to get " +
                        "method from SDK with exception :",ex);
                Debug.log("EncryptionHandler.handleRequest() : making second " +
                        "attempt to load method");
                method = Crypt.class.getMethod(STR_ENCRYPT_FUNCTION,
                        new Class[]{String.class});
                if (method == null) {
                    throw new InstallException(
                            LocalizedMessage.get(
                                    LOC_TSK_ERR_ENCRYPT_PASSWORD_INVOKE_METHOD));
                }
            }
        }
        if (method != null) {
            String encryptedText = (String) method.invoke(
                    Crypt.class, new Object[]{"passw0rd"});
            System.out.println(encryptedText);
        }
    }
}
