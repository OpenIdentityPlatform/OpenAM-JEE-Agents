## OpenAM Java EE Policy Agents
[![Latest release](https://img.shields.io/github/release/OpenIdentityPlatform/OpenAM-JEE-Agents.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/releases)
[![Build Status](https://travis-ci.org/OpenIdentityPlatform/OpenAM-JEE-Agents.svg)](https://travis-ci.org/OpenIdentityPlatform/OpenAM-JEE-Agents)
[![Issues](https://img.shields.io/github/issues/OpenIdentityPlatform/OpenAM-JEE-Agents.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/issues)
[![Last commit](https://img.shields.io/github/last-commit/OpenIdentityPlatform/OpenAM-JEE-Agents.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/commits/master)
[![License](https://img.shields.io/badge/license-CDDL-blue.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/blob/master/LICENSE.md)
[![Gitter](https://img.shields.io/gitter/room/nwjs/nw.js.svg)](https://gitter.im/OpenIdentityPlatform/OpenAM)
[![Top language](https://img.shields.io/github/languages/top/OpenIdentityPlatform/OpenAM-JEE-Agents.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents)
[![Code size in bytes](https://img.shields.io/github/languages/code-size/OpenIdentityPlatform/OpenAM-JEE-Agents.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents)

OpenAM Java EE Policy Agents is an OpenAM add-on component that functions as a Policy Enforcement Point (PEP) for applications deployed on a Java EE-based servlet container or application server. The policy agent protects web-based applications and implements single sign-on (SSO) capabilities for the applications deployed in the container.

## License
This project is licensed under the [Common Development and Distribution License (CDDL)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/blob/master/LICENSE.md). 

## Downloads 
* [OpenAM Java Policy Agent (Tomcat v6 v7 v8 ZIP)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/releases) (All OS)
* [OpenAM Java Policy Agent (Jetty v61 ZIP)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/releases) (All OS)
* [OpenAM Java Policy Agent (Jetty v7-v8 ZIP)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/releases) (All OS)
* [OpenAM Java Policy Agent (Appserver v10 ZIP)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/releases) (All OS)
* [OpenAM Java Policy Agent (JBoss v42 ZIP)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/releases) (All OS)
* [OpenAM Java Policy Agent (JBoss v7 ZIP)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/releases) (All OS)
* [OpenAM Java Policy Agent (JSR196 ZIP)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/releases) (All OS)
* Docker
    * [OpenAM Java Policy Agent on Tomcat](https://hub.docker.com/r/openidentityplatform/openam-j2ee-agent-tomcat)

Java 1.8+ required

## How-to build
```bash
git clone --recursive  https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents.git
mvn -DskipTests -Dmaven.javadoc.skip=true install -f OpenAM-JEE-Agents/OpenAM/OpenDJ/forgerock-parent
mvn -DskipTests -Dmaven.javadoc.skip=true install -f OpenAM-JEE-Agents/OpenAM/OpenDJ -P '!man-pages,!distribution'
mvn -DskipTests -Dmaven.javadoc.skip=true install -f OpenAM-JEE-Agents/OpenAM
mvn install -f OpenAM-JEE-Agents
```

## Support and Mailing List Information
* OpenAM Java Policy Agent Community Wiki: https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/wiki
* OpenAM Java Policy Agent Community Mailing List: open-identity-platform-openam@googlegroups.com
* OpenAM Java Policy Agent Community Archive: https://groups.google.com/d/forum/open-identity-platform-openam
* OpenAM Java Policy Agent Community on Gitter: https://gitter.im/OpenIdentityPlatform/OpenAM
* OpenAM Java Policy Agent Commercial support RFP: support@openam.org.ru (English, Russian)

## Contributing
Please, make [Pull request](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/pulls)

## Thanks for OpenAM Java Policy Agent
* Sun Access Manager
* Sun OpenSSO
* Oracle OpenSSO
* Forgerock OpenAM
