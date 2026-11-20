
package controller.internal;

import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;

import model.dto.DonateData;
import model.dto.response.DataCollectionResponse;

import service.external.DonateService;

import java.util.Optional;

/*
 관리자 전용 후원 라우터
 */
public class DonateRouter extends BaseRouter {

    private final DonateService donateService;

    public DonateRouter(DonateService donateService) {
        super("/internal/donate");
        this.donateService = donateService;
    }

    /*
      관리자 후원 전체 조회 및 ID 조회
     */
    @Get()
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public DataCollectionResponse<DonateData> getDonates(
            @Query("donate_id") Optional<Long> donateId) {

        return donateService.getAdminDonates(donateId);
    }
}
