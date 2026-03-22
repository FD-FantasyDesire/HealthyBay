package com.fancyframeflare.backend.controller;

import com.fancyframeflare.backend.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api/system/backup")
@CrossOrigin
@PreAuthorize("hasAuthority('system:setting')") // 使用系统设置权限
public class SystemBackupController {

    @Value("${spring.datasource.username:root}")
    private String dbUser;

    @Value("${spring.datasource.password:123456}")
    private String dbPassword;

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/healthy_bay?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai}")
    private String dbUrl;

    @PostMapping
    @com.fancyframeflare.backend.annotation.LogOperation("执行数据库备份")
    public Result<String> backup() {
        try {
            // 解析数据库名称
            String dbName = "healthy_bay"; // 默认或者从url解析
            if (dbUrl != null && dbUrl.contains("/")) {
                int questionMarkIndex = dbUrl.indexOf("?");
                int lastSlashIndex = dbUrl.lastIndexOf("/");
                if (lastSlashIndex != -1 && lastSlashIndex < dbUrl.length() - 1) {
                    if (questionMarkIndex != -1 && questionMarkIndex > lastSlashIndex) {
                        dbName = dbUrl.substring(lastSlashIndex + 1, questionMarkIndex);
                    } else if (questionMarkIndex == -1) {
                        dbName = dbUrl.substring(lastSlashIndex + 1);
                    }
                }
            }

            // 备份目录
            String backupDirPath = System.getProperty("user.dir") + File.separator + "backup";
            File backupDir = new File(backupDirPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String fileName = dbName + "_" + timeStamp + ".sql";
            String filePath = backupDirPath + File.separator + fileName;

            // 构建mysqldump命令
            // mysqldump -u username -ppassword dbname > filepath
            String command = String.format("mysqldump -u%s -p%s %s -r %s",
                    dbUser, dbPassword, dbName, filePath);

            // 注意：在Windows下执行可能需要通过cmd /c，Linux下通过/bin/sh -c
            Process process;
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                process = Runtime.getRuntime().exec(new String[] { "cmd", "/c", command });
            } else {
                process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", command });
            }

            int processComplete = process.waitFor();
            if (processComplete == 0) {
                return Result.success("备份成功，文件保存在: " + filePath);
            } else {
                return Result.error("备份失败，请检查mysqldump是否在系统环境变量中");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("备份发生异常: " + e.getMessage());
        }
    }
}
