package model.dto.response.donate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.BaseData;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class PkResponse extends BaseData{
    private Long userFk;

    private Long rewardFk;

    private BigDecimal amount;

    private Long quantity;

    private Boolean state;
}
