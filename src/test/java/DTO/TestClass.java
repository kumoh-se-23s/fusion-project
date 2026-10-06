package DTO;

import java.util.List;

public class TestClass {
    public static abstract class Request {};
    public static abstract class Response {

    }

    public static class DataCollectionResponse<DataObject> extends Response {
        public int amount = 0;
        public List<DataObject> datas = List.of();
    }

    public static class PKResponse extends Response {
        public Long pk = 0L;
    }

    public static class DeleteResponse extends Response {
        public Boolean result = false;
    }
}
