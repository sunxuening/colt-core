package codeOrchestra.colt.core.update.tasks

import codeOrchestra.colt.core.net.ProxyModel
import codeOrchestra.util.PathUtils
import codeOrchestra.util.SystemInfo

/**
 * @author Dima Kruk
 */
class UpdateManager {

    private static String UPDATE_URL = "http://codeorchestra.s3.amazonaws.com/colt_updates/"

    public static ArrayList<UpdateTask> checkForUpdate() {
        File baseDir = PathUtils.applicationBaseDir
        File jarDir = baseDir

        if (SystemInfo.isMac) {
            jarDir = new File(baseDir, "Contents/Java")
        } else if(SystemInfo.isWindows) {
            jarDir = new File(baseDir, "lib")
        }

        ArrayList<UpdateTask> result = new ArrayList<>()

        try {
            if (checkJar("colt-core.jar", jarDir)) {
                result.add(new UpdateTask(UPDATE_URL + "colt-core.jar", jarDir.path))
            }
            if (checkJar("colt-js.jar", jarDir)) {
                result.add(new UpdateTask(UPDATE_URL + "colt-js.jar", jarDir.path))
            }
            if (checkJar("colt-as.jar", jarDir)) {
                result.add(new UpdateTask(UPDATE_URL + "colt-as.jar", jarDir.path))
            }
            if (result.size() > 0) {
                result.add(new UpdateTask(UPDATE_URL + "lib.zip", baseDir.path + File.separator + "lib"))
            }
        } catch (Exception ignored) {
            return null
        }

        return result
    }

    private static boolean checkJar(String fileName, File baseDir) {
        File curJar = new File(baseDir, fileName)
        if (!curJar.exists()) {
            return false
        }

        URL url = new URL(UPDATE_URL + fileName)
        ProxyModel proxyModel = ProxyModel.instance
        HttpURLConnection httpConn
        if (proxyModel.usingProxy()) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyModel.host, proxyModel.port as int))
            httpConn = (HttpURLConnection) url.openConnection(proxy)
        } else {
            httpConn = (HttpURLConnection) url.openConnection()
        }
        int responseCode = httpConn.getResponseCode()

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String lastModified = httpConn.getHeaderField("Last-Modified")
            return new Date(lastModified).after(new Date(curJar.lastModified()))
        } else {
            throw new Exception()
        }
    }
}