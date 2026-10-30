package constant;

public enum FundingState {
    //펀딩 생성만 됨
    JUST_USER_INPUT,

    //승인 대기
    PENDING_APPROVAL,

    //승인됨
    APPROVED,

    //거절
    REJECTED,

    //재거절
    RE_REJECTED,

    //진행중
    IN_PROGRESS,

    //완료
    COMPLETED,

    //실패
    FAILED,

    //취소
    CANCELED
}
