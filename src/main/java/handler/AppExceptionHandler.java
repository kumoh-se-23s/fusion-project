package handler;

import com.minihttp.core.response.JsonResponse;
import com.minihttp.core.route.exception.ExceptionHandler;
import com.minihttp.packet.HttpRequest;
import com.minihttp.packet.HttpResponse;
import com.minihttp.packet.protocol.HttpStatus;
import exception.AppException;
import exception.BusinessException;

public class AppExceptionHandler implements ExceptionHandler<AppException> {
    private static final HttpStatus APP_EXCEPTION_STATE_CODE = HttpStatus.INTERNAL_SERVER_ERROR;
    @Override
    public HttpResponse handle(HttpRequest request, AppException exception) throws Exception {
        return new JsonResponse(APP_EXCEPTION_STATE_CODE, exception.getMessage());
    }
}
