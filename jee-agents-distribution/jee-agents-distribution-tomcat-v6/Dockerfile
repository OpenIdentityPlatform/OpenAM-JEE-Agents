FROM tomcat:8.5-jre8 

MAINTAINER Open Identity Platform Community <open-identity-platform-openam@googlegroups.com>

ENV OPENAM_SERVER="http://localhost:8081/openam"
ENV OPENAM_AGENT_USER="test"
ENV OPENAM_AGENT_PASSWORD="test"
ENV OPENAM_AGENT_URL="http://localhost:8080/agentapp"

ENV USER="tomcat"
ENV CATALINA_OPTS="-Xmx2048m -server -Dcom.iplanet.services.debug.level=error -Dcom.sun.identity.agents.config.local.logfile= -Dcom.iplanet.services.debug.directory"

COPY . target/*.zip ./

ADD run.sh bin/run.sh

ENV CATALINA_HOME=${CATALINA_HOME:-.}

RUN apt update && apt install -y curl unzip && rm -fr $CATALINA_HOME/webapps/*

ARG VERSION
RUN if [ ! -z "$VERSION" ] ; then rm -rf ./*.zip ; curl -L https://github.com/OpenIdentityPlatform/OpenAM-JEE-Agents/releases/download/$VERSION/tomcat_v6_v7_v8_agent_$VERSION.zip --output $VERSION.zip ; fi
RUN  unzip *.zip && rm -rf *.zip

RUN useradd -m -r -u 1001 -g root $USER \
  && chgrp -R 0 $CATALINA_HOME && chmod -R g=u $CATALINA_HOME \
  && chmod ugo+x bin/run.sh

USER $USER

ENTRYPOINT ["run.sh"]
