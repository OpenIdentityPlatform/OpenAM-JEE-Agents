#!/bin/bash
set -e

echo "OpenAM initial setup..."

cd /usr/openam/ssoconfiguratortools/

java -jar openam-configurator-tool*.jar --file openam.conf

echo "setup ssoadmin tools..."

cd /usr/openam/ssoadmintools

./setup -p $OPENAM_DATA_DIR -d /usr/openam/ssoadmintools/debug -d /usr/openam/ssoadmintools/log --acceptLicense

echo "create an agent..."

echo passw0rd > /tmp/pwd.txt && chmod 400 /tmp/pwd.txt

./openam/bin/ssoadm create-agent \
  --realm / \
  --agentname myAgent \
  --agenttype J2EEAgent \
  --adminid amadmin \
  --password-file /tmp/pwd.txt \
  --datafile agent.properties

echo "create a policy"

./openam/bin/ssoadm policy-import \
  --adminid amadmin \
  --password-file /tmp/pwd.txt \
  --servername http://openam.example.org:8080/openam \
  --realm / \
  --jsonfile policies.json

#echo "update servers and sites..."
#
#./openam/bin/ssoadm create-site \
#  --adminid amadmin \
#  --password-file /tmp/pwd.txt \
#  --sitename main \
#  --siteurl http://openam.example.org:8080/openam \
#  --secondaryurls http://localhost:8080/openam
#
#./openam/bin/ssoadm get-svrcfg-xml \
#  --adminid amadmin \
#  --password-file /tmp/pwd.txt \
#  --servername http://openam.example.org:8080/openam \
#  --outfile serverconfig.xml -v
#
#touch server.properties
#
#./openam/bin/ssoadm create-server \
#  --adminid amadmin \
#  --password-file /tmp/pwd.txt \
#  --servername http://localhost:8080/openam \
#  --datafile server.properties \
#  --serverconfigxml serverconfig.xml -v
#
#./openam/bin/ssoadm add-site-members \
#  --adminid amadmin \
#  --password-file /tmp/pwd.txt \
#  --servernames http://openam.example.org:8080/openam http://localhost:8080/openam \
#  --sitename main