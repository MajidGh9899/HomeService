package ir.maktab127.dto.User;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
public class UserSearchFilterDto {
    private String role;
    private String firstName;
    private String lastName;
    private String serviceName; // فقط برای متخصص
    private Integer minScore;    // فقط برای متخصص
    private Integer maxScore;

}
