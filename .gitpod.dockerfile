FROM gitpod/workspace-postgres:latest

USER gitpod

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh \
    && sdk default java 11.0.2-zulufx"
