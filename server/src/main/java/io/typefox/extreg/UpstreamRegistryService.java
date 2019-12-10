/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import static io.typefox.extreg.util.UrlUtil.createApiUrl;

import java.util.Arrays;

import com.google.common.base.Strings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import io.typefox.extreg.json.ExtensionJson;
import io.typefox.extreg.json.PublisherJson;
import io.typefox.extreg.json.ReviewListJson;
import io.typefox.extreg.json.SearchResultJson;
import io.typefox.extreg.util.NotFoundException;

@Component
public class UpstreamRegistryService implements IExtensionRegistry {

    @Autowired
    RestTemplate restTemplate;

    @Value("#{environment.OVSX_UPSTREAM_URL}")
    String upstreamUrl;

    public boolean isValid() {
        return !Strings.isNullOrEmpty(upstreamUrl);
    }

    @Override
    public PublisherJson getPublisher(String publisherName) {
        return restTemplate.getForObject(createApiUrl(upstreamUrl, publisherName), PublisherJson.class);
    }

    @Override
    public ExtensionJson getExtension(String publisherName, String extensionName) {
        return restTemplate.getForObject(createApiUrl(upstreamUrl, publisherName, extensionName), ExtensionJson.class);
    }

    @Override
    public ExtensionJson getExtension(String publisherName, String extensionName, String version) {
        return restTemplate.getForObject(createApiUrl(upstreamUrl, publisherName, extensionName, version), ExtensionJson.class);
    }

    @Override
    public byte[] getFile(String publisherName, String extensionName, String fileName) {
        return getFile(createApiUrl(upstreamUrl, publisherName, extensionName, "file", fileName));
    }

    @Override
    public byte[] getFile(String publisherName, String extensionName, String version, String fileName) {
        return getFile(createApiUrl(upstreamUrl, publisherName, extensionName, version, "file", fileName));
    }

    private byte[] getFile(String url) {
        var headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        var response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), byte[].class);
        switch (response.getStatusCode()) {
            case OK:
                return response.getBody();
            case NOT_FOUND:
                throw new NotFoundException();
            default:
                throw new ResponseStatusException(response.getStatusCode(),
                        "Upstream registry responded with status \"" + response.getStatusCode().getReasonPhrase() + "\".");
        }
    }

    @Override
    public ReviewListJson getReviews(String publisherName, String extensionName) {
        return restTemplate.getForObject(createApiUrl(upstreamUrl, publisherName, extensionName, "reviews"), ReviewListJson.class);
    }

	@Override
	public SearchResultJson search(String query, String category, int size, int offset) {
		return restTemplate.getForObject(createApiUrl(upstreamUrl, "-", "search"), SearchResultJson.class);
	}

}