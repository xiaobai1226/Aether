package com.xiaobai1226.aether.core.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.solon.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.solon.plugins.inner.PaginationInnerInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import javax.sql.DataSource;

/**
 * mybatis plus配置类
 *
 * @author bai
 */
@Configuration
public class MybatisPlusConfig {

    //此下的 db1 与 mybatis.db1 将对应在起来 //可以用 @Db("db1") 注入mapper
    //typed=true，表示默认数据源。@Db 可不带名字注入
    @Bean(value = "db1", typed = true)
    public DataSource db1(@Inject("${database.db1}") HikariDataSource ds) {
        return ds;
    }

    //调整 db1 的配置（如：增加插件）
    @Bean
    public void db1_cfg(@Db("db1") MybatisConfiguration cfg, @Db("db1") GlobalConfig globalConfig) {
        MybatisPlusInterceptor plusInterceptor = new MybatisPlusInterceptor();
        plusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        cfg.addInterceptor(plusInterceptor);
    }
}
