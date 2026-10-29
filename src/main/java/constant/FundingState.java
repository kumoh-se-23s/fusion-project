package constant;

public enum FundingState {
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
