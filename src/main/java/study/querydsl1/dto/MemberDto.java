package study.querydsl1.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //Getter, setter 등등 만들어 주는데 기본생성자는 안만들어줌
@NoArgsConstructor // 기본생성자(외 필요한 생성자를 모두) 만들어줌
public class MemberDto {
    private String username;
    private  int age;

    @QueryProjection //Dto를 Q파일로 생성
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
