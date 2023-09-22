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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.buildsystem.DependencyScope;
import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnRequestedDependency;
import io.spring.initializr.generator.io.template.MustacheTemplateRenderer;
import io.spring.initializr.generator.io.text.MustacheSection;
import io.spring.initializr.generator.language.ClassName;
import io.spring.initializr.generator.language.TypeDeclaration;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.project.contributor.MultipleResourcesProjectContributor;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import io.spring.initializr.generator.spring.code.MainApplicationTypeCustomizer;
import io.spring.initializr.generator.spring.documentation.HelpDocumentCustomizer;
import io.spring.initializr.generator.version.VersionReference;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

@ProjectGenerationConfiguration
public class NthProjectGenerationConfiguration {

	@Bean
	@ConditionalOnRequestedDependency("nth-common-bcdb")
	public MainApplicationTypeCustomizer<TypeDeclaration> nthBcdbApplicationAnnotator() {
		return (typeDeclaration) -> {
			typeDeclaration.annotations().add(ClassName.of("com.nth.common.bcdb.EnableBcdb"));
			typeDeclaration.annotations()
				.add(ClassName.of("org.springframework.scheduling.annotation.EnableScheduling"));
		};
	}

	@Bean
	@ConditionalOnRequestedDependency("nth-common-bcdb")
	public HelpDocumentCustomizer nthBcdbHelpDocumentCustomizer(MustacheTemplateRenderer templateRenderer) {
		return (document) -> document
			.addSection(new MustacheSection(templateRenderer, "nth-common-bcdb", Collections.emptyMap()));
	}

	@Bean
	@ConditionalOnRequestedDependency("nth-common-bcdb")
	public BuildCustomizer<Build> nthBcdbRestClientDependencyBuildCustomizer() {
		return (build) -> build.dependencies().add("nth-bcdb-rest-client");
	}

	@Bean
	public BuildCustomizer<Build> janinoDependencyBuildCustomizer() {
		return (build) -> build.dependencies().add("janino", "org.codehaus.janino", "janino", DependencyScope.COMPILE);
	}

	@Bean
	@ConditionalOnRequestedDependency("nth-common-watcher")
	public MainApplicationTypeCustomizer<TypeDeclaration> nthWatcherApplicationAnnotator() {
		return (typeDeclaration) -> typeDeclaration.annotations()
			.add(ClassName.of("com.nth.common.watcher.config.EnableWatcher"));
	}

	@Bean
	@ConditionalOnRequestedDependency("nth-common-leader-election")
	public MainApplicationTypeCustomizer<TypeDeclaration> nthLeaderElectionApplicationAnnotator() {
		return (typeDeclaration) -> typeDeclaration.annotations()
			.add(ClassName.of("com.nth.common.leader.EnableLeadershipElection"));
	}

	@Bean
	@ConditionalOnRequestedDependency("nth-common-mail")
	public MainApplicationTypeCustomizer<TypeDeclaration> nthMailApplicationAnnotator() {
		return (typeDeclaration) -> typeDeclaration.annotations().add(ClassName.of("com.nth.common.mail.EnableMail"));
	}

	@Bean
	@ConditionalOnRequestedDependency("nth-common-logging-error-mail")
	public MainApplicationTypeCustomizer<TypeDeclaration> nthLoggingErrorMailApplicationAnnotator() {
		return (typeDeclaration) -> typeDeclaration.annotations()
			.add(ClassName.of("com.nth.common.logging.mail.EnableLoggingErrorMail"));
	}

	@Bean
	@ConditionalOnRequestedDependency("nth-common-logging-error-mail")
	public HelpDocumentCustomizer nthLoggingErrorMailHelpDocumentCustomizer(MustacheTemplateRenderer templateRenderer) {
		return (document) -> document
			.addSection(new MustacheSection(templateRenderer, "nth-common-logging-error-mail", Collections.emptyMap()));
	}

	@Bean
	@ConditionalOnRequestedDependency("nth-common-data-jpa")
	public MainApplicationTypeCustomizer<TypeDeclaration> nthCommonDataJpaApplicationAnnotator() {
		return (typeDeclaration) -> typeDeclaration.annotations()
			.add(ClassName.of("org.springframework.data.jpa.repository.config.EnableJpaRepositories"),
					(builder) -> builder.add("repositoryFactoryBeanClass", ClassName
						.of("com.nth.common.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean")));
	}

	@Bean
	@ConditionalOnRequestedDependency("nth-common-data-elasticsearch")
	public MainApplicationTypeCustomizer<TypeDeclaration> nthCommonDataElasticsearchApplicationAnnotator() {
		return (typeDeclaration) -> typeDeclaration.annotations()
			.add(ClassName.of("com.nth.common.data.elasticsearch.support.EnableRollingElasticsearchRepositories"));
	}

	@Bean
	@ConditionalOnRequestedDependency("nth-spring-modules")
	public MainApplicationTypeCustomizer<TypeDeclaration> nthSpringModulesApplicationAnnotator(
			ProjectDescription projectDescription) {
		return (typeDeclaration) -> typeDeclaration.annotations()
			.add(ClassName.of("com.nth.modules.ModuleScan"),
					(builder) -> builder.add("value", projectDescription.getPackageName())
						.add("reloadableClassPath", "modules"));
	}

	@Bean
	public SingleResourceProjectContributor applicationYmlContributor() {
		return new SingleResourceProjectContributor("src/main/resources/application.yml",
				"classpath:configuration/application.yml.template");
	}

	@Bean
	public ProjectContributor applicationPropertiesRemoverContributor() {
		return new ProjectContributor() {
			@Override
			public void contribute(Path projectRoot) throws IOException {
				Path output = projectRoot.resolve("src/main/resources/application.properties");
				Files.delete(output);
			}

			@Override
			public int getOrder() {
				return Ordered.LOWEST_PRECEDENCE;
			}
		};
	}

	@Bean
	public MultipleResourcesProjectContributor projectTemplateResources() {
		return new MultipleResourcesProjectContributor("nth-project-template", (s) -> s.endsWith(".sh"));
	}

	@ConditionalOnBuildSystem(MavenBuildSystem.ID)
	@Bean
	public MultipleResourcesProjectContributor mavenProjectTemplateResources() {
		return new MultipleResourcesProjectContributor("nth-project-template-maven", (s) -> s.endsWith(".sh"));
	}

	@ConditionalOnBuildSystem(GradleBuildSystem.ID)
	@Bean
	public MultipleResourcesProjectContributor gradleProjectTemplateResources() {
		return new MultipleResourcesProjectContributor("nth-project-template-gradle", (s) -> s.endsWith(".sh"));
	}

	@ConditionalOnRequestedDependency("nth-inspinia-thymeleaf")
	@Bean
	public MultipleResourcesProjectContributor thymeleafDefaultTemplatesContributor() {
		return new MultipleResourcesProjectContributor("nth-project-template-thymeleaf");
	}

	@Bean
	@ConditionalOnRequestedDependency("nth-inspinia-thymeleaf")
	public BuildCustomizer<Build> thymeleafSpringSecurityCustomizer() {
		return (build) -> {
			build.dependencies().add("security");
			build.dependencies().add("web");
		};
	}

	@Bean
	@ConditionalOnRequestedDependency("nth-inspinia-thymeleaf")
	public HelpDocumentCustomizer thymeleafHelpDocumentCustomizer(MustacheTemplateRenderer templateRenderer) {
		return (document) -> document
			.addSection(new MustacheSection(templateRenderer, "nth-inspinia-thymeleaf", Collections.emptyMap()));
	}

	@Bean
	@ConditionalOnRequestedDependency("nth-kendoui-professional")
	public BuildCustomizer<Build> kendouiProfessionalSpringSecurityCustomizer() {
		return (build) -> {
			build.dependencies().add("security");
			build.dependencies().add("web");
		};
	}

	@Bean
	public HelpDocumentCustomizer nexusHelpDocumentCustomizer(MustacheTemplateRenderer templateRenderer) {
		return (document) -> document
			.addSection(new MustacheSection(templateRenderer, "nth-https-nexus", Collections.emptyMap()));
	}

	@Bean
	public ProjectContributor startScriptContributor(ProjectDescription projectDescription) {
		final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		return (projectRoot) -> {
			Path output = projectRoot.resolve("deploy/" + projectDescription.getArtifactId() + ".sh");
			if (!Files.exists(output)) {
				Files.createDirectories(output.getParent());
				Files.createFile(output);
			}
			Resource resource = resolver.getResource("nth-project-scripts/run.sh");
			byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
			String content = new String(bytes, StandardCharsets.UTF_8).replace("java-1.8",
					"java-" + projectDescription.getLanguage().jvmVersion());
			ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			FileCopyUtils.copy(is, Files.newOutputStream(output, StandardOpenOption.APPEND));
			output.toFile().setExecutable(true);
		};
	}

	@Bean
	public BuildCustomizer<MavenBuild> nthMavenBuildBuildCustomizer(ProjectDescription projectDescription) {
		return (build) -> {
			// add our distribution management
			build.distributionManagement()
				.snapshotRepository((repository) -> repository.id("deployment")
					.uniqueVersion(false)
					.url("https://dev1-git1.int.ch:8676/nexus/content/repositories/snapshot-policy"));
			build.distributionManagement()
				.repository((repository) -> repository.id("deployment")
					.url("https://dev1-git1.int.ch:8676/nexus/content/releases"));

			// configure spring-boot-maven-plugin
			build.plugins().add("org.springframework.boot", "spring-boot-maven-plugin", (plugin) -> {
				plugin.configuration((configuration) -> {
					configuration.add("executable", "true");
					configuration.configure("embeddedLaunchScriptProperties",
							(embeddedLaunchScriptProperties) -> embeddedLaunchScriptProperties.add("mode", "service"));
				});
				plugin.execution("default", (execution) -> execution.goal("build-info"));
			});

			// add maven-assembly-plugin
			build.plugins().add("org.apache.maven.plugins", "maven-assembly-plugin", (plugin) -> {
				plugin.configuration((configuration) -> {
					configuration.add("finalName", "${project.artifactId}-${project.version}");
					configuration.configure("descriptors",
							(descriptors) -> descriptors.add("descriptor", "assembly.xml"));
				});
				plugin.execution("make-assembly", (execution) -> execution.phase("package").goal("single"));
			});

			// add maven-jar-plugin for attaching sources
			build.plugins().add("org.apache.maven.plugins", "maven-jar-plugin", (plugin) -> {
				plugin.configuration((configuration) -> {
					configuration.configure("archive", (archive) -> {
						archive.configure("manifest", (manifest) -> {
							manifest.add("addDefaultImplementationEntries", "true");
							manifest.add("addDefaultSpecificationEntries", "true");
						});
					});
				});
			});

			// add maven-enforcer-plugin
			build.plugins().add("org.apache.maven.plugins", "maven-enforcer-plugin", (plugin) -> {
				plugin.execution("enforce-banned-dependencies", (execution) -> execution.goal("enforce"));
				plugin.configuration((configuration) -> {
					configuration.configure("rules", (rules) -> {
						rules.add("bannedDependencies", (bannedDependencies) -> {
							bannedDependencies.configure("excludes", (excludes) -> {
								excludes.add("exclude", "commons-logging:*");
								excludes.add("exclude", "org.codehaus.jackson:*");
							});
							bannedDependencies.add("searchTransitive", "true");
						});
						rules.add("banDuplicateClasses",
								(banDuplicateClasses) -> banDuplicateClasses.add("findAllDuplicates", "true"));
						/**
						 * Currently this is not rendering valid xml, initializr generator
						 * does not have support for xml attributes of node
						 * rules.add("restrictImports
						 * implementation=\"de.skuzzle.enforcer.restrictimports.rule.RestrictImports\"",
						 * (restrictImports) -> { restrictImports.add("reason", "Use new
						 * Jackson (com.fasterxml.jackson)");
						 * restrictImports.add("bannedImport", "org.codehaus.jackson.**");
						 * }); rules.add("restrictImports
						 * implementation=\"de.skuzzle.enforcer.restrictimports.rule.RestrictImports\"",
						 * (restrictImports) -> { restrictImports.add("reason", "Use
						 * DatatypeConverter.printBase64Binary instead of
						 * BASE64Encoder.encode"); restrictImports.add("bannedImport",
						 * "sun.misc.BASE64Encoder"); }); rules.add("restrictImports
						 * implementation=\"de.skuzzle.enforcer.restrictimports.rule.RestrictImports\"",
						 * (restrictImports) -> { restrictImports.add("reason", "Use Java
						 * 8 Time instead of Joda if possible
						 * (https://www.oracle.com/technical-resources/articles/java/jf14-date-time.html)");
						 * restrictImports.add("bannedImport", "org.joda.**");
						 * restrictImports.add("failBuild", "false"); });
						 */
					});
					configuration.add("fail", "true");
				});
				plugin.dependency("org.codehaus.mojo", "extra-enforcer-rules", "1.5.1");
				plugin.dependency("de.skuzzle.enforcer", "restrict-imports-enforcer-rule", "2.0.0");
			});
		};
	}

	@ConditionalOnBuildSystem(GradleBuildSystem.ID)
	@Bean
	public HelpDocumentCustomizer gradleHelpDocumentCustomizer(MustacheTemplateRenderer templateRenderer) {
		return (document) -> document
			.addSection(new MustacheSection(templateRenderer, "nth-gradle", Collections.emptyMap()));
	}

	@Bean
	public BuildCustomizer<GradleBuild> nthGradleBuildBuildCustomizer(ProjectDescription projectDescription) {
		return (build) -> {
			build.plugins().add("java-library");
			build.plugins().add("distribution");
			build.plugins().add("maven-publish");

			build.configurations().add("deploymentZip");

			// configure Spring Boot plugin
			build.tasks()
				.customize("bootJar", (bootJar) -> bootJar.nested("launchScript",
						(launchScript) -> launchScript.invoke("properties 'mode':", "'service'")));
		};
	}

	@Order
	@Bean
	public BuildCustomizer<Build> nthRepositoriesAndDependencyVersionsBuildCustomizer(
			ProjectDescription projectDescription) {
		return (build) -> {
			// add our repositories
			build.repositories()
				.add(MavenRepository
					.withIdAndUrl("nth-nexus-releases",
							"https://dev1-git1.int.ch:8676/nexus/content/repositories/releases")
					.name("NTH Nexus Releases")
					.snapshotsEnabled(false)
					.releasesEnabled(true));
			build.repositories()
				.add(MavenRepository
					.withIdAndUrl("nth-nexus-snapshots",
							"https://dev1-git1.int.ch:8676/nexus/content/repositories/snapshot-policy")
					.name("NTH Nexus Snapshots")
					.snapshotsEnabled(true)
					.releasesEnabled(false));

			if (StringUtils.hasText(projectDescription.getLanguage().jvmVersion())) {
				if (!"1.8".equals(projectDescription.getLanguage().jvmVersion())) {
					build.dependencies()
						.add("jaxb-runtime", "org.glassfish.jaxb", "jaxb-runtime", DependencyScope.COMPILE);
				}
			}

			// add configuration-processor
			build.dependencies().add("configuration-processor");

			NexusArtifactResolver resolver = new NexusArtifactResolver();
			// replace LATEST and RELEASE versions with real versions from Nexus
			build.dependencies().ids().parallel().filter((id) -> id.startsWith("nth-")).forEach((id) -> {
				Dependency dependency = build.dependencies().get(id);
				try {
					ArtifactResolveResource resolveResource = resolver.resolve(dependency);
					if (resolveResource != null) {
						String version = resolveResource.getBaseVersion();
						if (!StringUtils.hasText(version)) {
							version = resolveResource.getVersion();
						}
						if (StringUtils.hasText(version)) {
							build.dependencies()
								.add(id, Dependency.from(dependency).version(VersionReference.ofValue(version)));
						}
					}
				}
				catch (RestClientException ex) {
					LoggerFactory.getLogger(getClass())
						.error("Error resolving dependency {} at Nexus - {}", id, ex.getMessage());
				}
			});
		};
	}

}
