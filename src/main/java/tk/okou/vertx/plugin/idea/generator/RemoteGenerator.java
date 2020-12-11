package tk.okou.vertx.plugin.idea.generator;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.OpenOptions;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

import java.io.*;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RemoteGenerator implements Generator{
    WebClient client;
    Vertx vertx;
    public RemoteGenerator(Vertx vertx, WebClient client) {
        this.client = client;
        this.vertx = vertx;
    }
    @Override
    public void generate(String path, String groupId, String artifactId, String buildTool, String language, String vertxVersion, String jdkVersion) {
        String url = String.format(
                "https://start.vertx.io/starter.zip?artifactId=%s&buildTool=%s&groupId=%s&jdkVersion=%s&language=%s&packageName=&vertxDependencies=&vertxVersion=%s",
                artifactId, buildTool.toLowerCase(), groupId, jdkVersion, language.toLowerCase(), vertxVersion
        );
        HttpRequest<Buffer> request = client.getAbs(url);

        OpenOptions options = new OpenOptions();
        options.setCreate(true);
        options.setWrite(true);
        String filePath = path + File.separator + ".idea" + File.separator + "starter.zip";
        try {
            vertx
                    .fileSystem()
                    .open(filePath, options)
                    .compose(af -> request.timeout(60000).as(BodyCodec.pipe(af)).send())
                    .toCompletionStage()
                    .toCompletableFuture()
                    .get();

            File file = new File(filePath);
            unZip(file, path);
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("下载失败");
        }
    }
    /**
     * 解压zip文件
     *
     * @param zipFile 目标文件
     * @param descDir 解压后存放的位置
     * @return true/false
     */
    public static boolean unZip(File zipFile, String descDir) {
//        System.out.println("解压" + zipFile.getAbsolutePath() + "到" + descDir);
        boolean flag = false;
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        ZipFile zip = null;
        try {
            // 指定编码，否则压缩包里面不能有中文目录
            zip = new ZipFile(zipFile);
            for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
                ZipEntry entry = entries.nextElement();
                String zipEntryName = entry.getName();
                InputStream in = zip.getInputStream(entry);
//                System.out.println("解压" + zipEntryName);
                String zipEntryPath = zipEntryName.replace("/", File.separator);
                int idx = zipEntryPath.lastIndexOf(File.separator);
                if (idx != -1) {
                    File file = new File(pathFile, zipEntryPath.substring(0, idx));
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                }
                File outputFile = new File(pathFile, zipEntryPath);
                // 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                if (outputFile.isDirectory()) {
                    continue;
                }

                OutputStream out = new FileOutputStream(outputFile);
                byte[] buf1 = new byte[2048];
                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }
                in.close();
                out.close();
            }
            flag = true;
            // 必须关闭，否则无法删除该zip文件
            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }
}
