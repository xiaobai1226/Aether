package com.xiaobai1226.aether.core.db;

import com.baomidou.mybatisplus.solon.ddl.DdlHelper;
import com.baomidou.mybatisplus.solon.ddl.history.MysqlDdlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;

import javax.sql.DataSource;
import java.util.Arrays;

/**
 * 初始化数据库
 */
@Component
@Slf4j
public class DBInitialize {

    @Inject("db1")
    private DataSource db1;

    @Init
    public void init() throws Throwable {
        initDBStructure();
//        initDBData();
    }

    /**
     * 初始化数据库结构
     */
    private void initDBStructure() throws Exception {
        var sqls = Arrays.asList("classpath:sql/schema.sql", "classpath:sql/data.sql");

        DdlHelper.runScript(new MysqlDdlGenerator(), db1, sqls, true);
    }
}