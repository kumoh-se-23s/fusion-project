
package controller.external;

import auth.CurrentUser;
import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;

import model.dto.DonateData;
import model.dto.request.donate.CreateDonateRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.IdResponse;

import service.external.DonateService;

import java.util.Optional;


 //외부용 후원 라우터
public class DonateRouter extends BaseRouter {

    private final DonateService donateService;

    public DonateRouter(DonateService donateService) {
        super("/donate");
        this.donateService = donateService;
    }


     //후원 생성
    @Post()
    public IdResponse createDonate(
            @Depends(CurrentUser.class) Long userId,
            CreateDonateRequest dto) {

        return donateService.createDonate(userId, dto);
    }


     // 사용자, 리워드, 프로젝트별 후원 조회
     @Get()
     public DataCollectionResponse<DonateData> getDonates(
             @Depends(CurrentUser.class) Long loginUserId,
             @Query(value = "user_id", required = false) Long userId,
             @Query(value = "reward_id", required = false) Long rewardId,
             @Query(value = "project_id", required = false) Long projectId) {

         return donateService.getDonates(
                 loginUserId,
                 Optional.ofNullable(userId),
                 Optional.ofNullable(rewardId),
                 Optional.ofNullable(projectId)
         );
     }


     // 후원 환불

    @Patch("/{donate_id}")
    public void refundDonate(
            @Depends(CurrentUser.class) Long userId,
            @Path("donate_id") Long donateId) {

        donateService.refundDonate(donateId, userId);
    }
}
