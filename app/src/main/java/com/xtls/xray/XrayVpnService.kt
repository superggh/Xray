package com.xtls.xray

import android.content.Context
import android.content.Intent
<<<<<<< HEAD
<<<<<<< HEAD
import android.content.res.AssetManager
import android.net.ConnectivityManager
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.ProxyInfo
=======
>>>>>>> d746191b66978735e2483b68cbce72fd40a35cc6
=======
>>>>>>> d746191b66978735e2483b68cbce72fd40a35cc6
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.system.Os
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors.newFixedThreadPool


class XrayVpnService : VpnService() {

    inner class ServiceBinder : Binder() {
        fun getService(): XrayVpnService = this@XrayVpnService
    }

    private val binder: ServiceBinder = ServiceBinder()
    private var isRunning: Boolean = false
    private lateinit var tunDevice: ParcelFileDescriptor
    private var socksProcess: Process? = null
    private var tun2socksExecutor: ExecutorService? = null
    private lateinit var process: Process
    private lateinit var tunprocess: Process

    private val connectivity by lazy { getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    @delegate:RequiresApi(Build.VERSION_CODES.P)
    private val defaultNetworkCallback by lazy {
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                setUnderlyingNetworks(arrayOf(network))
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                // it's a good idea to refresh capabilities
                setUnderlyingNetworks(arrayOf(network))
            }

            override fun onLost(network: Network) {
                setUnderlyingNetworks(null)
            }
        }
    }

    fun getIsRunning(): Boolean = isRunning
    fun xrayPath(): String = "${applicationContext.applicationInfo.nativeLibraryDir}/libxray.so"
<<<<<<< HEAD
<<<<<<< HEAD
    fun tunPath(): String = "${applicationContext.applicationInfo.nativeLibraryDir}/libtun2socks.so"
    private fun bepassPath(): String = "${applicationContext.applicationInfo.nativeLibraryDir}/libbepass.so"
=======
>>>>>>> d746191b66978735e2483b68cbce72fd40a35cc6
=======
>>>>>>> d746191b66978735e2483b68cbce72fd40a35cc6
    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        Log.e("test","onCreate")
        super.onCreate()
        try {
            connectivity.requestNetwork(defaultNetworkRequest, defaultNetworkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun onDestroy() {
        stopVPN()
        super.onDestroy()
    }

    @delegate:RequiresApi(Build.VERSION_CODES.P)
    private val defaultNetworkRequest by lazy {
        NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .build()
    }

    fun isConfigExists(): Boolean {
        val config = Settings.xrayConfig(applicationContext)
        return config.exists() && config.isFile
    }

    fun installXray() {
        Log.e("test","installXray")
        val filesPath = applicationContext.filesDir.absolutePath
        assets.list("")?.forEach { fileName ->
            var input: InputStream? = null
            var output: OutputStream? = null
            try {
                val file = File(filesPath, fileName)
                input = assets.open(fileName)
                output = FileOutputStream(file)
                /** Copy file */
                val buffer = ByteArray(1024)
                var read: Int?
                while (input.read(buffer).also { read = it } != -1) {
                    read?.let { output.write(buffer, 0, it) }
                }
                /** Set file permission 644 */
                val permission = HashSet<PosixFilePermission>()
                permission.add(PosixFilePermission.OWNER_READ)
                permission.add(PosixFilePermission.OWNER_WRITE)
                permission.add(PosixFilePermission.OWNER_EXECUTE)
                permission.add(PosixFilePermission.GROUP_READ)
                permission.add(PosixFilePermission.GROUP_WRITE)
                permission.add(PosixFilePermission.GROUP_EXECUTE)
                permission.add(PosixFilePermission.OTHERS_READ)
                permission.add(PosixFilePermission.OTHERS_WRITE)
                permission.add(PosixFilePermission.OTHERS_EXECUTE)
                Log.e("test",file.toPath().toString())
                Files.setPosixFilePermissions(file.toPath(), permission)
            } catch (_: IOException) {
                // Ignore
            } finally {
                if (input != null) {
                    try { input.close() } catch (_: IOException) {}
                }
                if (output != null) {
                    try { output.close() } catch (_: IOException) {}
                }
            }
        }
    }
    fun copyAssets(context: Context) {
        val assetManager: AssetManager = context.assets
        var files: Array<String>? = null
        try {
            files = assetManager.list("")

            if (files != null) for (filename in files) {
                var `in`: InputStream? = null
                var out: OutputStream? = null
                try {
                    `in` = assetManager.open(filename)
                    val outFile = File(context.getExternalFilesDir(null), filename)
                    Log.e("test",outFile.absolutePath)
                    out = FileOutputStream(outFile)
                    copyFile(`in`, out)
//                    val permission = HashSet<PosixFilePermission>()
//                    permission.add(PosixFilePermission.OWNER_READ)
//                    permission.add(PosixFilePermission.OWNER_WRITE)
//                    permission.add(PosixFilePermission.OWNER_EXECUTE)
//                    permission.add(PosixFilePermission.GROUP_READ)
//                    permission.add(PosixFilePermission.GROUP_WRITE)
//                    permission.add(PosixFilePermission.GROUP_EXECUTE)
//                    permission.add(PosixFilePermission.OTHERS_READ)
//                    permission.add(PosixFilePermission.OTHERS_WRITE)
//                    permission.add(PosixFilePermission.OTHERS_EXECUTE)
//                    Files.setPosixFilePermissions(outFile.toPath(), permission)
                } catch (e: IOException) {
                    Log.e("tag", "Failed to copy asset file: $filename", e)
                } finally {
                    if (`in` != null) {
                        try {
                            `in`.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    if (out != null) {
                        try {
                            out.close()

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        } catch (e: IOException) {
            Log.e("tag", "Failed to get asset file list.", e)
        }

    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream?, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int? = null
        while (`in`?.read(buffer).also({ read = it!! }) != -1) {
            read?.let { out.write(buffer, 0, it) }
        }
    }

 

    fun startVPN() {
        isRunning = true
<<<<<<< HEAD
<<<<<<< HEAD

        if (Settings.useBepass) {
            Thread {
                val bepassCommand = arrayListOf(
                    bepassPath(), "-c", Settings.bepassConfig(applicationContext).absolutePath
                )
                socksProcess = ProcessBuilder(bepassCommand)
                    .directory(applicationContext.filesDir)
                    .redirectErrorStream(true)
                    .start()
                socksProcess!!.waitFor()
            }.start()
        } else if (Settings.useXray) {
            copyAssets(applicationContext)
            val out =   File("/storage/emulated/0/Android/data/com.xtls.xray/files/out.txt");
            val err =   File("/storage/emulated/0/Android/data/com.xtls.xray/files/err.txt");

=======
=======
>>>>>>> d746191b66978735e2483b68cbce72fd40a35cc6
        if (Settings.useXray) {
            /** Start xray */
>>>>>>> d746191b66978735e2483b68cbce72fd40a35cc6
            Thread {

                    val content = File("/storage/emulated/0/Android/data/com.xtls.xray/files/config.json").readText()
//                    Log.e("test",content)
                    Log.e("test", "xrayPath " + xrayPath())
                    Os.setenv("xray.location.asset", applicationContext.filesDir.absolutePath, true)

                    val xrayCommand = arrayListOf(
                        xrayPath(), "run", "-c", "/storage/emulated/0/Android/data/com.xtls.xray/files/config.json"
                    )
                    socksProcess = ProcessBuilder(xrayCommand)
                        .directory(applicationContext.filesDir)
                        .redirectOutput(out)
                        .redirectError(err)
                        .redirectErrorStream(true)
                        .start()

                     socksProcess!!.waitFor()
                     Log.e("test","xray process exited")
                }.start()
        }

        /** Create Tun */
        val tun = Builder()

        /** Basic tun config */
        tun.setMetered(false)
        tun.setMtu(1500)
        tun.setSession("tun0")
<<<<<<< HEAD
<<<<<<< HEAD
//        tun.addAddress("10.10.10.10", 24)
        tun.addAddress("26.26.26.1", 30)
        if (Settings.useBepass) {
            tun.setHttpProxy(ProxyInfo.buildDirectProxy(Settings.socksAddress, Settings.socksPort.toInt()))
        } else {
//            Log.e("test", Settings.primaryDns);
            tun.addDnsServer(Settings.primaryDns)
            tun.addDnsServer(Settings.secondaryDns)
//              tun.addDnsServer("26.26.26.2")
        }
=======
        tun.addAddress("10.10.10.10", 24)
        tun.addDnsServer(Settings.primaryDns)
        tun.addDnsServer(Settings.secondaryDns)
>>>>>>> d746191b66978735e2483b68cbce72fd40a35cc6
=======
        tun.addAddress("10.10.10.10", 24)
        tun.addDnsServer(Settings.primaryDns)
        tun.addDnsServer(Settings.secondaryDns)
>>>>>>> d746191b66978735e2483b68cbce72fd40a35cc6

        /** Pass all traffic to the tun (Except private IP addresses) */
        resources.getStringArray(R.array.publicIpAddresses).forEach {
            val address = it.split('/')
            tun.addRoute(address[0], address[1].toInt())
        }


        /** Exclude the app itself */
        tun.addDisallowedApplication(BuildConfig.APPLICATION_ID)
//        tun.addAllowedApplication(BuildConfig.APPLICATION_ID)
        Log.e("test",applicationContext.packageName.toString())
        Log.e("test",BuildConfig.APPLICATION_ID)
        /** Build tun device */

        try {
            tunDevice = tun.establish()!!
//            runTun2socks()
        } catch (e: Exception) {
            // non-nullable lateinit var
            e.printStackTrace()
        }

        /** Start tun2socks */
        tun2socksExecutor = newFixedThreadPool(1)
        tun2socksExecutor!!.submit {
            val tun2socks = engine.Key()
            tun2socks.mark = 0L
            tun2socks.mtu = 1500L
            tun2socks.`interface` = ""
            tun2socks.logLevel = "info"
            tun2socks.restAPI = ""
            tun2socks.tcpSendBufferSize = ""
            tun2socks.tcpReceiveBufferSize = ""
            tun2socks.tcpModerateReceiveBuffer = false
            tun2socks.device = "fd://${tunDevice!!.fd}"
            tun2socks.proxy = "socks5://192.168.1.7:10808"
            engine.Engine.insert(tun2socks)
            engine.Engine.start()
        }
    }
    private fun runTun2socks() {
        val socksPort = 10808

        val cmd = arrayListOf(File(applicationContext.applicationInfo.nativeLibraryDir, "libtun2socks.so").absolutePath,
            "--netif-ipaddr", "26.26.26.2",
            "--netif-netmask", "255.255.255.252",
            "--socks-server-addr", "127.0.0.1:10808",
            "--tunmtu", "1500",
            "--sock-path", "sock_path",
            "--enable-udprelay",
            "--loglevel", "notice")

//        if (settingsStorage?.decodeBool(AppConfig.PREF_PREFER_IPV6) == true) {
//            cmd.add("--netif-ip6addr")
//            cmd.add(PRIVATE_VLAN6_ROUTER)
//        }
//        if (settingsStorage?.decodeBool(AppConfig.PREF_LOCAL_DNS_ENABLED) == true) {
//            val localDnsPort = Utils.parseInt(settingsStorage?.decodeString(AppConfig.PREF_LOCAL_DNS_PORT), AppConfig.PORT_LOCAL_DNS.toInt())
//            cmd.add("--dnsgw")
//            cmd.add("127.0.0.1:${localDnsPort}")
//        }
        Log.e("test",  cmd.toString())
        val out2 =   File("/storage/emulated/0/Android/data/com.xtls.xray/files/out2.txt");
        val err2 =   File("/storage/emulated/0/Android/data/com.xtls.xray/files/err2.txt");

        try {
            if(!applicationContext.filesDir.isDirectory)
                applicationContext.filesDir.mkdirs()
            val proBuilder = ProcessBuilder(cmd)
            proBuilder.redirectErrorStream(true)
            tunprocess = proBuilder
                .directory(applicationContext.filesDir)
                .redirectOutput(out2)
                .redirectError(err2)
                .redirectErrorStream(true)
                .start()
            Thread(Runnable {
                Log.e("test","TUN2SOCKS check")
                tunprocess.waitFor()
                Log.e("test","TUN2SOCKS exited")
                if (isRunning) {
                    Log.e("test","TUN2SOCKS restart")
                    runTun2socks()
                }
            }).start()
            Log.e("test", tunprocess.toString())

            sendFd()
        } catch (e: Exception) {
            Log.e("test",  e.toString())
        }

    }
    private fun sendFd() {
        val fd = tunDevice.fileDescriptor
        val path = File(applicationContext.filesDir, "sock_path").absolutePath
        Log.e("test", path)

        GlobalScope.launch(Dispatchers.IO) {
            var tries = 0
            while (true) try {
                Thread.sleep(50L shl tries)
                Log.e("test", "sendFd tries: $tries")
                LocalSocket().use { localSocket ->
                    localSocket.connect(LocalSocketAddress(path, LocalSocketAddress.Namespace.FILESYSTEM))
                    localSocket.setFileDescriptorsForSend(arrayOf(fd))
                    localSocket.outputStream.write(42)
                }
                break
            } catch (e: Exception) {
                Log.e("test", e.toString())
                if (tries > 5) break
                tries += 1
            }
        }
    }
    fun stopVPN() {
        isRunning = false
        if (socksProcess != null) {
            if (socksProcess!!.isAlive) socksProcess!!.destroy()
            socksProcess = null
        }
        if (tun2socksExecutor != null) {
            tun2socksExecutor!!.shutdown()
            tun2socksExecutor!!.shutdownNow()
            tun2socksExecutor = null
        }

        try {
            tunDevice.close()
//            tunprocess.destroy()
        } catch (ignored: Exception) {
            // ignored
        }

//        if (tunDevice != null) {
//            tunDevice!!.close()
//            tunDevice = null
//        }
        stopSelf()
    }

}
