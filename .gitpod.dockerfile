FROM gitpod/workspace-postgres:latest

USER gitpod

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh \
    && sdk default java 11.0.5-open"

RUN curl https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.4.2-darwin-x86_64.tar.gz --output elasticsearch-7.4.2.tar.gz \
    && tar -xzf elasticsearch-7.4.2.tar.gz
ENV ES_HOME="$HOME/elasticsearch-7.4.2"
