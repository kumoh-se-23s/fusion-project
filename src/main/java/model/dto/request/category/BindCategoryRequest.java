package model.dto.request.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BindCategoryRequest extends Request {

    private Long fundingPKs;

    private List<Long> categoryPKs;
}
