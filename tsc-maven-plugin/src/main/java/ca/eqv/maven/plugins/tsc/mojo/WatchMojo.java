package ca.eqv.maven.plugins.tsc.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.Collections;
import java.util.List;

@Mojo(name = "watch", defaultPhase = LifecyclePhase.NONE)
public class WatchMojo extends AbstractTypeScriptMojo {
	@Override
	protected List<String> getModeArguments() {
		return Collections.singletonList("--watch");
	}
}
