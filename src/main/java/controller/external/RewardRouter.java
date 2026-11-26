
package controller.external;

import auth.CurrentUser;
import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;
import com.minihttp.packet.HttpResponse;
import com.minihttp.packet.protocol.ContentType;
import com.minihttp.packet.protocol.HttpHeaderField;
import com.minihttp.packet.protocol.HttpStatus;
import com.minihttp.packet.protocol.HttpVersion;
import com.minihttp.packet.structure.DefaultHttpBody;
import com.minihttp.packet.structure.DefaultHttpHeader;
import com.minihttp.packet.structure.HttpBody;
import com.minihttp.packet.structure.HttpHeader;
import model.dto.RewardData;
import model.dto.request.reward.CreateRewardDataRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.SingleDataResponse;
import model.dto.response.IdResponse;

import service.external.RewardService;


 // 외부용 리워드 라우터

public class RewardRouter extends BaseRouter {

    private final RewardService rewardService;

    public RewardRouter(RewardService rewardService) {
        super("/reward");
        this.rewardService = rewardService;
    }


    //  리워드 생성

    @Post("/projectid")
    public IdResponse createReward(
            @Depends(CurrentUser.class) Long userId,
            CreateRewardDataRequest dto) {

        return rewardService.createReward(userId, dto);
    }


     // 리워드 ID로 조회

    @Get("/{reward_id}")
    public SingleDataResponse<RewardData> getRewardById(
            @Path("reward_id") Long rewardId) {

        return rewardService.getRewardById(rewardId);
    }


    //  프로젝트 ID로 리워드 조회

    @Get("/project/{project_id}")
    public DataCollectionResponse<RewardData> getRewardsByProject(
            @Path("project_id") Long projectId) {

        return rewardService.findRewardByProject(projectId);
    }


    //  사용자가 후원한 리워드 조회

    @Get("/user_id/{user_id}")
    public DataCollectionResponse<RewardData> getRewardByDonate(
            @Depends(CurrentUser.class) Long loginUserId,
            @Path("user_id") Long userId) {

        return rewardService.getRewardByDonate(
                loginUserId, userId);
    }


    //  메이커별 리워드 조회

    @Get()
    public DataCollectionResponse<RewardData> getRewardByMaker(
            @Query("maker_id") Long makerId) {

        return rewardService.getRewardFindByMaker(makerId);
    }

    @Get("/image/{reward_id}")
    public HttpResponse getImage(@Path("reward_id") Long rewardId) {
        HttpHeader header = new DefaultHttpHeader();
        HttpBody body = new DefaultHttpBody(rewardService.getRewardImage(rewardId).getImageBytes());
        header.add(HttpHeaderField.CONTENT_TYPE, ContentType.IMAGE_PNG.headerValue());

        return new HttpResponse(
            HttpVersion.HTTP_1_1,
            header,
            body,
            HttpStatus.OK
        );
    }
     // 리워드 수정

    @Patch("/{reward_id}")
    public void updateReward(
            @Depends(CurrentUser.class) Long userId,
            @Path("reward_id") Long rewardId,
            CreateRewardDataRequest dto) {

        rewardService.updateReward(userId, rewardId, dto);
    }


     // 리워드 삭제

    @Delete("/{reward_id}")
    public void deleteReward(
            @Depends(CurrentUser.class) Long userId,
            @Path("reward_id") Long rewardId) {

        rewardService.deleteReward(userId, rewardId);
    }
}
