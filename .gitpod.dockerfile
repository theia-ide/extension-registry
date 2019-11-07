FROM gitpod/workspace-postgres:latest

USER gitpod

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh \
    && sdk default java 11.0.2-zulufx"

RUN curl https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.4.2-darwin-x86_64.tar.gz --output elasticsearch-7.4.2.tar.gz \
    && tar -xzf elasticsearch-7.4.2.tar.gz

EXPOSE 9200
EXPOSE 9300
ENTRYPOINT [ "./elasticsearch-7.4.2/bin/elasticsearch", "-d", "-Ediscovery.type=single-node", "-Expack.ml.enabled=false" ]
