package controller.external;

import auth.CurrentUser;
import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;
import model.dto.RewardData;
import model.dto.request.project.CreateProjectRequest;
import model.dto.request.reward.CreateRewardDataRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.IdResponse;
import service.external.RewardService;

public class RewardRouter extends BaseRouter {
    private final RewardService rewardService;

    public RewardRouter(RewardService rewardService){
        this.rewardService = rewardService;
        super("/reward");
    }

    @Post()
    public IdResponse createProject(CreateRewardDataRequest request) {
        return rewardService.createReward(request);

    }

    @Get("/{project_id}")
    public DataCollectionResponse<RewardData> getAllRewardsByProjectId(@Path("projecdt_id") Long projectId){
        return rewardService.findRewardByProject(projectId);
    }

    @Get("/{reward_id}")
    public DataCollectionResponse<RewardData> getAllRewardById(@Path("reward_id") Long rewardId){
        return rewardService.getRewardById(rewardId);
    }

    @Get("/{maker_id}")
    public DataCollectionResponser<RewardData> getAllRewardByMakerId(@Path("maker_id")Long makerId){
        return rewardService.getRewardFindByMaker(makerId);
    }

    @Patch(){
    public IdResponse updateProject(CreateRewardDataRequest request){
        return rewardService.updateReward(@Depends(CurrentUser.class) Long userId,);

        }
    }
}
