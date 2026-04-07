package com.distributed.sw3.config;

import com.distributed.sw3.config.DataSourceConfig.DataSourceContextHolder;
import com.distributed.sw3.config.DataSourceConfig.DataSourceType;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DataSourceAspect {

    @Pointcut("execution(* com.distributed.sw3.service.*.get*(..)) || execution(* com.distributed.sw3.service.*.find*(..)) || execution(* com.distributed.sw3.service.*.query*(..))")
    public void readPointcut() {}

    @Pointcut("execution(* com.distributed.sw3.service.*.save*(..)) || execution(* com.distributed.sw3.service.*.update*(..)) || execution(* com.distributed.sw3.service.*.delete*(..))")
    public void writePointcut() {}

    @Before("readPointcut()")
    public void setReadDataSource() {
        DataSourceContextHolder.setDataSourceType(DataSourceType.SLAVE);
    }

    @Before("writePointcut()")
    public void setWriteDataSource() {
        DataSourceContextHolder.setDataSourceType(DataSourceType.MASTER);
    }
}