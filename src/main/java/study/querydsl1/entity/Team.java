package study.querydsl1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // protected 타입인 기본생성자를 생성해주는 어노테이션
@ToString(of = {"id", "name"}) //아래 주석친 내용 생성, 연관관계 매핑값은 안넣는게 좋다 (무한루프돔)
public class Team {
    @Id @GeneratedValue
    @Column(name = "team_id")
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }
}
