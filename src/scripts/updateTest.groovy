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
def downloadFile = new File(g.plugin.tempFolder, filename)
if (downloadFile.exists()) downloadFile.delete()
downloadFile.withOutputStream { os ->  os << conn.inputStream }

// open zip
def downloadZip = new ZipFile(downloadFile)
def zipRoot = filename - '.zip'

// extract jar
def updateJar = new File(gbukkitDir, gbukkitJar)
if (updateJar.exists()) updateJar.delete()
updateJar.withOutputStream { os ->
    os << downloadZip.getInputStream(downloadZip.getEntry(zipRoot+'/'+gbukkitJar))
}

// extract scripts
def scriptsRoot = zipRoot + '/src/scripts/'
def scriptsDir = new File(g.plugin.dataFolder, 'scripts/')
scriptsDir.mkdirs()
downloadZip.entries().findAll{it.name.startsWith(scriptsRoot)}.each {
    name = it.name - scriptsRoot
    if (name && !name.endsWith('/')) {
        def scriptFile = new File(scriptsDir, name)
        println name + " (${scriptFile.exists()?'exists':'new'}) ${it.size} bytes"
        scriptFile.withOutputStream { os -> os << downloadZip.getInputStream(it) }
    }
}

pluginConfig.setProperty('update-last', filename)
s.reload()

"Loaded $filename"