package ca.eqv.maven.plugins.tsc.mojo;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.LocalArtifactRequest;
import org.eclipse.aether.repository.LocalArtifactResult;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE)
public class CompileMojo extends AbstractMojo {

	@Component
	private RepositorySystem repoSystem;

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	private RepositorySystemSession repoSession;

	@Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
	private List<RemoteRepository> projectRepoList;

	@Component
	private ArchiverManager archiverManager;

	/** (Internal) Temporary directory where the TypeScript distribution will be unpacked */
	@Parameter(defaultValue = "${project.build.directory}/tsc-maven/typescript")
	private File typescriptHome;

	/** (Internal) Maven groupId for the TypeScript distribution that's been re-packaged as a Maven artifact */
	@Parameter(defaultValue = "ca.eqv.maven.plugins.tsc")
	private String typescriptGroupId;

	/** (Internal) Maven artifactId for the TypeScript distribution that's been re-packaged as a Maven artifact */
	@Parameter(defaultValue = "typescript")
	private String typescriptArtifactId;

	/** (Internal) Maven classifier for the TypeScript distribution that's been re-packaged as a Maven artifact */
	@Parameter(defaultValue = "")
	private String typescriptClassifier;

	/** (Internal) Maven extension for the TypeScript distribution that's been re-packaged as a Maven artifact */
	@Parameter(defaultValue = "zip")
	private String typescriptExtension;

	/** (Internal) Maven version for the TypeScript distribution that's been re-packaged as a Maven artifact */
	@Parameter(defaultValue = "1.4.1")
	private String typescriptVersion;

	/** (Internal) Relative path to tsc */
	@Parameter(defaultValue = "bin/tsc")
	private String typescriptTscPath;

	/** (Internal) Maven version for the Avatar.js artifact */
	@Parameter(defaultValue = "0.10.32-SNAPSHOT")
	private String avatarJsVersion;

	/** (Internal) Temporary directory where the Avatar.js native library will be copied */
	@Parameter(defaultValue = "${project.build.directory}/tsc-maven/avatar-js")
	private File avatarJsHome;

	/** (Internal) Maven version for the org.codehaus.mojo:exec-maven-plugin artifact */
	@Parameter(defaultValue = "1.3.2")
	private String execMavenPluginVersion;

	@Component
	private MavenProject mavenProject;

	@Component
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;

	@Override
	public void execute() throws MojoExecutionException {
		if (!typescriptHome.isDirectory()) {
			unpackCompiler();
		}
		else {
			getLog().debug("Skipping tsc extraction");
		}

		if (!avatarJsHome.isDirectory()) {
			prepareAvatarJsNativeLibrary();
		}
		else {
			getLog().debug("Skipping Avatar.js preparation");
		}

		executeCompiler();
	}

	private void unpackCompiler() throws MojoExecutionException {
		final File file = getArtifact(typescriptGroupId, typescriptArtifactId, typescriptClassifier, typescriptExtension, typescriptVersion);
		final UnArchiver unarchiver;

		try {
			unarchiver = archiverManager.getUnArchiver(file);
		}
		catch (final NoSuchArchiverException e) {
			throw new MojoExecutionException("Unsupported archive type for artifact " + file.getName());
		}

		final boolean createdDirectory = typescriptHome.mkdirs();
		if (!createdDirectory) {
			throw new MojoExecutionException("Unable to create tsc directory " + typescriptHome.getAbsolutePath());
		}

		getLog().info("Extracting tsc to " + typescriptHome.getAbsolutePath());
		unarchiver.setSourceFile(file);
		unarchiver.setDestDirectory(typescriptHome);
		unarchiver.extract();
	}

	private void prepareAvatarJsNativeLibrary() throws MojoExecutionException {
		final AvatarJsNativeLibraryPlatform platform = AvatarJsNativeLibraryPlatform.detect();
		final String artifactId = "libavatar-js-" + platform.artifactIdSuffix;
		final File repoFile = getArtifact("com.oracle", artifactId, null, platform.extension, avatarJsVersion);

		final boolean createdDirectory = avatarJsHome.mkdirs();
		if (!createdDirectory) {
			throw new MojoExecutionException("Unable to create Avatar.js directory " + avatarJsHome.getAbsolutePath());
		}

		final String homePath = avatarJsHome.getAbsolutePath();
		final String nativeLibFilename = "libavatar-js." + platform.extension;
		final File nativeLibFile = new File(homePath + File.separator + nativeLibFilename);

		try {
			getLog().info("Preparing Avatar.js native library in " + avatarJsHome.getAbsolutePath());
			FileUtils.copyFile(repoFile, nativeLibFile);
		}
		catch (final IOException e) {
			throw new MojoExecutionException("Unable to prepare Avatar.js native library", e);
		}
	}

	private void executeCompiler() throws MojoExecutionException {
		final File avatarJs = getArtifact("com.oracle", "avatar-js", null, "jar", avatarJsVersion);

		getLog().info("Starting TypeScript compiler...");
		executeMojo(
				plugin("org.codehaus.mojo", "exec-maven-plugin", execMavenPluginVersion),
				"exec",
				configuration(
						element("executable", "java"),
						element("arguments",
								element("argument", "-Djava.library.path=" + avatarJsHome.getAbsolutePath()),
								element("argument", "-jar"),
								element("argument", avatarJs.getAbsolutePath()),
								element("argument", typescriptHome.getAbsolutePath() + File.separator + typescriptTscPath)
						)
				),
				executionEnvironment(mavenProject, mavenSession, pluginManager)
		);
		getLog().info("TypeScript compiler exited successfully.");
	}

	private File getArtifact(final String groupId, final String artifactId, final String classifier, final String extension, final String version) throws MojoExecutionException {
		final DefaultArtifact artifact = new DefaultArtifact(groupId, artifactId, classifier, extension, version);

		final LocalArtifactRequest localRequest = new LocalArtifactRequest(artifact, projectRepoList, null);
		final LocalRepositoryManager localManager = repoSession.getLocalRepositoryManager();
		final LocalArtifactResult localResult = localManager.find(repoSession, localRequest);
		if (localResult.isAvailable()) {
			return localResult.getFile();
		}

		try {
			final ArtifactRequest remoteRequest = new ArtifactRequest(artifact, projectRepoList, null);
			final ArtifactResult remoteResult = repoSystem.resolveArtifact(repoSession, remoteRequest);
			return remoteResult.getArtifact().getFile();
		}
		catch (final ArtifactResolutionException e) {
			throw new MojoExecutionException("Could not retrieve artifact", e);
		}
	}

}
