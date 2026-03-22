package com.fancyframeflare.backend.aspect;

import com.fancyframeflare.backend.annotation.LogOperation;
import com.fancyframeflare.backend.entity.OperationLog;
import com.fancyframeflare.backend.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Date;

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Around("execution(public * com.fancyframeflare.backend.controller.*.*(..)) && !execution(public * com.fancyframeflare.backend.controller.OperationLogController.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long beginTime = System.currentTimeMillis();
        OperationLog operationLog = new OperationLog();

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // 不记录查询类操作
                if ("GET".equalsIgnoreCase(request.getMethod())) {
                    return joinPoint.proceed();
                }

                operationLog.setRequestIp(request.getRemoteAddr());

                Long userId = (Long) request.getAttribute("userId");
                operationLog.setUserId(userId);
            }

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogOperation logAnnotation = method.getAnnotation(LogOperation.class);

            if (logAnnotation != null) {
                operationLog.setOperation(logAnnotation.value());
            }

            String className = joinPoint.getTarget().getClass().getName();
            String methodName = signature.getName();
            operationLog.setMethod(className + "." + methodName + "()");

            // Execute the actual method
            Object result = joinPoint.proceed();

            operationLog.setStatus(1); // Success
            return result;
        } catch (Throwable e) {
            operationLog.setStatus(0); // Failed
            operationLog.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            operationLog.setCreateTime(new Date());
            operationLog.setUpdateTime(new Date());
            operationLog.setDelFlag(0);
            operationLogService.save(operationLog);
        }
    }
}
