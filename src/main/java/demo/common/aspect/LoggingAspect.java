package demo.common.aspect;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 各レイヤー（Controller / Command / Task / Mapper）の開始・終了ログを出力する。
 * MDC（trackingId / userId / clientIp）は MdcFilter がセットするため、
 * ログパターンと合わせて 1 リクエストの処理を串刺しで追跡できる。
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* demo..controller.*Controller.*(..))"
            + " || execution(* demo..command.*Command.*(..))"
            + " || execution(* demo..task.*Task.*(..))"
            + " || execution(* demo..mapper.*Mapper.*(..))")
    public void layerMethods() {
    }

    @Around("layerMethods()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        String cls = pjp.getSignature().getDeclaringType().getSimpleName();
        String method = pjp.getSignature().getName();
        log.debug("[START] {}.{} args={}", cls, method, Arrays.toString(pjp.getArgs()));
        try {
            Object result = pjp.proceed();
            log.debug("[END]   {}.{}", cls, method);
            return result;
        } catch (Throwable t) {
            log.debug("[END]   {}.{} exception={}", cls, method, t.toString());
            throw t;
        }
    }
}
