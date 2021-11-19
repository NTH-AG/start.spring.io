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

import io.spring.initializr.generator.buildsystem.Dependency;
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
		return resolve(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion().getValue());
	}

	public static class ArtifactResolveResource {

		private String extension;

		private String sha1;

		private String fileName;

		private boolean snapshot;

		private String classifier;

		private long snapshotBuildNumber;

		private boolean presentLocally;

		private String repositoryPath;

		private String artifactId;

		private String groupId;

		private long snapshotTimeStamp;

		private String baseVersion;

		private String version;

		public String getExtension() {
			return this.extension;
		}

		public void setExtension(String extension) {
			this.extension = extension;
		}

		public String getSha1() {
			return this.sha1;
		}

		public void setSha1(String sha1) {
			this.sha1 = sha1;
		}

		public String getFileName() {
			return this.fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public boolean isSnapshot() {
			return this.snapshot;
		}

		public void setSnapshot(boolean snapshot) {
			this.snapshot = snapshot;
		}

		public String getClassifier() {
			return this.classifier;
		}

		public void setClassifier(String classifier) {
			this.classifier = classifier;
		}

		public long getSnapshotBuildNumber() {
			return this.snapshotBuildNumber;
		}

		public void setSnapshotBuildNumber(long snapshotBuildNumber) {
			this.snapshotBuildNumber = snapshotBuildNumber;
		}

		public boolean isPresentLocally() {
			return this.presentLocally;
		}

		public void setPresentLocally(boolean presentLocally) {
			this.presentLocally = presentLocally;
		}

		public String getRepositoryPath() {
			return this.repositoryPath;
		}

		public void setRepositoryPath(String repositoryPath) {
			this.repositoryPath = repositoryPath;
		}

		public String getArtifactId() {
			return this.artifactId;
		}

		public void setArtifactId(String artifactId) {
			this.artifactId = artifactId;
		}

		public String getGroupId() {
			return this.groupId;
		}

		public void setGroupId(String groupId) {
			this.groupId = groupId;
		}

		public long getSnapshotTimeStamp() {
			return this.snapshotTimeStamp;
		}

		public void setSnapshotTimeStamp(long snapshotTimeStamp) {
			this.snapshotTimeStamp = snapshotTimeStamp;
		}

		public String getBaseVersion() {
			return this.baseVersion;
		}

		public void setBaseVersion(String baseVersion) {
			this.baseVersion = baseVersion;
		}

		public String getVersion() {
			return this.version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("ArtifactResolveResource{");
			sb.append("extension='").append(this.extension).append('\'');
			sb.append(", sha1='").append(this.sha1).append('\'');
			sb.append(", fileName='").append(this.fileName).append('\'');
			sb.append(", snapshot=").append(this.snapshot);
			sb.append(", classifier='").append(this.classifier).append('\'');
			sb.append(", snapshotBuildNumber=").append(this.snapshotBuildNumber);
			sb.append(", presentLocally=").append(this.presentLocally);
			sb.append(", repositoryPath='").append(this.repositoryPath).append('\'');
			sb.append(", artifactId='").append(this.artifactId).append('\'');
			sb.append(", groupId='").append(this.groupId).append('\'');
			sb.append(", snapshotTimeStamp=").append(this.snapshotTimeStamp);
			sb.append(", baseVersion='").append(this.baseVersion).append('\'');
			sb.append(", version='").append(this.version).append('\'');
			sb.append('}');
			return sb.toString();
		}

	}

	public static class ArtifactResolveResourceResponse {

		private ArtifactResolveResource data;

		public ArtifactResolveResource getData() {
			return this.data;
		}

		public void setData(ArtifactResolveResource data) {
			this.data = data;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("ArtifactResolveResourceResponse{");
			sb.append("data=").append(this.data);
			sb.append('}');
			return sb.toString();
		}

	}

}
