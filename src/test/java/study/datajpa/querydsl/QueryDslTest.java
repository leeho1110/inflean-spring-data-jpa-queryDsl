package study.datajpa.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.datajpa.entity.Member;
import study.datajpa.entity.QMember;
import study.datajpa.repository.MemberRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@SpringBootTest
@Transactional
public class QueryDslTest {

    @Autowired
    MemberRepository memberRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void contextLoad() {
        // give
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember qMember = QMember.member;

        queryFactory
                .selectFrom(qMember)
                .fetchOne();
    }
}
