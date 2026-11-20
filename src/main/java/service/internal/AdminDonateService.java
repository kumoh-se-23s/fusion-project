package service.internal;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.Donate;
import model.dto.DonateData;
import repository.DonateRepository;
import service.BaseService;

import java.util.List;

@RequiredArgsConstructor
public class AdminDonateService extends BaseService {
    private final DonateRepository donateRepository;

    public List<DonateData> getAllDonate(){
        List<Donate> donateList = donateRepository.findAllDonates();
        return Serializer.toDataList(
            donateList,
            DonateData.class
        );//수정:response반환으로 수정
    }
    //내부  관리자 삭제
    public void deleteDonation(Long donateId){
        Donate donate = donateRepository.findById(donateId)
                .orElseThrow(NotFoundException::new);
        donateRepository.delete(donateId);
    }



}
