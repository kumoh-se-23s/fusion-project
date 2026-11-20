
package controller.internal;

import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;

import model.dto.RewardData;
import model.dto.response.DataCollectionResponse;

import service.external.RewardService;


//  관리자 전용 리워드 라우터

public class RewardRouter extends BaseRouter {

    private final RewardService rewardService;

    public RewardRouter(RewardService rewardService) {
        super("/internal/reward");
        this.rewardService = rewardService;
    }


     // 관리자 전체 리워드 조회

    @Get("/fetch-all")
    public DataCollectionResponse<RewardData> getAllRewards() {

        return rewardService.getAllRewards();
    }
}
