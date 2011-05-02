package net.krsmes.bukkit.groovy

import org.junit.Test
import org.yaml.snakeyaml.Yaml


class PlotTest {

    def dumpyaml(d) {
        Yaml yaml = new Yaml(new GroovyBukkitRepresenter())
        yaml.dump(d)
    }


    def loadyaml(d) {
        Yaml yaml = new Yaml(new GroovyBukkitConstructor(null, this.class.classLoader))
        yaml.load(d)
    }


    @Test
    void plotToYamlAndBack() {
//        Plot p = new Plot('myplot')
//        def y = dumpyaml(p)
//        assert y == ''
//        def np = loadyaml(y)
//        assert np instanceof Plot
//        assert np.name == 'myplot'
    }
}
