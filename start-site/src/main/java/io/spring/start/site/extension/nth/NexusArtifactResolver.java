/*
 * Copyright 2012-2019 the original author or authors.
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

import java.util.Optional;

import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.version.VersionReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.client.RestTemplate;

public class NexusArtifactResolver {

	private static final Logger log = LoggerFactory.getLogger(NexusArtifactResolver.class);

	private final String url = "http://dev1-git1.int.ch:8675/nexus/service/local/artifact/maven/resolve?g={g}&a={a}&v={v}&r={r}";

	/**
	 * Resolve artifact at Nexus.
	 * @param groupId group id of the artifact (Required).
	 * @param artifactId artifact id of the artifact (Required).
	 * @param version version of the artifact (Required) Supports resolving of "LATEST",
	 * "RELEASE" and snapshot versions ("1.0-SNAPSHOT") too.
	 * @param repository repository that the artifact is contained in (Required).
	 * @return artifact resolve resource
	 */
	public ArtifactResolveResource resolve(String groupId, String artifactId, String version, String repository) {
		log.info("resolve(groupId={}, artifactId={}, version={}, repository={})", groupId, artifactId, version,
				repository);
		ArtifactResolveResource data = new RestTemplate()
			.getForObject(this.url, ArtifactResolveResourceResponse.class, groupId, artifactId, version, repository)
			.getData();
		log.info("Resolved: {}", data);
		return data;
	}

	public ArtifactResolveResource resolve(String groupId, String artifactId, String version) {
		if ("RELEASE".equalsIgnoreCase(version)) {
			return resolve(groupId, artifactId, version, "releases");
		}
		else if ("LATEST".equalsIgnoreCase(version)) {
			return resolve(groupId, artifactId, version, "snapshot-policy");
		}
		return null;
	}

	public ArtifactResolveResource resolve(Dependency dependency) {
		return resolve(dependency.getGroupId(), dependency.getArtifactId(),
				Optional.ofNullable(dependency.getVersion()).map(VersionReference::getValue).orElse(null));
	}

}
