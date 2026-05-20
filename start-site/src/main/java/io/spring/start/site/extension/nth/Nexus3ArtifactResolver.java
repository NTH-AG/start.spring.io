/*
 * Copyright 2012 - present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.start.site.extension.nth;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.version.VersionReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

public class Nexus3ArtifactResolver {

	private static final Logger log = LoggerFactory.getLogger(Nexus3ArtifactResolver.class);

	private final String url = "https://dev-nexus1.nth.ch/service/rest/v1/search/assets?sort=version&maven.groupId={g}&maven.artifactId={a}&repository={r}&maven.extension=pom";

	/**
	 * Resolve artifact at Nexus.
	 * @param groupId group id of the artifact (Required).
	 * @param artifactId artifact id of the artifact (Required).
	 * @param version version of the artifact (Required) Supports resolving of "LATEST",
	 * "RELEASE" and snapshot versions ("1.0-SNAPSHOT") too.
	 * @param repository repository that the artifact is contained in (Required).
	 * @return artifact resolve resource
	 */
	public VersionReference resolve(String groupId, String artifactId, String version, String repository) {
		log.info("resolve(groupId={}, artifactId={}, version={}, repository={})", groupId, artifactId, version,
				repository);
		Pattern pattern = Pattern.compile("(.*)-(20[0-9]{6}.[0-9]*-[0-9]*)");
		Nexus3AssetsResponse data = new RestTemplate().getForObject(this.url, Nexus3AssetsResponse.class, groupId,
				artifactId, repository);
		log.info("Resolved: {}", data);
		if (data != null) {
			if (!CollectionUtils.isEmpty(data.items())) {
				return data.items().stream().map((i) -> i.maven2()).map((m) -> m.version()).filter((v) -> {
					if ("RELEASE".equals(version)) {
						return !pattern.matcher(v).matches();
					}
					return true;
				}).map((v) -> {
					if (StringUtils.hasText(v)) {
						Matcher matcher = pattern.matcher(v);
						if (matcher.matches()) {
							v = matcher.group(1) + "-SNAPSHOT";
						}
						return VersionReference.ofValue(v);
					}
					return null;
				}).filter(Objects::nonNull).findFirst().orElse(null);
			}
		}
		return null;
	}

	public VersionReference resolve(String groupId, String artifactId, String version) {
		return resolve(groupId, artifactId, version, "maven-public");
	}

	public VersionReference resolve(Dependency dependency) {
		return resolve(dependency.getGroupId(), dependency.getArtifactId(),
				Optional.ofNullable(dependency.getVersion()).map(VersionReference::getValue).orElse(null));
	}

	record Nexus3AssetsResponse(String continuationToken, List<Item> items) {

	}

	record Item(String repository, Maven2 maven2) {

	}

	record Maven2(String extension, String artifactId, String version, String groupId) {

	}

}
