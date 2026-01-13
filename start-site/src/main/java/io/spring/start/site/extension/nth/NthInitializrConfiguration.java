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

import io.spring.initializr.metadata.InitializrMetadataBuilder;
import io.spring.initializr.metadata.InitializrProperties;
import io.spring.initializr.web.support.DefaultInitializrMetadataProvider;
import io.spring.initializr.web.support.InitializrMetadataUpdateStrategy;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("nth")
@Configuration
@EnableConfigurationProperties(NthInitializrProperties.class)
public class NthInitializrConfiguration {

	@Bean
	@Primary
	DefaultInitializrMetadataProvider nthInitializrMetadataProvider(InitializrProperties initializrProperties,
			NthInitializrProperties nthInitializrProperties,
			InitializrMetadataUpdateStrategy initializrMetadataUpdateStrategy) {
		InitializrMetadataBuilder initializrMetadataBuilder = InitializrMetadataBuilder
			.fromInitializrProperties(nthInitializrProperties.getInitializr())
			.withInitializrProperties(initializrProperties, true);

		initializrMetadataBuilder.withCustomizer((metadata) -> {
			metadata.getTypes().getContent().forEach((t) -> t.setDefault(t.getId().equals("maven-project")));
			// set Java 21 as default
			metadata.getJavaVersions().getDefault().setDefault(false);
			metadata.getJavaVersions().get("21").setDefault(true);

			metadata.getPackagings()
				.setContent(metadata.getPackagings()
					.getContent()
					.stream()
					.filter(c -> c.getId().equals("jar"))
					.peek(c -> c.setDefault(true))
					.toList());
			metadata.getConfigurationFileFormats()
				.setContent(metadata.getConfigurationFileFormats()
					.getContent()
					.stream()
					.filter(c -> c.getId().equals("yaml"))
					.peek(c -> c.setDefault(true))
					.toList());

			metadata.getGroupId().merge(nthInitializrProperties.getInitializr().getGroupId().getValue());
			metadata.getArtifactId().merge(nthInitializrProperties.getInitializr().getArtifactId().getValue());
			metadata.getVersion().merge(nthInitializrProperties.getInitializr().getVersion().getValue());
			metadata.getName().merge(nthInitializrProperties.getInitializr().getName().getValue());
			metadata.getDescription().merge(nthInitializrProperties.getInitializr().getDescription().getValue());
			metadata.getPackageName().merge(nthInitializrProperties.getInitializr().getPackageName().getValue());
		});

		return new DefaultInitializrMetadataProvider(initializrMetadataBuilder.build(),
				initializrMetadataUpdateStrategy);
	}

}
