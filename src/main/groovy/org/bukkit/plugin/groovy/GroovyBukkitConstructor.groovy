package org.bukkit.plugin.groovy

import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.constructor.AbstractConstruct


class GroovyBukkitConstructor extends Constructor
{

	GroovyBukkitConstructor(GroovyRunner runner) {
		yamlConstructors[new Tag('!g')] = [
			construct: { node ->
				runner.runScript(constructScalar(node))
			}
		] as AbstractConstruct
	}

}
