package ca.eqv.maven.plugins.tsc.mojo;

import org.apache.commons.lang3.SystemUtils;

public enum AvatarJsNativeLibraryPlatform {
	WIN_X64("win-x64", "dll"),
	MACOSX_X64("macosx-x64", "dylib"),
	LINUX_X64("linux-x64", "so");

	public final String artifactIdSuffix;
	public final String extension;

	AvatarJsNativeLibraryPlatform(final String artifactIdSuffix, final String extension) {
		this.artifactIdSuffix = artifactIdSuffix;
		this.extension = extension;
	}

	public static AvatarJsNativeLibraryPlatform detect() {
		final String osArch = System.getProperty("os.arch");
		if (!"x86_64".equals(osArch) && !"amd64".equals(osArch)) {
			throw new UnsupportedOperationException("Unsupported system architecture " + osArch);
		}

		if (SystemUtils.IS_OS_WINDOWS) {
			return WIN_X64;
		}
		if (SystemUtils.IS_OS_MAC_OSX) {
			return MACOSX_X64;
		}
		if (SystemUtils.IS_OS_LINUX) {
			return LINUX_X64;
		}
		throw new UnsupportedOperationException("Unsupported operating system " + SystemUtils.OS_NAME);
	}

}
