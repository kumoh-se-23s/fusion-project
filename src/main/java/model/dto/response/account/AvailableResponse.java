package model.dto.response.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailableResponse extends Response {
    private boolean available;
}
