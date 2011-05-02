package net.krsmes.bukkit.groovy;

import org.bukkit.util.config.ConfigurationException;
import org.bukkit.util.config.ConfigurationNode;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class GroovyConfiguration extends ConfigurationNode {

    protected Yaml yaml;
    private File file;


    public GroovyConfiguration(String name) {
        super(new HashMap<String, Object>());
        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        yaml = new Yaml(new SafeConstructor(), new Representer(), options);

        this.file = new File(GroovyPlugin.instance.getDataFolder(), name + ".yaml");
    }


    public void load() {
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(file);
            read(yaml.load(new UnicodeReader(stream)));
        }
        catch (IOException e) {
            root = new HashMap<String, Object>();
        }
        catch (ConfigurationException e) {
            root = new HashMap<String, Object>();
        }
        finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            }
            catch (IOException ignored) {
            }
        }
    }


    public boolean save() {
        FileOutputStream stream = null;

        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        try {
            stream = new FileOutputStream(file);
            yaml.dump(root, new OutputStreamWriter(stream, "UTF-8"));
            return true;
        }
        catch (IOException ignored) {
        }
        finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            }
            catch (IOException ignored) {
            }
        }

        return false;
    }


    @SuppressWarnings("unchecked")
    private void read(Object input) throws ConfigurationException {
        try {
            if (null == input) {
                root = new HashMap<String, Object>();
            }
            else {
                root = (Map<String, Object>) input;
            }
        }
        catch (ClassCastException e) {
            throw new ConfigurationException("Root document must be an key-value structure");
        }
    }

}
