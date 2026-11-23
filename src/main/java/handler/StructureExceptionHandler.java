package handler;

import com.minihttp.core.response.JsonResponse;
import com.minihttp.core.route.exception.ExceptionHandler;
import com.minihttp.packet.HttpRequest;
import com.minihttp.packet.HttpResponse;
import com.minihttp.packet.protocol.HttpStatus;

import exception.StructureException;


public class StructureExceptionHandler implements ExceptionHandler<StructureException> {
    private static final HttpStatus STRUCTURE_EXCEPTION_STATE_CODE = HttpStatus.INTERNAL_SERVER_ERROR;

    @Override
    public HttpResponse handle(HttpRequest request, StructureException exception) throws Exception {
        return new JsonResponse(STRUCTURE_EXCEPTION_STATE_CODE, exception.getMessage());
    }
}
