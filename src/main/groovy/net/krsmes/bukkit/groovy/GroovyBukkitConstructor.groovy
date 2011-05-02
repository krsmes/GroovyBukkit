package net.krsmes.bukkit.groovy

import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.constructor.AbstractConstruct
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor


@SuppressWarnings("GroovyAccessibility")
class GroovyBukkitConstructor extends CustomClassLoaderConstructor
{
    static G_TAG = new Tag('!g')
    static WORLD_TAG = new Tag('!world')
    static PLAYER_TAG = new Tag('!player')

	GroovyBukkitConstructor(GroovyAPI api, ClassLoader loader) {
        super(loader)

        yamlConstructors.putAll([
            (G_TAG): ([
                construct: { node ->
                    Eval.me('g', api, 'g.' + constructScalar(node))
                }
            ] as AbstractConstruct),

            (WORLD_TAG): ([
                construct: { node ->
                    api.server.getWorld((String)constructScalar(node))
                }
            ] as AbstractConstruct),

            (PLAYER_TAG): ([
                construct: { node ->
                    api.server.getPlayer((String)constructScalar(node))
                }
            ] as AbstractConstruct)
        ])
    }

}
