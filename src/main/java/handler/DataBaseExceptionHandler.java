package handler;

import com.minihttp.core.response.JsonResponse;
import com.minihttp.core.route.exception.ExceptionHandler;
import com.minihttp.packet.HttpRequest;
import com.minihttp.packet.HttpResponse;
import com.minihttp.packet.protocol.HttpStatus;
import exception.DatabaseException;

public class DataBaseExceptionHandler implements ExceptionHandler<DatabaseException> {
        private static final HttpStatus DATABASE_EXCEPTION_STATE_CODE = HttpStatus.INTERNAL_SERVER_ERROR;
    @Override
    public HttpResponse handle(HttpRequest request, DatabaseException exception) throws Exception {
        return new JsonResponse(DATABASE_EXCEPTION_STATE_CODE, exception.getMessage());
    }
}

