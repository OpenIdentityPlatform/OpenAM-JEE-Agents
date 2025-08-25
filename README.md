## OpenAM Java EE Policy Agents
[![Latest release](https://img.shields.io/github/release/OpenIdentityPlatform/OpenAM-JEE-Agents.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/releases)
[![Build](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/actions/workflows/build.yml/badge.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/actions/workflows/build.yml)
[![Deploy](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/actions/workflows/deploy.yml/badge.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/actions/workflows/deploy.yml)
[![Issues](https://img.shields.io/github/issues/OpenIdentityPlatform/OpenAM-JEE-Agents.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/issues)
[![Last commit](https://img.shields.io/github/last-commit/OpenIdentityPlatform/OpenAM-JEE-Agents.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/commits/master)
[![License](https://img.shields.io/badge/license-CDDL-blue.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/blob/master/LICENSE.md)
[![Top language](https://img.shields.io/github/languages/top/OpenIdentityPlatform/OpenAM-JEE-Agents.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents)
[![Code size in bytes](https://img.shields.io/github/languages/code-size/OpenIdentityPlatform/OpenAM-JEE-Agents.svg)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents)

OpenAM Java EE Policy Agents is an OpenAM add-on component that functions as a Policy Enforcement Point (PEP) for applications deployed on a Java EE-based servlet container or application server. The policy agent protects web-based applications and implements single sign-on (SSO) capabilities for the applications deployed in the container.

## License
This project is licensed under the [Common Development and Distribution License (CDDL)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/blob/master/LICENSE.md). 

## Downloads 
* [OpenAM JavaEE Policy Agent (JAR With Libraries ZIP)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/releases) (All OS)
* [OpenAM Java Policy Agent (Uber JAR)](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/releases) (All OS)

Java 11+ required

## How-to build

```bash
git clone https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents.git
mvn install -f OpenAM-JEE-Agents
```

## Quick Start

Assume you have created an agent in your OpenAM instance. If not, create one as described in the [documentation](https://doc.openidentityplatform.org/openam/jee-users-guide/chap-jee-agent-config#create-agent-profiles).

### Distribution Files

Put the contents of any distribution into your container classpath folder.
Add a declaration of the Agent filter to the container's `xml` file:
For example, for Apache Tomcat it is `web.xml`, for Eclipse Jetty - `webdefault.xml`
```xml
<filter>
    <filter-name>Agent</filter-name>
    <filter-class>com.sun.identity.agents.filter.AmAgentFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>Agent</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

Set system properties for the filter

| Property                                   | Description                                                                                                                                                                                |
|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| com.iplanet.am.naming.url                  | Set this to the naming service URL(s) used for naming lookups in OpenAM. Separate multiple URLs with single space characters. Example: http://openam.example.org:8080/openam/namingservice |
| com.sun.identity.agents.app.username       | Set this to the OpenAM user, who has access to read the agent properties, for example, `amadmin`                                                                                           |
| com.iplanet.am.service.secret              | OpenAM user's password                                                                                                                                                                     |
 | am.encryption.pwd                          | When using an encrypted password, set this to the encryption key used to encrypt the agent profile password. If blank, the password is unencrypted.                                        |
| com.sun.identity.agents.config.profilename | Agent name, for example: `myAgent`                                                                                                                                                         |

Alternatively, you can set up the agent properties as init filter parameters.

You can also create the `OpenSSOAgentBootstrap.properties` file with the agent properties and put it into your web container classpath directory.

More info about J2EE agent parameters can be found in the [documentation](https://doc.openidentityplatform.org/openam/jee-users-guide/chap-jee-agent-config#configure-j2ee-policy-agent).


### Maven Dependency

You can use the Agent filter in your Java project as a Maven dependency when running, for example , an embedded web container. Include the Agent dependency in your `pom.xml` file:

```xml
<dependency>
    <groupId>org.openidentityplatform.openam</groupId>
    <artifactId>openam-clientsdk</artifactId>
    <version>5.0.0</version>
</dependency>
```

Then, add filter to your embedded web container, for example, [Eclipse Jetty](https://jetty.org/):

```java
Server jetty = new Server(8081);
ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
context.setContextPath("/");
FilterHolder filterHolder = new FilterHolder(AmAgentFilter.class);
filterHolder.setInitParameter("com.iplanet.am.naming.url", "http://openam.example.org:8080/openam/namingservice");
filterHolder.setInitParameter("com.sun.identity.agents.app.username", "amadmin");
filterHolder.setInitParameter("com.iplanet.am.service.secret", "passw0rd");
filterHolder.setInitParameter("com.sun.identity.agents.config.profilename", "myAgent");

context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
jetty.setHandler(context);
```

## Support and Mailing List Information
* OpenAM Java Policy Agent Community [documentation](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/wiki)
* OpenAM Java Policy Agent Community [discussions](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/discussions)
* OpenAM Java Policy Agent Community [issues](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/issues)
* OpenAM Java Policy Agent [commercial support](https://github.com/OpenIdentityPlatform/.github/wiki/Approved-Vendor-List)

## Contributing
Please, make [Pull request](https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/pulls)

## Thanks for OpenAM Java Policy Agent 🥰
* Sun Access Manager
* Sun OpenSSO
* Oracle OpenSSO
* Forgerock OpenAM
