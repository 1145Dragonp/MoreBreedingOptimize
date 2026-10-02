package mrmd.morebreedingoptimize.boosfight.dor;

import com.sun.net.httpserver.HttpServer;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AdServer {

    private static final int PORT = 2578;
    private static AdServer instance;

    private HttpServer server;
    private Path tempHtmlPath;
    private final AtomicInteger watched = new AtomicInteger(0);
    private final AtomicBoolean done = new AtomicBoolean(false);

    private AdServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/watch", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.startsWith("t=")) {
                int t = Integer.parseInt(query.substring(2));
                watched.updateAndGet(max -> Math.max(max, t));
            }
            String res = "{\"ok\":true,\"watched\":" + watched.get() + "}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, res.getBytes().length);
            exchange.getResponseBody().write(res.getBytes());
            exchange.getResponseBody().close();
        });

        server.createContext("/done", exchange -> {
            done.set(true);
            String res = "{\"ok\":true,\"done\":true}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, res.getBytes().length);
            exchange.getResponseBody().write(res.getBytes());
            exchange.getResponseBody().close();
            cleanupTempFile();
        });
    }

    /**
     * 创建临时HTML文件，返回其Path
     */
    public Path createTempHtml(String htmlContent) throws IOException {
        Path tempDir = Files.createTempDirectory("ad_server_");
        tempDir.toFile().deleteOnExit();
        tempHtmlPath = tempDir.resolve("ad.html");
        try (FileOutputStream fos = new FileOutputStream(tempHtmlPath.toFile())) {
            fos.write(htmlContent.getBytes("UTF-8"));
        }
        return tempHtmlPath;
    }

    /* ============ 以下为静态方法，供 KpScreen 直接调用 ============ */

    public static synchronized void init() throws IOException {
        if (instance == null) {
            instance = new AdServer();
        }
    }

    public static void start() {
        if (instance == null) return;
        instance.server.setExecutor(null);
        instance.server.start();
        System.out.println("AdServer started on port " + PORT);
    }

    public static void stop() {
        if (instance == null) return;
        instance.server.stop(0);
        System.out.println("AdServer stopped on port " + PORT);
        cleanupTempFile();
    }

    public static void reset() {
        if (instance == null) return;
        instance.watched.set(0);
        instance.done.set(false);
    }

    public static int getWatched() {
        if (instance == null) return 0;
        return instance.watched.get();
    }

    public static boolean isDone() {
        if (instance == null) return false;
        return instance.done.get();
    }

    private static void cleanupTempFile() {
        if (instance != null && instance.tempHtmlPath != null) {
            try {
                Files.deleteIfExists(instance.tempHtmlPath);
                Path parent = instance.tempHtmlPath.getParent();
                if (parent != null) {
                    Files.deleteIfExists(parent);
                }
                System.out.println("Temp file cleaned: " + instance.tempHtmlPath);
                instance.tempHtmlPath = null;
            } catch (IOException e) {
                System.err.println("Failed to cleanup temp file: " + e.getMessage());
            }
        }
    }
}