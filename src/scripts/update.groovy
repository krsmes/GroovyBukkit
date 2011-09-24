import java.util.zip.ZipFile

def force = args ? args[0] == 'force' : false
def pluginConfig = g.plugin.configuration

def gbukkitLoc = pluginConfig.getString('update-location', 'https://github.com/krsmes/GroovyBukkit/zipball/master')
def gbukkitDir = pluginConfig.getString('update-dir', 'plugins/update')
def gbukkitJar = pluginConfig.getString('update-jar', 'GroovyBukkit.jar')
def gbukkitLast = pluginConfig.getString('update-last')

def conn = new URL(gbukkitLoc).openConnection()
def filename = conn.headerFields.'Content-Disposition'[0].split('=')[1]

// make sure file is different than previous
if (filename == gbukkitLast && !force) {
    return "Already up-to-date"
}

// download file
def download = new File(g.plugin.tempFolder, filename)
download.withOutputStream { os ->
    os << conn.inputStream
}

// open zip
def zip = new ZipFile(download)

// extract GroovyBukkit.jar to plugins/update
new File(gbukkitDir, gbukkitJar).withOutputStream { os ->
    os << zip.getInputStream(zip.getEntry(gbukkitJar))
}

pluginConfig.setProperty('update-last', filename)
"Need to reload ($filename)"
