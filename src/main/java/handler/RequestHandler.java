package handler;

import annotation.Transaction;
import exception.AnnotationMissingException;
import exception.NotFoundException;
import model.dto.request.Request;
import model.dto.response.Response;
import repository.manager.transaction.TransactionTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 요청을 트랜잭션 이름으로 서비스 메서드에 연결하는 공통 처리 지점
 * 요청 하나 = 트랜잭션 하나. 서비스는 트랜잭션 코드를 작성하지 않는다.
 * 정상 종료 시 commit, 예외 발생 시 rollback 후 예외를 그대로 던진다 (응답 변환은 호출하는 쪽에서).
 *
 * 입력 방식(콘솔, 소켓, GUI 등)과 무관하게 handle(name, request)만 호출하면 된다.
 */
public class RequestHandler {
    private final TransactionTemplate transactionTemplate;
    private final Map<String, Route> routes = new HashMap<>();

    //등록된 요청 하나. readOnly면 읽기 전용 트랜잭션으로 실행
    private record Route(Function<Request, Response> action, boolean readOnly) {
    }

    public RequestHandler(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    //서비스의 @Transaction 메서드를 전부 등록. 규칙을 어긴 메서드가 있으면 시작 시 예외
    public RequestHandler registerAll(Object... services) {
        for (Object service : services) {
            for (Method method : service.getClass().getMethods()) {
                Transaction transaction = method.getAnnotation(Transaction.class);
                if (transaction != null) {
                    registerMethod(transaction.name(), transaction.readOnly(), service, method);
                }
            }
        }
        return this;
    }

    //요청 직접 등록. 같은 Request 타입을 여러 기능이 쓰기 때문에 요청 이름으로 구분한다
    public <Q extends Request> RequestHandler register(String name, Class<Q> requestType,
                                                      Function<Q, ? extends Response> action) {
        return register(name, false, requestType, action);
    }

    public <Q extends Request> RequestHandler register(String name, boolean readOnly, Class<Q> requestType,
                                                      Function<Q, ? extends Response> action) {
        if (routes.containsKey(name)) {
            throw new IllegalStateException("Transaction already registered: " + name);
        }
        routes.put(name, new Route(request -> {
            if (!requestType.isInstance(request)) {
                throw new IllegalArgumentException(
                        name + " expects " + requestType.getSimpleName()
                                + " but got " + (request == null ? "null" : request.getClass().getSimpleName()));
            }
            return action.apply(requestType.cast(request));
        }, readOnly));
        return this;
    }

    public Response handle(String name, Request request) {
        Route route = routes.get(name);
        if (route == null) {
            throw new NotFoundException("Unknown transaction: " + name);
        }
        return transactionTemplate.execute(route.readOnly(), () -> route.action().apply(request));
    }

    @SuppressWarnings("unchecked")
    private void registerMethod(String name, boolean readOnly, Object service, Method method) {
        String where = service.getClass().getSimpleName() + "." + method.getName();
        Class<?>[] params = method.getParameterTypes();
        if (Modifier.isStatic(method.getModifiers())) {
            throw new AnnotationMissingException("@Transaction method must not be static: " + where);
        }
        if (params.length != 1 || !Request.class.isAssignableFrom(params[0])) {
            throw new AnnotationMissingException("@Transaction method must take exactly one Request parameter: " + where);
        }
        if (!Response.class.isAssignableFrom(method.getReturnType())) {
            throw new AnnotationMissingException("@Transaction method must return a Response: " + where);
        }
        register(name, readOnly, (Class<Request>) params[0], request -> invoke(service, method, request));
    }

    //서비스에서 던진 예외는 감싸지 않고 그대로 전달 (롤백 후 호출한 쪽에서 처리)
    private static Response invoke(Object service, Method method, Request request) {
        try {
            return (Response) method.invoke(service, request);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException(cause);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot call " + method, e);
        }
    }
}
