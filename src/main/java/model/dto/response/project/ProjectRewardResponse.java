package model.dto.response.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;
import model.dto.response.reward.RewardUserResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * 프로젝트의 리워드 목록과, 리워드별 후원자 배송 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRewardResponse extends Response {
    //후원 1건 = 배송 1건
    private ProjectInfoResponse projectInfo;
    private List<RewardUserResponse> rewards;
}
