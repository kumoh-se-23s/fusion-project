package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser implements BaseDomain {

    private Long userPk;
    private String userName;
    private String userEmail;
    private String userPostal;
    private String userAddress;
    private String userPhone;
    private LocalDateTime createdAt;
}
