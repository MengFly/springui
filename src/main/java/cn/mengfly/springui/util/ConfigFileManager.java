package cn.mengfly.springui.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Mengfly
 */
public class ConfigFileManager implements Closeable {
    private static final Log log = LogFactory.getLog(ConfigFileManager.class);

    /**
     * 自动刷新线程,用于定时将数据刷新到文件中
     */
    private ScheduledExecutorService flushThreadPool;
    private static final int DEFAULT_FLUSH_SCHEDULE = 60;

    private final Properties prop;
    private final String itemConfigFile;

    public ConfigFileManager(String fileName) {
        this(fileName, false, -1);
    }

    public ConfigFileManager(File file) {
        this(file.getAbsolutePath());
    }


    public ConfigFileManager(String fileName, boolean defaultLoadFromResource, int refreshSchedule) {
        this.itemConfigFile = fileName;
        prop = new Properties();
        InputStreamReader reader = null;
        try {
            File configFile = new File(itemConfigFile);
            if (configFile.exists()) {
                log.info(">>>>>>>>>>>>>>>>" + configFile.getAbsolutePath());
                reader = new InputStreamReader(configFile.toURI().toURL().openStream(), StandardCharsets.UTF_8);
                prop.load(reader);
                loadMissingCfg(configFile);
            } else if (defaultLoadFromResource) {
                log.info(">>>>>>>>>>>>>>>> external config file not exists,load default");
                String name = configFile.getName();
                reader = new InputStreamReader(ResourceLoader.loadResource(name).openStream(), StandardCharsets.UTF_8);
                prop.load(reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(reader);
        }
        if (refreshSchedule <= 0) {
            log.info("disable schedule flush to data file");
        } else {
            if (refreshSchedule < DEFAULT_FLUSH_SCHEDULE) {
                log.warn("refresh schedule less than " + DEFAULT_FLUSH_SCHEDULE + "may be busy to flush file");
            }
            flushThreadPool = Executors.newSingleThreadScheduledExecutor();
            // 启动自动刷新任务
            flushThreadPool.scheduleWithFixedDelay(this::flushConfigToFile,
                    0, refreshSchedule, TimeUnit.SECONDS);
        }

    }

    public static void close(ConfigFileManager... managers) {
        if (managers == null) {
            return;
        }
        for (ConfigFileManager manager : managers) {
            if (manager != null) {
                manager.close();
            }
        }
    }

    private void loadMissingCfg(File configFile) throws IOException {
        // load Resource
        URL url = ResourceLoader.loadResource(configFile.getName());
        if (url != null) {
            InputStream in = url.openStream();
            try (InputStreamReader resourceReader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                Properties propResource = new Properties();
                propResource.load(resourceReader);
                Set<Object> objects = propResource.keySet();
                for (Object object : objects) {
                    if (!prop.containsKey(object)) {
                        prop.put(object, propResource.get(object));
                    }
                }
            }
        }
    }

    public synchronized String getItem(String key, String defaultValue) {
        return getItem(key, String::toString, defaultValue);
    }

    public synchronized String getItem(String key) {
        return getItem(key, String::toString, null);
    }

    public synchronized void removeItem(String key) {
        prop.remove(key);
    }

    public synchronized <T> T getItem(String key, Function<String, T> fun, T defaultVal) {
        if (prop.containsKey(key)) {
            Object o = prop.get(key);
            try {
                return fun.apply(String.valueOf(o));
            } catch (Exception e) {
                return defaultVal;
            }
        } else {
            return defaultVal;
        }
    }

    public synchronized <T> void putItem(String k, T v) {
        prop.put(k, String.valueOf(v));
    }

    private synchronized void flushConfigToFile() {
        // todo 备份冗余技术，写入文件的时候先写入到一个back文件里面，如果写入没有出错，将原来的文件重命名为 filename-date
        // 将新文件命名为 文件名
        FileOutputStream outputStream = null;
        OutputStreamWriter writer = null;
        try {
            outputStream = new FileOutputStream(itemConfigFile, false);
            writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            prop.store(writer, null);
        } catch (Exception e) {
            log.error(String.format("config file %s save error", itemConfigFile), e);
        } finally {
            closeStream(outputStream, writer);
        }
    }

    @Override
    public void close() {
        flushConfigToFile();
        if (flushThreadPool != null) {
            flushThreadPool.shutdown();
        }
    }

    private void closeStream(Closeable... closeable) {
        for (Closeable c : closeable) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public Set<String> keySet() {
        return prop.keySet().stream().map(String::valueOf).collect(Collectors.toSet());
    }
}
