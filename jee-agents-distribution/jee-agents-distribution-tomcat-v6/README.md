# How-to:
Build docker image:

    docker build . -t openidentityplatform/openam-j2ee-agent-tomcat

Run image

    docker run --rm -it  -e OPENAM_SERVER="http://host.docker.internal:8080/passport" -h app-01.domain.com -p 8081:8080 --name app-01 openidentityplatform/openam-j2ee-agent-tomcat

# Environment Variables

|Variable             |Default Value                 |Description           |
|---------------------|------------------------------|----------------------|
|OPENAM_SERVER        |http://localhost:8081/openam  |OpenAM Server URL     |
|OPENAM_AGENT_USER    |test                          |Agent profile name    |
|OPENAM_AGENT_PASSWORD|test                          |Agent profile password|
|OPENAM_AGENT_URL     |http://localhost:8080/agentapp|Agent app URL         |
