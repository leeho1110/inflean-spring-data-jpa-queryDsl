package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.annotation.Rollback;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember() {
        // give
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        // when
        Member findMember = memberRepository.findById(savedMember.getId()).get();

        // then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성 보장
    }

    @Test
    public void queryTest() {
        // given
        Team team1 = new Team("team1");
        Team team2 = new Team("team2");
        teamRepository.save(team1);
        teamRepository.save(team2);

        Member member15 = new Member("member15", 15, team1);
        Member member20 = new Member("member20", 20, team2);

        memberRepository.save(member15);
        memberRepository.save(member20);

        // when
        Optional<Member> byMyMind = memberRepository.findByMyMind(10, team1);

        // then
        byMyMind.ifPresent(member ->
                System.out.println("member = " + member.getAge() + "," + member.getTeam().getName())
        );
    }

    @Test
    public void testPaging(){
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        //when
        PageRequest pageRequest = PageRequest.of(0, 3);
        Slice<Member> page = memberRepository.findByAge(10, pageRequest);
//        Page<Member> page = memberRepository.findByAge(10, pageRequest);

        //then
        List<Member> content = page.getContent(); //조회된 데이터
        for (Member member : content) {
            System.out.println("member = " + member.getUsername());
        }

        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
//        assertThat(page.getTotalElements()).isEqualTo(5); //전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
//        assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
        assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?
    }

    @Test
    public void fetchJoin(){
        // given
        List<Member> fetchJoin = memberRepository.findFetchJoin();

        // when
        for (Member member : fetchJoin) {
            System.out.println("member = " + member.getClass());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
        }
    }

    @Test
    public void fetchFindAll(){
        List<Member> all = memberRepository.findAll();

        for (Member member : all) {
            System.out.println("member.getClass() = " + member.getClass());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
        }
    }
    @Test
    public void queryHint(){
        //given
        memberRepository.save(new Member("member1", 10));
        em.flush(); // 영속성 컨텍스트와 데이터베이스 싱크 맞춤
        em.clear(); // 영속성 컨텍스트 날림

        Member me = memberRepository.findReadOnlyByUsername("member1"); // 데이터베이스에서는 flush를 통해 insert 쿼리가 날아간 상태이기 때문에 조회 가능
        System.out.println(">>>>>> Entity Manager contain me: " + em.contains(me));

        me.setUsername("change"); // 변경하더라도 @QueryHint 때문에 update 쿼리가 안날아감
        me.setAge(20);
    }

}