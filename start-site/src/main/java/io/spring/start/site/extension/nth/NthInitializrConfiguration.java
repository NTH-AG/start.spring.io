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

import io.spring.initializr.metadata.InitializrMetadataBuilder;
import io.spring.initializr.metadata.InitializrProperties;
import io.spring.initializr.web.support.DefaultInitializrMetadataProvider;
import io.spring.initializr.web.support.InitializrMetadataUpdateStrategy;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
			// remove WAR
			metadata.getPackagings().getContent().removeIf((p) -> !p.isDefault());
			// remove non Maven
			metadata.getTypes().getContent().removeIf((t) -> !t.isDefault());
			// remove non Java
			metadata.getLanguages().getContent().removeIf((l) -> !l.isDefault());
			// set Java 11 as default
			metadata.getJavaVersions().getDefault().setDefault(false);
			metadata.getJavaVersions().get("11").setDefault(true);
		});

		return new DefaultInitializrMetadataProvider(initializrMetadataBuilder.build(),
				initializrMetadataUpdateStrategy);
	}

}
