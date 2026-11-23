package handler;

import com.minihttp.core.response.JsonResponse;
import com.minihttp.core.route.exception.ExceptionHandler;
import com.minihttp.packet.HttpRequest;
import com.minihttp.packet.HttpResponse;
import exception.BusinessException;

public class BusinessExceptionHandler implements ExceptionHandler<BusinessException> {
    @Override
    public HttpResponse handle(HttpRequest request, BusinessException exception) throws Exception {
        return new JsonResponse(exception.status(), exception.getMessage());
    }
}
