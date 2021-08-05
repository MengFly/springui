package io.github.mengfly.springui.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 只允许程序启动一个实例
 *
 * @author Mengfly
 * @date 2021/8/4 9:35
 */
public class SingletonStartLock {
    private final File identifierFile = new File("spring-ui-identifier");
    private static final Log log = LogFactory.getLog(SingletonStartLock.class);

    private final File appDirectory;
    private final boolean initialized;

    public SingletonStartLock() {
        String userHome = System.getProperty("user.home");
        if (StringUtil.isNullOrEmpty(userHome)) {
            userHome = "./";
        }
        appDirectory = new File(userHome, ".Spring-Ui");
        if (appDirectory.exists() && appDirectory.isDirectory()) {
            initialized = true;
        } else {
            initialized = appDirectory.mkdirs();
        }
    }

    public boolean tryLock() {
        if (!initialized) {
            log.error("Fail to create Application directory, Can't use singleton start !!!");
            return true;
        }
        String identifier = getApplicationIdentifier();
        if (identifier == null) {
            log.error("try get identifier fail, Can't use singleton start !!!");
            return true;
        }
        log.info("check for identifier " + identifier);
        File lockFile = new File(appDirectory, String.format("%s.lockfile", identifier));

        if (!checkLockFile(lockFile)) {
            log.info(String.format("can't create lockfile for : %s,  Can't use singleton start !!!", lockFile.getAbsolutePath()));
            return true;
        }
        // try lock
        return tryLockFile(lockFile);
    }

    private boolean checkLockFile(File lockFile) {
        if (lockFile.exists() && lockFile.isFile()) {
            return true;
        }
        try {
            Files.createFile(lockFile.toPath());
            return true;
        } catch (IOException e) {
            log.error("create lock file error", e);
            return false;
        }
    }

    private boolean tryLockFile(File lockFile) {
        // lockfile
        try {
            final RandomAccessFile accessFile = new RandomAccessFile(lockFile.getAbsolutePath(), "rws");
            FileLock fileLock = accessFile.getChannel().tryLock();
            return fileLock != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }


    private String getApplicationIdentifier() {
        // load exists identifier
        String identifier = loadIdentifier();

        // if identifier not exists
        if (identifier == null) {
            identifier = UUID.randomUUID().toString();
            if (!saveIdentifier(identifier)) {
                return null;
            }
        }
        return identifier;
    }

    private boolean saveIdentifier(String identifier) {
        try {
            // write identifier
            Files.write(identifierFile.toPath(), Collections.singleton(identifier), StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            return true;
        } catch (Exception e) {
            log.error("save identifier fail ", e);
        }
        return false;
    }

    private String loadIdentifier() {
        try {
            if (Files.isReadable(identifierFile.toPath())) {
                List<String> contentList = Files.readAllLines(identifierFile.toPath(), StandardCharsets.UTF_8);
                if (!contentList.isEmpty()) {
                    String identifier = contentList.get(0);
                    log.info("load application identifier : " + identifier);
                    return identifier;
                }
            }
        } catch (Exception e) {
            log.error("load identifier file fail", e);
            return null;
        }
        return null;

    }


}
