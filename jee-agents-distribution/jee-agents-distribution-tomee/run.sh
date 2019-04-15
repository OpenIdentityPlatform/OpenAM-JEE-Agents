#!/usr/bin/env bash

cd $CATALINA_HOME
echo $OPENAM_AGENT_PASSWORD >> .password
echo "CONFIG_DIR= $CATALINA_HOME/conf" >> .response
echo "AM_SERVER_URL= $OPENAM_SERVER" >> .response
echo "CATALINA_HOME= $CATALINA_HOME" >> .response
echo "INSTALL_GLOBAL_WEB_XML= " >> .response
echo "AGENT_URL= $OPENAM_AGENT_URL" >> .response
echo "AGENT_PROFILE_NAME= $OPENAM_AGENT_USER" >> .response
echo "AGENT_PASSWORD_FILE= $CATALINA_HOME/.password" >> .response
echo "Agent ----------------------------"; cat .response
j2ee_agents/tomcat_v6_agent/bin/agentadmin --install --acceptLicense --useResponse .response
rm -rf .password .response
echo "Agent.log ----------------------------"; cat j2ee_agents/tomcat_v6_agent/installer-logs/debug/Agent.log
echo "install.log ----------------------------"; cat j2ee_agents/tomcat_v6_agent/installer-logs/audit/install.log
catalina.sh run